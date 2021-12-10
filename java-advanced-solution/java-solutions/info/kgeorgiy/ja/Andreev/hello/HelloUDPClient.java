package info.kgeorgiy.ja.Andreev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient extends AbstractHelloUDPClient implements HelloClient {
    //java -cp . -p . -m info.kgeorgiy.java.advanced.hello client-i18n info.kgeorgiy.ja.Andreev.hello.HelloUDPClient


    private static final long AWAIT_TIME = 5;

    private static final int SO_TIMEOUT = 100;


    public static void main(String[] args) {
       MainUtil.ValidateArgsAndRunHelloClient(args, new HelloUDPClient());
    }

    /**
     * Runs Hello client.
     * This method should return when all requests completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(final String host, final int port, final String prefix,
                    final int threads, final int requests) {
        System.out.println("Server Socket port: " + port);
        try {
            final SocketAddress socketAddress =
                    new InetSocketAddress(InetAddress.getByName(host), port);
            ExecutorService requestPool =
                    Executors.newFixedThreadPool(threads);
            for (int threadId = 0; threadId < threads; threadId++) {
                final int finalThreadId = threadId;
                requestPool.submit(() ->
                        sendRequestsMassages(socketAddress, finalThreadId, prefix, requests));
            }
            ExecutorServiceUtil.awaitTermination(requestPool, threads * requests * AWAIT_TIME);
        } catch (UnknownHostException e) {
            System.err.println("Unable to reach specified host");
        } catch (InterruptedException e) {
            System.err.println("This Thread was Interrupted");
        }
    }


    private void sendRequestsMassages(final SocketAddress socketAddress, final int finalThreadId,
                                      final String prefix, final int requests) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(SO_TIMEOUT);
            int buffSize = clientSocket.getReceiveBufferSize();
            DatagramPacket packet = new DatagramPacket(new byte[buffSize], buffSize, socketAddress);
            for (int requestId = 0; requestId < requests; requestId++) {
                String request = getRequestBody(prefix, finalThreadId, requestId);
                while (!clientSocket.isClosed() && !Thread.interrupted()) {
                    try {
                        packet.setData(request.getBytes(CHARSET));
                        clientSocket.send(packet);
                        packet.setData(new byte[buffSize]);
                        clientSocket.receive(packet);
                        String response = new String(packet.getData(), packet.getOffset(), packet.getLength(), CHARSET);
                        if (validate(response, request)) {
                            System.out.println(request + "\n" + response);
                            break;
                        }
                    } catch (SocketTimeoutException ignore) {
                        //
                    } catch (IOException e) {
                        System.err.println("Error occurred during communication with the server");
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error with socket");
        }
    }




}
