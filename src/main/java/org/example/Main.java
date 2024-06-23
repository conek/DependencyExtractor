package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.example.formats.DefaultJsonMessageFormat;
import org.example.processors.DependencyProcessor;
import org.example.reports.ReporterWriter;
import org.example.maven.MavenInvoker;
import org.example.processors.JsonDependencyProcessor;
import org.example.reports.JsonReporterWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.example.maven.MavenInvoker.OutputType;

/**
 * The Main class for the Dependency Extractor application.
 *
 * Project Objective:
 * This application visualizes dependencies in source code, specifically targeting projects using Maven. It extracts dependencies from both single and modular projects of any complexity. The implemented solution guarantees correct results while being easy to understand, maintain, and develop.
 *
 * Implemented Solution:
 * Instead of duplicating Maven's logic, this application works alongside Maven. By using the Maven command `dependency:tree` with a specified `outputType` (e.g., `json`), the application ensures reliable and accurate dependency trees.
 *
 * Advantages:
 * - Reversed Responsibility: Maven provides the dependency tree, making it the primary source of truth.
 * - Scalable and Flexible: Handles projects of any complexity, ensuring correct identification and reporting of dependencies.
 * - Resistance to Change: The application remains stable and correct even if Maven's output format changes.
 * - Simplified Maintenance: Focuses on data parsing and presentation, making maintenance easier.
 *
 * Disadvantages:
 * - Dependency on Maven: Requires Maven to be available on the system. It can be accessed via environment variables, a user-designated path, or specific directories relative to the application.
 * - Potential Access Issues: Requires proper authentication to access private repositories when retrieving dependencies.
 *
 * Application Flow:
 * 1. Invokes Maven to analyze project dependencies.
 * 2. Parses the obtained information into an internal format.
 * 3. Formats and presents the data in a user-friendly format, such as JSON.
 *
 * @author Krzysztof Wieconkowski
 */
public class Main {
    private final MavenInvoker mavenInvoker;
    private final DependencyProcessor<ObjectNode> jsonDependencyProcessor;
    private final ReporterWriter<ObjectNode> reporterWriter;

    public static void main(String[] args) throws Exception {
        final Options options = getOptions();

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();
        final CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            String output = cmd.getOptionValue("output");
            boolean outputToConsole = "console".equalsIgnoreCase(output);

            if (!"console".equalsIgnoreCase(output) && !"file".equalsIgnoreCase(output)) {
                throw new ParseException("Output must be either 'console' or 'file'.");
            }

            if (!outputToConsole && !cmd.hasOption("file")) {
                throw new ParseException("File path to report is required when output is set to 'file'.");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("dependencyExtractor", options);

            System.exit(1);
            return;
        }

        final Path pomPath = Paths.get(cmd.getOptionValue("pom"));
        final String output = cmd.getOptionValue("output");
        final boolean outputToConsole = "console".equalsIgnoreCase(output);
        final Path reportPath = outputToConsole ? null : Paths.get(cmd.getOptionValue("file"));
        final Path mavenHomePath = cmd.hasOption("mavenHome") ? Paths.get(cmd.getOptionValue("mavenHome")) : null;

        final ObjectMapper objectMapper = new ObjectMapper();
        new Main(new MavenInvoker(mavenHomePath, OutputType.JSON),
                new JsonDependencyProcessor(new DefaultJsonMessageFormat(objectMapper)),
                new JsonReporterWriter(objectMapper))
                .run(pomPath, reportPath, outputToConsole);
    }

    private static Options getOptions() {
        Options options = new Options();

        Option pomOption = new Option("p", "pom", true, "Project pom path (required)");
        pomOption.setRequired(true);
        options.addOption(pomOption);

        Option outputOption = new Option("o", "output", true, "Output destination: 'console' or 'file' (required)");
        outputOption.setRequired(true);
        options.addOption(outputOption);

        Option filePathOption = new Option("f", "file", true, "Path to where generate output report (required if output is 'file')");
        filePathOption.setRequired(false);
        options.addOption(filePathOption);

        Option mavenHomeOption = new Option("m", "mavenHome", true, "Path to Maven home (optional)");
        mavenHomeOption.setRequired(false);
        options.addOption(mavenHomeOption);
        return options;
    }

    public Main(MavenInvoker mavenInvoker, DependencyProcessor<ObjectNode> jsonDependencyProcessor, ReporterWriter<ObjectNode> reporterWriter) {
        this.mavenInvoker = mavenInvoker;
        this.jsonDependencyProcessor = jsonDependencyProcessor;
        this.reporterWriter = reporterWriter;
    }

    private void run(Path pomPath, Path reportPath, boolean outputToConsole) throws Exception {
        System.out.println("Processing please wait...");
        if (outputToConsole) {
            System.out.println(reporterWriter.toString(jsonDependencyProcessor.process(mavenInvoker.run(pomPath))));
        } else {
            reporterWriter.writeToFile(reportPath, jsonDependencyProcessor.process(mavenInvoker.run(pomPath)));
            System.out.println("Report generated: " + reportPath);
        }
    }
}