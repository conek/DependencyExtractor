package org.example.maven;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class responsible for invoking Maven commands
 *
 * @author Krzysztof Wieconkowski
 */
public class MavenInvoker {
    private static final String DEPENDENCY_TREE_GOAL = "dependency:tree";
    private static final String OUTPUT_TYPE = "outputType";
    private static final List<PathAndErrorMessage> DEFAULTS_MAVEN_PATH = List.of(
            new PathAndErrorMessage(Paths.get("binary", "apache-maven"), "Maven not in path (%s)"),
            new PathAndErrorMessage(Paths.get("apache-maven"), "Maven not in path (%s)"));

    private final Path mavenPathHome;
    private final OutputType outputType;


    public MavenInvoker(Path mavenPathHome, OutputType outputType) {
        Objects.requireNonNull(outputType, "outputType must not be null");
        if (OutputType.JSON != outputType) {
            throw new UnsupportedOperationException("Only JSON output types are supported.");
        }
        this.mavenPathHome = mavenPathHome;
        this.outputType = outputType;
    }

    public String run(Path mainProjectPomPath) throws MavenInvocationException {
        verifyIfPomExists(mainProjectPomPath);

        final StringBuilder consoleOutput = new StringBuilder();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(mainProjectPomPath.toFile());
        request.setProperties(new Properties()).getProperties().setProperty(OUTPUT_TYPE, outputType.toString());
        request.setNoTransferProgress(true);
        request.setBatchMode(true);
        request.setOutputHandler(line -> consoleOutput.append(line).append(System.lineSeparator()));
        request.addArg(DEPENDENCY_TREE_GOAL);

        List<PathAndErrorMessage> invocationPaths = new LinkedList<>();
        invocationPaths.add(new PathAndErrorMessage(mavenPathHome,
                "Maven not in: 1. User provided path (%s),%n " +
                        "2. System environment path,%n " +
                        "3. Application execution path"));
        invocationPaths.addAll(DEFAULTS_MAVEN_PATH);

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = null;
        for (PathAndErrorMessage invocationPath : invocationPaths) {
            try {
                request.setMavenHome(toFile(invocationPath.mavenHomePath));
                result = invoker.execute(request);
                break;
            } catch (Exception ex) {
                System.err.println(invocationPath.errorMessage + ". Will try other options.");
            }
        }

        if (result == null) {
            System.err.println("Maven not found, application will exit.");
            System.exit(1);
        } else if (result.getExitCode() == 0) {
            return consoleOutput.toString();
        }
        throw new MavenInvocationException(String.format("Maven command failed. Exit code: %d. Console output:%n%n%s", result.getExitCode(), consoleOutput));
    }

    private File toFile(Path mavenHomePath) {
        return mavenHomePath != null ? mavenHomePath.toFile() : null;
    }

    private void verifyIfPomExists(Path mainProjectPomPath) {
        Objects.requireNonNull(mainProjectPomPath, "mainProjectPomPath must not be null");
        if (!Files.isRegularFile(mainProjectPomPath, LinkOption.NOFOLLOW_LINKS)) {
            System.err.println(String.format("Provided path to pom.xml file doesn't exist or is not a file (%s)", mainProjectPomPath));
            System.exit(1);
        }
    }

    private record PathAndErrorMessage(Path mavenHomePath, String errorMessage) {
        PathAndErrorMessage {
            errorMessage = String.format(errorMessage, mavenHomePath);
        }
    }

    @SuppressWarnings("unused")
    public enum OutputType {
        TEXT("text"),
        DOT("dot"),
        GRAPHML("graphml"),
        TGF("tgf"),
        JSON("json");

        private final String output;

        OutputType(String output) {
            this.output = output;
        }

        public String toString() {
            return output;
        }
    }
}