package com.comp455.httpclient.command;

import com.comp455.httpclient.client.HttpClient;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URL;

@Getter
@Setter
public class PostCommand extends HttpCommand {

    private String inlineData;
    private String dataFile;

    public PostCommand() {
        super(CommandType.HTTP_POST);
    }

    @SneakyThrows
    @Override
    public void run() {
        // TODO

        // parse url
        URL parsedUrl = new URL(this.getRequestUrl());

        new HttpClient().performPostRequest(this.getHeaders(), parsedUrl, inlineData);
    }
}
