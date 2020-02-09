package com.comp455.httpclient.argparser;

import lombok.SneakyThrows;
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
        options.addOption(Option.builder("h").hasArgs()
                .build());
        options.addOption(Option.builder("d").hasArg()
                .build());
        options.addOption(Option.builder("f").hasArg()
                .build());
        options.addOption(Option.builder("L")
                .build());
        options.addOption(Option.builder("o").hasArg()
                .build());
    }

    @SneakyThrows
    public CommandLine parse(List<String> args) {
        return commandLineParser.parse(options, args.toArray(new String[0]));
    }
}
