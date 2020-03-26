package com.comp445.common.net;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

@AllArgsConstructor
public class TCPSocket implements ISocket {

    private Socket socket;

    public TCPSocket() {
        this.socket = new Socket();
    }

    @Override
    public void connect(SocketAddress destination, int timeout) throws IOException {
        socket.connect(destination, timeout);
    }
    @Override
    public void close() throws IOException {
        socket.close();
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
}
