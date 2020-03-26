package com.comp445.common.net.selectiverepeat;

import com.comp445.common.net.IServerSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import static com.comp445.common.Util.DEFAULT_TIMEOUT;
import static com.comp445.common.Util.MAX_PACKET_LENGTH;

public class SelectiveRepeatServerSocket implements IServerSocket {

    DatagramSocket socket;
//    private InetAddress clientAddress;
//    private int clientPort;

    public SelectiveRepeatServerSocket(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public SelectiveRepeatSocket acceptClient() throws IOException {
        byte[] recBuffer = new byte[MAX_PACKET_LENGTH];
        DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
        socket.receive(recPacket);
        RouterPacket receivedRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));
        InetSocketAddress destination = new InetSocketAddress(receivedRouterPacket.getPeerAddress(), receivedRouterPacket.getPort());
        SelectiveRepeatSocket clientSocket = new SelectiveRepeatSocket(socket);
        clientSocket.implicitConnect(destination, DEFAULT_TIMEOUT);
        return clientSocket;
    }

//    public byte[] receive() throws IOException {
//        byte[] recBuf = new byte[MAX_PACKET_LENGTH];
//        DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
//        socket.receive(recPacket);
//
//        RouterPacket recRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));
//
//        clientAddress = recRouterPacket.getPeerAddress();
//        clientPort = recRouterPacket.getPort();
//
//        return recRouterPacket.getPayload();
//    }
//
//    public void sendResponse(byte[] data) throws IOException {
//        RouterPacket sendRouterPacket = new RouterPacket();
//        sendRouterPacket.setPeerAddress(clientAddress);
//        sendRouterPacket.setPort((short)clientPort);
//        sendRouterPacket.setPayload(data);
//        byte[] routerPacketBytes = sendRouterPacket.toByteArray();
//
//        DatagramPacket sendPacket = new DatagramPacket(routerPacketBytes, routerPacketBytes.length, clientAddress, ARQ_ROUTER_PORT);
//        socket.send(sendPacket);
//        socket.close();
//    }
}
