package com.comp445.common.http;

import com.comp445.common.selectiverepeat.SelectiveRepeatClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

public class UDPHttpClient extends HttpClient {

    public UDPHttpClient(boolean followRedirects) {
        super(followRedirects);
    }

    @Override
    protected HttpResponse performSimpleRequest(HttpRequest request) throws IOException {
        URL url = request.getUrl();

        // setup connection
        InetAddress address = InetAddress.getByName(url.getHost());
        int port = url.getPort() != -1 ? url.getPort() : 80;
        int timeout = 3000;
        SelectiveRepeatClient client = new SelectiveRepeatClient(new InetSocketAddress(address, port), timeout);

        // send request
        byte[] responseBytes = client.send(request.toByteArray());

        // get response
        HttpResponse response = HttpResponse.fromInputStream(new BufferedInputStream(new ByteArrayInputStream(responseBytes)));

        logRequestInfo(url, address, port, request, response);

        return response;
    }
}
