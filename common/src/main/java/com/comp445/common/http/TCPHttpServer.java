package com.comp445.common.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class TCPHttpServer extends HttpServer {

    public TCPHttpServer(int port, Function<HttpRequest, HttpResponse> requestHandler) {
        super(port, requestHandler);
    }

    @Override
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
