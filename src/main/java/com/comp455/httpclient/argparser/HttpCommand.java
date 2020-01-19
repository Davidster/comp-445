package com.comp455.httpclient.argparser;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class HttpCommand extends Command {
    private RequestMethod requestMethod;
    private Map<String, String> headers;
    private boolean verbose;
    private Optional<String> inlineData;
    private Optional<String> fileData;
    private Optional<String> requestUrl;

    public HttpCommand(RequestMethod requestMethod) {
        super(CommandType.HTTP);
        this.requestMethod = requestMethod;
        this.headers = new HashMap<>();
        this.verbose = false;
        this.inlineData = Optional.empty();
        this.fileData = Optional.empty();
        this.requestUrl = Optional.empty();
    }
}
