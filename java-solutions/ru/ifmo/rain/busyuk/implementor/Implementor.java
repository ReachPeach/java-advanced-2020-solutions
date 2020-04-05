package ru.ifmo.rain.busyuk.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements specified base token.
 */
public class Implementor implements info.kgeorgiy.java.advanced.implementor.Impler {
    /**
     * Tab space for generated classes.
     */
    private static final String TAB_SPACE = "\t";

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Null argument provided");
        }
        if (!token.isInterface()) {
            throw new ImplerException("Not Interface class token in args");
        }
        Path folderPath = root.resolve(getPackagePath(token));
        Path filePath = folderPath.resolve(token.getSimpleName() + "Impl.java");
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new ImplerException("Problems with creating directory" + e.getMessage(), e);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            List<String> data = new ArrayList<>();
            data.add(generatePackage(token));
            data.add(generateTitle(token));
            data.add(" {" + System.lineSeparator());

            for (Method method : token.getMethods()) {
                if (method.isDefault()) continue;

                int modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    modifiers -= Modifier.ABSTRACT;
                }
                if (Modifier.isTransient(modifiers)) {
                    modifiers -= Modifier.TRANSIENT;
                }
                data.add(generateAnnotations(method));
                data.add(generateModifiers(modifiers, method));
                data.add(generateArguments(method));
                data.add(generateExceptions(method));
                data.add(generateInnerCode(method));
            }
            data.add("}");
            for (String line : data) {
                writer.write(escapeUnicode(line));
            }
        } catch (IOException e) {
            throw new ImplerException("Problems with output" + e.getMessage(), e);
        }
    }

    /**
     * Returns package {@link Path} for specified token.
     *
     * @param token {@link Class} object
     * @return {@link Path} object of package containing token
     */
    protected Path getPackagePath(final Class<?> token) {
        return Path.of(token.getPackageName().replace(".", File.separator));
    }

    /**
     * Returns {@link String} information about token`s package.
     *
     * @param token {@link Class} object
     * @return {@link String} representing package`s name
     */
    private String generatePackage(Class<?> token) {
        return token.getPackage().getName() == null ? "" : ("package " + token.getPackage().getName() + ";"
                + System.lineSeparator() + System.lineSeparator());
    }

    /**
     * Returns declaration of the class.
     *
     * @param token {@link Class} object
     * @return {@link String} representing class`s title.
     * @throws ImplerException if no implementation could be made.
     */
    private String generateTitle(Class<?> token) throws ImplerException {
        String interfaceName = token.getCanonicalName();
        String className = token.getSimpleName() + "Impl";
        int mod = token.getModifiers();
        if (Modifier.isAbstract(mod)) {
            mod -= Modifier.ABSTRACT;
        }
        if (Modifier.isInterface(mod)) {
            mod -= Modifier.INTERFACE;
        }
        if (Modifier.isStatic(mod)) {
            mod -= Modifier.STATIC;
        }
        if (Modifier.isProtected(mod)) {
            mod -= Modifier.PROTECTED;
        }
        if (Modifier.isPrivate(mod)) {
            throw new ImplerException("Cannot implement private interfaces");
        }
        return (String.format("%s class %s implements %s", Modifier.toString(mod), className, interfaceName));
    }

    /**
     * Returns method`s annotations.
     *
     * @param method {@link Method} method.
     * @return {@link String} representing list of annotations for method.
     */
    private String generateAnnotations(Method method) {
        return (Arrays.stream(method.getAnnotations()).map(annotation -> "@" + annotation.annotationType()
                .getCanonicalName()).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator());
    }

    /**
     * Returns method`s modifiers.
     *
     * @param modifiers {@link Integer} method`s modifiers.
     * @param method    {@link Method} current method.
     * @return {@link String} representing list of modifiers for method.
     */
    private String generateModifiers(int modifiers, Method method) {
        return (System.lineSeparator() + TAB_SPACE + Modifier.toString(modifiers) + " " +
                method.getReturnType().getCanonicalName() + " " + method.getName());
    }

    /**
     * Returns method`s arguments.
     *
     * @param method {@link Method} current method.
     * @return {@link String} representing list of method`s arguments.
     */
    private String generateArguments(Method method) {
        return ('(' + Arrays.stream(method.getParameters()).map(parameter -> parameter.getType().getCanonicalName() +
                " " + parameter.getName()).collect(Collectors.joining(", ")) + ')');
    }

    /**
     * Returns exceptions that method could throw.
     *
     * @param method {@link Method} current method.
     * @return {@link String} representing list of exceptions that could be.
     */
    private String generateExceptions(Method method) {
        return method.getExceptionTypes().length == 0 ? "" : ("throws " + Arrays.stream(method.getExceptionTypes()).
                map(Class::getCanonicalName).collect(Collectors.joining(", ")));
    }

    /**
     * Returns method`s inner code.
     *
     * @param method {@link Method} current method.
     * @return {@link String} representing method`s inner code.
     */
    private String generateInnerCode(Method method) {
        StringBuilder innerCode = new StringBuilder(" {" + System.lineSeparator());
        if (method.getReturnType() != void.class) innerCode.append(generateReturn(method.getReturnType()));
        innerCode.append(System.lineSeparator()).append(TAB_SPACE).append('}').append(System.lineSeparator());
        return innerCode.toString();
    }

    /**
     * Returns return`s default value for method.
     *
     * @param returnType {@link Class} return type of current method.
     * @return {@link String} representing method`s default return value.
     */
    private String generateReturn(Class<?> returnType) {
        StringBuilder returnValue = new StringBuilder(TAB_SPACE + TAB_SPACE + "return ");
        if (returnType == boolean.class) {
            returnValue.append("false");
        } else {
            returnValue.append(returnType.isPrimitive() ? "0" : "null");
        }
        returnValue.append(';');
        return returnValue.toString();
    }

    /**
     * Convert given string in case to escape unicode.
     *
     * @param in {@link String} data.
     * @return {@link String} converted string.
     */
    private static String escapeUnicode(String in) {
        StringBuilder res = new StringBuilder();
        for (char c : in.toCharArray()) {
            res.append(c >= 128 ? String.format("\\u%04X", (int) c) : c);
        }
        return res.toString();
    }
}
