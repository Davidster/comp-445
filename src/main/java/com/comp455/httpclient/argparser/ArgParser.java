package com.comp455.httpclient.argparser;

import com.comp455.httpclient.command.*;
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
                parsedCommand = buildGetCommand(commandArgs);
                break;
            case "post":
                parsedCommand = buildPostCommand(commandArgs);
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

        CommandType commandToDescribe = CommandType.UNKNOWN;
        switch(commandArgs.get(0)) {
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
            postCommand.setInlineData(cmd.getOptionValue("d"));
            postCommand.setDataFile(cmd.getOptionValue("f"));
        });

        return postCommand;
    }

    private UnknownCommand buildUnknownCommand(String command) {
        return new UnknownCommand(command);
    }

    private Optional<CommandLine> parseCommonHttpOptions(HttpCommand httpCommand, List<String> commandArgs) {
        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        String requestUrl = commandArgs.remove(commandArgs.size() - 1);
        httpCommand.setRequestUrl(requestUrl);

        if(commandArgs.size() < 1) {
            return Optional.empty();
        }

        CommandOptionsParser commandOptionsParser = new CommandOptionsParser();
        CommandLine cmd = commandOptionsParser.parse(commandArgs);

        httpCommand.setVerbose(cmd.hasOption("v"));

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

        return Optional.of(cmd);
    }
}
