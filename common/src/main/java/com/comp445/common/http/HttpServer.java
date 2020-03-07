package com.comp445.common.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class HttpServer {

    private final HttpResponse DEFAULT_RESPONSE =
            new HttpResponse(new HttpStatus(HttpStatus.STATUS_NOT_FOUND), new HttpHeaders(), "hii".getBytes());

    public void startServer(int port, Function<HttpRequest, HttpResponse> requestHandler) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        //noinspection InfiniteLoopStatement
        while (true) {
            Socket serverClientSocket = serverSocket.accept();
            InputStream serverInputStream = serverClientSocket.getInputStream();
            OutputStream serverOutputStream = serverClientSocket.getOutputStream();

            HttpRequest request = HttpRequest.fromInputStream(serverInputStream);

            HttpResponse response = requestHandler != null ? requestHandler.apply(request) : DEFAULT_RESPONSE;


            serverOutputStream.write(response.toByteArray());
            serverOutputStream.flush();

            serverInputStream.close();
            serverOutputStream.close();
        }
    }
}
