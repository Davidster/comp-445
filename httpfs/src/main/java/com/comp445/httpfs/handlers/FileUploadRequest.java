package com.comp445.httpfs.handlers;

import java.nio.file.Path;

public class FileUploadRequest {
    Path destinationPath;
    byte[] content;
}
