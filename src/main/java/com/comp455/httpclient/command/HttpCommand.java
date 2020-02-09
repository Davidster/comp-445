package com.comp455.httpclient.command;

import com.comp455.httpclient.client.HttpResponse;
import com.comp455.httpclient.logger.LogLevel;
import com.comp455.httpclient.logger.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class HttpCommand extends Command {
    private Map<String, String> headers;
    private boolean verbose;
    private boolean followRedirects;
    private String requestUrl;
    private String outputFilePath;

    public HttpCommand(CommandType commandType) {
        super(commandType);
        this.headers = new HashMap<>();
        this.verbose = false;
        this.followRedirects = false;
    }

    protected void preResponse() {
        if(this.outputFilePath != null) {
            Logger.logLevel = LogLevel.ERROR;
        }
    }

    protected void postResponse(HttpResponse response) throws IOException {
        if(this.outputFilePath != null) {
            Files.writeString(Paths.get(this.outputFilePath), response.getBody(), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
