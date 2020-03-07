package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpStatus {
    String httpVersion;
    int code;
    String reason;

    public String toString() {
        return String.format("%s %s %s", httpVersion, code, reason);
    }
}
