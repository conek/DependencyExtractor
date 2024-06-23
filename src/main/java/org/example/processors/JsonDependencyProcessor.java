package org.example.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.formats.JsonMessageFormat;

import java.util.*;

/**
 * Class specialized with processing the dependencies from maven output tree in json
 *
 * @author Krzysztof Wieconkowski
 */
public class JsonDependencyProcessor implements DependencyProcessor<ObjectNode> {
    private static final State MAVEN_LINE_STATE = new MavenLineState();
    private static final State JSON_LINE_STATE = new JsonLineState();

    private final JsonMessageFormat messageFormat;

    public JsonDependencyProcessor(JsonMessageFormat messageFormat) {
        Objects.requireNonNull(messageFormat, "jsonMessageFormat must not be null");
        this.messageFormat = messageFormat;
    }

    public ObjectNode process(String mavenInput) {
        TaskContext taskContext = new TaskContext();
        State currentState = MAVEN_LINE_STATE;

        for (String line : extractToLines(mavenInput)) {
            currentState = currentState.handleLine(line.trim(), taskContext);
        }
        return messageFormat.processAndAggregate(taskContext.getJsonPerPomIterator());
    }

    private static List<String> extractToLines(String mavenInput) {
        return removeInfoPrefix(mavenInput).lines().toList();
    }

    private static String removeInfoPrefix(String mavenInput) {
        return mavenInput.replace("[INFO]", "");
    }


    private static class TaskContext {
        private final Map<String, String> jsons;
        private final StringBuilder jsonInProgress;
        private String currentPomPath;
        private int currentLevel;

        private TaskContext() {
            this.jsons = new LinkedHashMap<>();
            this.jsonInProgress = new StringBuilder();
            this.currentPomPath = "";
            this.currentLevel = 0;
        }

        public boolean addJsonSingleLine(String jsonPart) {
            jsonInProgress.append(jsonPart).append(System.lineSeparator());
            updateJsonLevel(jsonPart);
            if (isCompletedJson()) {
                completeCurrentJson();
                return true;
            }
            return false;
        }

        public void setPomPath(String pomPath) {
            currentPomPath = pomPath;
        }

        private void updateJsonLevel(String jsonPart) {
            for (char ch : jsonPart.toCharArray()) {
                if (ch == '{') {
                    currentLevel++;
                } else if (ch == '}') {
                    currentLevel--;
                }
            }
        }

        private boolean isCompletedJson() {
            return currentLevel == 0;
        }

        private void completeCurrentJson() {
            jsons.put(currentPomPath, jsonInProgress.toString());
            jsonInProgress.setLength(0);
            currentPomPath = "";
        }

        public Iterator<Map.Entry<String, String>> getJsonPerPomIterator() {
            return jsons.entrySet().iterator();
        }
    }

    private interface State {
        State handleLine(String line, TaskContext taskContext);
    }

    private static class MavenLineState implements State {
        private static final String POM_PREFIX = "from ";
        private static final String JSON_OPEN_BRACKET = "{";

        @Override
        public State handleLine(String line, TaskContext taskContext) {
            if (JSON_OPEN_BRACKET.equals(line)) {
                taskContext.addJsonSingleLine(line);
                return JSON_LINE_STATE;
            } else if (line.startsWith(POM_PREFIX)) {
                taskContext.setPomPath(line.substring(POM_PREFIX.length()));
            }
            return this;
        }
    }

    private static class JsonLineState implements State {
        @Override
        public State handleLine(String line, TaskContext taskContext) {
            return taskContext.addJsonSingleLine(line) ? MAVEN_LINE_STATE : this;
        }
    }
}