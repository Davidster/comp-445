package com.comp445.drivers;

import com.comp445.common.net.selectiverepeat.SRServerSocket;
import com.comp445.common.net.selectiverepeat.SRSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.comp445.common.Utils.EXECUTOR;

public class SelectiveRepeatTests {

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
        EXECUTOR.shutdown();
    }

    public static void testConnect() throws IOException, InterruptedException {

        SRSocket srClient = new SRSocket();
        SRServerSocket srServer = new SRServerSocket(REC_PORT);

        Instant start = Instant.now();

        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                srServer.acceptClient();
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
        System.out.println("----------------------");
    }

}

//        SRServerSocket serverSocket = new SRServerSocket(REC_PORT);
//        SRSocket serverClientSocket = new SRSocket(serverSocket.getUdpSocket());
//        SRSocket clientSocket = new SRSocket();
//
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//
//            try {
//                System.out.println(System.currentTimeMillis() + " waiting for first packet");
//                SRPacket packet1 = PacketUtils.receiveSRPacket(serverSocket, SR_MAX_PACKET_LENGTH);
//                System.out.println(System.currentTimeMillis() + " got first packet");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(System.currentTimeMillis() + " waiting for second packet");
//            try {
//                SRPacket ackRecPacket = PacketUtils.receiveSRPacketAsync(serverClientSocket, Utils.SR_MAX_PACKET_LENGTH).orTimeout(1000, TimeUnit.MILLISECONDS).join();
//            } catch(Exception e) {
//                e.printStackTrace();
//                System.out.println(System.currentTimeMillis() + " timeout while waiting for second packet");
//            }
//        });
//
//        Thread.sleep(100);
//        PacketUtils.sendSRPacketToRouter(clientSocket, PacketType.SYN, InetAddress.getByName("localhost"), REC_PORT);
//
//        future.join();
