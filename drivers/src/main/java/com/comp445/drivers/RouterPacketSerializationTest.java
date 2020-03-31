package com.comp445.drivers;

import com.comp445.common.net.selectiverepeat.PacketType;
import com.comp445.common.net.selectiverepeat.SRPacket;

import java.net.InetAddress;

import static com.comp445.common.Utils.doAssert;

public class RouterPacketSerializationTest {
    public static void main(String[] args) throws Exception {

        PacketType packetType = PacketType.NACK;
        int sequenceNumber = 60000;
        int ackSequenceNumber = 4302;
        InetAddress address = InetAddress.getByName("localhost");
        int port = 40000;
        byte[] payload = new byte[]{ (byte)42, (byte)69, (byte)69 };

        SRPacket somePacket = new SRPacket(packetType, sequenceNumber, ackSequenceNumber, address, port, payload);
        SRPacket serDeSer = SRPacket.fromByteArray(somePacket.toByteArray());

        doAssert(somePacket.equals(serDeSer));
    }
}
