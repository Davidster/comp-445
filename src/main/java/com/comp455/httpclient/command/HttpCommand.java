package com.comp455.httpclient.command;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class HttpCommand extends Command {
    private Map<String, String> headers;
    private boolean verbose;
    private String requestUrl;

    public HttpCommand(CommandType commandType) {
        super(commandType);
        this.headers = new HashMap<>();
        this.verbose = false;
    }
}
