package com.comp445.drivers;

import com.comp445.common.Utils;
import com.comp445.common.net.selectiverepeat.PacketType;
import com.comp445.common.net.selectiverepeat.PacketUtils;
import com.comp445.common.net.selectiverepeat.SRPacket;
import com.comp445.common.net.selectiverepeat.SRSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.comp445.common.Utils.EXECUTOR;
import static com.comp445.common.Utils.doAssert;

public class AsyncSocketTests {

    public static final int REC_PORT = 8888;
    public static final int REC_TIMEOUT = 300;
    public static final int WAIT_BEFORE_SEND = 200;

    public static void main(String[] args) throws IOException, InterruptedException {
        try {

            testUDP();
            testSR();

            System.out.println(System.currentTimeMillis() + " Tests passed");
        } catch(Exception e) {
            System.out.println(System.currentTimeMillis() + " Tests failed");
        }

        EXECUTOR.shutdown();
    }

    public static void testUDP() throws Exception {
        DatagramSocket recSocket = new DatagramSocket(REC_PORT);
        DatagramSocket sendSocket = new DatagramSocket();

        boolean timedOut = false;
        try {
            CompletableFuture<DatagramPacket> recFuture = PacketUtils.receiveUDPPacketAsync(recSocket, Utils.UDP_MAX_PACKET_LENGTH, REC_TIMEOUT);
            CompletableFuture<DatagramPacket> recFuture2 = PacketUtils.receiveUDPPacketAsync(recSocket, Utils.UDP_MAX_PACKET_LENGTH, REC_TIMEOUT);

            Thread.sleep(WAIT_BEFORE_SEND);
            byte[] sendBuffer = new byte[]{ (byte)42, (byte)69 };
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName("localhost"), REC_PORT);
            sendSocket.send(sendPacket);
            sendSocket.send(sendPacket);

            DatagramPacket recPacket1 = recFuture.join();
            DatagramPacket recPacket2 = recFuture2.join();

            doAssert(Arrays.equals(Arrays.copyOfRange(recPacket1.getData(), 0, recPacket1.getLength()), sendBuffer));
            doAssert(Arrays.equals(Arrays.copyOfRange(recPacket2.getData(), 0, recPacket2.getLength()), sendBuffer));
        } catch(Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                timedOut = true;
            } else {
                throw e;
            }
        }

        recSocket.close();
        sendSocket.close();

        doAssert(!timedOut);
    }

    public static void testSR() throws Exception {
        SRSocket recSocket = new SRSocket(new DatagramSocket(REC_PORT));
        SRSocket sendSocket = new SRSocket();

        boolean timedOut = false;
        try {
            CompletableFuture<SRPacket> recFuture = PacketUtils.receiveSRPacketAsync(recSocket, Utils.UDP_MAX_PACKET_LENGTH, REC_TIMEOUT);
            CompletableFuture<SRPacket> recFuture2 = PacketUtils.receiveSRPacketAsync(recSocket, Utils.UDP_MAX_PACKET_LENGTH, REC_TIMEOUT);
            Thread.sleep(WAIT_BEFORE_SEND);
            byte[] sendBuffer = new byte[]{ (byte)42, (byte)69 };
            PacketUtils.sendSRPacket(sendSocket, PacketType.DATA, InetAddress.getByName("localhost"), REC_PORT, sendBuffer);
            PacketUtils.sendSRPacket(sendSocket, PacketType.DATA, InetAddress.getByName("localhost"), REC_PORT, sendBuffer);

            SRPacket recPacket1 = recFuture.join();
            SRPacket recPacket2 = recFuture2.join();

            doAssert(Arrays.equals(recPacket1.getPayload(), new byte[]{ (byte)42, (byte)69 }));
            doAssert(Arrays.equals(recPacket2.getPayload(), sendBuffer));
        } catch(Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                timedOut = true;
            } else {
                throw e;
            }
        }

        recSocket.close();
        sendSocket.close();

        doAssert(!timedOut);
    }

}
