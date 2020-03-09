package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

@AllArgsConstructor
@RequiredArgsConstructor
public class HttpServer {

    @NonNull
    private int port;
    private Function<HttpRequest, HttpResponse> requestHandler;

    private static final byte[] DEFAULT_RESPONSE_BODY = "NOTHING SPECIAL HERE".getBytes();
    private static final HttpResponse DEFAULT_RESPONSE =
            new HttpResponse(new HttpStatus(HttpStatus.STATUS_NOT_FOUND), new HttpHeaders(), DEFAULT_RESPONSE_BODY);

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        //noinspection InfiniteLoopStatement
        while (true) {
            Socket serverClientSocket = serverSocket.accept();
            BufferedInputStream serverInputStream = new BufferedInputStream(serverClientSocket.getInputStream());
            OutputStream serverOutputStream = serverClientSocket.getOutputStream();

            HttpRequest request = HttpRequest.fromInputStream(serverInputStream);

            HttpResponse response = requestHandler != null ? requestHandler.apply(request) : DEFAULT_RESPONSE;

            serverOutputStream.write(response.toByteArray());

            serverOutputStream.close();
            serverInputStream.close();
        }
    }
}
