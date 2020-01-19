package com.comp455.httpclient.argparser;

public abstract class Command {
    CommandType commandType;
    public Command(CommandType commandType) {
        this.commandType = commandType;
    }
}
