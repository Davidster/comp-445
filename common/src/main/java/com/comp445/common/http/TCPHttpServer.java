package com.comp445.common.http;

import com.comp445.common.net.TCPServerSocket;

import java.util.function.Function;

public class TCPHttpServer extends HttpServer {
    public TCPHttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        super(TCPServerSocket.class, port, requestHandler);
    }
}
