package com.comp445.httpfs.argparser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgParser {

    public static final String VERBOSE_ARG_KEY = "v";
    public static final String PORT_ARG_KEY = "p";
    public static final String WORKING_DIR_ARG_KEY = "d";

    private static final HttpfsOptions DEFAULT_OPTIONS = new HttpfsOptions(
            false, 8080, Paths.get("")
    );

    private List<String> args;

    public ArgParser(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public HttpfsOptions parse() throws ParseException {
        HttpfsOptions options = DEFAULT_OPTIONS;

        CommandOptionsParser commandOptionsParser = new CommandOptionsParser();
        CommandLine cmd = commandOptionsParser.parse(args);

        options.setVerbose(cmd.hasOption(VERBOSE_ARG_KEY));

        if(cmd.hasOption(PORT_ARG_KEY)) {
            options.setPort(Integer.parseInt(cmd.getOptionValue(PORT_ARG_KEY)));
        }

        if(cmd.hasOption(WORKING_DIR_ARG_KEY)) {
            options.setWorkingDirectory(Paths.get(
                    cmd.getOptionValue(WORKING_DIR_ARG_KEY).replaceFirst("^~", System.getProperty("user.home"))
            ));
        }

        return options;
    }
}
