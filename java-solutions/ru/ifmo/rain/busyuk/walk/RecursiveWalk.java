package ru.ifmo.rain.busyuk.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    private static final int FNV32_PRIME = 0x01000193;

    static int getFnvHash(Path path) {
        int hashValue = 0x811c9dc5;
        try (FileInputStream inputStream = new FileInputStream(path.toString())) {
            byte[] buf = new byte[2048];
            int dataSize;
            while ((dataSize = inputStream.read(buf)) != -1) {
                for (int i = 0; i < dataSize; i++) {
                    hashValue = (hashValue * FNV32_PRIME) ^ (buf[i] & 0xff);
                }
            }
        } catch (IOException e) {
            hashValue = 0;
        }
        return hashValue;
    }

    private static void run(String[] args) throws RecursiveWalkException {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            throw new RecursiveWalkException("Too many or not enough files");
        }

        try {
            if (Paths.get(args[1]).getParent() != null && Files.notExists(Paths.get(args[1]).getParent())) {
                Files.createDirectory(Paths.get(args[1]).getParent());
            }
        } catch (IOException e) {
            throw new RecursiveWalkException("Error while trying to make parent directory for output file " + e.getMessage(), e);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid path for output file`s parent " + e.getMessage(), e);
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8))) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8))) {
                String directory;
                RecursiveFileVisitor fileVisitor = new RecursiveFileVisitor(writer);
                while ((directory = reader.readLine()) != null) {
                    fileVisitor.process(directory);
                }
            } catch (FileNotFoundException e) {
                throw new RecursiveWalkException("InputFile not found: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RecursiveWalkException("Input problems " + e.getMessage(), e);
            }
        } catch (
                FileNotFoundException e) {
            throw new RecursiveWalkException("OutFile not found: " + e.getMessage(), e);
        } catch (
                IOException e) {
            throw new RecursiveWalkException("Output problems " + e.getMessage(), e);
        }

    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (RecursiveWalkException e) {
            System.out.println(e.getMessage());
        }
    }
}
