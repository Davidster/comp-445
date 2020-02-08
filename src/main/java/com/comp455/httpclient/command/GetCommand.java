package com.comp455.httpclient.command;

import com.comp455.httpclient.client.HttpClient;
import lombok.SneakyThrows;

import java.net.URL;

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

        // parse url
        URL parsedUrl = new URL(this.getRequestUrl());

        new HttpClient().performGetRequest(this.getHeaders(), parsedUrl);
    }
}
