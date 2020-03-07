package com.comp445.common.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class HttpStatus {

    public static final String VERSION_1_0 = "HTTP/1.0";

    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    public static final Map<Integer, String> STATUS_REASON_MAP;
    static {
        Map<Integer, String> srm = new HashMap<>();
        srm.put(STATUS_OK, "OK");
        srm.put(STATUS_CREATED, "Created");
        srm.put(STATUS_BAD_REQUEST, "Bad Request");
        srm.put(STATUS_FORBIDDEN, "Forbidden");
        srm.put(STATUS_NOT_FOUND, "Not Found");
        srm.put(STATUS_INTERNAL_SERVER_ERROR, "Internal Server Error");
        STATUS_REASON_MAP = Collections.unmodifiableMap(srm);
    }

    String httpVersion;
    int code;
    String reason;

    public HttpStatus(int code) {
        this.httpVersion = VERSION_1_0;
        this.code = code;
        this.reason = STATUS_REASON_MAP.get(code);
    }

    public String toString() {
        return String.format("%s %s %s", httpVersion, code, reason);
    }
}