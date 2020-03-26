package com.comp445.common.http;

import java.io.IOException;
import java.util.function.Function;

public abstract class HttpServer {

    public static final byte[] DEFAULT_RESPONSE_BODY = "NOTHING SPECIAL HERE".getBytes();
    public static final HttpResponse DEFAULT_RESPONSE =
            new HttpResponse(new HttpStatus(HttpStatus.STATUS_NOT_FOUND), new HttpHeaders(), DEFAULT_RESPONSE_BODY);

    protected int port;
    protected Function<HttpRequest, HttpResponse> requestHandler;

    public HttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
    }

    public abstract void start() throws IOException;
}
