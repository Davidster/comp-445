package com.comp445.common;

import com.comp445.common.http.*;
import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class TestRunner {

    private static final String HTTPBIN_BASE_URL = "http://httpbin.org";

    private static final HttpHeaders emptyHeaders = HttpHeaders.fromMap(new HashMap<>());

    @SneakyThrows
    public static void main(String[] args) {
        Logger.logLevel = LogLevel.VERBOSE;

        HttpClient httpClient = new HttpClient(true);

        testGet(httpClient);
        testGetNoBody(httpClient);
        testRedirectedGet(httpClient);
        testPost(httpClient);
    }

    @SneakyThrows
    private static void testGet(HttpClient httpClient) {
        String key = "hello";
        String value = "world";
        URL url = new URL(HTTPBIN_BASE_URL + String.format("/get?%s=%s", key, value));

        HttpRequest request = new HttpRequest(HttpMethod.GET, url, emptyHeaders);

        HttpResponse httpResponse = httpClient.performRequest(request);

        int expectedStatusCode = 200;
        String expectedStatusReason = "OK";
        Map.Entry<String, String> expectedHeader = new AbstractMap.SimpleEntry<>("Content-Type", "application/json");
        String expectedBody = String.format(
            (
                "  \"args\": {\n" +
                "    \"%s\": \"%s\"\n" +
                "  }"
            ),
            key, value
        );

        assert httpResponse.getStatus().getCode() == expectedStatusCode;
        assert httpResponse.getStatus().getReason().equals(expectedStatusReason);
        assert httpResponse.getHeaders().containsKey(expectedHeader.getKey());
        assert httpResponse.getHeaders().get(expectedHeader.getKey())
                .equals(expectedHeader.getValue());
        assert new String(httpResponse.getBody()).contains(expectedBody);
    }

    @SneakyThrows
    private static void testGetNoBody(HttpClient httpClient) {
        URL url = new URL(HTTPBIN_BASE_URL + "/status/201");

        HttpRequest request = new HttpRequest(HttpMethod.GET, url, emptyHeaders);

        HttpResponse httpResponse = httpClient.performRequest(request);

        int expectedStatusCode = 201;
        String expectedStatusReason = "CREATED";
        Map.Entry<String, String> expectedHeader = new AbstractMap.SimpleEntry<>("Content-Length", "0");

        assert httpResponse.getStatus().getCode() == expectedStatusCode;
        assert httpResponse.getStatus().getReason().equals(expectedStatusReason);
        assert httpResponse.getHeaders().containsKey(expectedHeader.getKey());
        assert httpResponse.getHeaders().get(expectedHeader.getKey())
                .equals(expectedHeader.getValue());
        assert httpResponse.getBody().length == 0;
    }

    @SneakyThrows
    private static void testRedirectedGet(HttpClient httpClient) {
        URL url = new URL(HTTPBIN_BASE_URL + "/redirect/10");

        HttpRequest request = new HttpRequest(HttpMethod.GET, url, emptyHeaders);

        HttpResponse httpResponse = httpClient.performRequest(request);

        int expectedStatusCode = 200;
        String expectedStatusReason = "OK";

        assert httpResponse.getStatus().getCode() == expectedStatusCode;
        assert httpResponse.getStatus().getReason().equals(expectedStatusReason);
    }

    @SneakyThrows
    private static void testPost(HttpClient httpClient) {
        String key = "mynameis";
        String value = "david";
        URL url = new URL(HTTPBIN_BASE_URL + "/post");
        byte[] body = String.format("{ \"%s\": \"%s\" }", key, value).getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");

        HttpRequest request = new HttpRequest(HttpMethod.POST, url, headers, body);

        HttpResponse httpResponse = httpClient.performRequest(request);

        int expectedStatusCode = 200;
        String expectedStatusReason = "OK";
        Map.Entry<String, String> expectedHeader = new AbstractMap.SimpleEntry<>("Content-Type", "application/json");
        String expectedBody = String.format(
                (
                    "  \"json\": {\n" +
                    "    \"%s\": \"%s\"\n" +
                    "  }"
                ),
                key, value
        );

        assert httpResponse.getStatus().getCode() == expectedStatusCode;
        assert httpResponse.getStatus().getReason().equals(expectedStatusReason);
        assert httpResponse.getHeaders().containsKey(expectedHeader.getKey());
        assert httpResponse.getHeaders().get(expectedHeader.getKey())
                .equals(expectedHeader.getValue());
        assert new String(httpResponse.getBody()).contains(expectedBody);
    }
}
