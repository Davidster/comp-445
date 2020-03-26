package com.comp445.common.http;

import com.comp445.common.selectiverepeat.SelectiveRepeatServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Function;

public class UDPHttpServer extends HttpServer {

    public UDPHttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        super(port, requestHandler);
    }

    @Override
    public void start() throws IOException {
        //noinspection InfiniteLoopStatement
        while(true) {
            SelectiveRepeatServer server = new SelectiveRepeatServer(port);

            byte[] requestBytes = server.receive();
            HttpRequest request = HttpRequest.fromInputStream(new BufferedInputStream(new ByteArrayInputStream(requestBytes)));

            HttpResponse response = requestHandler != null ? requestHandler.apply(request) : DEFAULT_RESPONSE;

            server.sendResponse(response.toByteArray());
        }
    }
}
