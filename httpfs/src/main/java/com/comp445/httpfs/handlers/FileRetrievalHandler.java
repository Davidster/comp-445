package com.comp445.httpfs.handlers;

import com.comp445.common.Util;
import com.comp445.common.http.HttpHeaders;
import com.comp445.common.http.HttpResponse;
import com.comp445.common.http.HttpStatus;
import com.comp445.httpfs.templates.TemplateManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileRetrievalHandler implements Function<Path, HttpResponse> {

    @Override
    public HttpResponse apply(Path path) {
        HttpResponse catchAll = Util.handleError(TemplateManager.TEMPLATE_500);
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
            throw new Exception(String.format("File (%s) exists but is neither a file nor directory", path.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catchAll;
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
                Util.getHtmlPageCommonHeaders(),
                String.format(TemplateManager.TEMPLATE_DIRECTORY_LISTING, pageTitle, pageBody));
    }

    private HttpResponse handleNonExistent() {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_NOT_FOUND),
                Util.getHtmlPageCommonHeaders(),
                TemplateManager.TEMPLATE_404);
    }
}
