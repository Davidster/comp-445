package com.comp445.common.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HttpServer {

    public void startServer(int port, Function<HttpRequest, HttpResponse> requestHandler) throws IOException {
        String clientSentence;
        String capitalizedSentence;
        ServerSocket serverSocket = new ServerSocket(port);

        //noinspection InfiniteLoopStatement
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream clientOutputWriter = new DataOutputStream(connectionSocket.getOutputStream());

            List<String> fullResponseLines = new ArrayList<>();
            do {
                fullResponseLines.add(clientInputReader.readLine());
            }
            while(!Util.isEmptyLine(fullResponseLines.get(fullResponseLines.size() - 1)));

//            clientSentence = clientInputReader.readLine();
            System.out.println("Received: " + fullResponseLines.stream().collect(Collectors.joining("\n")));
//            capitalizedSentence = clientSentence.toUpperCase() + 'n';
            clientOutputWriter.writeBytes(fullResponseLines.stream().collect(Collectors.joining("\n")));
        }
    }

}
