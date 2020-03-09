package com.comp445.httpfs.handlers;

import com.comp445.common.http.HttpHeaders;
import com.comp445.common.http.HttpResponse;
import com.comp445.common.http.HttpStatus;
import com.comp445.common.http.Util;
import com.comp445.httpfs.templates.TemplateManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.comp445.common.http.Util.HTML_PAGE_COMMON_HEADERS;

public class FileRetrievalHandler implements Function<Path, HttpResponse> {

    @Override
    public HttpResponse apply(Path path) {
        System.out.println();
        try {
            if(!Files.exists(path)) {
                return handleNonExistent();
            }
            if(Files.isDirectory(path)) {
                return handleDirectory(path);
            }
            if(Files.isRegularFile(path)) {
                return handleRegularFile(path);
            }
        } catch (Exception e) {
            return Util.handleError(TemplateManager.TEMPLATE_500);
        }
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_INTERNAL_SERVER_ERROR),
                new HttpHeaders(),
                TemplateManager.TEMPLATE_500);
    }

    private HttpResponse handleRegularFile(Path path) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_OK),
                headers,
                Files.readAllBytes(path));
    }

    private HttpResponse handleDirectory(Path path) throws IOException {
        List<Path> files = Util.listFiles(path);

        String pageTitle = path.getFileName() + "/";
        String pageBody;
        if(files.size() == 0) {
            pageBody = "Directory contains no files";
        } else {
            String filesListItems = files.stream().map(filePath -> {
                String fileNameString = filePath.getFileName().toString();
                if(Files.isDirectory(filePath)) {
                    fileNameString += "/";
                }
                return String.format("    <li><a href=\"%s\">%s</a></li>", fileNameString, fileNameString);
            }).collect(Collectors.joining("\n"));
            pageBody = String.format("\n  <ul>\n%s\n  </ul>\n", filesListItems);
        }
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_OK),
                HTML_PAGE_COMMON_HEADERS,
                String.format(TemplateManager.TEMPLATE_DIRECTORY_LISTING, pageTitle, pageBody));
    }

    private HttpResponse handleNonExistent() throws IOException {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_NOT_FOUND),
                HTML_PAGE_COMMON_HEADERS,
                TemplateManager.TEMPLATE_404);
    }
}
