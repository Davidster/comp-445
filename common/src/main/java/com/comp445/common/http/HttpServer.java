package com.comp445.common.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class HttpServer {

    private final HttpResponse DEFAULT_RESPONSE =
            new HttpResponse(new HttpStatus(HttpStatus.STATUS_NOT_FOUND), new HttpHeaders());

    public void startServer(int port, Function<HttpRequest, HttpResponse> requestHandler) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        //noinspection InfiniteLoopStatement
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream clientOutputWriter = new DataOutputStream(connectionSocket.getOutputStream());

            HttpRequest request = HttpRequest.fromInputStream(clientInputReader);

            HttpResponse response = requestHandler != null ? requestHandler.apply(request) : DEFAULT_RESPONSE;

            clientOutputWriter.writeBytes(response.toString());
            clientOutputWriter.close();
        }
    }
}
