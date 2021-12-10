package info.kgeorgiy.ja.Andreev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer extends AbstractHelloUDPServer implements HelloServer {
    //java -cp . -p . -m info.kgeorgiy.java.advanced.hello server info.kgeorgiy.ja.Andreev.hello.HelloUDPServer

    private DatagramSocket serverSocket;
    private int buffSize = 0;

    public static void main(String[] args) {
        try (HelloServer helloServer = new HelloUDPServer()) {
            MainUtil.ValidateArgsAndRunHelloService(args, helloServer);
        }
    }

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        responsePool = Executors.newFixedThreadPool(threads);
        try {
            serverSocket = new DatagramSocket(port);
            buffSize = serverSocket.getReceiveBufferSize();
            for (int threadId = 0; threadId < threads; threadId++) {
                responsePool.submit(this::processingRequest);
            }
        } catch (SocketException e) {
            System.err.println("Error with socket");
        }
    }

    private void processingRequest() {
        final DatagramPacket packet = new DatagramPacket(new byte[buffSize], buffSize);
        while (!serverSocket.isClosed() && !Thread.interrupted()) {
            try {
                serverSocket.receive(packet);
                byte[] newData = createNewData(packet.getData(), packet.getLength());
                //System.out.println("send data:" + new String(createNewData(packet.getData(), packet.getLength()), CHARSET));
                packet.setData(newData);
                serverSocket.send(packet);
            } catch (final SocketTimeoutException ignored) {
                System.err.println("Server did not receive any requests in given time");
            } catch (final IOException e) {
                if (!serverSocket.isClosed()) { //
                    System.err.println("Error occurred during communication with the client");
                }
            }
        }

    }


    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        if (!Objects.isNull(serverSocket)) {
            serverSocket.close();
        }
        if (!Objects.isNull(responsePool)) {
            try {
                ExecutorServiceUtil.awaitTermination(responsePool, 10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
