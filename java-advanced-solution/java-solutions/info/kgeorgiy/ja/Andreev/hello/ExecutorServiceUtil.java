package info.kgeorgiy.ja.Andreev.hello;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceUtil {
     static void  awaitTermination(ExecutorService executorService, long time) throws InterruptedException {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(time, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(2 * time, TimeUnit.SECONDS))
                    System.err.println("ExecutorService did not terminate");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new InterruptedException();
        }
    }
}
