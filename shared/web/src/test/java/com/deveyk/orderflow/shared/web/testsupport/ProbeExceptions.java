package com.deveyk.orderflow.shared.web.testsupport;

import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowConflictException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowTechnicalException;

import java.io.Serial;

public final class ProbeExceptions {

    private ProbeExceptions() {
    }

    public static final class NotFound extends OrderflowResourceNotFoundException {
        @Serial
        private static final long serialVersionUID = 1L;

        public NotFound(final String id) {
            super(ProbeErrorCode.PROBE_NOT_FOUND, id);
        }
    }

    public static final class Conflict extends OrderflowConflictException {
        @Serial
        private static final long serialVersionUID = 1L;

        public Conflict(final String id) {
            super(ProbeErrorCode.PROBE_CONFLICT, id);
        }
    }

    public static final class InvalidState extends OrderflowInvalidStateException {
        @Serial
        private static final long serialVersionUID = 1L;

        public InvalidState(final String id, final String current, final String target) {
            super(ProbeErrorCode.PROBE_INVALID_STATE, id, current, target);
        }
    }

    public static final class BusinessRule extends OrderflowBusinessRuleException {
        @Serial
        private static final long serialVersionUID = 1L;

        public BusinessRule(final String id) {
            super(ProbeErrorCode.PROBE_BUSINESS_RULE, id);
        }
    }

    public static final class Technical extends OrderflowTechnicalException {
        @Serial
        private static final long serialVersionUID = 1L;

        public Technical() {
            super(ProbeErrorCode.PROBE_UNAVAILABLE);
        }
    }

    public static final class Root extends OrderflowException {
        @Serial
        private static final long serialVersionUID = 1L;

        public Root() {
            super(ProbeErrorCode.PROBE_ROOT);
        }
    }
}
