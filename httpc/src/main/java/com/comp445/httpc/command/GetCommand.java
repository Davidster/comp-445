package com.comp445.httpc.command;

import com.comp445.common.http.*;
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

        URL parsedUrl = new URL(this.getRequestUrl());

        HttpRequest request = new HttpRequest(
                HttpMethod.GET,
                parsedUrl,
                HttpHeaders.fromLines(this.getHeaders()));

        HttpResponse httpResponse = new HttpClient(this.isFollowRedirects())
                .performRequest(request);

        postResponse(httpResponse);
    }
}
