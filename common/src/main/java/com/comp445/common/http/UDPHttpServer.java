package com.comp445.common.http;

import com.comp445.common.net.selectiverepeat.SelectiveRepeatServerSocket;

import java.util.function.Function;

public class UDPHttpServer extends HttpServer {
    public UDPHttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        super(SelectiveRepeatServerSocket.class, port, requestHandler);
    }
}
