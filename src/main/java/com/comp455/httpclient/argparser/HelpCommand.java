package com.comp455.httpclient.argparser;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class HelpCommand extends Command {
    private Optional<CommandType> commandToDescribe;

    public HelpCommand() {
        super(CommandType.HELP);
        commandToDescribe = Optional.empty();
    }
}