package com.comp455.httpclient;

import com.comp455.httpclient.client.HttpClient;
import com.comp455.httpclient.client.HttpResponse;
import com.comp455.httpclient.logger.LogLevel;
import com.comp455.httpclient.logger.Logger;
import javafx.util.Pair;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestRunner {

    private static final String HTTPBIN_BASE_URL = "http://httpbin.org";

    private static final Map<String, String> emptyHeaderMap =
            Collections.unmodifiableMap(new HashMap<>());

    @SneakyThrows
    public static void main(String[] args) {
        Logger.logLevel = LogLevel.ERROR;

        HttpClient httpClient = new HttpClient();

        testGet(httpClient);
        testGetNoBody(httpClient);
        testPost(httpClient);
    }

    @SneakyThrows
    private static void testGet(HttpClient httpClient) {
        String key = "hello";
        String value = "world";
        URL url = new URL(HTTPBIN_BASE_URL + String.format("/get?%s=%s", key, value));

        HttpResponse httpResponse = httpClient.performGetRequest(emptyHeaderMap, url);

        int expectedStatusCode = 200;
        String expectedStatusReason = "OK";
        Pair<String, String> expectedHeader = new Pair<>("Content-Type", "application/json");
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
        assert httpResponse.getBody().contains(expectedBody);
    }

    @SneakyThrows
    private static void testGetNoBody(HttpClient httpClient) {
        URL url = new URL(HTTPBIN_BASE_URL + "/status/201");

        HttpResponse httpResponse = httpClient.performGetRequest(emptyHeaderMap, url);

        int expectedStatusCode = 201;
        String expectedStatusReason = "CREATED";
        Pair<String, String> expectedHeader = new Pair<>("Content-Length", "0");
        String expectedBody = "";

        assert httpResponse.getStatus().getCode() == expectedStatusCode;
        assert httpResponse.getStatus().getReason().equals(expectedStatusReason);
        assert httpResponse.getHeaders().containsKey(expectedHeader.getKey());
        assert httpResponse.getHeaders().get(expectedHeader.getKey())
                .equals(expectedHeader.getValue());
        assert httpResponse.getBody().equals(expectedBody);
    }

    @SneakyThrows
    private static void testPost(HttpClient httpClient) {
        String key = "mynameis";
        String value = "david";
        URL url = new URL(HTTPBIN_BASE_URL + "/post");
        String body = String.format("{ \"%s\": \"%s\" }", key, value);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        HttpResponse httpResponse = httpClient.performPostRequest(headers, url, body);

        int expectedStatusCode = 200;
        String expectedStatusReason = "OK";
        Pair<String, String> expectedHeader = new Pair<>("Content-Type", "application/json");
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
        assert httpResponse.getBody().contains(expectedBody);
    }
}
