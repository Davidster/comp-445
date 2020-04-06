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
import java.util.Random;

@Getter
public class SRServerSocket implements IServerSocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;

    public SRServerSocket(int port) throws SocketException {
        this.udpSocket = new DatagramSocket(port);
    }

    public SRSocket acceptClient() throws IOException {
        while (true) {
            SRPacket synRecPacket = PacketUtils.receiveSRPacket(this);
            if (!synRecPacket.getType().equals(PacketType.SYN)) {
                continue;
            }

            InetSocketAddress inetDestination = new InetSocketAddress(synRecPacket.getPeerAddress(), synRecPacket.getPort());
            int sequenceNumber = new Random().nextInt(Utils.SR_MAX_SEQUENCE_NUM);
            int peerSequenceNumber = Utils.incSeqNum(synRecPacket.getSequenceNumber());
            SRSocket clientSocket = new SRSocket(this.udpSocket);
            try {
                SRPacket ackPacket = clientSocket.doHandshake(inetDestination, Utils.SR_SERVER_CONNECTION_TIMEOUT, PacketType.SYNACK, PacketType.ACK, sequenceNumber, peerSequenceNumber);
            } catch(SocketTimeoutException e) {
//                System.out.println("Socket timeout");
//                continue;
                return null;
            }

            clientSocket.implicitConnect(inetDestination, Utils.incSeqNum(sequenceNumber), peerSequenceNumber);
//            System.out.println("SR Server connected!");
            return clientSocket;
        }
    }

    @Override
    public void close() {
        this.udpSocket.close();
    }
}
