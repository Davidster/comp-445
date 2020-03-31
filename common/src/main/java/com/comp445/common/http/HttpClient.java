package com.comp445.common.http;

import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import com.comp445.common.net.ISocket;
import lombok.AllArgsConstructor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static com.comp445.common.Utils.DEFAULT_SOCKET_TIMEOUT;

@AllArgsConstructor
public class HttpClient {

    private static final int MAX_REDIRECTS = 50;

    private Class<? extends ISocket> socketClass;
    private boolean followRedirects;

    public HttpResponse performRequest(HttpRequest request) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return this.followRedirects ?
                performRedirectableRequest(request) :
                performSimpleRequest(request);
    }

    private HttpResponse performRedirectableRequest(HttpRequest request) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

    private HttpResponse performSimpleRequest(HttpRequest request) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        URL url = request.getUrl();

        if(url.getProtocol().equals("https")) {
            throw new MalformedURLException("Protocol: https not supported");
        }

        // setup connection
        InetAddress address = InetAddress.getByName(url.getHost());
        int port = url.getPort() != -1 ? url.getPort() : 80;
        ISocket clientSocket = socketClass.getConstructor().newInstance();
        clientSocket.connect(new InetSocketAddress(address, port), DEFAULT_SOCKET_TIMEOUT);
        BufferedInputStream clientInputStream = new BufferedInputStream(clientSocket.getInputStream());
        OutputStream clientOutputStream = clientSocket.getOutputStream();

        Logger.log(String.format("\nConnected to host %s (%s) on port %s",
                url.getHost(), address.getHostAddress(), port), LogLevel.VERBOSE);
        String reqVDelimiter = "\n> ";
        Logger.log(reqVDelimiter + String.join(reqVDelimiter, request.toHeadersList()) + reqVDelimiter, LogLevel.VERBOSE);

        // send request
        clientOutputStream.write(request.toByteArray());
        clientOutputStream.flush();

        // get response
        HttpResponse response = HttpResponse.fromInputStream(clientInputStream);

        String resVDelimiter = "\n< ";
        Logger.log(resVDelimiter + response.getStatus().toString(), LogLevel.VERBOSE);
        Logger.log( "< " + String.join(resVDelimiter, response.getHeaders().toStringList()) + resVDelimiter,
                LogLevel.VERBOSE);
        if(response.getBody() != null) {
            Logger.log(new String(response.getBody()));
        }

        // clean up
        clientInputStream.close();
        clientOutputStream.close();
        clientSocket.close();

        return response;
    }
}
