package com.comp455.httpclient.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpResponse {
    String headers;
    String body;
}
