package info.kgeorgiy.ja.Andreev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.Arrays;
import java.util.Objects;

public class MainUtil {

    public static void printErrAndOut(String massage) {
        System.err.println(massage);
        System.exit(0);
    }

    public static void ValidateArgsAndRunHelloClient(String[] args, HelloClient helloClient) {
        if (Objects.isNull(args) || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            MainUtil.printErrAndOut("Invalid parameter list");
        }
        try {
            final String host = args[0];
            final int port = Integer.parseInt(args[1]);
            final String prefix = args[2];
            final int threads = Integer.parseInt(args[3]);
            final int requests = Integer.parseInt(args[4]);
            helloClient.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            MainUtil.printErrAndOut("Invalid argument format");
        }
    }

    public static void ValidateArgsAndRunHelloService(String[] args, HelloServer helloServer) {
        if (Objects.isNull(args) || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            MainUtil.printErrAndOut("Invalid parameter list");
        }
        try {
            final int port = Integer.parseInt(args[0]);
            final int threads = Integer.parseInt(args[1]);
            helloServer.start(port, threads);
        } catch (NumberFormatException e) {
            MainUtil.printErrAndOut("Invalid argument format");
        }
    }
}
