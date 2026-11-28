package com.deveyk.orderflow.shared.kafka;

import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowConflictException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

import java.sql.SQLException;
import java.util.List;

// 06 §8'in sınıflandırması: hangi hatada kaç retry, DLT metriğinde hangi kod. Tek yer burasıdır; yeni bir exception
// ailesinden geldiği sürece ayrı ayar gerekmez.
public final class KafkaErrorPolicy {

    // Tekrar denense sonuç değişmez. Spring'in kendi listesi (DeserializationException …) da geçerli kalır.
    static final List<Class<? extends Exception>> NOT_RETRYABLE = List.of(
            OrderflowValidationException.class,
            OrderflowConflictException.class,
            OrderflowInvalidStateException.class,
            OrderflowBusinessRuleException.class,
            JacksonException.class);

    // Eventual consistency: ilgili kayıt henüz gelmemiş olabilir (saga'ya order.created'dan önce gelen iptal isteği gibi).
    static final BackOff NOT_FOUND_BACK_OFF = new FixedBackOff(500L, 3L);

    // İdempotency yarışı: ikinci denemede iş zaten yapılmış olarak görünür.
    static final BackOff UNIQUE_VIOLATION_BACK_OFF = new FixedBackOff(200L, 2L);

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private KafkaErrorPolicy() {
    }

    // null: varsayılan backoff (geçici hatalar, optimistic lock ve bilinmeyenler; SharedKafkaAutoConfiguration).
    static BackOff backOffFor(final Throwable exception) {

        for (Throwable current = exception; current != null; current = current.getCause()) {

            if (current instanceof OrderflowResourceNotFoundException) {
                return NOT_FOUND_BACK_OFF;
            }

            if (current instanceof final SQLException sqlException && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return UNIQUE_VIOLATION_BACK_OFF;
            }
        }

        return null;
    }

    // Zincirdeki ilk OrderflowException'ın kodu (ORD_202 …); yoksa en içteki exception'ın adı. Metrik etiketi olduğu için
    // değer kümesi sınırlıdır.
    static String errorCode(final Throwable exception) {

        if (exception == null) {
            return "unknown";
        }

        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof final OrderflowException orderflowException) {
                return orderflowException.getErrorCode().code();
            }
        }

        return rootCause(exception).getClass().getSimpleName();
    }

    static Throwable rootCause(final Throwable exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
