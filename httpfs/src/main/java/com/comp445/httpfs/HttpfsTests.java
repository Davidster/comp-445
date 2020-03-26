package com.comp445.httpfs;

import com.comp445.common.http.*;
import com.comp445.common.logger.LogLevel;
import com.comp445.common.logger.Logger;
import com.comp445.httpfs.argparser.HttpfsOptions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class HttpfsTests {

    public static void main(String[] args) throws IOException {
        Logger.logLevel = LogLevel.VERBOSE;

        int port = 8082;

        String tempFolder = UUID.randomUUID().toString();
        Path workingDir = Paths.get("");
        Path tempFolderPath = workingDir.resolve(tempFolder);

        String file1Name = UUID.randomUUID().toString();
        String file1Content =  UUID.randomUUID().toString();
        String file2Name = UUID.randomUUID().toString();
        String file2Content = "";
        String nonExistantFileName = UUID.randomUUID().toString();

        Files.createDirectory(tempFolderPath);

        HttpClient httpClient = new UDPHttpClient(true);
        Httpfs httpFileServer = new Httpfs(new HttpfsOptions(true, port, Paths.get(tempFolder)));

        Thread serverThread = new Thread(() -> {
            try {
                httpFileServer.startServer();
            } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try {
            Thread.sleep(100);

            String baseUrl = String.format("http://localhost:%s", port);



            URL file1Url = new URL(String.format("%s/%s", baseUrl, file1Name));
            HttpResponse file1CreationResponse = httpClient.performRequest(new HttpRequest(HttpMethod.POST, file1Url, new HttpHeaders(), file1Content.getBytes()));
            doAssert(file1CreationResponse.getStatus().getCode() == 201);

            Thread.sleep(10);

            URL file2Url = new URL(String.format("%s/%s", baseUrl, file2Name));
            HttpResponse file2CreationResponse = httpClient.performRequest(new HttpRequest(HttpMethod.POST, file2Url, new HttpHeaders(), file2Content.getBytes()));
            doAssert(file2CreationResponse.getStatus().getCode() == 201);

            Thread.sleep(10);

            HttpResponse file1RetrievalResponse = httpClient.performRequest(new HttpRequest(HttpMethod.GET, file1Url, new HttpHeaders()));
            doAssert(file1RetrievalResponse.getStatus().getCode() == 200);
            doAssert(new String(file1RetrievalResponse.getBody()).equals(file1Content));

            Thread.sleep(10);

            HttpResponse file2RetrievalResponse = httpClient.performRequest(new HttpRequest(HttpMethod.GET, file2Url, new HttpHeaders()));
            doAssert(file2RetrievalResponse.getStatus().getCode() == 200);
            doAssert(new String(file2RetrievalResponse.getBody()).equals(file2Content));

            Thread.sleep(10);

            HttpResponse fileListingResponse = httpClient.performRequest(new HttpRequest(HttpMethod.GET, new URL(baseUrl), new HttpHeaders()));
            doAssert(fileListingResponse.getStatus().getCode() == 200);
            doAssert(new String(fileListingResponse.getBody()).contains(file1Name));
            doAssert(new String(fileListingResponse.getBody()).contains(file2Name));

            Thread.sleep(10);

            URL nonExistantFileUrl = new URL(String.format("%s/%s", baseUrl, nonExistantFileName));
            HttpResponse nonExistantRetrievalResponse = httpClient.performRequest(new HttpRequest(HttpMethod.GET, nonExistantFileUrl, new HttpHeaders()));
            doAssert(nonExistantRetrievalResponse.getStatus().getCode() == 404);

            Thread.sleep(10);

            URL illegalFolderUrl = new URL(String.format("%s/../", baseUrl));
            HttpResponse illegalFolderResponse = httpClient.performRequest(new HttpRequest(HttpMethod.GET, illegalFolderUrl, new HttpHeaders()));
            doAssert(illegalFolderResponse.getStatus().getCode() == 403);

            Thread.sleep(10);

            HttpResponse writeToFolderResponse = httpClient.performRequest(new HttpRequest(HttpMethod.POST, new URL(baseUrl), new HttpHeaders()));
            doAssert(writeToFolderResponse.getStatus().getCode() == 400);
            doAssert(new String(writeToFolderResponse.getBody()).contains("Destination path is a directory"));

            Thread.sleep(10);

            HttpResponse nullBodyResponse = httpClient.performRequest(new HttpRequest(HttpMethod.POST, file1Url, new HttpHeaders()));
            doAssert(nullBodyResponse.getStatus().getCode() == 400);
            doAssert(new String(nullBodyResponse.getBody()).contains("Body is required"));

            Logger.log("Tests passed");
        } catch(Exception e) {
            e.printStackTrace();
            Logger.log("Tests failed");
        }

        tryToDelete(tempFolderPath.resolve(file1Name));
        tryToDelete(tempFolderPath.resolve(file2Name));
        tryToDelete(tempFolderPath);
        System.exit(0);
    }
    
    private static void doAssert(boolean b) throws Exception {
        if(!b) {
            throw new Exception();
        }
    }

    private static void tryToDelete(Path filePath) {
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
