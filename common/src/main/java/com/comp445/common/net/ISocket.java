package com.comp445.common.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public interface ISocket extends Closeable {
    void connect(SocketAddress destination, int timeout) throws IOException;
    void close() throws IOException;
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
}
