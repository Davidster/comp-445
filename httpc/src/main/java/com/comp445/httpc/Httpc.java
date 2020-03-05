package com.comp445.httpc;

import com.comp445.httpc.argparser.ArgParser;
import com.comp445.httpc.command.Command;
import com.comp445.httpc.command.CommandType;
import com.comp445.httpc.command.HelpCommand;
import com.comp445.httpc.command.HttpCommand;
import com.comp445.httpc.logger.LogLevel;
import com.comp445.httpc.logger.Logger;

public class Httpc {
    public static void main(String[] args) {
        // parse cli args
        Command command = new ArgParser(args).parse()
                .orElse(new HelpCommand());
        CommandType commandType = command.getCommandType();

        // set the log level
        switch(commandType) {
            case HTTP_GET:
            case HTTP_POST:
                Logger.logLevel = ((HttpCommand)command).isVerbose() ? LogLevel.VERBOSE : LogLevel.INFO;
        }

        // run the command
        command.run();
    }
}
