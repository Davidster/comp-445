package com.comp445.drivers;

import com.comp445.common.net.selectiverepeat.PacketType;
import com.comp445.common.net.selectiverepeat.RouterPacket;

import java.net.InetAddress;

import static com.comp445.common.Util.doAssert;

public class RouterPacketSerializationTest {
    public static void main(String[] args) throws Exception {

        PacketType packetType = PacketType.NACK;
        int sequenceNumber = 34432985;
        InetAddress address = InetAddress.getByName("localhost");
        int port = 40000;
        byte[] payload = new byte[]{ (byte)42, (byte)69, (byte)69 };

        RouterPacket somePacket = new RouterPacket(packetType, sequenceNumber, address, port, payload);
        RouterPacket serDeSer = RouterPacket.fromByteArray(somePacket.toByteArray());

        doAssert(somePacket.equals(serDeSer));
    }
}
