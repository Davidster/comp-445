package com.comp445.common.http;

import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HttpClient {

    private static final String LOCATION = "Location";

    private static final int MAX_REDIRECTS = 50;

    private boolean followRedirects;

    public HttpClient(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public HttpResponse performRequest(HttpRequest request) throws IOException {
        return this.followRedirects ?
                performRedirectableRequest(request) :
                performSimpleRequest(request);
    }

    private HttpResponse performRedirectableRequest(HttpRequest request) throws IOException {
        HttpResponse response;
        int redirects = 0;
        do {
            response = performSimpleRequest(request);
            Optional<String> locationOpt = getRedirectLocation(response);
            if(locationOpt.isEmpty()) {
                break;
            }
            String location = locationOpt.get();
            if(location.indexOf("http") != 0) {
                location = String.format("http://%s%s", request.url.getHost(), location);
            }
            request.setUrl(new URL(location));
            redirects++;
        } while(redirects < MAX_REDIRECTS);
        return response;
    }

    private HttpResponse performSimpleRequest(HttpRequest request) throws IOException {
        URL url = request.getUrl();

        if(url.getProtocol().equals("https")) {
            throw new MalformedURLException("Protocol: https not supported");
        }

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
        Logger.log(reqVDelimiter + String.join(reqVDelimiter, request.toStringList()) + reqVDelimiter, LogLevel.VERBOSE);

        // send request
        out.println(request.toString());

        HttpResponse response = HttpResponse.fromLines(in.lines().collect(Collectors.toList()));

        String resVDelimiter = "\n< ";
        Logger.log(resVDelimiter + response.getStatus().toString(), LogLevel.VERBOSE);
        Logger.log( "< " + String.join(resVDelimiter, response.getHeaders().toStringList()) + resVDelimiter,
                    LogLevel.VERBOSE);
        Logger.log(response.getBody());

        // clean up
        in.close();
        out.close();
        clientSocket.close();

        return response;
    }

    private Optional<String> getRedirectLocation(HttpResponse response) {
        int statusCode = response.getStatus().getCode();
        Map<String, String> responseHeaders = response.getHeaders();
        if(statusCode >= 300 && statusCode < 400 && responseHeaders.containsKey(LOCATION)) {
            return Optional.of(responseHeaders.get(LOCATION));
        }
        return Optional.empty();
    }
}
