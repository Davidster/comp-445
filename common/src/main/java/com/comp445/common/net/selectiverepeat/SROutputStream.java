package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;
import com.comp445.common.net.UDPSocketContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static com.comp445.common.Utils.*;

public class SROutputStream extends OutputStream implements UDPSocketContainer {

    @Getter private DatagramSocket udpSocket;
    private InetSocketAddress destination;
    private byte[] buffer;
    private int size;
    private int nextSequenceNumber;
    @Getter @Setter private int leftWindowEdge;
    private Map<Integer, Future<Void>> packetSendTasks;
    private RTOCalculator rtoCalculator;
    private final Object ackWaitNotify;

    @Getter private final SortedSet<Integer> ackedSquenceNumbers;

    public SROutputStream(DatagramSocket udpSocket, InetSocketAddress destination, int sequenceNumber) {
        this.udpSocket = udpSocket;
        this.destination = destination;
        this.buffer = new byte[SR_MAX_PAYLOAD_SIZE];
        this.size = 0;
        this.nextSequenceNumber = sequenceNumber;
        this.leftWindowEdge = sequenceNumber - 1;
        this.packetSendTasks = new HashMap<>();
        this.rtoCalculator = new RTOCalculator();
        this.ackWaitNotify = new Object();
        this.ackedSquenceNumbers = Collections.synchronizedSortedSet(new TreeSet<>());
    }

    public void queuePacket() {
        if (this.size == 0) {
            return;
        }

        int sequenceNumber = this.nextSequenceNumber;
        this.nextSequenceNumber = Utils.incSeqNum(this.nextSequenceNumber);

        byte[] sendBuffer = Arrays.copyOfRange(this.buffer, 0, this.size);

        Future<Void> packetSendTask = EXECUTOR.submit(() -> {

            while(true) {
                synchronized (this.ackedSquenceNumbers) {
                    if (seqInWindow(Utils.incSeqNum(this.leftWindowEdge), sequenceNumber)) {
                        break;
                    }
                }
                synchronized (ackWaitNotify) {
                    try {
//                        System.out.println(sequenceNumber + ": waiting for ack notify (not yet sent)");
                        ackWaitNotify.wait();
//                        System.out.println(sequenceNumber + ": stopped waiting for ack notify (not yet sent)");
                    } catch (InterruptedException e) {
                        System.out.println("Unknown error in packetSendTask from ackWaitNotify.wait()");
                        e.printStackTrace();
                    }
                }
            }

            boolean isRetransmission = false;
            retransmissionLoop:
            while(true) {
                try {
                    SRPacket dataSendPacket = SRPacket.builder()
                            .peerAddress(this.destination.getAddress())
                            .port(this.destination.getPort())
                            .type(PacketType.DATA)
                            .sequenceNumber(sequenceNumber)
                            .payload(sendBuffer)
                            .build();
//                    System.out.println("Sending data: " + dataSendPacket.toString());
                    PacketUtils.sendSRPacketToRouter(this, dataSendPacket);
                } catch (IOException e) {
//                    e.printStackTrace();
                    System.out.println("Unknown error in packetSendTask near PacketUtils.sendSRPacketToRouter()");
                    throw new CompletionException(e);
                }

                Instant sendTime = Instant.now();
                Instant timeoutTime = sendTime.plus(Duration.of(this.rtoCalculator.getLatestRto(), ChronoUnit.MILLIS));

                while(Instant.now().isBefore(timeoutTime.minus(SR_CLOCK_GRANULARITY_D))) {
                    boolean acked;
                    synchronized (ackWaitNotify) {
                        ackWaitNotify.wait(Duration.between(Instant.now(), timeoutTime).toMillis() + 1);
                    }
                    synchronized (this.ackedSquenceNumbers) {
                        // TODO: less-than operator wont work for sequence numbers are circular
                        acked = sequenceNumber <= this.leftWindowEdge || this.ackedSquenceNumbers.contains(sequenceNumber);
                    }
                    if(acked) {
                        if(!isRetransmission) {
                            this.rtoCalculator.update(Duration.between(sendTime, Instant.now()).toMillis());
//                            System.out.println(String.format("%s: rto recalculated by value of %sms. new rto is %s", sequenceNumber, Duration.between(sendTime, Instant.now()).toMillis(), rtoCalculator.getLatestRto()));
                        }
                        break retransmissionLoop;
                    }
                }

                this.rtoCalculator.onTimeout();
//                System.out.println(String.format("%s: rto recalc due to timeout. new rto is %s", sequenceNumber, this.rtoCalculator.getLatestRto()));
                isRetransmission = true;
            }
//            this.packetSendTasks.remove(sequenceNumber);
            return null;
        });

        this.packetSendTasks.put(sequenceNumber, packetSendTask);

        Arrays.fill(this.buffer, (byte)0);
        this.size = 0;
    }

    @Override
    public void flush() throws IOException {
        queuePacket();
        this.packetSendTasks.forEach((seqNum, task) -> {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        });
    }

    public void notifyAllPacketTasks() {
        synchronized (ackWaitNotify) {
            ackWaitNotify.notifyAll();
        }
    }

    @Override
    public void write(int b) {
        this.buffer[this.size++] = (byte)b;
        if (this.size == SR_MAX_PAYLOAD_SIZE) {
            queuePacket();
        }
    }

    @Override
    public void close() {
        this.packetSendTasks.forEach((sequenceNumber, task) -> {
            task.cancel(true);
        });
        this.ackedSquenceNumbers.clear();
    }
}
