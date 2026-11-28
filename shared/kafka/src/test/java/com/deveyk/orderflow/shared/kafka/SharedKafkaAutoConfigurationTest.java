package com.deveyk.orderflow.shared.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SharedKafkaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SharedKafkaAutoConfiguration.class))
            .withBean(KafkaProperties.class, KafkaProperties::new);

    @Test
    @DisplayName("KafkaTemplate varsa ortak error handler ve topic ayarları (varsayılanlarla) kurulur")
    void givenKafkaTemplate_whenContextStarts_thenErrorHandlerAndTopicProperties() {
        contextRunner
                .withBean(KafkaTemplate.class, () -> Mockito.mock(KafkaTemplate.class))
                .run(context -> {
                    // Then
                    assertThat(context).hasSingleBean(DefaultErrorHandler.class);
                    assertThat(context.getBean(KafkaTopicProperties.class)).isEqualTo(new KafkaTopicProperties(3, 3, 2));
                });
    }

    @Test
    @DisplayName("Serviste kendi error handler'ı varsa ortak olan geri çekilir")
    void givenServiceErrorHandler_whenContextStarts_thenSharedErrorHandlerBacksOff() {
        contextRunner
                .withBean(KafkaTemplate.class, () -> Mockito.mock(KafkaTemplate.class))
                .withBean(CommonErrorHandler.class, CommonLoggingErrorHandler::new)
                .run(context -> {
                    // Then
                    assertThat(context).hasSingleBean(CommonErrorHandler.class);
                    assertThat(context).doesNotHaveBean(DefaultErrorHandler.class);
                });
    }

    @Test
    @DisplayName("Auto-configuration doğru klasördeki imports dosyasında kayıtlı (Faz 2'deki src/main/java/resources hatası)")
    void givenImportsFile_whenRead_thenAutoConfigurationRegistered() throws IOException {
        // When
        final String imports = new ClassPathResource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
                .getContentAsString(StandardCharsets.UTF_8);

        // Then
        assertThat(imports).contains(SharedKafkaAutoConfiguration.class.getName());
    }
}
