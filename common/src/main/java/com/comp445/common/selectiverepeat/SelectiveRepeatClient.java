package com.comp445.common.selectiverepeat;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static com.comp445.common.http.Util.ARQ_ROUTER_PORT;
import static com.comp445.common.http.Util.MAX_PACKET_LENGTH;

@AllArgsConstructor
public class SelectiveRepeatClient {

    private InetSocketAddress destination;
    private int timeout;

    public byte[] send(byte[] data) throws IOException {
        RouterPacket sendRouterPacket = new RouterPacket();
        sendRouterPacket.setPeerAddress(destination.getAddress());
        sendRouterPacket.setPort((short) destination.getPort());
        sendRouterPacket.setPayload(data);
        byte[] routerPacketBytes = sendRouterPacket.toByteArray();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket sendPacket = new DatagramPacket(routerPacketBytes, routerPacketBytes.length, destination.getAddress(), ARQ_ROUTER_PORT);
        socket.send(sendPacket);

        byte[] recBuf = new byte[MAX_PACKET_LENGTH];
        DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
        socket.receive(recPacket);

        socket.close();

        RouterPacket recRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));
        return recRouterPacket.getPayload();
    }
}
