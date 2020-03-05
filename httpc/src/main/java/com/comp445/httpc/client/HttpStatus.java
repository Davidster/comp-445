package com.comp445.httpc.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpStatus {
    int code;
    String reason;
}
