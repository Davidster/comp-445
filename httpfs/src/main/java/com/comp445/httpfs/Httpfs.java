package com.comp445.httpfs;

import com.comp445.common.http.HttpServer;

public class Httpfs {
    public static void main(String[] args) throws Exception {
        new HttpServer().startServer(8081, null);
    }
}
