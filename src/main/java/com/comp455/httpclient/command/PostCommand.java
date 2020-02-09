package com.comp455.httpclient.command;

import com.comp455.httpclient.client.HttpClient;
import com.comp455.httpclient.client.HttpResponse;
import com.comp455.httpclient.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
            entityBody = FileUtils.readFileToString(new File(dataFilePath), StandardCharsets.UTF_8);
        }

        HttpResponse httpResponse = new HttpClient(this.isFollowRedirects())
                .performPostRequest(this.getHeaders(), parsedUrl, entityBody);

        postResponse(httpResponse);
    }
}
