package com.comp455.httpclient.argparser;

import lombok.SneakyThrows;
import org.apache.commons.cli.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgParser {
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
            case "post":
                RequestMethod requestMethod = RequestMethod.valueOf(commandName.toUpperCase());
                parsedCommand = buildHttpCommand(requestMethod, commandArgs);
                break;
            default:
                parsedCommand = buildUnknownCommand(argList.get(0));
                break;
        }

        return Optional.of(parsedCommand);
    }

    private HelpCommand buildHelpCommand(List<String> commandArgs) {
        HelpCommand helpCommand = new HelpCommand();

        if(commandArgs.size() < 1) {
            return helpCommand;
        }

        CommandType commandToDescribe = CommandType.valueOf(commandArgs.get(0).toUpperCase());
        helpCommand.setCommandToDescribe(Optional.of(commandToDescribe));
        return helpCommand;
    }

    private HttpCommand buildHttpCommand(RequestMethod requestMethod, List<String> commandArgs) {
        HttpCommand httpCommand = new HttpCommand(requestMethod);

        if(commandArgs.size() < 1) {
            return httpCommand;
        }

        String requestUrl = commandArgs.remove(commandArgs.size() - 1);
        httpCommand.setRequestUrl(Optional.of(requestUrl));

        if(commandArgs.size() < 1) {
            return httpCommand;
        }

        CommandOptionsParser commandOptionsParser = new CommandOptionsParser();
        CommandLine cmd = commandOptionsParser.parse(commandArgs);

        httpCommand.setVerbose(cmd.hasOption("v"));
        httpCommand.setInlineData(Optional.ofNullable(cmd.getOptionValue("d")));
        httpCommand.setFileData(Optional.ofNullable(cmd.getOptionValue("f")));

        // parse header values and insert them into a map
        String[] headerArgs = cmd.getOptionValues("h");
        if(headerArgs != null && headerArgs.length > 0) {
            httpCommand.setHeaders(
                Stream.of(headerArgs)
                        .map(headerArg -> headerArg.split(":"))
                        .collect(Collectors.toMap(
                            headerArgSplit -> headerArgSplit[0],
                            headerArgSplit -> headerArgSplit[1])));
        }

        return httpCommand;
    }

    private UnknownCommand buildUnknownCommand(String command) {
        return new UnknownCommand(command);
    }
}
