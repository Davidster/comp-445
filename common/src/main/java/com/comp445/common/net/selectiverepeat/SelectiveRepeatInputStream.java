package com.comp445.common.net.selectiverepeat;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import static com.comp445.common.Util.MAX_PACKET_LENGTH;

public class SelectiveRepeatInputStream extends InputStream {

    private DatagramSocket socket;
    private byte[] buffer;
    private int position;

    public SelectiveRepeatInputStream(DatagramSocket socket) {
        this.socket = socket;
//        this.buffer = new byte[0];
        this.position = 0;
    }

    @Override
    public int read() throws IOException {
        if (buffer == null) {
            byte[] recBuffer = new byte[MAX_PACKET_LENGTH];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            socket.receive(recPacket);
            RouterPacket receivedRouterPacket = RouterPacket.fromByteArray(Arrays.copyOfRange(recPacket.getData(), 0, recPacket.getLength()));
            buffer = receivedRouterPacket.getPayload();
            position = 0;
        }
        if(position == buffer.length) {
            return -1;
        }
        return buffer[position++];
    }
}
