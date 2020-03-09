package com.comp445.httpfs.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
public class FileUploadRequest {
    Path destinationPath;
    byte[] content;
}
