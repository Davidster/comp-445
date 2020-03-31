package com.comp445.common.http;

import com.comp445.common.net.selectiverepeat.SRSocket;

public class UDPHttpClient extends HttpClient {
    public UDPHttpClient(boolean followRedirects) {
        super(SRSocket.class, followRedirects);
    }
}
