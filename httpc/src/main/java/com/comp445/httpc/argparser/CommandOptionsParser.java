package com.comp445.httpc.argparser;

import org.apache.commons.cli.*;

import java.util.List;

import static com.comp445.httpc.argparser.ArgParser.*;

public class CommandOptionsParser {
    private CommandLineParser commandLineParser;
    private Options options;

    public CommandOptionsParser() {
        this.commandLineParser = new DefaultParser();
        this.options = new Options();

        options.addOption(Option.builder(VERBOSE_ARG_KEY)
                .build());
        options.addOption(Option.builder(HEADERS_ARG_KEY).hasArgs()
                .build());
        options.addOption(Option.builder(INLINE_DATA_ARG_KEY).hasArg()
                .build());
        options.addOption(Option.builder(FILE_DATA_PATH_ARG_KEY).hasArg()
                .build());
        options.addOption(Option.builder(FOLLOW_REDIRECTS_ARG_KEY)
                .build());
        options.addOption(Option.builder(OUTPUT_FILE_PATH_ARG_KEY).hasArg()
                .build());
    }

    public CommandLine parse(List<String> args) throws ParseException {
        return commandLineParser.parse(options, args.toArray(new String[0]));
    }
}
