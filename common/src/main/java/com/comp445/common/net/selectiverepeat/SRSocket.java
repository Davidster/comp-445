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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static com.comp445.common.Utils.EXECUTOR;
import static com.comp445.common.Utils.SR_CLOCK_GRANULARITY;

@Getter
public class SRSocket implements ISocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;
    private SRInputStream inputStream;
    private SROutputStream outputStream;

    public SRSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    public SRSocket() throws SocketException {
        this.udpSocket = new DatagramSocket();
    }

    public void connect(SocketAddress destination, int connectionTimeout) throws IOException {
        InetSocketAddress inetDestination = (InetSocketAddress)destination;

        doHandshake(inetDestination, connectionTimeout, PacketType.SYN, PacketType.SYNACK);

        Utils.sleep((int)SR_CLOCK_GRANULARITY);
        PacketUtils.sendSRPacketToRouter(this, PacketType.ACK, inetDestination.getAddress(), inetDestination.getPort());

        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, inetDestination);
    }

    public void doHandshake(InetSocketAddress destination, int connectionTimeout, PacketType synType, PacketType expectedAckType) throws IOException {
        CompletableFuture<Void> timedHandshakeFuture = new CompletableFuture<>();
        long connectionTimeoutNanos = connectionTimeout * 1000000L;
        AtomicLong latestConnectionRestart = new AtomicLong(System.nanoTime());

        Future<Void> handshakeFuture = EXECUTOR.submit(() -> {
            RTOCalculator rtoCalculator = new RTOCalculator();
            boolean canMeasureRtt = true;
            SRPacket ackRecPacket = null;
            while(true) {
                CompletableFuture<SRPacket> ackRecFuture = PacketUtils.receiveSRPacketAsync(this, Utils.SR_MAX_PACKET_LENGTH, rtoCalculator.getLatestRto());
                Utils.sleep((int)SR_CLOCK_GRANULARITY);
                try {
                    PacketUtils.sendSRPacketToRouter(this, synType, destination.getAddress(), destination.getPort());
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
                Instant sendTime = Instant.now();

                try {
                    ackRecPacket = PacketUtils.awaitTimeoutableSRPacket(ackRecFuture);
                    latestConnectionRestart.set(System.nanoTime());
                    if(canMeasureRtt) {
                        rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
                    }
                    if(ackRecPacket.getType().equals(expectedAckType)) {
                        break;
                    } else {
                        canMeasureRtt = false;
                    }
                } catch (SocketTimeoutException ignored) {
                    rtoCalculator.onTimeout();
                    canMeasureRtt = false;
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

    public void close() {
        this.udpSocket.close();
    }
}
