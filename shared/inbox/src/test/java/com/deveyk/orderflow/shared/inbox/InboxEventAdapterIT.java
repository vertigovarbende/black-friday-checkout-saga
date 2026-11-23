package com.deveyk.orderflow.shared.inbox;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.inbox.testsupport.SharedInboxTestContainerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboxEventAdapterIT extends SharedInboxTestContainerConfiguration {

    @Autowired
    private InboxEventPort inboxEventPort;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanInbox() {
        jdbcTemplate.execute("TRUNCATE TABLE inbox_event");
    }

    @Test
    @DisplayName("İlk kayıt true, aynı consumer + eventId'nin ikinci kaydı false döner; duplicate transaction'ı bozmaz")
    void givenSameMessageTwiceInOneTransaction_whenTryRecord_thenSecondReturnsFalseAndTransactionCommits() {
        // Given
        final InboxMessage message = new InboxMessage("order-service", UuidV7.next(), "ProbeChanged", "probe-1", null);

        // When
        final Boolean[] results = transactionTemplate.execute(status ->
                new Boolean[]{inboxEventPort.tryRecord(message), inboxEventPort.tryRecord(message)});

        // Then
        assertThat(results).containsExactly(true, false);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_event", Integer.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transaction dışında çağrılırsa reddedilir (MANDATORY): kayıt işten bağımsız commit olamaz")
    void givenNoTransaction_whenTryRecord_thenRejected() {
        // Given
        final InboxMessage message = new InboxMessage("order-service", UuidV7.next(), "ProbeChanged", "probe-1", null);

        // When / Then
        assertThatThrownBy(() -> inboxEventPort.tryRecord(message))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_event", Integer.class)).isZero();
    }
}
