package info.kgeorgiy.ja.Andreev.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class RecursiveWalk {

    static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Incorrect set of arguments. Expected format: java Walk <input file> <output file>");
            return;
        }
        Path inputFile;
        Path outputFile;
        try {
            inputFile = Path.of(args[0]);
            outputFile = Path.of(args[1]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid input/output File name \n" + e);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(inputFile, FILE_CHARSET)) {
            try(BufferedWriter writer = Files.newBufferedWriter(outputFile, FILE_CHARSET)) {
                Visitor visitor = new Visitor(writer);
                String name;
                while ((name = reader.readLine()) != null) {
                    try {
                        Files.walkFileTree(Path.of(name), visitor);
                    } catch (InvalidPathException e) {
                        writer.write("0000000000000000" + " " + name);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("Can't write data");
                        return;
                    }
                }
            } 
        } catch (IOException e) {
            System.err.println("Can't read data");
        }
    }
}
