package com.comp445.common.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class TCPHttpClient extends HttpClient {

    public TCPHttpClient(boolean followRedirects) {
        super(followRedirects);
    }

    @Override
    protected HttpResponse performSimpleRequest(HttpRequest request) throws IOException {
        URL url = request.getUrl();

        if(url.getProtocol().equals("https")) {
            throw new MalformedURLException("Protocol: https not supported");
        }

        // setup connection
        InetAddress address = InetAddress.getByName(url.getHost());
        int port = url.getPort() != -1 ? url.getPort() : 80;
        int timeout = 3000;
        Socket clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(address, port), timeout);
        BufferedInputStream clientInputStream = new BufferedInputStream(clientSocket.getInputStream());
        OutputStream clientOutputStream = clientSocket.getOutputStream();

        // send request
        clientOutputStream.write(request.toByteArray());
        clientOutputStream.flush();

        // get response
        HttpResponse response = HttpResponse.fromInputStream(clientInputStream);

        // clean up
        clientInputStream.close();
        clientOutputStream.close();
        clientSocket.close();

        logRequestInfo(url, address, port, request, response);

        return response;
    }
}
