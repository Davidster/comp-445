package com.comp455.httpclient.command;

import com.comp455.httpclient.client.HttpClient;
import com.comp455.httpclient.client.HttpResponse;
import lombok.SneakyThrows;

import java.net.URL;

public class GetCommand extends HttpCommand {

    public GetCommand() {
        super(CommandType.HTTP_GET);
    }

    @SneakyThrows
    @Override
    public void run() {
        preResponse();

        // parse url
        URL parsedUrl = new URL(this.getRequestUrl());

        HttpResponse httpResponse = new HttpClient(this.isFollowRedirects())
                .performGetRequest(this.getHeaders(), parsedUrl);

        postResponse(httpResponse);
    }
}
