package com.comp445.drivers;

import com.comp445.common.net.selectiverepeat.SRServerSocket;
import com.comp445.common.net.selectiverepeat.SRSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.comp445.common.Utils.EXECUTOR;

public class SRTests {

    public static final int REC_PORT = 8888;

    // ./router_x64 --port=3000 --drop-rate=0.1 --max-delay=0.1s --seed=5
    // ^^^ makes the final ACK get dropped in one of the tests
    public static void main(String[] args) throws IOException, InterruptedException {
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        testConnect();
        System.out.println();
        EXECUTOR.shutdown();
    }

    public static void testConnect() throws IOException, InterruptedException {

        SRSocket srClient = new SRSocket();
        SRServerSocket srServer = new SRServerSocket(REC_PORT);
        AtomicReference<SRSocket> srServerClient = new AtomicReference<>();

        Instant start = Instant.now();

        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                srServerClient.set(srServer.acceptClient());
                System.out.println(System.currentTimeMillis() + " Server connected in " + Duration.between(start, Instant.now()).toMillis() + "ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, EXECUTOR);

        srClient.connect(new InetSocketAddress(InetAddress.getByName("localhost"), REC_PORT), 10000);
        System.out.println(System.currentTimeMillis() + " Client connected in " + Duration.between(start, Instant.now()).toMillis() + "ms");

        serverFuture.join();
        srServer.close();
        srClient.close();
        if(srServerClient.get() != null) {
            srServerClient.get().close();
        }
        System.out.println("----------------------");
        // give os some time to unbind port
        Thread.sleep(25);
    }

}
