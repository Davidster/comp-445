package com.comp455.httpclient.command;

import com.comp455.httpclient.logger.Logger;

public class UnknownCommand extends Command {
    private String unknownCommand;

    public UnknownCommand(String unknownCommand) {
        super(CommandType.UNKNOWN);
        this.unknownCommand = unknownCommand;
    }

    @Override
    public void run() {
        Logger.logError(String.format("Unknown command: %s", this.unknownCommand));
    }
}
