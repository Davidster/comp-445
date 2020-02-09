package com.comp455.httpclient.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpStatus {
    int code;
    String reason;
}
