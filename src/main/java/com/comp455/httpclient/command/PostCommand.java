package com.comp455.httpclient.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCommand extends HttpCommand {

    private String inlineData;
    private String dataFile;

    public PostCommand() {
        super(CommandType.HTTP_POST);
    }

    @Override
    public void run() {
        // TODO
    }
}
