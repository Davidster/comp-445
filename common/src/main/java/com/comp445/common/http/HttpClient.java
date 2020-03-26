package com.comp445.common.http;

import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public abstract class HttpClient {

    private static final int MAX_REDIRECTS = 50;

    protected boolean followRedirects;

    protected abstract HttpResponse performSimpleRequest(HttpRequest request) throws IOException;

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
                location = String.format("http://%s%s", request.getUrl().getHost(), location);
            }
            request.setUrl(new URL(location));
            redirects++;
        } while(redirects < MAX_REDIRECTS);
        return response;
    }

    private Optional<String> getRedirectLocation(HttpResponse response) {
        int statusCode = response.getStatus().getCode();
        Map<String, String> responseHeaders = response.getHeaders();
        if(statusCode >= 300 && statusCode < 400 && responseHeaders.containsKey(HttpHeaders.LOCATION)) {
            return Optional.of(responseHeaders.get(HttpHeaders.LOCATION));
        }
        return Optional.empty();
    }

    protected void logRequestInfo(URL url, InetAddress address, int port, HttpRequest request, HttpResponse response) {
        Logger.log(String.format("\nConnected to host %s (%s) on port %s",
                url.getHost(), address.getHostAddress(), port), LogLevel.VERBOSE);
        String reqVDelimiter = "\n> ";
        Logger.log(reqVDelimiter + String.join(reqVDelimiter, request.toHeadersList()) + reqVDelimiter, LogLevel.VERBOSE);
        String resVDelimiter = "\n< ";
        Logger.log(resVDelimiter + response.getStatus().toString(), LogLevel.VERBOSE);
        Logger.log( "< " + String.join(resVDelimiter, response.getHeaders().toStringList()) + resVDelimiter,
                LogLevel.VERBOSE);
        if(response.getBody() != null) {
            Logger.log(new String(response.getBody()));
        }
    }
}
