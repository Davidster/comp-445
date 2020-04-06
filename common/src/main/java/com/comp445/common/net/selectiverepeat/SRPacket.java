package com.comp445.common.net.selectiverepeat;

import lombok.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static com.comp445.common.Utils.SR_HEADER_SIZE;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class SRPacket implements Comparable<SRPacket> {

    private PacketType type; // 1 byte
    private int sequenceNumber; // 2 bytes
    private int ackSequenceNumber; // 2 bytes
    private InetAddress peerAddress; // 4 bytes
    private int port; // 2 bytes
    @Builder.Default private byte[] payload = new byte[0]; // variable length

    public static SRPacket fromByteArray(byte[] bytes) throws UnknownHostException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new SRPacket(
                PacketType.fromByteValue(byteBuffer.get(0)),
                byteBuffer.getShort(1) & 0xffff,
                byteBuffer.getShort(3) & 0xffff,
                InetAddress.getByAddress(Arrays.copyOfRange(bytes, 5, 9)),
                byteBuffer.getShort(9) & 0xffff,
                Arrays.copyOfRange(bytes, SR_HEADER_SIZE, bytes.length)
        );
    }

    public static SRPacket fromUDPPacket(DatagramPacket packet) throws UnknownHostException {
        return fromByteArray(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(SR_HEADER_SIZE + this.payload.length)
                .put(this.type.getValue())
                .putShort((short) this.sequenceNumber)
                .putShort((short) this.ackSequenceNumber)
                .put(this.peerAddress.getAddress())
                .putShort((short) this.port)
                .put(this.payload)
                .array();
    }

    @Override
    public int compareTo(SRPacket other) {
        return Integer.compare(sequenceNumber, other.sequenceNumber);
    }
}
