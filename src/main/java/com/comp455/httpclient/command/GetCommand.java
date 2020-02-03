package com.comp455.httpclient.command;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class GetCommand extends HttpCommand {

    public GetCommand() {
        super(CommandType.HTTP_GET);
    }

    @SneakyThrows
    @Override
    public void run() {
        // TODO

        if(this.getRequestUrl() == null) {
            // TODO: print warning?
            return;
        }

        System.out.println(String.format("Should make a GET request to URL: %s", this.getRequestUrl()));

        if(this.getHeaders() != null && !this.getHeaders().isEmpty()) {
            System.out.println("With headers:");
            this.getHeaders().forEach((name, value) -> {
                System.out.println(String.format("'%s': '%s'", name, value));
            });
        }

        // PARSE URL
        URL parsedUrl = new URL(this.getRequestUrl());

        // GET IP ADDRESS
        InetAddress address = InetAddress.getByName(parsedUrl.getHost());

        // OPEN SOCKET
        Socket clientSocket = new Socket(address, 80);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // SEND HTTP DATA
        out.println(List.of(
                String.format("GET %s HTTP/1.0", parsedUrl.getPath()),
                String.format("Host: %s", parsedUrl.getHost()),
                ""
                ).stream().collect(Collectors.joining("\n")));

        // READ RESPONSE
        String responseString = in.lines().collect(Collectors.joining("\n"));

        // CLEAN UP
        in.close();
        out.close();
        clientSocket.close();

        System.out.println(String.format("\nResponse:\n\n%s", responseString));
    }
}
