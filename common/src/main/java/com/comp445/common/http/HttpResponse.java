package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class HttpResponse {
    HttpStatus status;
    Map<String, String> headers;
    String body;
}
