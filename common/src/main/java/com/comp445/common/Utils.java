package com.comp445.common;

import com.comp445.common.http.HttpHeaders;
import com.comp445.common.http.HttpResponse;
import com.comp445.common.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Utils {
    public static final Pattern emptyLine = Pattern.compile("^\\s*$");

    private static char QUOTE = '\'';

    public static final int MAX_HEADER_COUNT = 1000;
    public static final int BODY_READ_CHUNK_SIZE = 4000;
    public static final int MAX_BODY_SIZE = 100000000; // 100 MB

    public static final int ARQ_ROUTER_PORT = 3000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    public static final int UDP_MAX_PACKET_LENGTH = Short.MAX_VALUE * 2 + 1;

    public static final int SR_MAX_PACKET_LENGTH = 1024;
    public static final int SR_SERVER_CONNECTION_TIMEOUT = 3000;
    public static final float SR_CLOCK_GRANULARITY = 10f;

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

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

    public static HttpHeaders getHtmlPageCommonHeaders() {
        HttpHeaders htmlPageCommonHeaders = new HttpHeaders();
        htmlPageCommonHeaders.put(HttpHeaders.CONTENT_TYPE, "text/html");
        return htmlPageCommonHeaders;
    }

    public static HttpResponse handleError(String template) {
        return new HttpResponse(
                new HttpStatus(HttpStatus.STATUS_INTERNAL_SERVER_ERROR),
                getHtmlPageCommonHeaders(),
                template);
    }

    public static void doAssert(boolean b) throws Exception {
        if(!b) {
            throw new Exception();
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep((int)SR_CLOCK_GRANULARITY);
        } catch (InterruptedException ignored) {}
    }

    public static Future<Void> asyncTimer(int timeout) {
        return EXECUTOR.submit(() -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) {}
            return null;
        });
    }
}

