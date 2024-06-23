package org.example.formats;

import java.util.Iterator;

/**
 * Interface that combine aggregated dependencies by pom to one format
 *
 * @author Krzysztof Wieconkowski
 */
@FunctionalInterface
public interface MessageFormat<T, R> {
    R processAndAggregate(Iterator<T> dependenciesGroupedByPomIterator);
}
