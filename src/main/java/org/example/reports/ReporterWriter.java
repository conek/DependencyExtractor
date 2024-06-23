package org.example.reports;

import java.nio.file.Path;

/**
 * Interface responsible for storing the report containing dependencies
 *
 * @author Krzysztof Wieconkowski
 */
public interface ReporterWriter<T> {
    String toString(T report) throws Exception;
    void writeToFile(Path reportPath, T report) throws Exception;
}
