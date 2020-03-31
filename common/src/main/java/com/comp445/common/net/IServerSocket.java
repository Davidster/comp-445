package com.comp445.common.net;

import java.io.IOException;

public interface IServerSocket {
    ISocket acceptClient() throws IOException;
}
