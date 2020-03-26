package com.comp445.common.http;

import com.comp445.common.Util;
import lombok.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HttpRequest {

    @NonNull
    private HttpMethod method;
    @NonNull
    private URL url;
    @NonNull
    private HttpHeaders headers;
    private byte[] body;

    public static HttpRequest fromInputStream(BufferedInputStream input) throws IOException {
        // parse method
        String methodLine = Util.readLine(input).trim();
        String[] methodLineSplit = methodLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(methodLineSplit[0]);
        String path = methodLineSplit.length > 1 ?
                URLDecoder.decode(methodLineSplit[1], StandardCharsets.UTF_8) : null;
        String httpVersion = methodLineSplit.length > 2 ?
                methodLineSplit[2] : null;

        HttpHeaders headers = HttpHeaders.fromInputStream(input);

        byte[] body = null;
        String contentLengthString = headers.get(HttpHeaders.CONTENT_LENGTH);
        if(contentLengthString != null) {
            int contentLength = Integer.parseInt(contentLengthString);
            body = contentLength > 0 ?
                    input.readNBytes(contentLength)
                    : new byte[0];
        }

        String host = headers.getOrDefault(HttpHeaders.HOST, "localhost");
        URL url = new URL(String.format("http://%s%s", host, path));

        return new HttpRequest(method, url, headers, body);
    }

    public List<String> toHeadersList() {
        if(method == HttpMethod.POST && body != null) {
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

    public byte[] toByteArray() throws IOException {
        String headersString = String.join("\n", this.toHeadersList()) + "\n\r\n";

        if(method == HttpMethod.POST && body != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headersString.getBytes());
            outputStream.write(body);
            return outputStream.toByteArray();
        }
        return headersString.getBytes();
    }

}
