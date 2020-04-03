package ru.ifmo.rain.busyuk.jarImplementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Implementor implements info.kgeorgiy.java.advanced.implementor.Impler {
    private static final String TAB_SPACE = "    ";

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
            writer.write(escapeUnicode(generatePackage(token)));
            writer.write(escapeUnicode(generateTitle(token)));
            writer.write(escapeUnicode(" {" + System.lineSeparator()));

            for (Method method : token.getMethods()) {
                if (method.isDefault()) continue;

                int modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    modifiers -= Modifier.ABSTRACT;
                }
                if (Modifier.isTransient(modifiers)) {
                    modifiers -= Modifier.TRANSIENT;
                }
                writer.write(escapeUnicode(generateAnnotations(method)));
                writer.write(escapeUnicode(generateModifiers(modifiers, method)));
                writer.write(escapeUnicode(generateArguments(method)));
                writer.write(escapeUnicode(generateExceptions(method)));
                writer.write(escapeUnicode(generateInnerCode(method)));
            }
            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException("Problems with output" + e.getMessage(), e);
        }
    }

    protected Path getPackagePath(final Class<?> token) {
        return Path.of(token.getPackageName().replace(".", File.separator));
    }

    private String generatePackage(Class<?> token) {
        return token.getPackage().getName() == null ? "" : ("package " + token.getPackage().getName() + ";"
                + System.lineSeparator() + System.lineSeparator());
    }

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

    private String generateAnnotations(Method method) {
        return (Arrays.stream(method.getAnnotations()).map(annotation -> "@" + annotation.annotationType()
                .getCanonicalName()).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator());
    }

    private String generateModifiers(int modifiers, Method method) {
        return (System.lineSeparator() + TAB_SPACE + Modifier.toString(modifiers) + " " +
                method.getReturnType().getCanonicalName() + " " + method.getName());
    }

    private String generateArguments(Method method) {
        return ('(' + Arrays.stream(method.getParameters()).map(parameter -> parameter.getType().getCanonicalName() +
                " " + parameter.getName()).collect(Collectors.joining(", ")) + ')');
    }

    private String generateExceptions(Method method) {
        return method.getExceptionTypes().length == 0 ? "" : ("throws " + Arrays.stream(method.getExceptionTypes()).
                map(Class::getCanonicalName).collect(Collectors.joining(", ")));
    }

    private String generateInnerCode(Method method) {
        StringBuilder innerCode = new StringBuilder(" {" + System.lineSeparator());
        if (method.getReturnType() != void.class) innerCode.append(generateReturn(method.getReturnType()));
        innerCode.append(System.lineSeparator()).append(TAB_SPACE).append('}').append(System.lineSeparator());
        return innerCode.toString();
    }

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

    private static String escapeUnicode(String in) {
        StringBuilder res = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c >= 128) {
                res.append(String.format("\\u%04X", (int) c));
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }
}
