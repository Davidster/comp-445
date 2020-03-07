package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
    String body;


    public static HttpResponse fromLines(List<String> lines) {
        // parse status
        String statusLine = lines.get(0).trim();
        String[] statusLineSplit = statusLine.split(" ");
        String httpVersion = statusLineSplit[0];
        int statusCode = statusLineSplit.length > 1 ?
                Integer.parseInt(statusLineSplit[1])
                : -1;
        String statusReason = statusLineSplit.length > 2 ?
                statusLine.substring(statusLine.indexOf(statusLineSplit[1]) + 4)
                : null;

        // parse headers/body
        int bodySeparatorIndex = lines.stream()
                .map(Util::isEmptyLine)
                .collect(Collectors.toList())
                .indexOf(true);
        if(bodySeparatorIndex == -1) {
            bodySeparatorIndex = lines.size() - 1;
        }
        String body = (bodySeparatorIndex < lines.size() - 1) ?
                String.join("\n",
                        lines.subList(bodySeparatorIndex + 1, lines.size()))
                : "";

        HttpHeaders headers = HttpHeaders.fromLines(lines.subList(1, bodySeparatorIndex));

        return new HttpResponse(new HttpStatus(httpVersion, statusCode, statusReason), headers, body);
    }

    public List<String> toHeadersList() {
        headers.put("Content-Length", String.valueOf(
                body != null ? body.length() : 0
        ));
        return Stream.concat(
                    Collections.singletonList(status.toString()).stream(),
                    headers.toStringList().stream()
                ).collect(Collectors.toList());
    }

    public String toString() {
        String responseString = String.join("\n", toHeadersList()) + "\n\r\n";
        if(body != null) {
            return responseString + "\r\n" + body;
        }
        return responseString;
    }
}
