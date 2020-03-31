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

    public void doHandshake(InetSocketAddress inetDestination, int connectionTimeout, PacketType synType, PacketType expectedAckType) throws IOException {

        this.connectionTimer = new ConnectionTimer(connectionTimeout);
        RTOCalculator rtoCalculator = new RTOCalculator();
        boolean canMeasureRtt = true;

        SRPacket ackRecPacket = null;
        while(true) {
            // TODO: since the below packet receive call is blocking, must somehow convert the timer to make it interrupt
            if (connectionTimer.isTimedOut()) {
                throw new SocketTimeoutException("Socket timed out during handshake");
            }

            CompletableFuture<SRPacket> ackRecFuture = PacketUtils.receiveSRPacketAsync(this, Utils.SR_MAX_PACKET_LENGTH, rtoCalculator.getLatestRto());
            Utils.sleep((int)SR_CLOCK_GRANULARITY);
            PacketUtils.sendSRPacketToRouter(this, synType, inetDestination.getAddress(), inetDestination.getPort());
            Instant sendTime = Instant.now();

            try {
                ackRecPacket = PacketUtils.awaitTimeoutableSRPacket(ackRecFuture);
                if(canMeasureRtt) {
                    rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
                }
                connectionTimer.reset();
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
    }

    public void implicitConnect(SocketAddress destination) {
        this.inputStream = new SRInputStream(udpSocket);
        this.outputStream = new SROutputStream(udpSocket, (InetSocketAddress)destination);
    }

    public void close() {
        this.udpSocket.close();
        this.connectionTimer.close();
    }
}
