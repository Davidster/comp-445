package com.comp445.drivers;

import com.comp445.common.Utils;
import com.comp445.common.net.selectiverepeat.SRInputStream;
import com.comp445.common.net.selectiverepeat.SROutputStream;
import com.comp445.common.net.selectiverepeat.SRServerSocket;
import com.comp445.common.net.selectiverepeat.SRSocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.comp445.common.Utils.EXECUTOR;

public class SRDataTests {

    public static final int REC_PORT = 8888;

    private static byte[] delim = new byte[]{ 0, 0, 0, 0 };

    public static void main(String[] args) throws IOException, InterruptedException {
        SRSocket srClient = new SRSocket();
        SRServerSocket srServer = new SRServerSocket(REC_PORT);
        AtomicReference<SRInputStream> serverInputStream = new AtomicReference<>();
        AtomicReference<SRSocket> srServerClient = new AtomicReference<>();

        Instant start = Instant.now();


        byte[] sendPayload = new byte[100000];
        new Random().nextBytes(sendPayload);
        sendPayload[sendPayload.length - 1] = 0;
        sendPayload[sendPayload.length - 2] = 0;
        sendPayload[sendPayload.length - 3] = 0;
        sendPayload[sendPayload.length - 4] = 0;
//        byte[] sendPayload = new byte[]{ 34, 24, 33, 54, 63, 76, 23, 76, 97, 90, 95, 34, 54 };

        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                srServerClient.set(srServer.acceptClient());
                System.out.println(System.currentTimeMillis() + " Server connected in " + Duration.between(start, Instant.now()).toMillis() + "ms");

                serverInputStream.set(srServerClient.get().getInputStream());

                Thread.sleep(1000);

                verifyPayloadReceived(sendPayload, srServerClient.get().getInputStream());

                srServerClient.get().getOutputStream().write(sendPayload);
                srServerClient.get().getOutputStream().queuePacket();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, EXECUTOR);

        srClient.connect(new InetSocketAddress(InetAddress.getByName("localhost"), REC_PORT), 30000);
        System.out.println(System.currentTimeMillis() + " Client connected in " + Duration.between(start, Instant.now()).toMillis() + "ms");


        SROutputStream clientOutputStream = srClient.getOutputStream();
//        Thread.sleep(100);
        try {
            clientOutputStream.write(sendPayload);
            clientOutputStream.queuePacket();

            verifyPayloadReceived(sendPayload, srClient.getInputStream());
        } catch(Exception e) {
            e.printStackTrace();
        }

//        serverFuture.join();

        // give some time for ACKS to come in
//        Thread.sleep(1000);

        srServer.close();
        srClient.close();
        if(srServerClient.get() != null) {
            srServerClient.get().close();
        }

        System.out.println(String.format(
                "Server received %s unique packets, %s total packets, %s ack packets",
                serverInputStream.get().getUniqueDataPacketsReceived(),
                serverInputStream.get().getTotalDataPacketsReceived(),
                serverInputStream.get().getTotalAckPacketsReceived()));

        EXECUTOR.shutdown();
    }

    private static void verifyPayloadReceived(byte[] payload, SRInputStream srInputStream) throws InterruptedException, IOException {
        byte[] recPayload = Utils.readTillDelim(new BufferedInputStream(srInputStream), delim);

//        Thread.sleep(250);

        if(payload.length != recPayload.length) {
            System.out.println("Length not equal!");
        }
        boolean good = true;
        for (int i = 0; i < payload.length; i++) {
            if(i == recPayload.length) {
                System.out.println("Reached end of recPayload unexpectedly");
                good = false;
                break;
            }
            if(recPayload[i] != payload[i]) {
                System.out.println(String.format("Found different byte at index %s (expected %s but found %s)", i, payload[i], recPayload[i]));
                good = false;
            }
        }
        if(good) {
            System.out.println("Data received properly");
        } else {
            System.out.println("Data NOT received properly");
        }
    }
}
