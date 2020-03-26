package com.comp445.common.selectiverepeat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@AllArgsConstructor
@Getter
@Setter
public class RouterPacket {

    private PacketType type; // 1 byte
    private int sequenceNumber; // 4 bytes
    private InetAddress peerAddress; // 4 bytes
    private int port; // 2 bytes
    private byte[] payload; // variable length

    private static final int MIN_PACKET_SIZE = 1 + 4 + 4 + 2;

    public RouterPacket() throws UnknownHostException {
        this.type = PacketType.DATA;
        this.sequenceNumber = 0;
        this.peerAddress = InetAddress.getByAddress(new byte[]{ (byte)127, (byte)0, (byte)0, (byte)1 });
        this.port = 8080;
        this.payload = new byte[0];
    }

    public static RouterPacket fromByteArray(byte[] bytes) throws UnknownHostException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
//        System.out.println(byteBuffer.getShort(9));
        return new RouterPacket(
                PacketType.fromByteValue(byteBuffer.get(0)),
                byteBuffer.getInt(1),
                InetAddress.getByAddress(Arrays.copyOfRange(bytes, 5, 9)),
                byteBuffer.getShort(9) & 0xffff,
                Arrays.copyOfRange(bytes, MIN_PACKET_SIZE, bytes.length)
        );
    }

    public byte[] toByteArray() throws IOException {
        return ByteBuffer.allocate(MIN_PACKET_SIZE + payload.length)
                .put(type.getValue())
                .putInt(sequenceNumber)
                .put(peerAddress.getAddress())
                .putShort((short) port)
                .put(payload)
                .array();
    }

//    public static void main(String[] args) throws IOException {
//        RouterPacket somePacket = new RouterPacket(
//                PacketType.NACK,
//                34432985,
//                InetAddress.getByName("localhost"),
//                40000,
//                new byte[]{(byte) 42, (byte) 69, (byte) 69}
//        );
//        byte[] asBytes = somePacket.toByteArray();
//        RouterPacket serDeSer = fromByteArray(asBytes);
//        System.out.println(serDeSer.payload[0]);
//    }
}
