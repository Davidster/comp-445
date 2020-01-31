package com.comp455.httpclient.command;

public class UnknownCommand extends Command {
    private String unknownCommand;

    public UnknownCommand(String unknownCommand) {
        super(CommandType.UNKNOWN);
        this.unknownCommand = unknownCommand;
    }

    @Override
    public void run() {
        // TODO
    }
}
