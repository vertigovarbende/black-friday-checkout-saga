package com.deveyk.orderflow.shared.inbox.support;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.inbox.SharedInboxTestApplication;
import com.deveyk.orderflow.shared.inbox.port.InboxMessage;
import com.deveyk.orderflow.shared.inbox.testsupport.SharedInboxTestContainerConfiguration;
import com.deveyk.orderflow.shared.observability.MdcKeys;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboxProcessorIT extends SharedInboxTestContainerConfiguration {

    @Autowired
    private InboxProcessor inboxProcessor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    private final AtomicInteger runs = new AtomicInteger();

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("TRUNCATE TABLE inbox_event, inbox_probe");
    }

    @Test
    @DisplayName("İlk kez gelen mesajda iş çalışır ve mesaj inbox'a consumer + eventId ile kaydedilir")
    void givenNewMessage_whenProcess_thenBusinessRunsAndMessageRecorded() {
        // Given
        final InboxMessage message = message("order-service");

        // When
        inboxProcessor.process(message, runs::incrementAndGet);

        // Then
        assertThat(runs).hasValue(1);
        final Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM inbox_event WHERE consumer = ? AND event_id = ?", message.consumer(), message.eventId());
        assertThat(row.get("event_type")).isEqualTo("ProbeChanged");
        assertThat(row.get("aggregate_id")).isEqualTo("probe-1");
        assertThat(((Timestamp) row.get("processed_at")).toInstant()).isEqualTo(SharedInboxTestApplication.NOW);
    }

    @Test
    @DisplayName("Aynı mesaj ikinci kez gelince iş çalışmaz; inbox'ta tek satır kalır ve duplicate sayacı artar")
    void givenSameMessageTwice_whenProcess_thenBusinessRunsOnceAndDuplicateCounted() {
        // Given
        final String consumer = "consumer-" + UUID.randomUUID();
        final InboxMessage message = message(consumer);

        // When
        inboxProcessor.process(message, runs::incrementAndGet);
        inboxProcessor.process(message, runs::incrementAndGet);

        // Then
        assertThat(runs).hasValue(1);
        assertThat(inboxRows(message)).isEqualTo(1);
        assertThat(meterRegistry.get(InboxProcessor.DUPLICATE_COUNTER).tag("consumer", consumer).counter().count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("İş hata fırlatırsa hata yutulmaz; inbox kaydı ve işin yazdığı veri birlikte geri alınır")
    void givenBusinessFails_whenProcess_thenExceptionPropagatesAndInboxAndDataRolledBack() {
        // Given
        final InboxMessage message = message("order-service");

        // When / Then
        assertThatThrownBy(() -> inboxProcessor.process(message, failingAfterWrite(message)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("business failed");
        assertThat(inboxRows(message)).isZero();
        assertThat(probeRows(message)).isZero();
    }

    @Test
    @DisplayName("Hata yüzünden geri alınan mesaj tekrar gelince duplicate sayılmaz, baştan işlenir")
    void givenEarlierAttemptFailed_whenRedelivered_thenProcessedNormally() {
        // Given
        final InboxMessage message = message("order-service");
        assertThatThrownBy(() -> inboxProcessor.process(message, failingAfterWrite(message)))
                .isInstanceOf(IllegalStateException.class);

        // When
        inboxProcessor.process(message, () -> writeProbe(message));

        // Then
        assertThat(inboxRows(message)).isEqualTo(1);
        assertThat(probeRows(message)).isEqualTo(1);
    }

    @Test
    @DisplayName("Aynı event'i iki ayrı consumer işler; tekillik consumer başınadır")
    void givenSameEventForTwoConsumers_whenProcess_thenBothProcess() {
        // Given
        final InboxMessage forOrder = message("order-service");
        final InboxMessage forNotification = new InboxMessage("notification-service", forOrder.eventId(),
                forOrder.eventType(), forOrder.aggregateId(), forOrder.correlationId());

        // When
        inboxProcessor.process(forOrder, runs::incrementAndGet);
        inboxProcessor.process(forNotification, runs::incrementAndGet);

        // Then
        assertThat(runs).hasValue(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_event WHERE event_id = ?",
                Integer.class, forOrder.eventId())).isEqualTo(2);
    }

    @Test
    @DisplayName("İş boyunca MDC'de mesajın correlationId'si ve kimlikleri vardır; işten sonra kaldırılır")
    void givenMessage_whenProcess_thenMdcCarriesMessageContextOnlyDuringBusiness() {
        // Given
        final InboxMessage message = message("order-service");
        final Map<String, String> seen = new HashMap<>();

        // When
        inboxProcessor.process(message, () -> {
            seen.put(MdcKeys.CORRELATION_ID, MDC.get(MdcKeys.CORRELATION_ID));
            seen.put(MdcKeys.EVENT_ID, MDC.get(MdcKeys.EVENT_ID));
            seen.put(MdcKeys.EVENT_TYPE, MDC.get(MdcKeys.EVENT_TYPE));
            seen.put(MdcKeys.AGGREGATE_ID, MDC.get(MdcKeys.AGGREGATE_ID));
            seen.put(MdcKeys.CONSUMER, MDC.get(MdcKeys.CONSUMER));
        });

        // Then
        assertThat(seen).containsExactlyInAnyOrderEntriesOf(Map.of(
                MdcKeys.CORRELATION_ID, "black-friday-checkout-1",
                MdcKeys.EVENT_ID, message.eventId().toString(),
                MdcKeys.EVENT_TYPE, "ProbeChanged",
                MdcKeys.AGGREGATE_ID, "probe-1",
                MdcKeys.CONSUMER, "order-service"));
        assertThat(MDC.get(MdcKeys.CORRELATION_ID)).isNull();
        assertThat(MDC.get(MdcKeys.EVENT_ID)).isNull();
        assertThat(MDC.get(MdcKeys.CONSUMER)).isNull();
    }

    @Test
    @DisplayName("Zarfta correlationId yoksa MDC'ye konmaz")
    void givenMessageWithoutCorrelationId_whenProcess_thenCorrelationIdNotInMdc() {
        // Given
        final InboxMessage message = new InboxMessage("order-service", UuidV7.next(), "ProbeChanged", "probe-1", null);
        final Map<String, String> seen = new HashMap<>();

        // When
        inboxProcessor.process(message, () -> seen.put(MdcKeys.CORRELATION_ID, MDC.get(MdcKeys.CORRELATION_ID)));

        // Then
        assertThat(seen.get(MdcKeys.CORRELATION_ID)).isNull();
    }

    private static InboxMessage message(final String consumer) {
        return new InboxMessage(consumer, UuidV7.next(), "ProbeChanged", "probe-1", "black-friday-checkout-1");
    }

    private Runnable failingAfterWrite(final InboxMessage message) {
        return () -> {
            writeProbe(message);
            throw new IllegalStateException("business failed");
        };
    }

    private void writeProbe(final InboxMessage message) {
        jdbcTemplate.update("INSERT INTO inbox_probe (event_id) VALUES (?)", message.eventId());
    }

    private int inboxRows(final InboxMessage message) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_event WHERE consumer = ? AND event_id = ?",
                Integer.class, message.consumer(), message.eventId());
    }

    private int probeRows(final InboxMessage message) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_probe WHERE event_id = ?",
                Integer.class, message.eventId());
    }
}
