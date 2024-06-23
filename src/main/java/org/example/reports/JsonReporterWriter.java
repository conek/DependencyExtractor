package org.example.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Reporter writer class specialized working with json format
 *
 * @author Krzysztof Wieconkowski
 */
public class JsonReporterWriter implements ReporterWriter<ObjectNode> {
    private final ObjectWriter objectWriter;

    public JsonReporterWriter(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public String toString(ObjectNode report) throws Exception {
        return objectWriter.writeValueAsString(report);
    }

    @Override
    public void writeToFile(Path reportPath, ObjectNode report) throws Exception {
        objectWriter.writeValue(reportPath.toFile(), report);
    }
}