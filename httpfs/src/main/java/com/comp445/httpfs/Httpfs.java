package com.comp445.httpfs;

import com.comp445.common.http.*;
import com.comp445.common.logger.Logger;
import com.comp445.httpfs.argparser.ArgParser;
import com.comp445.httpfs.argparser.HttpfsOptions;
import com.comp445.httpfs.handlers.FileRetrievalHandler;
import com.comp445.httpfs.templates.TemplateManager;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.comp445.common.http.Util.HTML_PAGE_COMMON_HEADERS;

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

//        FileManager fileManager = new FileManager();

//        String pomContents = new String(fileManager.readFile(Paths.get("pom.xml")));
//        List<Path> files = fileManager.listFiles(Paths.get(""));
//        fileManager.writeFile(Paths.get("pomCopy.xml"), pomContents + "heythere");

        if(args.length > 0 && args[0].equals("help")) {
            Logger.log(BASE_HELP);
            return;
        }

        HttpfsOptions httpfsOptions = new ArgParser(args).parse();

        HttpServer server = new HttpServer(8081, request -> {
            Logger.log("Received a request:");
            Logger.log(String.format("  Method: %s", request.getMethod()));
            Logger.log(String.format("  Path: %s", request.getUrl().getPath()));
            Logger.log("  Headers:");
            Logger.log("    " + String.join("\n    ", request.getHeaders().toStringList()));
            if(request.getBody() != null) {
                Logger.log("  Body:\n*********START BODY*********");
                String body = new String(request.getBody());
                Logger.log(body);
                Logger.log("*********END BODY***********\n");
            }

            HttpResponse response = Util.handleError(TemplateManager.TEMPLATE_500);
            if(request.getMethod().equals(HttpMethod.GET)) {
                Path requestPath = Paths.get(request.getUrl().getPath().substring(1));
                Path workingDir = httpfsOptions.getWorkingDirectory();
                Path finalPath = workingDir.resolve(requestPath).normalize();
                if(!finalPath.startsWith(workingDir)) {
                    response = handleUnauthorized();
                } else {
                    response = new FileRetrievalHandler().apply(finalPath);
                }
            }

            return response;
        });
        server.start();
    }

//    private boolean isIllegalPath() {
//
//    }

    private static HttpResponse handleUnauthorized() {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_FORBIDDEN),
                HTML_PAGE_COMMON_HEADERS,
                TemplateManager.TEMPLATE_403);
    }
}
