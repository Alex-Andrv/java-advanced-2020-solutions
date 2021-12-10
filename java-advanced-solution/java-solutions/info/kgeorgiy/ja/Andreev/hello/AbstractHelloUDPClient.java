package info.kgeorgiy.ja.Andreev.hello;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AbstractHelloUDPClient {

 //   private static final int BUF_SIZE =

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    protected String getRequestBody(String prefix, int threadId, int requestId) {
        String request = String.format("%s%d_%d", prefix, threadId, requestId);
        return request;
    }

    protected boolean validate(String response, String request) {
        return response.contains(request);
    }
}
