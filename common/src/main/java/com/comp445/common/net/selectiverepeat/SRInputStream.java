package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;
import com.comp445.common.net.UDPSocketContainer;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

import static com.comp445.common.Utils.EXECUTOR;

public class SRInputStream extends InputStream implements UDPSocketContainer {

    @Getter private DatagramSocket udpSocket;
    private byte[] buffer;
    private int position;
    private int latestSeqNumDeliveredUp;

    private final SortedSet<SRPacket> bufferedPackets;
    private Future<Void> packetReceiverLoop;
    private SROutputStream outputStream;
    private SRPacket.SRPacketBuilder packetBuilder;
    private final Object dataAvailableWaitNotify;
    @Getter private Integer totalDataPacketsReceived;
    @Getter private Integer uniqueDataPacketsReceived;
    @Getter private Integer totalAckPacketsReceived;

    public SRInputStream(DatagramSocket udpSocket, InetSocketAddress source, int peerSequenceNumber, SROutputStream outputStream) {
        this.udpSocket = udpSocket;
        this.position = 0;
        this.latestSeqNumDeliveredUp = peerSequenceNumber - 1;
        this.bufferedPackets = Collections.synchronizedSortedSet(new TreeSet<>());
        this.outputStream = outputStream;
        this.packetBuilder = SRPacket.builder()
                .peerAddress(source.getAddress())
                .port(source.getPort());
        this.dataAvailableWaitNotify = new Object();
        this.totalDataPacketsReceived = 0;
        this.uniqueDataPacketsReceived = 0;
        this.totalAckPacketsReceived = 0;
        startReceivingPackets();
    }

    private synchronized void startReceivingPackets() {
        this.packetReceiverLoop = EXECUTOR.submit(() -> {
            try {
                CompletableFuture<SRPacket> recPacketFuture = PacketUtils.receiveSRPacketAsync(this);
                //noinspection InfiniteLoopStatement
                while(true) {
                    SRPacket recPacket = recPacketFuture.join();
                    recPacketFuture = PacketUtils.receiveSRPacketAsync(this);

                    switch(recPacket.getType()) {
                        case DATA:
                            handleDataPacket(recPacket);
                            break;
                        case SYNACK:
                            handleSynAckPacket(recPacket);
                            break;
                        case ACK:
                            handleAckPacket(recPacket);
                            break;
                        default:
                            System.out.println("SRInputStream received unexpected packet type: " + recPacket.toString());
                            break;
                    }
                }
            } catch (Exception e) {
                if (e.getCause() instanceof SocketException) {
                    // socket closed
//                    System.out.println(packetBuilder.build().getPort() + ": " + e.getCause().getMessage());
//                    synchronized (this.dataAvailableWaitNotify) {
//                        dataAvailableWaitNotify.notify();
//                    }
                } else {
                    System.out.println("Unknown error in startReceivingPackets");
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    /* if packet type is SYNACK, it means the server never received the ACK
       during the handshake, so we must resend it before the server can start
       accepting data */
    private void handleSynAckPacket(SRPacket packet) throws IOException {
        SRPacket ackSendPacket = packetBuilder
                .type(PacketType.ACK)
                .ackSequenceNumber(Utils.incSeqNum(packet.getSequenceNumber()))
                .build();
//        System.out.println("ACK RESEND");
        PacketUtils.sendSRPacketToRouter(this, ackSendPacket);
    }

    private void handleAckPacket(SRPacket packet) {
        synchronized (this.totalAckPacketsReceived) {
            this.totalAckPacketsReceived++;
        }

//        System.out.println("Got ack: " + packet.toString());
        synchronized (this.outputStream.getAckedSquenceNumbers()) {
            int oldLeftWindowEdge = this.outputStream.getLeftWindowEdge();
            if(packet.getAckSequenceNumber() <= oldLeftWindowEdge) {
                return;
            }
            SortedSet<Integer> ackedSeqNums = this.outputStream.getAckedSquenceNumbers();
            ackedSeqNums.add(Utils.decSeqNum(packet.getAckSequenceNumber()));

            ArrayList<Integer> toRemove = new ArrayList<>();
            for (int seqNum: ackedSeqNums) {
                int nextExpectedSeqAckNum = Utils.incSeqNum(this.outputStream.getLeftWindowEdge());
                if (seqNum == nextExpectedSeqAckNum) {
                    toRemove.add(seqNum);
                    this.outputStream.setLeftWindowEdge(seqNum);
                } else {
                    break;
                }
            }
            toRemove.forEach(ackedSeqNums::remove);
            if(!toRemove.isEmpty()) {
//                System.out.println("Updated sender's left window edge to: " + this.outputStream.getLeftWindowEdge());
            }
        }

        this.outputStream.notifyAllPacketTasks();
    }

    private void handleDataPacket(SRPacket packet) throws IOException {
        synchronized (this.totalDataPacketsReceived) {
            this.totalDataPacketsReceived++;
        }

        boolean newDataAvailable;
        synchronized (this.bufferedPackets) {
            if(!Utils.seqInWindow(Utils.incSeqNum(this.latestSeqNumDeliveredUp), packet.getSequenceNumber())) {
//                System.out.println(String.format(
//                        "SRInputStream outside window: latestSeqNumDeliveredUp = %s and found %s",
//                        this.latestSeqNumDeliveredUp, packet.getSequenceNumber()));
                return;
            }
            newDataAvailable = this.bufferedPackets.add(packet);
        }
        if(newDataAvailable) {
            synchronized (this.dataAvailableWaitNotify) {
                dataAvailableWaitNotify.notify();
            }
        }

        SRPacket ackSendPacket = this.packetBuilder
                .type(PacketType.ACK)
                .ackSequenceNumber(Utils.incSeqNum(packet.getSequenceNumber()))
                .build();
//        System.out.println("Sending ack: " + ackSendPacket.toString());
        PacketUtils.sendSRPacketToRouter(this, ackSendPacket);
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        ArrayList<Byte> allBytes = new ArrayList<>();
        while(true) {
            int b = read();
            if(b == -1) {
                break;
            }
            allBytes.add((byte)b);
            if(allBytes.size() == len) {
                break;
            }
        }
        return Utils.toByteArray(allBytes);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        ArrayList<Byte> allBytes = new ArrayList<>();
        while(true) {
            int b = read();
            if(b == -1) {
                break;
            }
            allBytes.add((byte)b);
        }
        return Utils.toByteArray(allBytes);
    }

    @Override
    public int read() throws IOException {
        if (this.buffer == null) {
            try {
                this.buffer = getAvailableBytes();
                while(this.buffer == null) {
                    synchronized (this.dataAvailableWaitNotify) {
                        dataAvailableWaitNotify.wait(1000);
                    }
                    if(this.udpSocket.isClosed()) {
                        return -1;
                    }
                    this.buffer = getAvailableBytes();
                }
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for payload bytes to become available");
            }
            this.position = 0;
        }
        if(position == this.buffer.length) {
            this.buffer = null;
            return -1;
        }
        return this.buffer[position++] & 0xff;
    }

    private byte[] getAvailableBytes() throws InterruptedException {
        List<SRPacket> availablePackets = new ArrayList<>();
        int availableByteCount = 0;

        synchronized (this.bufferedPackets) {
            for (SRPacket packet: this.bufferedPackets) {
                int nextExpectedReadSeqNum = Utils.incSeqNum(this.latestSeqNumDeliveredUp);
                if(packet.getSequenceNumber() == nextExpectedReadSeqNum) {
                    availablePackets.add(packet);
                    availableByteCount += packet.getPayload().length;
                    this.latestSeqNumDeliveredUp = Utils.incSeqNum(this.latestSeqNumDeliveredUp);
                } else {
                    // System.out.println(String.format("Hole found: expected %s but received %s", nextExpectedReadSeqNum, packet.getSequenceNumber()));
                    break;
                }
            }
            availablePackets.forEach(this.bufferedPackets::remove);
            if(!availablePackets.isEmpty()) {
//                System.out.println("Updated receiver's latestSeqNumDeliveredUp to: " + this.latestSeqNumDeliveredUp);
            }
        }

        if(availablePackets.isEmpty()) {
            return null;
        }

        synchronized (this.uniqueDataPacketsReceived) {
            this.uniqueDataPacketsReceived += availablePackets.size();
        }

        ByteBuffer availableBytes = ByteBuffer.allocate(availableByteCount);
        availablePackets.forEach(packet -> availableBytes.put(packet.getPayload()));
        return availableBytes.array();
    }

    @Override
    public void close() {
        this.packetReceiverLoop.cancel(true);
        this.bufferedPackets.clear();
    }
}
