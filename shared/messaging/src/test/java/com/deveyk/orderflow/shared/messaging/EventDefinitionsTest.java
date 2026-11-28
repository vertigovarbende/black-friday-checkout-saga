package com.deveyk.orderflow.shared.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EventDefinitionsTest {

    @Test
    @DisplayName("Her tanımda aggregate tipi topic'in ilk parçası, tip topic parçalarının PascalCase birleşimidir")
    void givenAllDefinitions_whenCompareWithTopic_thenFollowNamingRule() {
        for (final EventDefinition definition : allDefinitions()) {
            // Then
            assertThat(definition.aggregateType()).as(definition.topic()).isEqualTo(definition.topic().split("\\.")[0]);
            assertThat(definition.eventType()).as(definition.topic()).isEqualTo(pascalCase(definition.topic()));
        }
    }

    @Test
    @DisplayName("TopicNames'teki her topic'in tek bir tanımı vardır")
    void givenAllTopicNames_whenCompareWithDefinitions_thenOneDefinitionPerTopic() {
        // Given
        final List<String> definedTopics = allDefinitions().stream().map(EventDefinition::topic).toList();

        // Then
        assertThat(definedTopics).doesNotHaveDuplicates();
        assertThat(definedTopics).containsExactlyInAnyOrderElementsOf(allTopicNames());
    }

    private static String pascalCase(final String topic) {
        return Arrays.stream(topic.split("\\."))
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining());
    }

    private static List<EventDefinition> allDefinitions() {
        return constants(EventDefinitions.class, EventDefinition.class);
    }

    private static List<String> allTopicNames() {
        return constants(TopicNames.class, String.class);
    }

    private static <T> List<T> constants(final Class<?> holder, final Class<T> type) {
        return Arrays.stream(holder.getFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == type)
                .map(field -> type.cast(valueOf(field)))
                .toList();
    }

    private static Object valueOf(final Field field) {
        try {
            return field.get(null);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
