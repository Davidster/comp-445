package com.comp445.common.net.selectiverepeat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static com.comp445.common.Utils.ARQ_ROUTER_PORT;
import static com.comp445.common.Utils.SR_MAX_PACKET_LENGTH;
import static com.comp445.common.net.selectiverepeat.SRPacket.MAX_PAYLOAD_SIZE;

public class SROutputStream extends OutputStream {

    private DatagramSocket socket;
    private InetSocketAddress destination;
    private byte[] buffer;
    private int size;

    public SROutputStream(DatagramSocket socket, InetSocketAddress destination) {
        this.socket = socket;
        this.destination = destination;
        this.buffer = new byte[MAX_PAYLOAD_SIZE];
        this.size = 0;
    }

    @Override
    public void flush() throws IOException {
        if (this.size == 0) {
            return;
        }

        SRPacket sendSRPacket = SRPacket.builder()
                .peerAddress(this.destination.getAddress())
                .port(this.destination.getPort())
                .payload(Arrays.copyOfRange(this.buffer, 0, size))
                .build();
        byte[] sendBuffer = sendSRPacket.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, this.destination.getAddress(), ARQ_ROUTER_PORT);
        this.socket.send(sendPacket);

        Arrays.fill(this.buffer, (byte)0);
        this.size = 0;
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer[size++] = (byte)b;
        if (this.size == SR_MAX_PACKET_LENGTH) {
            flush();
        }
    }
}
