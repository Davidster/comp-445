package com.comp445.httpc.command;

import com.comp445.httpc.client.HttpClient;
import com.comp445.httpc.client.HttpResponse;
import com.comp445.httpc.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
public class PostCommand extends HttpCommand {

    private String inlineData;
    private String dataFilePath;

    public PostCommand() {
        super(CommandType.HTTP_POST);
    }

    @SneakyThrows
    @Override
    public void run() {
        preResponse();

        // parse url
        URL parsedUrl = new URL(this.getRequestUrl());

        String entityBody = "";
        if(inlineData != null && dataFilePath != null) {
            Logger.logError("Warning: both -f and -d options supplied. Ignoring -f option.");
        } else if(inlineData != null) {
            entityBody = inlineData;
        } else if(dataFilePath != null) {
            entityBody = Files.readString(Paths.get(dataFilePath), StandardCharsets.UTF_8);
        }

        HttpResponse httpResponse = new HttpClient(this.isFollowRedirects())
                .performPostRequest(this.getHeaders(), parsedUrl, entityBody);

        postResponse(httpResponse);
    }
}
