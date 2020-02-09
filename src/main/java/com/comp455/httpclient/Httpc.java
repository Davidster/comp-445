package com.comp455.httpclient;

import com.comp455.httpclient.argparser.ArgParser;
import com.comp455.httpclient.command.Command;
import com.comp455.httpclient.command.CommandType;
import com.comp455.httpclient.command.HelpCommand;
import com.comp455.httpclient.command.HttpCommand;
import com.comp455.httpclient.logger.LogLevel;
import com.comp455.httpclient.logger.Logger;

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
