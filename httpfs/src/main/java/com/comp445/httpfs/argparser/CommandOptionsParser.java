package com.comp445.httpfs.argparser;

import org.apache.commons.cli.*;

import java.util.List;

public class CommandOptionsParser {
    private CommandLineParser commandLineParser;
    private Options options;

    public CommandOptionsParser() {
        this.commandLineParser = new DefaultParser();
        this.options = new Options();

        options.addOption(Option.builder("v")
                .build());
        options.addOption(Option.builder("p").hasArg()
                .build());
        options.addOption(Option.builder("d").hasArg()
                .build());
    }

    public CommandLine parse(List<String> args) throws ParseException {
        return commandLineParser.parse(options, args.toArray(new String[0]));
    }
}
