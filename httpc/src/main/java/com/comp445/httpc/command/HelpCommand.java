package com.comp445.httpc.command;

import com.comp445.httpc.logger.Logger;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelpCommand extends Command {

    private static final String NEW_LINE = "\n";

    private static final String BASE_HELP = String.join(NEW_LINE,
            "httpc is a curl-like application but supports HTTP protocol only.",
            "Usage:",
            "  httpc command [arguments]",
            "The commands are:",
            "  get executes a HTTP GET request and prints the response.",
            "  post executes a HTTP POST request and prints the response.",
            "  help prints this screen.",
            "",
            "Use 'httpc help [command]' for more information about a command");

    private static final String GET_HELP = String.join(NEW_LINE,
            "usage: httpc get [-v] [-h key:value] URL",
            "",
            "Get executes a HTTP GET request for a given URL.",
            "",
            "  -v            Prints the detail of the response such as protocol, status, and headers.",
            "  -L            Follows 3xx redirects automatically by inspecting the 'Location' header",
            "  -o file       Writes response body to a file instead of the console",
            "  -h key:value  Associates headers to HTTP Request with the format 'key:value'.");

    private static final String POST_HELP = String.join(NEW_LINE,
            "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL",
            "",
            "Post executes a HTTP POST request for a given URL with inline data or from file.",
            "",
            "Get executes a HTTP GET request for a given URL.",
            "",
            "  -v            Prints the detail of the response such as protocol, status, and headers.",
            "  -L            Follows 3xx redirects automatically by inspecting the 'Location' header",
            "  -o file       Writes response body to a file instead of the console",
            "  -h key:value  Associates headers to HTTP Request with the format 'key:value'.",
            "  -d string     Associates an inline data to the body HTTP POST request.",
            "  -f file       Associates the content of a file to the body HTTP POST request.",
            "",
            "Either [-d] or [-f] can be used but not both.");

    private CommandType commandToDescribe;

    public HelpCommand() {
        super(CommandType.HELP);
    }

    @Override
    public void run() {
        if(commandToDescribe == null) {
            Logger.log(BASE_HELP);
            return;
        }

        switch (commandToDescribe) {
            case HTTP_GET:
                Logger.log(GET_HELP);
                break;
            case HTTP_POST:
                Logger.log(POST_HELP);
                break;
        }
    }
}