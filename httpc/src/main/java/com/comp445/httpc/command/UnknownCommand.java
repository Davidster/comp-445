package com.comp445.httpc.command;

import com.comp445.common.logger.Logger;

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
