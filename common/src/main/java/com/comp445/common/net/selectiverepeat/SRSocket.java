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

import static com.comp445.common.Utils.SR_CLOCK_GRANULARITY;

@Getter
public class SRSocket implements ISocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;
    private SRInputStream inputStream;
    private SROutputStream outputStream;
    private ConnectionTimer connectionTimer;
//    private int sequenceNumber;
//    private ConnectionState connectionState = IDLE;

    public SRSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    public SRSocket() throws SocketException {
        this.udpSocket = new DatagramSocket();
    }

    public void connect(SocketAddress destination, int connectionTimeout) throws IOException {
//        this.connectionState = CONNECTING;
        InetSocketAddress inetDestination = (InetSocketAddress)destination;
        this.connectionTimer = new ConnectionTimer(connectionTimeout);
        RTOCalculator rtoCalculator = new RTOCalculator();

        boolean canMeasureRtt = true;
        SRPacket synAckRecPacket = null;
        while(true) {
            if (connectionTimer.isTimedOut()) {
                throw new SocketTimeoutException("Socket timed out during handshake");
            }
            CompletableFuture<SRPacket> synAckRecFuture = PacketUtils.receiveSRPacketAsync(this, Utils.SR_MAX_PACKET_LENGTH, rtoCalculator.getLatestRto());
            Utils.sleep((int)SR_CLOCK_GRANULARITY);
            PacketUtils.sendSRPacketToRouter(this, PacketType.SYN, inetDestination.getAddress(), inetDestination.getPort());
            Instant sendTime = Instant.now();
            System.out.println(System.currentTimeMillis() + " Client: sent SYN");
            try {
                synAckRecPacket = PacketUtils.awaitTimeoutableSRPacket(synAckRecFuture);
                if(canMeasureRtt) {
                    rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
                    System.out.println(System.currentTimeMillis() + " Client: updated rto: " + rtoCalculator.getLatestRto());
                }

                connectionTimer.reset();
//                System.out.println(System.currentTimeMillis() + " client received response from server: " + synAckRecPacket.toString());
                System.out.println(System.currentTimeMillis() + " Client: received " + synAckRecPacket.getType().toString());
                if(synAckRecPacket.getType().equals(PacketType.SYNACK)) {
                    break;
                } else {
                    canMeasureRtt = false;
                }
            } catch (SocketTimeoutException ignored) {
                System.out.println(System.currentTimeMillis() + " Client: timed out while waiting for SYNACK");
                rtoCalculator.onTimeout();
                System.out.println(System.currentTimeMillis() + " Client: updated rto: " + rtoCalculator.getLatestRto());
                canMeasureRtt = false;
            }
        }

        Utils.sleep((int)SR_CLOCK_GRANULARITY);
        PacketUtils.sendSRPacketToRouter(this, PacketType.ACK, synAckRecPacket.getPeerAddress(), synAckRecPacket.getPort());
        System.out.println(System.currentTimeMillis() + " Client: sent ACK");
//        this.connectionState = CONNECTED;

        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, inetDestination);
    }

    public void implicitConnect(SocketAddress destination) {
//        this.connectionState = CONNECTED;
        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, (InetSocketAddress)destination);
    }

    public void close() {
        this.udpSocket.close();
        this.connectionTimer.close();
    }
}
