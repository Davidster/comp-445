package com.comp445.httpc.command;

import com.comp445.common.http.HttpResponse;
import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class HttpCommand extends Command {
    private List<String> headers;
    private boolean verbose;
    private boolean followRedirects;
    private String requestUrl;
    private String outputFilePath;

    public HttpCommand(CommandType commandType) {
        super(commandType);
        this.headers = new ArrayList<>();
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
