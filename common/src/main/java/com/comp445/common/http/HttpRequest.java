package com.comp445.common.http;

import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HttpRequest {

    private static final int MAX_HEADER_COUNT = 1000;
    private static final int BODY_READ_CHUNK_SIZE = 4000;
    private static final int MAX_BODY_SIZE = 100000000; // 100 MB

    @NonNull
    HttpMethod method;
    @NonNull
    URL url;
    @NonNull
    HttpHeaders headers;
    String body;

    public static HttpRequest fromInputStream(BufferedReader input) throws IOException {
        // parse method
        String methodLine = input.readLine().trim();
        String[] methodLineSplit = methodLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(methodLineSplit[0]);
        String path = methodLineSplit.length > 1 ?
                methodLineSplit[1] : null;
        String httpVersion = methodLineSplit.length > 2 ?
                methodLineSplit[2] : null;

        // parse headers
        List<String> headerLines = new ArrayList<>();
        do {
            String headerLine = input.readLine().trim();
            if(Util.isEmptyLine(headerLine)) {
                break;
            }
            headerLines.add(headerLine);
        } while(headerLines.size() < MAX_HEADER_COUNT);


        HttpHeaders headers = HttpHeaders.fromLines(headerLines);

        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeaders.CONTENT_LENGTH, "0"));

        // parse body
        int totalBytesConsumed = 0;
        char[] bodyBytes = new char[contentLength];
        while(totalBytesConsumed < contentLength && totalBytesConsumed < MAX_BODY_SIZE) {
            int chunkSize = Math.min(BODY_READ_CHUNK_SIZE, contentLength - totalBytesConsumed);
            int bytesConsumed = input.read(bodyBytes, totalBytesConsumed, chunkSize);

            if(bytesConsumed == -1) {
                Logger.log("Warning: request input stream ended unexpectedly", LogLevel.INFO);
                break;
            }

            totalBytesConsumed += bytesConsumed;
        }

        String body = totalBytesConsumed > 0 ?
                new String(Arrays.copyOfRange(bodyBytes, 0, totalBytesConsumed))
                : null;

        String host = headers.getOrDefault(HttpHeaders.HOST, "localhost");
        URL url = new URL(String.format("http://%s%s", host, path));

        return new HttpRequest(method, url, headers, body);
    }

    public List<String> toHeadersList() {
        if(method == HttpMethod.POST) {
            headers.put("Content-Length", String.valueOf(body.length()));
        }

        String path = url.getFile().equals("") ? "/" : url.getFile();
        return Stream.concat(
                List.of(
                        String.format("%s %s HTTP/1.0", method.toString(), path),
                        String.format("Host: %s", url.getHost())
                ).stream(),
                headers.toStringList().stream()
        ).collect(Collectors.toList());
    }

    @SneakyThrows
    public String toString() {
        String requestString = String.join("\n", this.toHeadersList()) + "\n";

        if(method == HttpMethod.POST) {
            return requestString + "\r\n" + body;
        }
        return requestString;
    }

}
