package com.deveyk.orderflow.shared.web.error;

import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowConflictException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowTechnicalException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.text.MessageFormat;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String ERROR_COUNTER = "orderflow.errors";
    static final String RETRY_AFTER_SECONDS = "5";

    // ----- families -----
    private static final String VALIDATION = "validation";
    private static final String NOT_FOUND = "not_found";
    private static final String CONFLICT = "conflict";
    private static final String INVALID_STATE = "invalid_state";
    private static final String BUSINESS_RULE = "business_rule";
    private static final String TECHNICAL = "technical";
    private static final String UNEXPECTED = "unexpected";

    private final TraceContextProvider traceContextProvider;
    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(final ObjectProvider<TraceContextProvider> traceContextProvider,
                                  final ObjectProvider<MeterRegistry> meterRegistry) {

        this.traceContextProvider = traceContextProvider.getIfAvailable(() -> new TraceContextProvider(Tracer.NOOP));
        this.meterRegistry = meterRegistry.getIfAvailable(() -> Metrics.globalRegistry);

    }

    // ----- framework exception'ları -----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception,
                                                               final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.subErrors(exception.getBindingResult().getFieldErrors()), CommonErrorCode.VALIDATION_ERROR, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException exception,
                                                            final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.subErrors(exception.getConstraintViolations()), CommonErrorCode.VALIDATION_ERROR, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidationException(final HandlerMethodValidationException exception,
                                                                final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.VALIDATION_ERROR, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception,
                                                                   final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.subErrors(exception), CommonErrorCode.TYPE_MISMATCH, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception,
                                                               final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.MALFORMED_REQUEST, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException exception,
                                                             final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.MISSING_REQUEST_HEADER, request, exception.getHeaderName());
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResourceFoundException(final NoResourceFoundException exception,
                                                        final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.RESOURCE_NOT_FOUND, request);
        this.logClientError(exception, errorResponse, NOT_FOUND);
        return errorResponse;

    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException exception,
                                                                      final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.METHOD_NOT_ALLOWED, request, exception.getMethod());
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException exception,
                                                                  final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.UNSUPPORTED_MEDIA_TYPE, request, String.valueOf(exception.getContentType()));
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(final DataIntegrityViolationException exception,
                                                               final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.DATA_CONFLICT, request);
        this.logClientError(exception, errorResponse, CONFLICT);
        return errorResponse;

    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockingFailureException(final OptimisticLockingFailureException exception,
                                                                 final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.CONCURRENT_MODIFICATION, request);
        this.logClientError(exception, errorResponse, CONFLICT);
        return errorResponse;

    }

    // CannotGetJdbcConnectionException bir DataAccessResourceFailureException'dır.
    @ExceptionHandler({TransientDataAccessException.class, DataAccessResourceFailureException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleTransientDataAccessException(final Exception exception,
                                                            final HttpServletRequest request,
                                                            final HttpServletResponse response) {

        response.setHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.SERVICE_UNAVAILABLE, request);
        this.logServerError(exception, errorResponse, TECHNICAL);
        return errorResponse;

    }

    // ----- aileler: her aile için TEK metot; status aileden -----

    @ExceptionHandler(OrderflowValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOrderflowValidationException(final OrderflowValidationException exception,
                                                            final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logClientError(exception, errorResponse, VALIDATION);
        return errorResponse;

    }

    @ExceptionHandler(OrderflowResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderflowResourceNotFoundException(final OrderflowResourceNotFoundException exception,
                                                                  final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logClientError(exception, errorResponse, NOT_FOUND);
        return errorResponse;

    }

    @ExceptionHandler(OrderflowConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderflowConflictException(final OrderflowConflictException exception,
                                                          final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logClientError(exception, errorResponse, CONFLICT);
        return errorResponse;

    }

    @ExceptionHandler(OrderflowInvalidStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOrderflowInvalidStateException(final OrderflowInvalidStateException exception,
                                                              final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logClientError(exception, errorResponse, INVALID_STATE);
        return errorResponse;

    }

    @ExceptionHandler(OrderflowBusinessRuleException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleOrderflowBusinessRuleException(final OrderflowBusinessRuleException exception,
                                                              final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logClientError(exception, errorResponse, BUSINESS_RULE);
        return errorResponse;

    }

    @ExceptionHandler(OrderflowTechnicalException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleOrderflowTechnicalException(final OrderflowTechnicalException exception,
                                                           final HttpServletRequest request,
                                                           final HttpServletResponse response) {

        response.setHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logServerError(exception, errorResponse, TECHNICAL);
        return errorResponse;

    }

    // ----- fallback'ler -----

    @ExceptionHandler(OrderflowException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOrderflowException(final OrderflowException exception, final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.toErrorResponse(exception, request);
        this.logServerError(exception, errorResponse, UNEXPECTED);
        return errorResponse;

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception exception, final HttpServletRequest request) {

        final ErrorResponse errorResponse = this.common(ErrorResponse.builder(), CommonErrorCode.INTERNAL_SERVER_ERROR, request);
        this.logServerError(exception, errorResponse, UNEXPECTED);
        return errorResponse;

    }

    // ----- yardımcılar -----

    private ErrorResponse common(final ErrorResponse.ErrorResponseBuilder builder,
                                 final CommonErrorCode errorCode,
                                 final HttpServletRequest request,
                                 final Object... args) {

        return builder
                .header(errorCode.header())
                .code(errorCode.code())
                .message(MessageFormat.format(errorCode.defaultMessage(), args))
                .path(request.getRequestURI())
                .traceId(currentTraceId())
                .build();
    }

    private ErrorResponse toErrorResponse(final OrderflowException exception, final HttpServletRequest request) {

        return ErrorResponse.builder()
                .header(exception.getErrorCode().header())
                .code(exception.getErrorCode().code())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .traceId(currentTraceId())
                .build();
    }

    private String currentTraceId() {
        return traceContextProvider.currentTraceId().orElse(null);
    }

    private void logClientError(final Exception exception, final ErrorResponse errorResponse, final String family) {

        this.countError(errorResponse, family);
        log.info("responseCode:{} | path:{} | {}", errorResponse.getCode(), errorResponse.getPath(), exception.getMessage());
        log.debug("responseCode:{} | StackTrace:", errorResponse.getCode(), exception);

    }

    private void logServerError(final Exception exception, final ErrorResponse errorResponse, final String family) {

        this.countError(errorResponse, family);
        log.error("responseCode:{} | path:{} | {}", errorResponse.getCode(), errorResponse.getPath(), exception.getMessage(), exception);

    }

    private void countError(final ErrorResponse errorResponse, final String family) {

        Counter.builder(ERROR_COUNTER)
                .tag("code", Objects.requireNonNullElse(errorResponse.getCode(), "unknown"))
                .tag("family", family)
                .register(meterRegistry)
                .increment();

    }
}
