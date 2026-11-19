package com.deveyk.orderflow.shared.observability;

import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowConflictException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowTechnicalException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;

// Metrik etiketlerindeki aile adları (08 §8, §12). orderflow.usecase{outcome} ve orderflow.errors{family}
// aynı değerleri kullansın diye tek yerde.
public final class ErrorFamily {

    public static final String VALIDATION = "validation";
    public static final String NOT_FOUND = "not_found";
    public static final String CONFLICT = "conflict";
    public static final String INVALID_STATE = "invalid_state";
    public static final String BUSINESS_RULE = "business_rule";
    public static final String TECHNICAL = "technical";
    public static final String UNEXPECTED = "unexpected";

    private ErrorFamily() {
    }

    public static String of(final Throwable throwable) {
        return switch (throwable) {
            case OrderflowValidationException ignored -> VALIDATION;
            case OrderflowResourceNotFoundException ignored -> NOT_FOUND;
            case OrderflowConflictException ignored -> CONFLICT;
            case OrderflowInvalidStateException ignored -> INVALID_STATE;
            case OrderflowBusinessRuleException ignored -> BUSINESS_RULE;
            case OrderflowTechnicalException ignored -> TECHNICAL;
            default -> UNEXPECTED;
        };
    }
}
