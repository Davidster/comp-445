package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;
import com.comp445.common.net.ISocket;
import com.comp445.common.net.UDPSocketContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.comp445.common.Utils.EXECUTOR;
import static com.comp445.common.Utils.SR_CLOCK_GRANULARITY;

@Getter
public class SRSocket implements ISocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;
    private SRInputStream inputStream;
    private SROutputStream outputStream;
    private int sequenceNumber;
    @Setter private int peerSequenceNumber;

    public SRSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
        init();
    }

    public SRSocket() throws SocketException {
        this.udpSocket = new DatagramSocket();
        init();
    }

    private void init() {
        this.sequenceNumber = new Random().nextInt(Utils.SR_MAX_SEQUENCE_NUM);
    }

    public void connect(SocketAddress destination, int connectionTimeout) throws IOException {
        InetSocketAddress inetDestination = (InetSocketAddress)destination;

        SRPacket synAckPacket = doHandshake(inetDestination, connectionTimeout, PacketType.SYN, PacketType.SYNACK);
        this.peerSequenceNumber = synAckPacket.getSequenceNumber();

        Utils.sleep((int)SR_CLOCK_GRANULARITY);
        SRPacket srPacket = SRPacket.builder()
                .type(PacketType.ACK)
                .ackSequenceNumber(Utils.nextSequenceNumber(this.peerSequenceNumber))
                .peerAddress(inetDestination.getAddress())
                .port(inetDestination.getPort())
                .build();
        sendPacket(srPacket);

        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, inetDestination);
    }

    public SRPacket doHandshake(InetSocketAddress destination, int connectionTimeout, PacketType synType, PacketType expectedAckType) throws IOException {
        CompletableFuture<Void> timedHandshakeFuture = new CompletableFuture<>();
        long connectionTimeoutNanos = connectionTimeout * 1000000L;
        AtomicLong latestConnectionRestart = new AtomicLong(System.nanoTime());
        AtomicReference<SRPacket> ackRecPacket = new AtomicReference<>();

        Future<Void> handshakeFuture = EXECUTOR.submit(() -> {
            RTOCalculator rtoCalculator = new RTOCalculator();
            boolean isRetransmission = false;
            while(true) {
                CompletableFuture<SRPacket> ackRecFuture = PacketUtils.receiveSRPacketAsync(this, Utils.SR_MAX_PACKET_LENGTH, rtoCalculator.getLatestRto());
                Utils.sleep((int)SR_CLOCK_GRANULARITY);
                try {
                    SRPacket srPacket = SRPacket.builder()
                            .type(synType)
                            .sequenceNumber(this.sequenceNumber)
                            .ackSequenceNumber(this.peerSequenceNumber)
                            .peerAddress(destination.getAddress())
                            .port(destination.getPort())
                            .build();
                    sendPacket(srPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new CompletionException(e);
                }
                Instant sendTime = Instant.now();

                try {
                    ackRecPacket.set(PacketUtils.awaitTimeoutableSRPacket(ackRecFuture));
                    latestConnectionRestart.set(System.nanoTime());
                    if(!isRetransmission) {
                        rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
                    }
                    int nextSequenceNumber = Utils.nextSequenceNumber(this.sequenceNumber);
                    boolean validAckType = ackRecPacket.get().getType().equals(expectedAckType);
                    boolean validSequenceNumber = ackRecPacket.get().getAckSequenceNumber() == nextSequenceNumber;
                    if (validAckType && validSequenceNumber) {
                        this.sequenceNumber = nextSequenceNumber;
                        break;
                    }
                    isRetransmission = true;
                } catch (SocketTimeoutException ignored) {
                    rtoCalculator.onTimeout();
                    isRetransmission = true;
                }
            }
            timedHandshakeFuture.complete(null);
            return null;
        });

        Future<Void> timeoutFuture = EXECUTOR.submit(() -> {
            do {
                Thread.sleep((latestConnectionRestart.get() + connectionTimeoutNanos - System.nanoTime()) / 1000000);
            } while(System.nanoTime() - latestConnectionRestart.get() < (connectionTimeoutNanos - ((int)SR_CLOCK_GRANULARITY) * 1000000));
            timedHandshakeFuture.completeExceptionally(new SocketTimeoutException("Socket timed out during handshake"));
            return null;
        });

        try {
            timedHandshakeFuture.join();
            return ackRecPacket.get();
        } catch(CompletionException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) e.getCause();
            } else if(e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        } finally {
            timeoutFuture.cancel(true);
            handshakeFuture.cancel(true);
        }
    }

    public void implicitConnect(SocketAddress destination) {
        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, (InetSocketAddress)destination);
    }

    public void sendPacket(SRPacket packet) throws IOException {
        PacketUtils.sendSRPacketToRouter(this, packet);
    }

    public void close() {
        this.udpSocket.close();
    }
}
