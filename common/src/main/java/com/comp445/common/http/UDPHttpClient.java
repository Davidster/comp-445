package com.comp445.common.http;

import com.comp445.common.net.selectiverepeat.SelectiveRepeatSocket;

public class UDPHttpClient extends HttpClient {
    public UDPHttpClient(boolean followRedirects) {
        super(SelectiveRepeatSocket.class, followRedirects);
    }
}
