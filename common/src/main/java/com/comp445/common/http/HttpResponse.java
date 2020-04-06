package com.comp445.common.http;

import com.comp445.common.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class HttpResponse {

    @NonNull
    private HttpStatus status;
    @NonNull
    private HttpHeaders headers;
    private byte[] body;

    public HttpResponse(HttpStatus status, HttpHeaders headers, String body) {
        this.status = status;
        this.headers = headers;
        this.body = body.getBytes();
    }

    public static HttpResponse fromInputStream(BufferedInputStream input) throws IOException {
        // parse status
        String statusLine =  Utils.readLine(input).trim();
        String[] statusLineSplit = statusLine.split(" ");
        String httpVersion = statusLineSplit[0];
        int statusCode = statusLineSplit.length > 1 ?
                Integer.parseInt(statusLineSplit[1])
                : -1;
        String statusReason = statusLineSplit.length > 2 ?
                statusLine.substring(statusLine.indexOf(statusLineSplit[1]) + 4)
                : null;


        HttpHeaders headers = HttpHeaders.fromInputStream(input);

        byte[] body = null;
        String contentLengthString = headers.get(HttpHeaders.CONTENT_LENGTH);
        if(contentLengthString != null) {
            int contentLength = Integer.parseInt(contentLengthString);
            body = contentLength > 0 ?
                    input.readNBytes(contentLength)
                    : new byte[0];
        }

        return new HttpResponse(new HttpStatus(httpVersion, statusCode, statusReason), headers, body);
    }

    public List<String> toHeadersList() {
        headers.put("Content-Length", String.valueOf(
                body != null ? body.length : 0
        ));
        return Stream.concat(
                    Collections.singletonList(status.toString()).stream(),
                    headers.toStringList().stream()
                ).collect(Collectors.toList());
    }

    public byte[] toByteArray() throws IOException {
        String headersString = String.join("\n", this.toHeadersList()) + "\n\r\n";

        if(body != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headersString.getBytes());
            outputStream.write(body);
            return outputStream.toByteArray();
        }
        return headersString.getBytes();
    }
}
