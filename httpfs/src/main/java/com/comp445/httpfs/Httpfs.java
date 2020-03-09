package com.comp445.httpfs;

import com.comp445.common.http.HttpResponse;
import com.comp445.common.http.HttpServer;
import com.comp445.common.http.HttpStatus;
import com.comp445.common.http.Util;
import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import com.comp445.httpfs.argparser.ArgParser;
import com.comp445.httpfs.argparser.HttpfsOptions;
import com.comp445.httpfs.handlers.FileRetrievalHandler;
import com.comp445.httpfs.handlers.FileUploadHandler;
import com.comp445.httpfs.handlers.FileUploadRequest;
import com.comp445.httpfs.templates.TemplateManager;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Httpfs {

    private static final String NEW_LINE = "\n";
    private static final String BASE_HELP = String.join(NEW_LINE,
            "httpfs is a simple file server.",
            "usage: httpfs [v] [-p PORT] [-d] PATH-TO-DIR",
            "",
            "  -v  Prints debugging messages.",
            "  -p  Specifies the port number that the server will listen and serve at.",
            "      Default is 8080",
            "  -v  Specifies the directory that the server will use to read/write requested files.",
            "      Default is the current directory when launching the application.");

    public static void main(String[] args) throws ParseException, IOException {

        TemplateManager.init();

        if(args.length > 0 && args[0].equals("help")) {
            Logger.log(BASE_HELP);
            return;
        }

        HttpfsOptions httpfsOptions = new ArgParser(args).parse();

        Logger.logLevel = httpfsOptions.isVerbose() ? LogLevel.VERBOSE : LogLevel.INFO;

        HttpServer server = new HttpServer(httpfsOptions.getPort(), request -> {
            Logger.log("Received a request:", LogLevel.VERBOSE);
            Logger.log(String.format("  Method: %s", request.getMethod()), LogLevel.VERBOSE);
            Logger.log(String.format("  Path: %s", request.getUrl().getPath()), LogLevel.VERBOSE);
            Logger.log("  Headers:", LogLevel.VERBOSE);
            Logger.log("    " + String.join("\n    ", request.getHeaders().toStringList()), LogLevel.VERBOSE);
            if(request.getBody() != null) {
                Logger.log("  Body:\n*********START BODY*********", LogLevel.VERBOSE);
                String body = new String(request.getBody());
                Logger.log(body, LogLevel.VERBOSE);
                Logger.log("*********END BODY***********\n", LogLevel.VERBOSE);
            }

            HttpResponse response = Util.handleError(TemplateManager.TEMPLATE_500);

            Path requestPath = Paths.get(request.getUrl().getPath().substring(1));
            Path workingDir = httpfsOptions.getWorkingDirectory();
            Path finalPath = workingDir.resolve(requestPath).normalize();
            if(!finalPath.startsWith(workingDir)) {
                response = handleUnauthorized();
            } else {
                switch (request.getMethod()) {
                    case GET:
                        response = new FileRetrievalHandler().apply(finalPath);
                        break;
                    case POST:
                        response = new FileUploadHandler().apply(new FileUploadRequest(finalPath, request.getBody()));
                        break;
                }
            }

            response.getHeaders().putAll(request.getHeaders());

            return response;
        });
        server.start();
    }

    private static HttpResponse handleUnauthorized() {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_FORBIDDEN),
                Util.getHtmlPageCommonHeaders(),
                TemplateManager.TEMPLATE_403);
    }
}
