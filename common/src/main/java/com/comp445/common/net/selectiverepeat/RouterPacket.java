package com.comp445.common.net.selectiverepeat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.comp445.common.Util.MAX_PACKET_LENGTH;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class RouterPacket {

    private PacketType type; // 1 byte
    private int sequenceNumber; // 4 bytes
    private InetAddress peerAddress; // 4 bytes
    private int port; // 2 bytes
    private byte[] payload; // variable length

    private static final int MIN_PACKET_SIZE = 1 + 4 + 4 + 2;
    public static final int MAX_PAYLOAD_SIZE = MAX_PACKET_LENGTH - MIN_PACKET_SIZE;

    public RouterPacket() throws UnknownHostException {
        this.type = PacketType.DATA;
        this.sequenceNumber = 0;
        this.peerAddress = InetAddress.getByAddress(new byte[]{ (byte)127, (byte)0, (byte)0, (byte)1 });
        this.port = 8080;
        this.payload = new byte[0];
    }

    public static RouterPacket fromByteArray(byte[] bytes) throws UnknownHostException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new RouterPacket(
                PacketType.fromByteValue(byteBuffer.get(0)),
                byteBuffer.getInt(1),
                InetAddress.getByAddress(Arrays.copyOfRange(bytes, 5, 9)),
                byteBuffer.getShort(9) & 0xffff,
                Arrays.copyOfRange(bytes, MIN_PACKET_SIZE, bytes.length)
        );
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(MIN_PACKET_SIZE + payload.length)
                .put(type.getValue())
                .putInt(sequenceNumber)
                .put(peerAddress.getAddress())
                .putShort((short) port)
                .put(payload)
                .array();
    }
}
