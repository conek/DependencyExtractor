package org.example.formats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Class that combine aggregated dependencies in json by pom to another json format
 *
 * @author Krzysztof Wieconkowski
 */
public class DefaultJsonMessageFormat implements JsonMessageFormat {
    private final ObjectMapper objectMapper;

    public DefaultJsonMessageFormat(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.objectMapper = objectMapper;
    }

    @Override
    public ObjectNode processAndAggregate(Iterator<Map.Entry<String, String>> jsonPerPomIterator) {
        final ObjectNode fullDependenciesMap = objectMapper.createObjectNode();
        final ArrayNode arrayNode = fullDependenciesMap.putArray("dependenciesForMavenPoms");
        jsonPerPomIterator
                .forEachRemaining(entry -> createDependenciesPerPom(arrayNode.addObject(), entry.getKey(), entry.getValue()));

        return fullDependenciesMap;
    }

    private void createDependenciesPerPom(ObjectNode nodeToPopulate, String pomPath, String json) {
        try {
            nodeToPopulate.put("mavenPomPath", pomPath.replace("\\", "/"));
            nodeToPopulate.set("dependencies", objectMapper.readTree(json));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Error processing JSON for POM path: " + pomPath, ex);
        }
    }
}