package com.comp445.httpfs.handlers;

import com.comp445.common.Utils;
import com.comp445.common.http.HttpHeaders;
import com.comp445.common.http.HttpResponse;
import com.comp445.common.http.HttpStatus;
import com.comp445.httpfs.templates.TemplateManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class FileUploadHandler implements Function<FileUploadRequest, HttpResponse> {
    @Override
    public HttpResponse apply(FileUploadRequest request) {
        HttpResponse catchAll = Utils.handleError(TemplateManager.TEMPLATE_500);
        try {
            Path filePath = request.getDestinationPath();
            if(Files.isDirectory(filePath)) {
                return handleBadRequest("Destination path is a directory");
            }
            if(request.content == null) {
                return handleBadRequest("Body is required");
            }

            boolean isNewFile = !Files.exists(filePath);
            Utils.writeFile(request.destinationPath, request.content);

            return new HttpResponse(new HttpStatus(isNewFile ? HttpStatus.STATUS_CREATED : HttpStatus.STATUS_OK), new HttpHeaders());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catchAll;
    }

    private HttpResponse handleBadRequest(String reason) {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_BAD_REQUEST),
                Utils.getHtmlPageCommonHeaders(),
                String.format(TemplateManager.TEMPLATE_400, reason));
    }
}
