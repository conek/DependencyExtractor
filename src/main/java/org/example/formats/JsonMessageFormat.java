package org.example.formats;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Interface that combine aggregated dependencies in json by pom to another json format
 *
 * @author Krzysztof Wieconkowski
 */
@FunctionalInterface
public interface JsonMessageFormat extends MessageFormat<Map.Entry<String, String>, ObjectNode> { }