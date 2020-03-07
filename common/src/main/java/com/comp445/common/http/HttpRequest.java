package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
@Setter
public class HttpRequest {

    HttpMethod method;
    URL url;
    HttpHeaders headers;
    String body;

    public HttpRequest(HttpMethod method, URL url, HttpHeaders headers) {
        this.method = method;
        this.url = url;
        this.headers = headers;
    }

    public List<String> toStringList() {
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
        String requestBody = String.join("\n", this.toStringList()) + "\n";

        if(method == HttpMethod.POST) {
            return requestBody + "\r\n" + body;
        }
        return requestBody;
    }

}
