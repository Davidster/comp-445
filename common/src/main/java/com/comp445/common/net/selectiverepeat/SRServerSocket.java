package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;
import com.comp445.common.net.IServerSocket;
import com.comp445.common.net.UDPSocketContainer;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.comp445.common.Utils.SR_CLOCK_GRANULARITY;
import static com.comp445.common.Utils.SR_MAX_PACKET_LENGTH;

@Getter
public class SRServerSocket implements IServerSocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;
    private ConnectionTimer connectionTimer;
//    private ConnectionState connectionState = IDLE;

    public SRServerSocket(int port) throws SocketException {
        this.udpSocket = new DatagramSocket(port);
    }

    public SRSocket acceptClient() throws IOException {
        synStage:
        while (true) {

            SRPacket synRecPacket = PacketUtils.receiveSRPacket(this, SR_MAX_PACKET_LENGTH);
            System.out.println(System.currentTimeMillis() + " Server: received " + synRecPacket.getType().toString());

            if (!synRecPacket.getType().equals(PacketType.SYN)) {
                continue;
            }


            InetSocketAddress destination = new InetSocketAddress(synRecPacket.getPeerAddress(), synRecPacket.getPort());
            SRSocket clientSocket = new SRSocket(this.udpSocket);
            this.connectionTimer = new ConnectionTimer(Utils.SR_SERVER_CONNECTION_TIMEOUT);
            RTOCalculator rtoCalculator = new RTOCalculator();

//            this.connectionState = CONNECTING;

            boolean canMeasureRtt = true;
            SRPacket ackRecPacket = null;
            synAckStage:
            while(true) {
                if (connectionTimer.isTimedOut()) {
                    continue synStage;
//                    throw new SocketTimeoutException("Socket timed out during handshake");
                }
//            System.out.println(rtoCalculator.getLatestRto());
                CompletableFuture<SRPacket> ackRecFuture = PacketUtils.receiveSRPacketAsync(clientSocket, Utils.SR_MAX_PACKET_LENGTH, rtoCalculator.getLatestRto());
                Utils.sleep((int)SR_CLOCK_GRANULARITY);
                PacketUtils.sendSRPacketToRouter(this, PacketType.SYNACK, destination.getAddress(), destination.getPort());
                Instant sendTime = Instant.now();
                System.out.println(System.currentTimeMillis() + " Server: sent SYNACK");
                try {
                    ackRecPacket = PacketUtils.awaitTimeoutableSRPacket(ackRecFuture);
                    if(canMeasureRtt) {
                        rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
                        System.out.println(System.currentTimeMillis() + " Server: updated rto: " + rtoCalculator.getLatestRto());
                    }
                    connectionTimer.reset();
//                    System.out.println(System.currentTimeMillis() + " server received response from client: " + ackRecPacket.toString());
                    System.out.println(System.currentTimeMillis() + " Server: received " + ackRecPacket.getType().toString());
                    if(ackRecPacket.getType().equals(PacketType.ACK)) {
                        break synAckStage;
                    } else {
                        if(ackRecPacket.getType().equals(PacketType.SYN)) {
                            // TODO: check that the sequence number is the same?
                            // if it's a different sequence number, we should probably 'continue synStage;'
                        }
                        canMeasureRtt = false;
                    }
                } catch (SocketTimeoutException ignored) {
                    System.out.println(System.currentTimeMillis() + " Server: timed out while waiting for ACK");
                    rtoCalculator.onTimeout();
                    System.out.println(System.currentTimeMillis() + " Server: updated rto: " + rtoCalculator.getLatestRto());
                    canMeasureRtt = false;
                }
            }

            clientSocket.implicitConnect(destination);

            // this.connectionState = CONNECTED;
            return clientSocket;
        }
    }

    @Override
    public void close() {
        this.udpSocket.close();
        this.connectionTimer.close();
    }
}
