package com.comp445.common.http;

import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpClient {

    private static final String LOCATION = "Location";

    private boolean followRedirects;

    public HttpClient(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public HttpResponse performGetRequest(Map<String, String> headers, URL url) throws IOException {
        HttpResponse response;
        do {
            String requestBody = buildBaseRequestBody(HttpMethod.GET, headers, url);
            response = performRequest(requestBody, url);
            url = requiresRedirect(response, url);
        } while(url != null);
        return response;
    }

    public HttpResponse performPostRequest(Map<String, String> headers, URL url, String body) throws IOException {
        HttpResponse response;
        do {
            String requestBody = buildBaseRequestBody(HttpMethod.POST, headers, url)
                    + "Content-Length: " + body.length()
                    + "\n\r\n" + body;
            response = performRequest(requestBody, url);
            url = requiresRedirect(response, url);
        } while(url != null);
        return response;
    }

    private String buildBaseRequestBody(HttpMethod httpMethod, Map<String, String> headers, URL url) throws MalformedURLException {
        if(url.getProtocol().equals("https")) {
            throw new MalformedURLException("Protocol: https not supported");
        }
        String path = url.getFile().equals("") ? "/" : url.getFile();
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
        int port = url.getPort() != -1 ? url.getPort() : 80;
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
        List<String> fullResponseLines = in.lines().collect(Collectors.toList());

        // parse status
        String statusLine = fullResponseLines.get(0).trim();
        String[] statusLineSplit = statusLine.split(" ");
        int statusCode = statusLineSplit.length > 1 ?
                Integer.parseInt(statusLineSplit[1])
                : -1;
        String statusReason = statusLineSplit.length > 2 ?
                statusLine.substring(statusLine.indexOf(statusLineSplit[1]) + 4)
                : null;

        // parse headers/body
        Pattern emptyLine = Pattern.compile("^\\s*$");
        int bodySeparatorIndex = fullResponseLines.stream()
                .map(line -> emptyLine.matcher(line).matches())
                .collect(Collectors.toList())
                .indexOf(true);
        if(bodySeparatorIndex == -1) {
            bodySeparatorIndex = fullResponseLines.size() - 1;
        }
        String responseBody = (bodySeparatorIndex < fullResponseLines.size() - 1) ?
                String.join("\n",
                        fullResponseLines.subList(bodySeparatorIndex + 1, fullResponseLines.size()))
                : "";

        List<String> headerLines = fullResponseLines.subList(1, bodySeparatorIndex);
        Map<String, String> responseHeaders =
                headerLines.stream()
                    .map(String::trim)
                    .map(headerArg -> headerArg.split(":", 2))
                    .collect(Collectors.toMap(
                            headerArgSplit -> headerArgSplit[0].trim(),
                            headerArgSplit -> headerArgSplit[1].trim(),
                            (val1, val2) -> val1));

        String resVDelimiter = "\n< ";
        Logger.log(resVDelimiter + statusLine, LogLevel.VERBOSE);
        Logger.log( "< " + String.join(resVDelimiter, headerLines) + resVDelimiter,
                    LogLevel.VERBOSE);
        Logger.log(responseBody);

        // clean up
        in.close();
        out.close();
        clientSocket.close();

        return new HttpResponse(new HttpStatus(statusCode, statusReason), responseHeaders, responseBody);
    }

    private URL requiresRedirect(HttpResponse response, URL originalUrl) throws IOException {
        if(!this.followRedirects) {
            return null;
        }
        int statusCode = response.getStatus().getCode();
        Map<String, String> responseHeaders = response.getHeaders();
        if(statusCode >= 300 && statusCode < 400 && responseHeaders.containsKey(LOCATION)) {
            String location = responseHeaders.get(LOCATION);
            if(location.indexOf("http") != 0) {
                location = String.format("http://%s%s", originalUrl.getHost(), location);
            }
            return new URL(location);
        }
        return null;
    }
}
