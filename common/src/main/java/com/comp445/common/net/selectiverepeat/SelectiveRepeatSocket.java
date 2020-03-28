package com.comp445.common.net.selectiverepeat;

import com.comp445.common.net.ISocket;
import lombok.Getter;

import java.io.IOException;
import java.net.*;

import static com.comp445.common.Util.ARQ_ROUTER_PORT;

@Getter
public class SelectiveRepeatSocket implements ISocket {

    private DatagramSocket socket;
    private SelectiveRepeatInputStream inputStream;
    private SelectiveRepeatOutputStream outputStream;

    public SelectiveRepeatSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public SelectiveRepeatSocket() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public void connect(SocketAddress destination, int timeout) throws IOException {
        // TODO: perform 3-way handshake
        InetSocketAddress inetDestination = (InetSocketAddress)destination;

        RouterPacket sendRouterPacket = new RouterPacket();
        sendRouterPacket.setPeerAddress(inetDestination.getAddress());
        sendRouterPacket.setPort(inetDestination.getPort());
        byte[] sendBuffer = sendRouterPacket.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetDestination.getAddress(), ARQ_ROUTER_PORT);
        socket.send(sendPacket);

        this.inputStream = new SelectiveRepeatInputStream(socket);
        this.outputStream = new SelectiveRepeatOutputStream(socket, inetDestination);
    }

    public void implicitConnect(SocketAddress destination, int timeout) throws IOException {
        this.inputStream = new SelectiveRepeatInputStream(socket);
        this.outputStream = new SelectiveRepeatOutputStream(socket, (InetSocketAddress)destination);
    }

    public void close() {
        socket.close();
    }
}
