package com.comp445.httpc.command;

import com.comp445.common.http.HttpClient;
import com.comp445.common.http.HttpResponse;
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
