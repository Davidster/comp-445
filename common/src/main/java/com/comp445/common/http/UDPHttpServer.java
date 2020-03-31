package com.comp445.common.http;

import com.comp445.common.net.selectiverepeat.SRServerSocket;

import java.util.function.Function;

public class UDPHttpServer extends HttpServer {
    public UDPHttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        super(SRServerSocket.class, port, requestHandler);
    }
}
