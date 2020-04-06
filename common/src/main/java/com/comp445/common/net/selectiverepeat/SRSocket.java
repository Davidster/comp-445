package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;
import com.comp445.common.net.ISocket;
import com.comp445.common.net.UDPSocketContainer;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.comp445.common.Utils.*;

@Getter
public class SRSocket implements ISocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;
    private SRInputStream inputStream;
    private SROutputStream outputStream;
    private InetSocketAddress destination;



    public SRSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
        init();
    }

    public SRSocket() throws SocketException {
        this.udpSocket = new DatagramSocket();
        init();

    }

    public void init() {
    }

    public void connect(SocketAddress destination, int connectionTimeout) throws IOException {
        this.destination = (InetSocketAddress)destination;
        int sequenceNumber = new Random().nextInt(Utils.SR_MAX_SEQUENCE_NUM);

        SRPacket synAckPacket = doHandshake(this.destination, connectionTimeout, PacketType.SYN, PacketType.SYNACK, sequenceNumber);
        int peerSequenceNumber = Utils.incSeqNum(synAckPacket.getSequenceNumber());

        Utils.sleep((int)SR_CLOCK_GRANULARITY);
        SRPacket srPacket = SRPacket.builder()
                .type(PacketType.ACK)
                .ackSequenceNumber(peerSequenceNumber)
                .peerAddress(this.destination.getAddress())
                .port(this.destination.getPort())
                .build();
        sendPacket(srPacket);


        this.outputStream = new SROutputStream(udpSocket, this.destination, Utils.incSeqNum(sequenceNumber));
        this.inputStream = new SRInputStream(udpSocket, this.destination, peerSequenceNumber, this.outputStream);
        System.out.println("SR Client connected!");
    }

    public void implicitConnect(SocketAddress destination, int sequenceNumber, int peerSequenceNumber) {
        this.outputStream = new SROutputStream(udpSocket, (InetSocketAddress)destination, sequenceNumber);
        this.inputStream = new SRInputStream(udpSocket, (InetSocketAddress)destination, peerSequenceNumber, this.outputStream);
    }

    public SRPacket doHandshake(InetSocketAddress destination, int connectionTimeout, PacketType synType, PacketType expectedAckType, int sequenceNumber) throws IOException {
        return doHandshake(destination, connectionTimeout, synType, expectedAckType, sequenceNumber, 0);
    }

    public SRPacket doHandshake(InetSocketAddress destination, int connectionTimeout, PacketType synType, PacketType expectedAckType, int sequenceNumber, int ackSequenceNumber) throws IOException {
        CompletableFuture<Void> timedHandshakeFuture = new CompletableFuture<>();
        long connectionTimeoutNanos = connectionTimeout * 1000000L;
        AtomicLong latestConnectionRestart = new AtomicLong(System.nanoTime());
        AtomicReference<SRPacket> ackRecPacket = new AtomicReference<>();

        Future<Void> handshakeFuture = EXECUTOR.submit(() -> {
            RTOCalculator rtoCalculator = new RTOCalculator();
            boolean isRetransmission = false;

            while(true) {
                CompletableFuture<SRPacket> ackRecFuture = PacketUtils.receiveSRPacketAsync(this, rtoCalculator.getLatestRto());
                Utils.sleep((int)SR_CLOCK_GRANULARITY);
                try {
                    SRPacket synPacket = SRPacket.builder()
                            .type(synType)
                            .sequenceNumber(sequenceNumber)
                            .ackSequenceNumber(ackSequenceNumber)
                            .peerAddress(destination.getAddress())
                            .port(destination.getPort())
                            .build();
                    sendPacket(synPacket);
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
                    int nextSequenceNumber = Utils.incSeqNum(sequenceNumber);
                    boolean validAckType = ackRecPacket.get().getType().equals(expectedAckType);
                    boolean validSequenceNumber = ackRecPacket.get().getAckSequenceNumber() == nextSequenceNumber;
                    if (validAckType && validSequenceNumber) {
                        break;
                    }
                    isRetransmission = true;
                } catch (SocketTimeoutException ignored) {
                    rtoCalculator.onTimeout();
                    isRetransmission = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            timedHandshakeFuture.complete(null);
            return null;
        });

        Future<Void> timeoutFuture = EXECUTOR.submit(() -> {
            do {
                Thread.sleep((latestConnectionRestart.get() + connectionTimeoutNanos - System.nanoTime()) / 1000000);
            } while(System.nanoTime() - latestConnectionRestart.get() < (connectionTimeoutNanos - ((int)SR_CLOCK_GRANULARITY) * 1000000));
            timedHandshakeFuture.completeExceptionally(new SocketTimeoutException("Socket timed out during handshake (timeout = " + connectionTimeoutNanos / 1000000 + ")"));
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

    public void sendPacket(SRPacket packet) throws IOException {
        PacketUtils.sendSRPacketToRouter(this, packet);
    }

    public void close() {
        this.inputStream.close();
        this.outputStream.close();
        this.udpSocket.close();
    }
}
