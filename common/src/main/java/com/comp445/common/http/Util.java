package com.comp445.common.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Util {
    public static final Pattern emptyLine = Pattern.compile("^\\s*$");

    private static char QUOTE = '\'';

    public static final int MAX_HEADER_COUNT = 1000;
    public static final int BODY_READ_CHUNK_SIZE = 4000;
    public static final int MAX_BODY_SIZE = 100000000; // 100 MB

    public static final HttpHeaders HTML_PAGE_COMMON_HEADERS = new HttpHeaders();
    static {
        HTML_PAGE_COMMON_HEADERS.put(HttpHeaders.CONTENT_TYPE, "text/html");
    }

    public static boolean isEmptyLine(String s) {
        return emptyLine.matcher(s).matches();
    }

    public static String readLine(BufferedInputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        while(true) {
            int data = input.read();
            if(data == -1) {
                continue;
            }
            char c = (char) data;
            if(c == '\n') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String parseCliArgString(String rawString) {
        if(rawString == null) {
            return null;
        }
        String result = rawString;
        if(result.charAt(0) == QUOTE &&
                result.charAt(rawString.length() - 1) == QUOTE) {
            result = result.substring(1, result.length() - 1);
        }
        return result.trim();
    }

    public static void writeFile(Path filePath, String content) throws IOException {
        writeFile(filePath, content.getBytes());
    }

    public static void writeFile(Path filePath, byte[] content) throws IOException {
        Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static List<Path> listFiles(Path directoryPath) throws IOException {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath);
        List<Path> files = StreamSupport.stream(directoryStream.spliterator(), false)
                .collect(Collectors.toList());
        directoryStream.close();
        return files;
    }

    public static HttpResponse handleError(String template) {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_INTERNAL_SERVER_ERROR),
                HTML_PAGE_COMMON_HEADERS,
                template);
    }
}

