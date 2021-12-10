package info.kgeorgiy.ja.Andreev.walk;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Visitor extends SimpleFileVisitor<Path> {
    static final String SIXTEEN_ZEROS = "0000000000000000";
    static final int BYTE_ARRAY_SIZE = 1024;
    private final BufferedWriter writer;

    Visitor(BufferedWriter writer) {
        this.writer = writer;
    }

    private FileVisitResult writeAns(String ans) throws IOException {
        writer.write(ans);
        writer.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        long h = 0;
        try (BufferedInputStream reader = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] byteArray = new byte[BYTE_ARRAY_SIZE];
            // :NOTE: there's no buffer - зачем???????
            int countByte;
            while ((countByte = reader.read(byteArray)) != -1) {
                for (int i = 0; i < countByte; i++) {
                    h = (h << 8) + (byteArray[i] & 0xff);
                    long high = h & 0xff00_0000_0000_0000L;
                    if (high != 0) {
                        h ^= high >> 48;
                        h &= ~high;
                    }
                }
            }
        } catch (IOException e) {
            return writeAns(SIXTEEN_ZEROS + " " + path.toString());
        }
        return writeAns(String.format("%016x %s", h, path.toString()));
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        return writeAns(SIXTEEN_ZEROS + " " + path.toString());
    }
}
