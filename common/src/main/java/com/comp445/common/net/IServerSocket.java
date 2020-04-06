package com.comp445.common.net;

import java.io.Closeable;
import java.io.IOException;

public interface IServerSocket extends Closeable {
    ISocket acceptClient() throws IOException;
}
