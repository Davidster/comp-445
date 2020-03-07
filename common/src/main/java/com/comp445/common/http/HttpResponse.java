package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class HttpResponse {

    @NonNull
    HttpStatus status;
    @NonNull
    HttpHeaders headers;
    byte[] body;

    public static HttpResponse fromInputStream(InputStream input) throws IOException {

        BufferedInputStream bytesReader = new BufferedInputStream(input);

        // parse status
        String statusLine =  Util.readLine(bytesReader).trim();
        String[] statusLineSplit = statusLine.split(" ");
        String httpVersion = statusLineSplit[0];
        int statusCode = statusLineSplit.length > 1 ?
                Integer.parseInt(statusLineSplit[1])
                : -1;
        String statusReason = statusLineSplit.length > 2 ?
                statusLine.substring(statusLine.indexOf(statusLineSplit[1]) + 4)
                : null;

        HttpHeaders headers = HttpHeaders.fromInputStream(bytesReader);

        byte[] body = bytesReader.readAllBytes();

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

    public byte[] toByteArray() {
        String headersString = String.join("\n", this.toHeadersList()) + "\n\r\n";

        if(body != null) {
            headersString = headersString + "\r\n";
            return ArrayUtils.addAll(headersString.getBytes(), body);
        }
        return headersString.getBytes();
    }
}
