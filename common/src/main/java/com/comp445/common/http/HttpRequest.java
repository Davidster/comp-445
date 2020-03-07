package com.comp445.common.http;

import lombok.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HttpRequest {

    @NonNull
    HttpMethod method;
    @NonNull
    URL url;
    @NonNull
    HttpHeaders headers;
    byte[] body;

    public static HttpRequest fromInputStream(InputStream input) throws IOException {

        BufferedInputStream bytesReader = new BufferedInputStream(input);

        // parse method
        String methodLine = Util.readLine(bytesReader).trim();
        String[] methodLineSplit = methodLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(methodLineSplit[0]);
        String path = methodLineSplit.length > 1 ?
                methodLineSplit[1] : null;
        String httpVersion = methodLineSplit.length > 2 ?
                methodLineSplit[2] : null;

        HttpHeaders headers = HttpHeaders.fromInputStream(bytesReader);

        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeaders.CONTENT_LENGTH, "0"));
        byte[] body = contentLength > 0 ?
                bytesReader.readNBytes(contentLength)
                : null;

        String host = headers.getOrDefault(HttpHeaders.HOST, "localhost");
        URL url = new URL(String.format("http://%s%s", host, path));

        return new HttpRequest(method, url, headers, body);
    }

    public List<String> toHeadersList() {
        if(method == HttpMethod.POST) {
            headers.put("Content-Length", String.valueOf(body.length));
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
    public byte[] toByteArray() {
        String headersString = String.join("\n", this.toHeadersList()) + "\n\r\n";

        if(method == HttpMethod.POST) {
            return ArrayUtils.addAll(headersString.getBytes(), body);
        }
        return headersString.getBytes();
    }

}
