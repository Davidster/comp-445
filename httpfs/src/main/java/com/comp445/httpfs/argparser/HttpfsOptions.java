package com.comp445.httpfs.argparser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
@Setter
public class HttpfsOptions {
    private boolean verbose;
    private int port;
    private Path workingDirectory;
}
