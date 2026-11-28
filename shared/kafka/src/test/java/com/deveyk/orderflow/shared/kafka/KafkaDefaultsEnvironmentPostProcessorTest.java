package com.deveyk.orderflow.shared.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaDefaultsEnvironmentPostProcessorTest {

    @Test
    @DisplayName("Servis bir ayarı vermemişse ortak varsayılan gelir; group id servis adıdır")
    void givenServiceWithoutKafkaSettings_whenPostProcess_thenDefaultsApplied() {
        // Given
        final StandardEnvironment environment = environmentWith(Map.of("spring.application.name", "order-service"));

        // When
        new KafkaDefaultsEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        // Then
        assertThat(environment.getProperty("spring.kafka.consumer.group-id")).isEqualTo("order-service");
        assertThat(environment.getProperty("spring.kafka.consumer.properties.group.protocol")).isEqualTo("consumer");
        assertThat(environment.getProperty("spring.kafka.consumer.value-deserializer"))
                .isEqualTo("org.springframework.kafka.support.serializer.ErrorHandlingDeserializer");
        assertThat(environment.getProperty("spring.kafka.listener.concurrency")).isEqualTo("3");
    }

    @Test
    @DisplayName("Servisin kendi değeri varsayılanı ezer: varsayılanlar en düşük öncelikte")
    void givenServiceOverride_whenPostProcess_thenServiceValueWins() {
        // Given
        final StandardEnvironment environment = environmentWith(Map.of("spring.kafka.listener.concurrency", "6"));

        // When
        new KafkaDefaultsEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        // Then
        assertThat(environment.getProperty("spring.kafka.listener.concurrency")).isEqualTo("6");
        assertThat(environment.getPropertySources().stream().reduce((first, second) -> second).orElseThrow().getName())
                .isEqualTo(KafkaDefaultsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
    }

    @Test
    @DisplayName("Post processor spring.factories'te kayıtlı: Spring Boot onu kendiliğinden çalıştırır")
    void givenSpringFactories_whenRead_thenPostProcessorRegistered() throws IOException {
        // When
        final Properties factories = PropertiesLoaderUtils.loadProperties(new ClassPathResource("META-INF/spring.factories"));

        // Then
        assertThat(factories.getProperty("org.springframework.boot.EnvironmentPostProcessor"))
                .contains(KafkaDefaultsEnvironmentPostProcessor.class.getName());
    }

    private static StandardEnvironment environmentWith(final Map<String, Object> serviceProperties) {
        final StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("service", serviceProperties));
        return environment;
    }
}
