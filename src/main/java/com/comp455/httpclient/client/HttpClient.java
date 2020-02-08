package com.comp455.httpclient.client;

import com.comp455.httpclient.logger.LogLevel;
import com.comp455.httpclient.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpClient {

    public HttpResponse performGetRequest(Map<String, String> headers, URL url) throws IOException {
        String requestBody = buildBaseRequestBody(HttpMethod.GET, headers, url);
        return performRequest(requestBody, url);
    }

    public HttpResponse performPostRequest(Map<String, String> headers, URL url, String body) throws IOException {
        String requestBody = buildBaseRequestBody(HttpMethod.POST, headers, url)
                + "\r\n" + body;
        return performRequest(requestBody, url);
    }

    private String buildBaseRequestBody(HttpMethod httpMethod, Map<String, String> headers, URL url) {
        String path = url.getPath().equals("") ? "/" : url.getPath();
        return Stream.concat(
                List.of(
                        String.format("%s %s HTTP/1.0", httpMethod.toString(), path),
                        String.format("Host: %s", url.getHost())
                ).stream(),
                headers.keySet().stream().map(key ->
                        String.format("%s: %s", key, headers.get(key)))
        )
                .collect(Collectors.joining("\n")) + "\n";
    }

    private HttpResponse performRequest(String requestBody, URL url) throws IOException {
        // get ip address
        InetAddress address = InetAddress.getByName(url.getHost());

        // open/connect socket
        int port = url.getPort() != -1 ?
                url.getPort() : 80;
        int timeout = 3000;
        Socket clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(address, port), timeout);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Logger.log(String.format("\nConnected to host %s (%s) on port %s",
                url.getHost(), address.getHostAddress(), port), LogLevel.VERBOSE);
        String reqVDelimiter = "\n> ";
        Logger.log(reqVDelimiter + String.join(reqVDelimiter, requestBody.split("\n")) + reqVDelimiter, LogLevel.VERBOSE);

        // send request
        out.println(requestBody);

        // read response
        String fullResponse = in.lines().collect(Collectors.joining("\n"));
        int bodySeparatorIndex = fullResponse.indexOf("\n\n");
        String responseHeaders, responseBody;
        if(bodySeparatorIndex == -1) {
            responseHeaders = fullResponse;
            responseBody = "";
        } else {
            responseHeaders = fullResponse.substring(0, bodySeparatorIndex);
            responseBody = fullResponse.substring(bodySeparatorIndex + 2);
        }

        String resVDelimiter = "\n< ";
        Logger.log(resVDelimiter + String.join(resVDelimiter, responseHeaders.split("\n")) + resVDelimiter, LogLevel.VERBOSE);
        Logger.log(responseBody);

        // clean up
        in.close();
        out.close();
        clientSocket.close();

        return new HttpResponse(responseHeaders, responseBody);
    }
}
