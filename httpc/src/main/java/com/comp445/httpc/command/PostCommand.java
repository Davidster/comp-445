package com.comp445.httpc.command;

import com.comp445.common.http.*;
import com.comp445.common.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URL;
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

        URL parsedUrl = new URL(this.getRequestUrl());

        byte[] entityBody = null;
        if(inlineData != null && dataFilePath != null) {
            Logger.logError("Warning: both -f and -d options supplied. Ignoring -f option.");
        } else if(inlineData != null) {
            entityBody = inlineData.getBytes();
        } else if(dataFilePath != null) {
            entityBody = Files.readAllBytes(Paths.get(dataFilePath));
        }

        HttpRequest request = new HttpRequest(
                HttpMethod.POST,
                parsedUrl,
                HttpHeaders.fromLines(this.getHeaders()),
                entityBody);

        HttpResponse httpResponse = new TCPHttpClient(this.isFollowRedirects())
                .performRequest(request);

        postResponse(httpResponse);
    }
}
