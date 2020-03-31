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

import static com.comp445.common.Utils.SR_MAX_PACKET_LENGTH;

@Getter
public class SRServerSocket implements IServerSocket, UDPSocketContainer, Closeable {

    private DatagramSocket udpSocket;

    public SRServerSocket(int port) throws SocketException {
        this.udpSocket = new DatagramSocket(port);
    }

    public SRSocket acceptClient() throws IOException {
        while (true) {
            SRPacket synRecPacket = PacketUtils.receiveSRPacket(this, SR_MAX_PACKET_LENGTH);
            if (!synRecPacket.getType().equals(PacketType.SYN)) {
                continue;
            }

            InetSocketAddress inetDestination = new InetSocketAddress(synRecPacket.getPeerAddress(), synRecPacket.getPort());
            SRSocket clientSocket = new SRSocket(this.udpSocket);
            try {
                clientSocket.doHandshake(inetDestination, Utils.SR_SERVER_CONNECTION_TIMEOUT, PacketType.SYNACK, PacketType.ACK);
            } catch(SocketTimeoutException e) {
                System.out.println("Socket timeout");
                continue;
            }

            clientSocket.implicitConnect(inetDestination);
            return clientSocket;
        }
    }

    @Override
    public void close() {
        this.udpSocket.close();
    }
}
