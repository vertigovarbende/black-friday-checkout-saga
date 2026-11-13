package com.deveyk.orderflow.shared.web.testsupport;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ProbeErrorCode implements ErrorCode {

    PROBE_INVALID("TST_001", "PROBE_INVALID", "Probe {0} is invalid"),
    PROBE_NOT_FOUND("TST_002", "PROBE_NOT_FOUND", "Probe not found: {0}"),
    PROBE_CONFLICT("TST_003", "PROBE_CONFLICT", "Probe {0} already exists"),
    PROBE_INVALID_STATE("TST_004", "PROBE_INVALID_STATE", "Probe {0} cannot move from {1} to {2}"),
    PROBE_BUSINESS_RULE("TST_005", "PROBE_BUSINESS_RULE", "Probe {0} breaks a business rule"),
    PROBE_UNAVAILABLE("TST_006", "PROBE_UNAVAILABLE", "Probe dependency is unavailable"),
    PROBE_ROOT("TST_007", "PROBE_ROOT", "Probe exception without a family");

    private final String code;
    private final String header;
    private final String defaultMessage;
}
