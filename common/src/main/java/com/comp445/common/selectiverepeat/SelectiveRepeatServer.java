package com.comp445.common.selectiverepeat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import static com.comp445.common.http.Util.ARQ_ROUTER_PORT;
import static com.comp445.common.http.Util.MAX_PACKET_LENGTH;

public class SelectiveRepeatServer {

    DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;

    public SelectiveRepeatServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public byte[] receive() throws IOException {
        byte[] recBuf = new byte[MAX_PACKET_LENGTH];
        DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
        socket.receive(recPacket);

        RouterPacket recRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));

        clientAddress = recRouterPacket.getPeerAddress();
        clientPort = recRouterPacket.getPort();

        return recRouterPacket.getPayload();
    }

    public void sendResponse(byte[] data) throws IOException {
        RouterPacket sendRouterPacket = new RouterPacket();
        sendRouterPacket.setPeerAddress(clientAddress);
        sendRouterPacket.setPort((short)clientPort);
        sendRouterPacket.setPayload(data);
        byte[] routerPacketBytes = sendRouterPacket.toByteArray();

        DatagramPacket sendPacket = new DatagramPacket(routerPacketBytes, routerPacketBytes.length, clientAddress, ARQ_ROUTER_PORT);
        socket.send(sendPacket);
        socket.close();
    }
}
