package com.comp445.common.net.selectiverepeat;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static com.comp445.common.Utils.SR_MAX_PACKET_LENGTH;

public class SRInputStream extends InputStream {

    private DatagramSocket socket;
    private byte[] buffer;
    private int position;

    public SRInputStream(DatagramSocket socket) {
        this.socket = socket;
//        this.buffer = new byte[0];
        this.position = 0;
    }

    @Override
    public int read() throws IOException {
        if (buffer == null) {
            byte[] recBuffer = new byte[SR_MAX_PACKET_LENGTH];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            socket.receive(recPacket);
            SRPacket receivedSRPacket = SRPacket.fromUDPPacket(recPacket);
            buffer = receivedSRPacket.getPayload();
            position = 0;
        }
        if(position == buffer.length) {
            return -1;
        }
        return buffer[position++];
    }
}
