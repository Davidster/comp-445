package com.comp445.common.net.selectiverepeat;

import com.comp445.common.net.UDPSocketContainer;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.comp445.common.Utils.ARQ_ROUTER_PORT;
import static com.comp445.common.Utils.EXECUTOR;

public class PacketUtils {
    public static void sendSRPacketToRouter(UDPSocketContainer socketContainer, PacketType type, InetAddress destination, int port) throws IOException {
        sendSRPacketToRouter(socketContainer, type, destination, port, new byte[0]);
    }

    public static void sendSRPacketToRouter(UDPSocketContainer socketContainer, PacketType type, InetAddress destination, int port, byte[] payload) throws IOException {
        sendSRPacket(socketContainer, type, destination, port, payload, ARQ_ROUTER_PORT);
    }

    public static void sendSRPacket(UDPSocketContainer socketContainer, PacketType type, InetAddress destination, int port) throws IOException {
        sendSRPacket(socketContainer, type, destination, port, new byte[0]);
    }

    public static void sendSRPacket(UDPSocketContainer socketContainer, PacketType type, InetAddress destination, int port, byte[] payload) throws IOException {
        sendSRPacket(socketContainer, type, destination, port, payload, port);
    }

    public static void sendSRPacket(UDPSocketContainer socketContainer, PacketType type, InetAddress destination, int port, byte[] payload, int routerPort) throws IOException {
        SRPacket srPacket = new SRPacket();
        srPacket.setType(type);
        srPacket.setPeerAddress(destination);
        srPacket.setPort(port);
        srPacket.setPayload(payload);
        byte[] sendBuffer = srPacket.toByteArray();
        DatagramPacket udpPacket = new DatagramPacket(sendBuffer, sendBuffer.length, destination, routerPort);
        socketContainer.getUdpSocket().send(udpPacket);
    }

    public static SRPacket receiveSRPacket(UDPSocketContainer socketContainer, int maxPacketLength) throws IOException {
        return SRPacket.fromUDPPacket(receiveUDPPacket(socketContainer.getUdpSocket(), maxPacketLength));
    }

    public static DatagramPacket receiveUDPPacket(DatagramSocket socket, int maxPacketLength) throws IOException {
        byte[] recBuffer = new byte[maxPacketLength];
        DatagramPacket packet = new DatagramPacket(recBuffer, recBuffer.length);
        socket.setSoTimeout(0);
        socket.receive(packet);
        return packet;
    }

    public static SRPacket receiveSRPacket(UDPSocketContainer socketContainer, int maxPacketLength, int timeout) throws SocketTimeoutException {
        return awaitTimeoutableSRPacket(receiveSRPacketAsync(socketContainer, maxPacketLength, timeout));
    }

    public static DatagramPacket receiveUDPPacket(DatagramSocket socket, int maxPacketLength, int timeout) throws SocketTimeoutException {
        return awaitTimeoutableUDPPacket(receiveUDPPacketAsync(socket, maxPacketLength, timeout));
    }

    public static CompletableFuture<SRPacket> receiveSRPacketAsync(UDPSocketContainer socketContainer, int maxPacketLength) {
        return receiveSRPacketAsync(socketContainer, maxPacketLength, 0);
    }

    public static CompletableFuture<SRPacket> receiveSRPacketAsync(UDPSocketContainer socketContainer, int maxPacketLength, int timeout) {
        return receiveUDPPacketAsync(socketContainer.getUdpSocket(), maxPacketLength, timeout).thenApply(udpPacket -> {
            try {
                return SRPacket.fromUDPPacket(udpPacket);
            } catch (UnknownHostException e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<DatagramPacket> receiveUDPPacketAsync(DatagramSocket socket, int maxPacketLength) {
        return receiveUDPPacketAsync(socket, maxPacketLength, 0);
    }

    public static CompletableFuture<DatagramPacket> receiveUDPPacketAsync(DatagramSocket socket, int maxPacketLength, int timeout) {
        byte[] recBuffer = new byte[maxPacketLength];
        DatagramPacket udpPacket = new DatagramPacket(recBuffer, recBuffer.length);
        return CompletableFuture.supplyAsync(() -> {
            try {
                socket.setSoTimeout(timeout);
                socket.receive(udpPacket);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
            return udpPacket;
        }, EXECUTOR);

    }

    public static SRPacket awaitTimeoutableSRPacket(CompletableFuture<SRPacket> srPacketFuture) throws SocketTimeoutException {
        return (SRPacket) awaitTimeoutable(srPacketFuture);
    }

    public static DatagramPacket awaitTimeoutableUDPPacket(CompletableFuture<DatagramPacket> srPacketFuture) throws SocketTimeoutException {
        return (DatagramPacket) awaitTimeoutable(srPacketFuture);
    }

    public static Object awaitTimeoutable(CompletableFuture<?> completableFuture) throws SocketTimeoutException {
        try {
            return completableFuture.join();
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
}
