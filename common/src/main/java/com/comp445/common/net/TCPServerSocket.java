package com.comp445.common.net;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServerSocket extends ServerSocket implements IServerSocket {
    public TCPServerSocket(int port) throws IOException {
        super(port);
    }
    @Override
    public ISocket acceptClient() throws IOException {
        return new TCPSocket(super.accept());
    }
}
