package ru.ifmo.rain.busyuk.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;
    private final String OUTPUT_FORMAT = "%08x %s";

    RecursiveFileVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    private FileVisitResult writeToFile(String result) {
        try {
            writer.write(result + System.lineSeparator());
            return FileVisitResult.CONTINUE;
        } catch (IOException e) {
            System.out.println("Error while writing in recursive walk" + e.getMessage());
            return FileVisitResult.TERMINATE;
        }
    }

    protected void process(String directory) throws RecursiveWalkException {
        try {
            Path path = Paths.get(directory);
            Files.walkFileTree(path, this);
        } catch (InvalidPathException e) {
            writeToFile(String.format(OUTPUT_FORMAT, 0, directory));
        } catch (IOException e) {
            throw new RecursiveWalkException("Error while recursive walking" + e.getMessage(), e);
        }
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
        return writeToFile(String.format(OUTPUT_FORMAT, RecursiveWalk.getFnvHash(path), path.toString()));
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) {
        return writeToFile(String.format(OUTPUT_FORMAT, 0, path.toString()));
    }
}
