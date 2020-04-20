package ru.ifmo.rain.busyuk.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Incorrect type provided to implemet");
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

            if (!token.isInterface()) {
                boolean found = false;
                for (Constructor constructor : token.getDeclaredConstructors()) {
                    found = found || generateConstructor(constructor, token, data);
                }
                if (!found) {
                    throw new ImplerException("All constructors are private!");
                }
            }

            for (Method method : generateAllMethods(token)) {
                if (method.isDefault()) continue;

                int modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    modifiers -= Modifier.ABSTRACT;
                }
                if (Modifier.isTransient(modifiers)) {
                    modifiers -= Modifier.TRANSIENT;
                }
                data.add(generateModifiers(modifiers, method));
                data.add(generateArguments(method.getParameters()));
                data.add(generateExceptions(method.getExceptionTypes()));
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
     * Returns {@link Boolean} weather it adds information about {@link Constructor} of token to {@link List} data
     *
     * @param constructor {@link Constructor} token`s constructor
     * @param token       {@link Class} object
     * @param data        {@link List} generated code collector
     * @return true if constructor wasn`t private and code was added to data
     */
    private boolean generateConstructor(Constructor constructor, Class<?> token, List<String> data) {
        if (Modifier.isPrivate(constructor.getModifiers())) {
            return false;
        }
        StringBuilder result = new StringBuilder();
        StringBuilder paramsOfConstructor = new StringBuilder();
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            if (i == 0) {
                paramsOfConstructor.append("X").append(i);
            } else {
                paramsOfConstructor.append(", " + "X").append(i);
            }
        }
        result.append(TAB_SPACE + "public" + " ").append(token.getSimpleName()).append("Impl")
                .append(generateArguments(constructor.getParameters()))
                .append(generateExceptions(constructor.getExceptionTypes())).append(" ")
                .append("{").append(System.lineSeparator());
        result.append(TAB_SPACE + TAB_SPACE + "super" + "(")
                .append(paramsOfConstructor).append(");").append(System.lineSeparator());
        result.append(TAB_SPACE + "}").append(System.lineSeparator()).append(System.lineSeparator());
        data.add(result.toString());
        return true;
    }

    /**
     * Returns declaration of the class.
     *
     * @param token {@link Class} object
     * @return {@link String} representing class`s title.
     * @throws ImplerException if no implementation could be made.
     */
    private String generateTitle(Class<?> token) throws ImplerException {
        String className = token.getCanonicalName();
        String implerName = token.getSimpleName() + "Impl";
        int mod = token.getModifiers();
        String originClass = (token.isInterface() ? "implements" : "extends");
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

        return (String.format("%s class %s %s %s", Modifier.toString(mod), implerName, originClass, className));
    }

    /**
     * Returns {@link List} of {@link Method} that token has or extends
     *
     * @param token {@link Class} object
     * @return all methods that provide given token
     */
    private List<Method> generateAllMethods(final Class<?> token) {
        List<Class<?>> list = new ArrayList<>();
        list.add(token);

        Set<Class<?>> visited = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            Class<?> current = list.get(i);
            for (Class<?> interfaceToken : current.getInterfaces()) {
                if (!visited.contains(interfaceToken)) {
                    list.add(interfaceToken);
                    visited.add(interfaceToken);
                }
            }

            Class<?> base = current.getSuperclass();
            if (base != null) {
                list.add(base);
                visited.add(base);
            }
        }

        List<Method> result = new ArrayList<>();

        for (final Class<?> current : list) {
            Arrays.stream(current.getDeclaredMethods())
                    .filter(method -> {
                        int mod = method.getModifiers();
                        return !Modifier.isPrivate(mod)
                                && (Modifier.isPublic(mod)
                                || Modifier.isProtected(mod)
                                || method.getDeclaringClass().getPackage().equals(token.getPackage()));
                    })
                    .collect(Collectors.toCollection(() -> result));
        }

        Collection<Method> distinct = result.stream()
                .collect(Collectors.toMap(
                        method -> method.getName() + Arrays.toString(method.getParameterTypes()),
                        method -> method,
                        (lhs, rhs) -> lhs))
                .values();

        return distinct.stream()
                .filter(method -> {
                    int mod = method.getModifiers();
                    return Modifier.isAbstract(mod);
                }).collect(Collectors.toList());
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
     * @param parameters {@link Parameter} current method.
     * @return {@link String} representing list of method`s arguments.
     */
    private String generateArguments(Parameter[] parameters) {
        StringBuilder tmp = new StringBuilder("( ");
        for (int i = 0; i < parameters.length; i++) {
            tmp.append(parameters[i].getType().getCanonicalName()).append(" ").append("X")
                    .append(i).append((i + 1 == parameters.length) ? "" : "," + " ");
        }
        tmp.append(" )");
        return tmp.toString();
    }

    /**
     * Returns exceptions that method could throw.
     *
     * @param exceptions {@link Method} current method.
     * @return {@link String} representing list of exceptions that could be.
     */
    private String generateExceptions(Class<?>[] exceptions) {
        StringBuilder tmp = new StringBuilder();
        if (exceptions.length > 0) {
            tmp.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                tmp.append(exceptions[i].getCanonicalName()).append((i + 1 == exceptions.length ? "" : ", "));
            }
        }
        return tmp.toString();
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
