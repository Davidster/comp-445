package com.comp445.httpc.argparser;

import com.comp445.common.Util;
import com.comp445.httpc.command.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgParser {

    public static final String VERBOSE_ARG_KEY = "v";
    public static final String FOLLOW_REDIRECTS_ARG_KEY = "L";
    public static final String OUTPUT_FILE_PATH_ARG_KEY = "o";
    public static final String HEADERS_ARG_KEY = "h";
    public static final String INLINE_DATA_ARG_KEY = "d";
    public static final String FILE_DATA_PATH_ARG_KEY = "f";

    private List<String> args;

    public ArgParser(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public Optional<Command> parse() throws ParseException {
        if(args.size() < 1) {
            return Optional.empty();
        }

        List<String> commandArgs = args.subList(1, args.size());
        String commandName = args.get(0).toLowerCase();
        Command parsedCommand;
        switch(commandName) {
            case "help":
                parsedCommand = buildHelpCommand(commandArgs);
                break;
            case "get":
                parsedCommand = buildGetCommand(commandArgs);
                break;
            case "post":
                parsedCommand = buildPostCommand(commandArgs);
                break;
            default:
                parsedCommand = new UnknownCommand(
                        Util.parseCliArgString(args.get(0)));
                break;
        }

        return Optional.of(parsedCommand);
    }

    private HelpCommand buildHelpCommand(List<String> commandArgs) {
        HelpCommand helpCommand = new HelpCommand();

        if(commandArgs.size() < 1) {
            return helpCommand;
        }

        CommandType commandToDescribe = CommandType.UNKNOWN;
        String subCommand = Util.parseCliArgString(commandArgs.get(0));
        switch(subCommand) {
            case "get":
                commandToDescribe = CommandType.HTTP_GET;
                break;
            case "post":
                commandToDescribe = CommandType.HTTP_POST;
                break;
        }
        helpCommand.setCommandToDescribe(commandToDescribe);
        return helpCommand;
    }

    private GetCommand buildGetCommand(List<String> commandArgs) throws ParseException {
        GetCommand getCommand = new GetCommand();
        parseCommonHttpOptions(getCommand, commandArgs);
        return getCommand;
    }

    private PostCommand buildPostCommand(List<String> commandArgs) throws ParseException {
        PostCommand postCommand = new PostCommand();
        Optional<CommandLine> optCmd = parseCommonHttpOptions(postCommand, commandArgs);

        optCmd.ifPresent(cmd -> {
            postCommand.setInlineData(
                    Util.parseCliArgString(cmd.getOptionValue(INLINE_DATA_ARG_KEY)));
            postCommand.setDataFilePath(
                    Util.parseCliArgString(cmd.getOptionValue(FILE_DATA_PATH_ARG_KEY)));
        });

        return postCommand;
    }

    private Optional<CommandLine> parseCommonHttpOptions(HttpCommand httpCommand, List<String> commandArgs) throws ParseException {
        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        String requestUrl = Util.parseCliArgString(
                commandArgs.remove(commandArgs.size() - 1));
        httpCommand.setRequestUrl(requestUrl);

        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        CommandOptionsParser commandOptionsParser = new CommandOptionsParser();
        CommandLine cmd = commandOptionsParser.parse(commandArgs);

        httpCommand.setVerbose(cmd.hasOption(VERBOSE_ARG_KEY));

        httpCommand.setFollowRedirects(cmd.hasOption(FOLLOW_REDIRECTS_ARG_KEY));

        httpCommand.setOutputFilePath(cmd.getOptionValue(OUTPUT_FILE_PATH_ARG_KEY));

        // parse header values and insert them into a map
        String[] headerArgs = cmd.getOptionValues(HEADERS_ARG_KEY);

        if(headerArgs != null && headerArgs.length > 0) {
            httpCommand.setHeaders(
                    Stream.of(headerArgs)
                            .map(Util::parseCliArgString)
                            .collect(Collectors.toList())
            );
        }

        return Optional.of(cmd);
    }
}
