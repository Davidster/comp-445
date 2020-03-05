package com.comp445.httpc.argparser;

import com.comp445.httpc.command.*;
import org.apache.commons.cli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgParser {

    private static char QUOTE = '\'';

    private String[] args;

    public ArgParser(String[] args) {
        this.args = args;
    }

    public Optional<Command> parse() {
        List<String> argList = new ArrayList<>();
        Collections.addAll(argList, args);

        if(argList.size() < 1) {
            return Optional.empty();
        }

        List<String> commandArgs = argList.subList(1, argList.size());
        String commandName = argList.get(0).toLowerCase();
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
                        parseStringValue(argList.get(0)));
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
        String subCommand = parseStringValue(commandArgs.get(0));
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

    private GetCommand buildGetCommand(List<String> commandArgs) {
        GetCommand getCommand = new GetCommand();
        parseCommonHttpOptions(getCommand, commandArgs);
        return getCommand;
    }

    private PostCommand buildPostCommand(List<String> commandArgs) {
        PostCommand postCommand = new PostCommand();
        Optional<CommandLine> optCmd = parseCommonHttpOptions(postCommand, commandArgs);

        optCmd.ifPresent(cmd -> {
            postCommand.setInlineData(
                    parseStringValue(cmd.getOptionValue("d")));
            postCommand.setDataFilePath(
                    parseStringValue(cmd.getOptionValue("f")));
        });

        return postCommand;
    }

    private Optional<CommandLine> parseCommonHttpOptions(HttpCommand httpCommand, List<String> commandArgs) {
        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        String requestUrl = parseStringValue(
                commandArgs.remove(commandArgs.size() - 1));
        httpCommand.setRequestUrl(requestUrl);

        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        CommandOptionsParser commandOptionsParser = new CommandOptionsParser();
        CommandLine cmd = commandOptionsParser.parse(commandArgs);

        httpCommand.setVerbose(cmd.hasOption("v"));

        httpCommand.setFollowRedirects(cmd.hasOption("L"));

        httpCommand.setOutputFilePath(cmd.getOptionValue("o"));

        // parse header values and insert them into a map
        String[] headerArgs = cmd.getOptionValues("h");
        if(headerArgs != null && headerArgs.length > 0) {
            httpCommand.setHeaders(
                    Stream.of(headerArgs)
                            .map(this::parseStringValue)
                            .map(headerArg -> headerArg.split(":", 2))
                            .collect(Collectors.toMap(
                                    headerArgSplit -> headerArgSplit[0].trim(),
                                    headerArgSplit -> headerArgSplit[1].trim())));
        }

        return Optional.of(cmd);
    }

    private String parseStringValue(String rawString) {
        if(rawString == null) {
            return null;
        }
        String result = rawString;
        if(result.charAt(0) == QUOTE &&
           result.charAt(rawString.length() - 1) == QUOTE) {
            result = result.substring(1, result.length() - 1);
        }
        return result.trim();
    }
}
