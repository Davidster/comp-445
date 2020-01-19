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
                .desc("Prints the detail of the response such as protocol, status, and headers.")
                .build());
        options.addOption(Option.builder("h").hasArgs()
                .desc("Associates headers to HTTP Request with the format 'key:value'.")
                .build());
        options.addOption(Option.builder("d").hasArg()
                .desc("Associates an inline data to the body HTTP POST request.")
                .build());
        options.addOption(Option.builder("f").hasArg()
                .desc("Associates the content of a file to the body HTTP POST request.")
                .build());
    }

    @SneakyThrows
    public CommandLine parse(List<String> args) {
        return commandLineParser.parse(options, args.toArray(new String[0]));
    }
}
