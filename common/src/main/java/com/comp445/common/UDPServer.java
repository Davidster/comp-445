package com.comp445.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[512];
        DatagramSocket socket = new DatagramSocket(4445);
        boolean running = true;

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received
                    = new String(packet.getData(), 0, packet.getLength());

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(packet.getData(), packet.getLength(), address, port);


            System.out.println(received);

            if (received.equals("end")) {
                running = false;
                continue;
            }
            socket.send(packet);
        }
        socket.close();
    }
}
