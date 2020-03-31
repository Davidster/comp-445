package com.comp445.common.net.selectiverepeat;

import lombok.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.comp445.common.Utils.SR_MAX_PACKET_LENGTH;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class SRPacket {

    private PacketType type; // 1 byte
    private int sequenceNumber; // 2 bytes
    private int ackSequenceNumber; // 2 bytes
    private InetAddress peerAddress; // 4 bytes
    private int port; // 2 bytes
    private byte[] payload; // variable length

    private static final int MIN_PACKET_SIZE = 1 + 4 + 4 + 2;
    public static final int MAX_PAYLOAD_SIZE = SR_MAX_PACKET_LENGTH - MIN_PACKET_SIZE;

    public SRPacket() throws UnknownHostException {
        this.type = PacketType.DATA;
        this.sequenceNumber = 0;
        this.ackSequenceNumber = 0;
        this.peerAddress = InetAddress.getByAddress(new byte[]{ (byte)127, (byte)0, (byte)0, (byte)1 });
        this.port = 8080;
        this.payload = new byte[0];
    }

    public static SRPacket fromByteArray(byte[] bytes) throws UnknownHostException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new SRPacket(
                PacketType.fromByteValue(byteBuffer.get(0)),
                byteBuffer.getShort(1) & 0xffff,
                byteBuffer.getShort(3) & 0xffff,
                InetAddress.getByAddress(Arrays.copyOfRange(bytes, 5, 9)),
                byteBuffer.getShort(9) & 0xffff,
                Arrays.copyOfRange(bytes, MIN_PACKET_SIZE, bytes.length)
        );
    }

    public static SRPacket fromUDPPacket(DatagramPacket packet) throws UnknownHostException {
        return fromByteArray(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(MIN_PACKET_SIZE + payload.length)
                .put(type.getValue())
                .putShort((short) sequenceNumber)
                .putShort((short) ackSequenceNumber)
                .put(peerAddress.getAddress())
                .putShort((short) port)
                .put(payload)
                .array();
    }
}
