package com.comp445.common.http;

import com.comp445.common.net.TCPSocket;

public class TCPHttpClient extends HttpClient {
    public TCPHttpClient(boolean followRedirects) {
        super(TCPSocket.class, followRedirects);
    }
}
