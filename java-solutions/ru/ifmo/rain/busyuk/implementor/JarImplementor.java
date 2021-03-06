package ru.ifmo.rain.busyuk.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Implements specified base token and produces jar file with result.
 */
public class JarImplementor extends Implementor implements info.kgeorgiy.java.advanced.implementor.JarImpler {
    /**
     * Creates new instance of {@link JarImplementor}
     */
    public JarImplementor() {
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path rootTemp;
        try {
            rootTemp = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Problems with creating directory " + e.getMessage(), e);
        }
        JarImplementor implementor = new JarImplementor();
        implementor.implement(token, rootTemp);

        Path packageRoot = rootTemp.resolve(getPackagePath(token));
        File sourceFile = packageRoot.resolve(token.getSimpleName() + "Impl.java").toFile();
        Path classFilePath = packageRoot.resolve(token.getSimpleName() + "Impl.class");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String classpath = getTokenClasspath(token);
        if (compiler.run(null, null, null, "-cp", classpath, sourceFile.toString()) != 0) {
            throw new ImplerException("Problems with compiling");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try {
            Files.createDirectories(jarFile.getParent());
        } catch (IOException e) {
            throw new ImplerException("Cannot create output directories ", e);
        }
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()), manifest)) {
            String entryName = toJarEntryName(getPackagePath(token).resolve(token.getSimpleName() + "Impl.class"));
            jos.putNextEntry(new JarEntry(entryName));
            Files.copy(classFilePath, jos);
            jos.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Problems with output" + e.getMessage(), e);
        }
    }

    /**
     * Returns token`s classpath.
     *
     * @param token {@link Class} object.
     * @return {@link String} representing token`s classpath.
     */
    private String getTokenClasspath(final Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Converts path to string with correct filepath related to jar.
     *
     * @param filepath {@link Path} current token`s filepath.
     * @return {@link String} filepath with correct jar separators.
     */
    private String toJarEntryName(Path filepath) {
        return filepath.toString().replace(File.separator, "/");
    }

    /**
     * Generates implementation for specified command line arguments. Required
     * arguments are {@code classname} and {@code output file}. Usage:
     * {@code -jar <classname> <output file>}
     *
     * @param args three command line arguments for running an application.
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 3 || !"-jar".equals(args[0])) {
                throw new ImplerException("Wrong usage! Use -jar <classname> <output file>");
            }
            try {
                new JarImplementor().implementJar(Class.forName(args[1]), Path.of(args[2]));
            } catch (ClassNotFoundException e) {
                throw new ImplerException("Class not found" + e.getMessage(), e);
            } catch (InvalidPathException e) {
                throw new ImplerException("Invalid path" + e.getMessage(), e);
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
