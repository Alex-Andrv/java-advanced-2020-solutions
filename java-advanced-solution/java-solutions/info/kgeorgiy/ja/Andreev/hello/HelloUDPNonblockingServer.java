package info.kgeorgiy.ja.Andreev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class HelloUDPNonblockingServer extends AbstractHelloUDPServer implements HelloServer {

    private Queue<SendingData> completedTask;

    private Selector selector;

    private DatagramChannel datagramChannel;

    private ExecutorService workers;

    private final ExecutorService server = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        try (HelloServer helloServer = new HelloUDPNonblockingServer()) {
            MainUtil.ValidateArgsAndRunHelloService(args, helloServer);
        }
    }


    private static class SendingData {
        ByteBuffer buffer;
        SocketAddress address;

        SendingData(ByteBuffer buffer, SocketAddress address) {
            this.buffer = buffer;
            this.address = address;
        }
    }


    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param threads number of working threads.
     * @ param port    server port.
     */
    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
            workers = Executors.newFixedThreadPool(threads);
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
            datagramChannel.register(selector, SelectionKey.OP_READ);
            datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            completedTask = new LinkedBlockingDeque<>();
            server.submit(this::startServer);
        } catch (IOException e) {
            writeErrorAndClosed("Can't open selector");
        }
    }

    private void startServer() {
        try {
            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                selector.select(this::processKey);
            }
        } catch (ClosedSelectorException e) {
            System.err.println("Selector was closed");
        } catch (IOException e) {
            System.err.println("Something terrible happened to the selector");
        }
    }

    void read(SelectionKey key, ByteBuffer buffer, int bufferSize, SocketAddress socketAddress) {
        ByteBuffer request = ByteBuffer.allocate(bufferSize);
        request.put(HELLO).put(buffer.flip());
        synchronized (key) {
            completedTask.add(new SendingData(request.flip(), socketAddress));
            if (key.isValid()) {
                key.interestOpsOr(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }
    }

    void write(SelectionKey key, DatagramChannel datagramChannel) {
        SendingData sendingData = completedTask.poll();
        if (!Objects.isNull(sendingData)) {
            try {
                datagramChannel.send(sendingData.buffer, sendingData.address);
            } catch (IOException e) {
                System.err.println("Can't send data");
            }
        } else {
           writeErrorAndClosed("Unexpected behavior: key interest ops contains Write, " +
                   "but it unexpected");
        }
        synchronized (key) {
            if (completedTask.size() == 0) {
                if (key.isValid()) {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    void processKey(SelectionKey key) {
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        if (key.isReadable()) {
            try {
                final int bufferSize = datagramChannel.socket().getReceiveBufferSize();
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                SocketAddress socketAddress = datagramChannel.receive(buffer);
                workers.submit(() -> read(key, buffer, bufferSize, socketAddress));
            } catch (IOException e) {
                writeErrorAndClosed("Error with datagramChannel");
            }
        }
        if (key.isWritable()) {
            write(key, datagramChannel);
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        if (!Objects.isNull(selector)) {
            try {
                selector.close();
            } catch (IOException e) {
                System.err.println("Error during closing the selector");
            }
        }
        if (!Objects.isNull(datagramChannel)) {
            try {
                datagramChannel.close();
            } catch (IOException e) {
                System.err.println("Can't close Datagram Channel");
            }
        }
        try {
            if (!Objects.isNull(workers)) {
                ExecutorServiceUtil.awaitTermination(workers, 10000);
            }
            ExecutorServiceUtil.awaitTermination(server, 10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void writeErrorAndClosed(final String  Error) {
        System.err.println(Error);
        close();
    }
}
