package com.comp445.common;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        String msg = "hello world";

        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");

        byte[] sendBuf = msg.getBytes();
        System.out.println("sendBuf length: " + sendBuf.length);
        DatagramPacket packet
                = new DatagramPacket(sendBuf, sendBuf.length, address, 4445);
        socket.send(packet);

        byte[] recBuf = new byte[256];
        packet = new DatagramPacket(recBuf, recBuf.length);
        socket.receive(packet);
        String received = new String(
                packet.getData(), 0, packet.getLength());

        System.out.println(received);
    }
}
