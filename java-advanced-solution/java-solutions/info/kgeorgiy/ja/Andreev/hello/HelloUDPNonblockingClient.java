package info.kgeorgiy.ja.Andreev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPNonblockingClient extends AbstractHelloUDPClient implements HelloClient {

    private static final long TIME_LIMIT = 100;


    public static void main(String[] args) {
        MainUtil.ValidateArgsAndRunHelloClient(args, new HelloUDPNonblockingClient());
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
    public void run(String host, int port, final String prefix, int threads, int requests) {
        List<DatagramChannel> datagramChannels = new ArrayList<>();
        try (Selector selector = Selector.open()) {
            registerThreadsDatagramChannels(selector, new InetSocketAddress(host, port),
                    threads, requests, datagramChannels);
            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                if (selector.select((key) -> processKey(key, prefix), TIME_LIMIT) == 0) {
                    selector.keys().forEach((key) -> key.interestOps(SelectionKey.OP_WRITE));
                }
            }
        } catch (ClosedSelectorException e) {
            System.err.println("Selector was closed");
        } catch (IOException e) {
            System.err.println("Error with a selector");
        } finally {
            closeDatagramChannels(datagramChannels);
        }
    }

    private void closeDatagramChannels(List<DatagramChannel> datagramChannels) {
        for (DatagramChannel datagramChannel : datagramChannels) {
            try {
                datagramChannel.close();
            } catch (IOException e) {
                System.err.println("Can't close channel");
            }
        }
    }

    private void processKey(final SelectionKey key, final String prefix) {
        ThreadEmulator currentThread = (ThreadEmulator) key.attachment();
        DatagramChannel channel = (DatagramChannel) key.channel();
        int currentThreadId = currentThread.threadId;
        ByteBuffer buffer = currentThread.buffer;
        String request = getRequestBody(prefix, currentThreadId,
                currentThread.requestsId);
        try {
            if (key.isReadable()) {
                try {
                    channel.read(buffer.clear());
                    String response = CHARSET.decode(buffer.flip()).toString();
                    System.out.println("Receive: " + response);
                    if (validate(response, request)) {
                        currentThread.requestsId++;
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                    if (currentThread.requestsId == currentThread.requests) {
                        channel.close();
                    }
                } catch (IOException e) {
                    closeChanelWithError(channel,
                            "Something terrible happened to the channel");
                }
            } else if (key.isWritable()) {
                try {
                    channel.write(ByteBuffer.wrap(request.getBytes(CHARSET)));
                    System.out.println("send: " + request);
                    key.interestOps(SelectionKey.OP_READ);
                } catch (IOException e) {
                    closeChanelWithError(channel,
                            "Something terrible happened to the channel");
                }
            }
        } catch (final IOException e) {
            System.err.println("Error during closing socket");
            throw new UncheckedIOException(e);
        }

    }

    private void registerThreadsDatagramChannels(Selector selector, SocketAddress socketAddress,
                                                 int threads, int requests, List<DatagramChannel> datagramChannels) {
        for (int threadId = 0; threadId < threads; threadId++) {
            try {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(socketAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE, new ThreadEmulator(threadId, requests,
                        datagramChannel.socket().getReceiveBufferSize()));
                datagramChannels.add(datagramChannel);
            } catch (IOException e) {
                MainUtil.printErrAndOut(String.format("Can't create %dth DatagramChannel", threadId));
            }
        }
    }

    static class ThreadEmulator {
        final int threadId;
        final int requests;
        ByteBuffer buffer;
        final int bufferSize;
        private int requestsId;

        ThreadEmulator(int threadId, int requests, int bufferSize) {
            this.threadId = threadId;
            this.requests = requests;
            this.bufferSize = bufferSize;
            buffer = ByteBuffer.allocate(bufferSize);
            requestsId = 0;
        }
    }

    private void closeChanelWithError(Channel channel, String error) throws IOException {
        channel.close();
        System.err.println(error);
    }
}
