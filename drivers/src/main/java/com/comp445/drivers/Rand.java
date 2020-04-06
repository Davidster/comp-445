package com.comp445.drivers;

import com.comp445.common.Utils;
import com.comp445.common.net.selectiverepeat.SRPacket;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.comp445.common.Utils.SR_MAX_SEQUENCE_NUM;

public class Rand {
    public static void main(String[] args) {
//        System.out.println(Utils.decSeqNum(SR_MAX_SEQUENCE_NUM - 1));
//        System.out.println(Utils.decSeqNum(1));
//        System.out.println(Utils.decSeqNum(0));

        SortedSet<SRPacket> bufferedPackets = Collections.synchronizedSortedSet(new TreeSet<>());

        SRPacket packet3 = SRPacket.builder().sequenceNumber(25).build();
        SRPacket packet2 = SRPacket.builder().sequenceNumber(23).build();
        SRPacket packet1 = SRPacket.builder().sequenceNumber(24).build();

        bufferedPackets.add(packet1);
        bufferedPackets.add(packet2);
        bufferedPackets.add(packet3);
        bufferedPackets.add(packet3);
        bufferedPackets.add(packet3);

        SRPacket first = bufferedPackets.first();
        SRPacket last = bufferedPackets.last();
//        System.out.println(String.format("First: %s", first.getSequenceNumber()));
//        System.out.println(String.format("Last: %s", last.getSequenceNumber()));
//        System.out.println(String.format("Size: %s", bufferedPackets.size()));

        int nextSequenceNumber = 24;

//        boolean hasHole = false;
//        int lastSequenceNumber = Utils.decSeqNum(nextSequenceNumber);
//        for (SRPacket packet: bufferedPackets) {
//            int nextExpectedSequenceNumber = Utils.incSeqNum(lastSequenceNumber);
//            if(packet.getSequenceNumber() != nextExpectedSequenceNumber) {
//                System.out.println(String.format("Hole found: %s -> %s", lastSequenceNumber, packet.getSequenceNumber()));
//                hasHole = true;
//                break;
//            }
//            lastSequenceNumber = packet.getSequenceNumber();
//        }
//        System.out.println("Has hole: " + hasHole);
    }
}
