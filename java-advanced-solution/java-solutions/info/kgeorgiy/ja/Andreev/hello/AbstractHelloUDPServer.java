package info.kgeorgiy.ja.Andreev.hello;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class AbstractHelloUDPServer {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    protected final static byte[] HELLO = "Hello, ".getBytes(CHARSET);
    protected final static int HELLO_LEN = HELLO.length;

    protected ExecutorService responsePool;

    protected byte[] createNewData(byte[] data, int dataLen) {
        byte[] ans = new byte[dataLen + HELLO_LEN];
        System.arraycopy(HELLO, 0, ans, 0, HELLO_LEN);
        System.arraycopy(data, 0, ans, HELLO_LEN, dataLen);
        return ans;
    }
}
