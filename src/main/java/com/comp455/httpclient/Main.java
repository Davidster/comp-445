package com.comp455.httpclient;

import com.comp455.httpclient.argparser.ArgParser;
import com.comp455.httpclient.command.Command;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // parse cli args
        Optional<Command> command = new ArgParser(args).parse();

        if(command.isEmpty()) {
            // TODO: maybe change to HelpCommand.run() ?
            System.err.println("Error parsing args");
            System.exit(1);
        }

        Command c = command.get();
        switch (c.getCommandType()) {
            case HELP:
                c.run();
                break;
            case HTTP_GET:
                break;
            case HTTP_POST:
                break;
            case UNKNOWN:
                break;
        }
    }
}
