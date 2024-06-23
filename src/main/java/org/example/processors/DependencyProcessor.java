package org.example.processors;

/**
 * Interface for processing the dependencies from maven output tree
 *
 * @author Krzysztof Wieconkowski
 */
@FunctionalInterface
public interface DependencyProcessor<R> {
    R process(String mavenInput);
}
