package info.kgeorgiy.ja.Andreev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;


/**
 * Class implementing {@link JarImpler}. Provides public methods to implement <code>.java</code>
 * files for classes extending or implementing given class of interface.
 *
 * @author Alex_Andrv
 */
public class Implementor implements JarImpler {
    //jar cfe info.kgeorgiy.ja.Andreev.implementor.jar info\kgeorgiy\ja\Andreev\implementor.Implementor info\*

    /**
     * This method convert all characters, that Unicode code unit bigger then 127 to Unicode escape format.
     * @param str input stream
     * @return a {@link String} representing unicode-escaped {@code arg}
     */
    private static String toUnicode(String str) {
        StringBuilder builder = new StringBuilder();
        for (char c : str.toCharArray()) {
            builder.append(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
        }
        return builder.toString();
    }

    /**
     * Create new {@link Implementor}
     */
    public Implementor() {
        super();
    }

    /**
     * Prints the required format of the input data and terminates the program.
     */
    private static void printUsage() {
        System.err.println("Usage: java [-jar] <full name> [<class name>|<interface name>] [path | path.jar]");
        System.exit(1);
    }

    /**
     * Provides a console interface for {@link Implementor}.
     * Has three options for starting, depending on {@code args}:
     * <ul>
     * <li>1-argument <code>className</code> creates <code>.java</code> file in the current directory.
     * Uses the method {@link #implement(Class, Path)} to create {@code .java}, where {@code className}
     * indicates the current directory.</li>
     * <li>2-argument {@code className OutputPath}
     *      <ul>
     *          <li>If Path end with .jar, then use method {@link #implementJar(Class, Path)}</li>
     *          <li>If Path not end with .jar, then use method {@link #implement(Class, Path)}</li>
     *      </ul>
     *      </li>
     * </ul>
     * All arguments must be correct and not-null. If some arguments are incorrect
     * or an error occurs in runtime an information message is printed and implementation is aborted.
     *
     * @param args command line arguments for application
     * @see #implement(Class, Path)
     * @see #implementJar(Class, Path)
     */
    public static void main(String[] args) {

        if (args == null || (args.length != 2 && args.length != 1)) {
            printUsage();
        }

        for (String arg : args) {
            if (arg == null) {
                printUsage();
            }
        }

        Implementor implementor = new Implementor();

        try {
            Class<?> token = Class.forName(args[0]);

            Path root = Path.of("");

            if (args.length == 2) {
                root = root.resolve(args[1]);
                if (args[1].endsWith(".jar")) {
                    implementor.implementJar(token, root);
                    return;
                }
            }
            implementor.implement(token, root);
        } catch (ClassNotFoundException e) {
            printUsage();
        } catch (ImplerException e) {
            System.err.println("An error occurred during implementation: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Produces code implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <var>root</var> directory and have correct file name. For example, the implementation of the
     * interface {@link List} should go to <var>$root/java/util/ListImpl.java</var>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be
     *                         generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        validateInputParameters(token, root);
        new GeneratorImplementationClass(token).writeClass(root);
    }


    /**
     * Validates input values. If some argument is not valid, it is thrown {@link ImplerException}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be generated.
     */
    private void validateInputParameters(Class<?> token, Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token is null");
        }
        if (root == null) {
            throw new ImplerException("Path is null");
        }
        if (token.isPrimitive() || token.isArray() || token == Enum.class
                || Modifier.isPrivate(token.getModifiers()) || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Incorrect class token");
        }
    }


    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        validateInputParameters(token, jarFile);

        final Path tempDir = Path.of(".tempDir");
        final Path tmpSourceDir = tempDir.resolve("src");
        final Path tmpOutDir = tempDir.resolve("out");

        implement(token, tmpSourceDir);

        final String className = Path.of(token.getPackageName()
                .replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl").toString();
        compile(token, tmpSourceDir, tmpOutDir, className);
        createJar(jarFile, tmpOutDir, className);

        try {
            clean(tempDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    /**
     * Write jar file.
     * @param jarFile {@link Path} to jar file
     * @param tmpOutDir output dir
     * @param className source class
     * @throws ImplerException when can't create Jar
     */
    private void createJar(Path jarFile, Path tmpOutDir, String className) throws ImplerException {
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))) {
            writer.putNextEntry(new ZipEntry(className.replace(File.separatorChar, '/') + ".class"));
            Files.copy(Paths.get(tmpOutDir.resolve(className).toString() + ".class"), writer);
        } catch (IOException e) {
            throw new ImplerException("Failed to write to jar file", e);
        }
    }

    /**
     *  Compile input class
     * @param token type token to create implementation for.
     * @param tmpSourceDir directory with source
     * @param tmpOutDir output directory
     * @param className class name
     * @throws ImplerException when we can't compile file.
     */
    private void compile(Class<?> token, Path tmpSourceDir, Path tmpOutDir, String className) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Can't get compiler");
        }
        try {
            if (compiler.run(null, null, null, "-d", tmpOutDir.toString(), "-cp",
                    tmpSourceDir.toString() + File.pathSeparator +
                            Path.of(token.getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI()).toString() + File.pathSeparator +
                            System.getProperty("java.class.path"),
                    tmpSourceDir.resolve(className).toString() + ".java") != 0) {
                throw new ImplerException("Failed to compile files");
            }
        } catch (URISyntaxException e) {
            throw new ImplerException("URISyntaxException", e);
        }
    }

    /**
     * Recursively remove all folders and files from {@code root} directory.
     *
     * @param root root directory
     * @throws IOException when clean cannot delete dir.
     */
    private static void clean(final Path root) throws IOException {
        if (Files.exists(root)) {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    /**
     * Inner class encapsulating {@code .java} file creation that implement / extends {@code token}.
     * Has one public constructor {@link #GeneratorImplementationClass(Class)} with the required token parameter.
     * And the public method {@link #writeClass(Path)} that creates this file in path {@code path}.
     */
    public static class GeneratorImplementationClass  {

        /**
         * Standard Charsets for generated file
         */
        private static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

        /**
         * File type for generated <code>.java</code> files
         */
        private static final String JAVA = "java";

        /**
         * Endings of generated file names
         */
        private static final String IMPL = "Impl";

        /**
         * Space-type indentation for generated <code>.java</code> files
         */
        private static final String SPACE = " ";

        /**
         * Tabulation-type indentation for generated <code>.java</code> files.
         */
        private static final String TAB = "    ";

        /**
         * {@code public} access modifier for generated methods
         */
        private static final String PUBLIC = "public";

        /**
         * {@code protected} access modifier for generated methods
         */
        private static final String PROTECTED = "protected";

        /**
         * {@code private} access modifier for generated methods
         */
        private static final String PRIVATE = "private";

        /**
         * private String constant variable for generated methods
         */
        private static final String CLASS = "class";

        /**
         * private String constant variable for generated methods
         */
        private static final String EXTENDS = "extends";

        /**
         * private String constant variable for generated methods
         */
        private static final String IMPLEMENTS = "implements";

        /**
         * private String constant variable for generated methods
         */
        private static final String RETURN = "return";

        /**
         * Empty Sting constant
         */
        private static final String EMPTY = "";

        /**
         * Null Sting constant
         */
        private static final String NULL = "null";

        /**
         * Zero Sting constant
         */
        private static final String ZERO = "0";

        /**
         * False Sting constant
         */
        private static final String FALSE = "false";

        /**
         * End of line character for generated {@code .java} files.
         */
        private static final String COMMA = ",";

        /**
         * {@code package} String constant
         */
        private static final String PACKAGE = "package";

        /**
         * End of line character for generated <code>.java</code> files.
         */
        private static final String SEMICOLON = ";";

        /**
         * {@code super} String constant
         */
        private static final String SUPER = "super";

        /**
         * {@code throws} String constant
         */
        private static final String THROWS = "throws";

        /**
         * Dot String constant
         */
        private static final char DOT = '.';

        /**
         * New line for generated methods
         */
        private static final String NEW_LINE = System.lineSeparator();

        /**
         * Type token to create implementation for.
         */
        final private Class<?> token;

        /**
         * Set of overriding methods
         */
        private Set<MethodsWithEquals> methods;


        /**
         * Initializes a newly created GeneratorImplementationClass object
         *
         * @param token type token to create implementation for.
         */
        public GeneratorImplementationClass(Class<?> token) {
            this.token = token;
            this.methods = new HashSet<>();
        }

        /**
         * Class wrapper for Method. Provides custom equality check for {@link Method}.
         */
        private static class MethodsWithEquals {

            /**
             * Inner wrapped {@link Method} instance.
             */
            private final Method method;

            /**
             * Wrapping constructor. Creates new instance of {@link MethodsWithEquals} with wrapped {@link Method} inside.
             * @param method instance of {@link Method} class to be wrapped inside
             */
            MethodsWithEquals(Method method) {
                this.method = method;
            }

            /**
             * Getter for wrapped instance of {@link Method} class.
             *
             * @return wrapped {@link #method}
             */
            public Method getMethod() {
                return method;
            }

            /**
             * Indicates whether some other object is "equal to" this one.
             * Object is booth equal if {@code obj} is an instance of {@link MethodsWithEquals}
             * and has a wrapped {@link #method} inside with same name, parameter types and return type.
             *
             * @param obj object to compare with wrapped {@link #method}
             * @return <code>true</code> if objects are equal, <code>false</code> otherwise
             * @see java.lang.Object#equals(java.lang.Object)
             * @see java.lang.System#identityHashCode
             */
            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (obj instanceof MethodsWithEquals) {
                    MethodsWithEquals other = (MethodsWithEquals) obj;
                    return method.getReturnType().equals(other.method.getReturnType())
                            && method.getName().equals(other.method.getName())
                            && Arrays.equals(method.getParameterTypes(), other.method.getParameterTypes());
                }
                return false;
            }

            /**
             * Returns a hash code value for the object.
             * The general contract of {@code hashCode} is:
             * <ul>
             * <li>Whenever it is invoked on the same object more than once during
             *     an execution of a Java application, the {@code hashCode} method
             *     return the same integer.
             * <li>If two objects are equal according to the {@code equals(Object)}
             *     method, then calling the {@code hashCode} method on each of
             *     the two object return the same integer result.
             * <li>It is <em>not</em> required that if two objects are unequal
             *     according to the {@link #equals(java.lang.Object)}
             *     method, then calling the {@code hashCode} method on each of the
             *     two objects must produce distinct integer results.  However, the
             *     programmer should be aware that producing distinct integer results
             *     for unequal objects may improve the performance of hash tables.
             * </ul>
             * <p>
             *
             * @return a hash code value for this object.
             * @see java.lang.Object#equals(java.lang.Object)
             * @see java.lang.System#identityHashCode
             */
            @Override
            public int hashCode() {
                return (Arrays.hashCode(method.getParameterTypes())
                        ^ method.getReturnType().hashCode())
                        ^ method.getName().hashCode();
            }


        }

        /**
         * Returns a {@link String} representation of the default value for a class {@code token}
         * @param token token some method return value type
         * @return a {@link String} representing default return value of this type
         */
        private static String getDefaultValue(Class<?> token) {
            if (token.equals(boolean.class)) {
                return FALSE;
            } else if (token.equals(void.class)) {
                return EMPTY;
            } else if (token.isPrimitive()) {
                return ZERO;
            }
            return NULL;
        }

        /**
         * Returns a {@link String} representation of the access {@link Executable} modifiers.
         * @param executable token some method return value type
         * @return a {@link String} representing access modifiers
         */
        private String getModifier(Executable executable) {
            int modifier = executable.getModifiers();
            if (Modifier.isProtected(modifier)) {
                return PROTECTED;
            } else if (Modifier.isPublic(modifier)) {
                return PUBLIC;
            } else if (Modifier.isPrivate(modifier)) {
                return PRIVATE;
            }
            return EMPTY;
        }

        /**
         * Returns a {@link String} representation of {@link Executable} argument list with [types](optional) and names.
         * @param executable an instance of {@link Executable}
         * @param withType indicate whether to return variable type names.
         * @return {@link String}: [typeVar1] var1, [typeVar2] var2, ...
         */
        private String getParameters(Executable executable, boolean withType) {
            StringBuilder ans = new StringBuilder();
            Parameter[] paramArr = executable.getParameters();
            for (int i = 0; i < paramArr.length; i++) {
                ans.append(withType ? paramArr[i].getType().getCanonicalName() : EMPTY)
                        .append(SPACE)
                        .append(paramArr[i].getName());
                if (i + 1 != paramArr.length) {
                    ans.append(COMMA)
                            .append(SPACE);
                }
            }
            return ans.toString();
        }


        /**
         * Returns a {@link String} representation return type {@link Executable}.
         * <ul>
         *     <li>If {@code executable instanceof {@link Constructor}}, return {@link #EMPTY} {@link String} </li>
         *     <li>If {@code executable instanceof {@link Method}}, return {@link String} representation return type </li>
         * </ul>
         * @param executable an instance of {@link Executable}
         * @return {@link String} representation return type
         */
        private String getReturnType(Executable executable) {
            if (!(executable instanceof Method)) {
                return EMPTY;
            }
            Method method = (Method) executable;
            return method.getReturnType().getCanonicalName();
        }

        /**
         * Return {@link String} representation package name of {@link #token}. Returns an empty string
         * if the package does not exist.
         * @return {@link String} representation package name
         * @see Class#getPackageName()
         */
        private String getPackageName() {
            return token.getPackageName();
        }

        /**
         * Return {@link String} representation name class of {@link #token}.
         * @return {@link String} representation name class
         * @see Class#getPackageName()
         */
        private String getSimpleName() {
            return token.getSimpleName();
        }

        /**
         * Return {@link String} representation name class implement / extends {@link #token}.
         * @return {@link String} representation name class ends with {@link #IMPL}
         * @see Class#getSimpleName()
         */
        private String getClassName() {
            return getSimpleName() + IMPL;
        }

        /**
         * Return {@link String} representation {@link Executable} body.
         * <ul>
         *     <li>If {@code executable instanceof {@link Constructor}},
         *          return {@link String} representation super with parameter</li>
         *     <li>If {@code executable instanceof {@link Method}},
         *          return {@link String}  representation return with default value</li>
         * </ul>
         *
         * @param executable an instance of {@link Executable}
         * @return {@link String} representation {@link Executable} body
         * @see #getParameters(Executable, boolean)
         * @see #getDefaultValue(Class)
         */
        private String getBodyExecutable(Executable executable) {
            if (executable instanceof Constructor) {
                return SUPER + "(" + getParameters(executable, false) + ")";
            } else {
                Method method = (Method) executable;
                return RETURN + SPACE + getDefaultValue(method.getReturnType());
            }
        }

        /**
         * Return {@link String} representation {@link Executable} name.
         * @param executable executable an instance of {@link Executable}
         * @return {@link String} representation {@link Executable} name
         * @see #getClassName()
         * @see Executable#getName()
         */
        private String getName(Executable executable) {
            return executable instanceof Constructor ? getClassName() : executable.getName();
        }

        /**
         * Return {@link String} representation {@link Executable} throws classes.
         * <ul>
         *     <li>If {@code executable instanceof {@link Constructor}},
         *          return {@link String} representation trows classes</li>
         *     <li>If {@code executable instanceof {@link Method}},
         *          return {@link #EMPTY} {@link String}</li>
         * </ul>
         * @param executable executable an instance of {@link Executable}
         * @return {@code {@link #THROWS}} + {@link String} representation {@link Executable} throws classes.
         * @see Executable#getExceptionTypes()
         */
        private String getThrows(Executable executable) {
            if (executable instanceof Method) {
                return EMPTY;
            }
            Class<?>[] arrThrows = executable.getExceptionTypes();
            if (arrThrows.length == 0)
                return EMPTY;
            StringBuilder exceptions = new StringBuilder();
            for (int i = 0; i < arrThrows.length; i++) {
                exceptions.append(SPACE)
                        .append(arrThrows[i].getCanonicalName());
                if (i + 1 != arrThrows.length) {
                    exceptions.append(COMMA);
                }
            }
            return THROWS + exceptions;
        }


        /**
         * Return {@link String} representation {@link Executable}.
         * @param executable executable an instance of {@link Executable}
         * @return {@link String} representation {@link Executable}
         * @see #getModifier(Executable)
         * @see #getReturnType(Executable)
         * @see #getName(Executable)
         * @see #getParameters(Executable, boolean)
         * @see #getThrows(Executable)
         */
        private String ExecutableToString(Executable executable) {
            return getModifier(executable) + SPACE + getReturnType(executable) + SPACE + getName(executable) +
                    "(" + getParameters(executable, true) + ")" + SPACE + getThrows(executable) + "{" +
                    NEW_LINE + TAB + getBodyExecutable(executable) + SEMICOLON + NEW_LINE + "}" + NEW_LINE;
        }

        /**
         * Return {@link String} representation class signature
         * @return {@link String} representation class signature
         * @see #getClassName()
         */
        private String getClassSignature() {
            return PUBLIC + SPACE + CLASS + SPACE + getClassName()
                    + SPACE + (token.isInterface() ? IMPLEMENTS : EXTENDS) + SPACE + token.getCanonicalName();
        }

        /**
         * Generate {@link Set<MethodsWithEquals>} with {@link MethodsWithEquals}.
         * if {@code abstractMethods} equals {@code true}, then return {@link Set<MethodsWithEquals>} with all declared
         * abstract methods
         * if {@code abstractMethods} equals {@code false}, then return {@link Set<MethodsWithEquals>} with all declared
         * not abstract methods
         * @param token type token
         * @param abstractMethods class type indicator
         * @return {@link Set<MethodsWithEquals>} with all overriding methods.
         */
        private static Set<MethodsWithEquals> getMethods(Class<?> token, boolean abstractMethods) {
            return Arrays.stream(token.getDeclaredMethods())
                    .filter(method -> abstractMethods == Modifier.isAbstract(method.getModifiers()))
                    .map(MethodsWithEquals::new)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        /**
         * Delete overriding method from {@link #methods}
         * @param token type token
         * @param methods {@link Set<MethodsWithEquals>}
         * @return {@link Set<MethodsWithEquals>} without overriding methods
         */
        private Set<MethodsWithEquals> deleteOverrideMethods(Class<?> token, Set<MethodsWithEquals> methods) {
            Set<MethodsWithEquals> overrideMethods = getMethods(token, false);
            return methods.stream().filter(s -> !overrideMethods.contains(s)).collect(Collectors.toCollection(HashSet::new));
        }


        /**
         * Recursive walk all superclass and implementing interfaces
         * @param token type token
         * @return {@link Set<MethodsWithEquals>} that mast override
         */
        private Set<MethodsWithEquals> recursiveWalkMethods(Class<?> token) {
            if (!Modifier.isAbstract(token.getModifiers())) {
                return new HashSet<>();
            }
            Class<?> superClass = token.getSuperclass();
            if (superClass != null) {
                methods.addAll(recursiveWalkMethods(superClass));
            }
            for (Class<?> intr : token.getInterfaces()) {
                methods.addAll(recursiveWalkMethods(intr));
            }
            if (Modifier.isAbstract(token.getModifiers())) {
                methods = deleteOverrideMethods(token, methods);
            }
            methods.addAll(getMethods(token, true));
            return methods;
        }

        /**
         * Create directories, if it doesn't exist
         * @param dir directories, that should create
         * @throws ImplerException when can't create this dir
         */
        private void createDirectories(Path dir) throws ImplerException {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories", e);
            }
        }

        /**
         * Write class to the root dir
         * @param root root dir of class
         * @throws ImplerException when can't write class
         */
        public void writeClass(Path root) throws ImplerException {
            Path dir = root.resolve(getPackageName().replace(DOT, File.separatorChar));
            Path path = dir.resolve(getClassName() + DOT + JAVA);
            createDirectories(dir);
            methods = recursiveWalkMethods(token);
            try (BufferedWriter writer = Files.newBufferedWriter(path, FILE_CHARSET)) {
                WriterImplementationClass writerImplementationClass = new WriterImplementationClass(writer);
                writerImplementationClass.writeImplementationClass();
            } catch (IOException e) {
                throw new ImplerException("Unable to write to output file", e);
            }
        }

        /**
         * Inner class that write class to file
         */
        private class WriterImplementationClass {

            /**
             *  Output stream
             */
            private final BufferedWriter writer;


            /**
             * Initializes a newly created {@link WriterImplementationClass} object
             * @param writer where should write
             */
            WriterImplementationClass(BufferedWriter writer) {
                this.writer = writer;
            }

            /**
             * write line
             * @param line input line
             * @throws IOException where we can't write
             */
            private void writeLine(String line) throws IOException {
                writer.write(toUnicode(line + NEW_LINE));
            }

            /**
             * Write implementation class
             * @throws IOException where we can't write
             * @throws ImplerException where we can't write
             */
            public void writeImplementationClass() throws IOException, ImplerException {
                writePackageName();
                writeBody();
            }

            /**
             * write package name
             * @throws IOException where we can't write package
             */
            private void writePackageName() throws IOException {
                String packageName = getPackageName();
                if (!packageName.isEmpty()) {
                    writeLine(PACKAGE + SPACE + packageName + SEMICOLON + NEW_LINE);
                }
            }


            /**
             * write methods
             * @throws IOException where we can't write methods
             */
            private void writeMethods() throws IOException {
                for (MethodsWithEquals method : methods) {
                    writeLine(ExecutableToString(method.getMethod()));
                }
            }

            /**
             * write constructors
             * @throws IOException where we can't write constructors
             * @throws ImplerException where we can't
             */
            private void writeConstructors() throws IOException, ImplerException {
                for (Constructor<?> constructor : token.getDeclaredConstructors()) {
                    if (!Modifier.isPrivate(constructor.getModifiers())) {
                        writeLine(ExecutableToString(constructor));
                        return;
                    }
                }
                throw new ImplerException("No non-private constructors");
            }

            /**
             * write body class
             * @throws IOException where we can't write body class
             * @throws ImplerException where we can't write body class
             */
            private void writeBody() throws IOException, ImplerException {
                writeLine(getClassSignature() + SPACE + "{" + NEW_LINE);
                if (!token.isInterface()) {
                    writeConstructors();
                }
                writeMethods();
                writeLine("}");
            }
        }
    }
}
