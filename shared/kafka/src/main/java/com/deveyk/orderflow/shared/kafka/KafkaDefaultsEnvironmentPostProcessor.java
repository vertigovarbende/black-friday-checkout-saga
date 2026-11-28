package com.deveyk.orderflow.shared.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.StringOrBytesSerializer;

import java.util.Map;

// Servislerin ortak spring.kafka.* ayarları. En düşük öncelikle eklenir: servisin application.yml'i, profil dosyaları,
// env var'lar ve testlerin @DynamicPropertySource'u hepsini ezebilir. bootstrap-servers ortama bağlı, serviste kalır.
public class KafkaDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE_NAME = "orderflowKafkaDefaults";

    static final Map<String, Object> DEFAULTS = Map.ofEntries(
            // Servis başına tek consumer group; KIP-848 protokolü (ADR-0018).
            Map.entry("spring.kafka.consumer.group-id", "${spring.application.name}"),
            Map.entry("spring.kafka.consumer.auto-offset-reset", "earliest"),
            Map.entry("spring.kafka.consumer.properties.group.protocol", "consumer"),
            // Değerler String okunur, JSON'u listener çözer (EventJsonReader). Çözülemeyen kayıt poll()'da patlamasın (deney 5.5).
            Map.entry("spring.kafka.consumer.key-deserializer", ErrorHandlingDeserializer.class.getName()),
            Map.entry("spring.kafka.consumer.value-deserializer", ErrorHandlingDeserializer.class.getName()),
            Map.entry("spring.kafka.consumer.properties.spring.deserializer.key.delegate.class", StringDeserializer.class.getName()),
            Map.entry("spring.kafka.consumer.properties.spring.deserializer.value.delegate.class", StringDeserializer.class.getName()),
            // Sadece DLT'ye yazmak için. Çözülemeyen kayıt DLT'ye orijinal byte'larıyla gider; String ya da byte[] yazabilmeli.
            Map.entry("spring.kafka.producer.key-serializer", StringOrBytesSerializer.class.getName()),
            Map.entry("spring.kafka.producer.value-serializer", StringOrBytesSerializer.class.getName()),
            // concurrency = partition sayısı: bloklayan retry sadece kendi partition'ını bekletir (deney 5.2).
            Map.entry("spring.kafka.listener.concurrency", "3"),
            Map.entry("spring.kafka.listener.observation-enabled", "true"),
            Map.entry("spring.kafka.template.observation-enabled", "true")
    );

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, DEFAULTS));
    }

    // application.yml'i yükleyen ConfigDataEnvironmentPostProcessor'dan sonra çalışsın: addLast'ın eklediği kaynak
    // ancak o zaman gerçekten en sondadır.
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
