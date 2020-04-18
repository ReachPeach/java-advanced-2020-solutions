package ru.ifmo.rain.busyuk.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import static java.nio.file.Paths.get;

public class Walk {

    private static final int FNV32_PRIME = 0x01000193;

    private static String hash32(String file) {
        try (BufferedInputStream reader = new BufferedInputStream(Files.newInputStream(get(file)))) {
            int hval = 0x811c9dc5;
            byte[] bytes = new byte[1024];
            int dataSize;
            while ((dataSize = reader.read(bytes)) != -1) {
                for (int i = 0; i < dataSize; i++) {
                    hval = (hval * FNV32_PRIME) ^ (bytes[i] & 0xff);
                }
            }

            return String.format("%08x", hval);
        } catch (IOException | InvalidPathException e) {
            System.out.println("File not found: " + file);
            return "00000000";
        }
    }

    public static void main(String[] args) {
        if (args == null || args[0] == null || args[1] == null || args.length != 2) {
            System.out.println("Too many or not enough files");
            return;
        }

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8));
        ) {
            String file;
            while ((file = reader.readLine()) != null) {
                writer.write(hash32(file) + " " + file + System.lineSeparator());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Input problems " + e.getMessage());
        }

    }
}