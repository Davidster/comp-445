package com.comp445.httpc.command;

import lombok.Getter;

@Getter
public abstract class Command implements Runnable {
    CommandType commandType;
    public Command(CommandType commandType) {
        this.commandType = commandType;
    }
}
