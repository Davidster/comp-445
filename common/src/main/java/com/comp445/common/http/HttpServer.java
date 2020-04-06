package com.comp445.common.http;

import com.comp445.common.net.IServerSocket;
import com.comp445.common.net.ISocket;
import lombok.AllArgsConstructor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

@AllArgsConstructor
public class HttpServer {

    public static final byte[] DEFAULT_RESPONSE_BODY = "NOTHING SPECIAL HERE".getBytes();
    public static final HttpResponse DEFAULT_RESPONSE =
            new HttpResponse(new HttpStatus(HttpStatus.STATUS_NOT_FOUND), new HttpHeaders(), DEFAULT_RESPONSE_BODY);

    private Class<? extends IServerSocket> serverSocketClass;
    private int port;
    private Function<HttpRequest, HttpResponse> requestHandler;

    public void start() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {

        //noinspection InfiniteLoopStatement
        while (true) {
            IServerSocket serverSocket = serverSocketClass.getConstructor(int.class).newInstance(port);

            ISocket serverClientSocket = serverSocket.acceptClient();
            BufferedInputStream serverInputStream = new BufferedInputStream(serverClientSocket.getInputStream());
            OutputStream serverOutputStream = serverClientSocket.getOutputStream();

            HttpRequest request = HttpRequest.fromInputStream(serverInputStream);

            HttpResponse response = requestHandler != null ? requestHandler.apply(request) : DEFAULT_RESPONSE;

            serverOutputStream.write(response.toByteArray());
            serverOutputStream.flush();

            serverSocket.close();
            serverClientSocket.close();
            serverOutputStream.close();
            serverInputStream.close();
            Thread.sleep(25);
        }
    }
}
