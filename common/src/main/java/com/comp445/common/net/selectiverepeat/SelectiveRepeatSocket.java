package com.comp445.common.net.selectiverepeat;

import com.comp445.common.net.ISocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import static com.comp445.common.Util.ARQ_ROUTER_PORT;

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

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public void close() {
        socket.close();
    }

//    public byte[] send(byte[] data) throws IOException {
//        RouterPacket sendRouterPacket = new RouterPacket();
//        sendRouterPacket.setPeerAddress(destination.getAddress());
//        sendRouterPacket.setPort((short) destination.getPort());
//        sendRouterPacket.setPayload(data);
//        byte[] routerPacketBytes = sendRouterPacket.toByteArray();
//
//        DatagramSocket socket = new DatagramSocket();
//        DatagramPacket sendPacket = new DatagramPacket(routerPacketBytes, routerPacketBytes.length, destination.getAddress(), ARQ_ROUTER_PORT);
//        socket.send(sendPacket);
//
//        byte[] recBuf = new byte[MAX_PACKET_LENGTH];
//        DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
//        socket.receive(recPacket);
//
//        socket.close();
//
//        RouterPacket recRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));
//        return recRouterPacket.getPayload();
//    }
}
