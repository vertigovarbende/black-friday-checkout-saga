package com.deveyk.orderflow.shared.web.testsupport;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/probe")
public class ErrorProbeController {

    private final Validator validator;

    public ErrorProbeController(final Validator validator) {
        this.validator = validator;
    }

    // ----- aileler

    @GetMapping("/validation")
    public void validation() {
        throw new DomainValidationException(ProbeErrorCode.PROBE_INVALID, "p-1");
    }

    @GetMapping("/not-found")
    public void notFound() {
        throw new ProbeExceptions.NotFound("p-1");
    }

    @GetMapping("/conflict")
    public void conflict() {
        throw new ProbeExceptions.Conflict("p-1");
    }

    @GetMapping("/invalid-state")
    public void invalidState() {
        throw new ProbeExceptions.InvalidState("p-1", "COMPLETED", "CANCELLED");
    }

    @GetMapping("/business-rule")
    public void businessRule() {
        throw new ProbeExceptions.BusinessRule("p-1");
    }

    @GetMapping("/technical")
    public void technical() {
        throw new ProbeExceptions.Technical();
    }

    @GetMapping("/root")
    public void root() {
        throw new ProbeExceptions.Root();
    }

    // ----- framework

    @PostMapping(path = "/body", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void body(@Valid @RequestBody final ProbeBody body) {
        // geçerli gövde: 200
    }

    @GetMapping("/constraint")
    public void constraint() {
        throw new ConstraintViolationException(validator.validate(new ProbeBody(null, 1)));
    }

    @GetMapping("/method-validation")
    public void methodValidation(@RequestParam @Min(1) final int size) {
        // size >= 1: 200
    }

    @GetMapping("/items/{id}")
    public void item(@PathVariable final UUID id) {
        // geçerli UUID: 200
    }

    @GetMapping("/header")
    public void header(@RequestHeader("X-User-Id") final String userId) {
        // header var: 200
    }

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
    }

    @GetMapping("/optimistic-lock")
    public void optimisticLock() {
        throw new OptimisticLockingFailureException("row was updated by another transaction");
    }

    @GetMapping("/transient")
    public void transientFailure() {
        throw new QueryTimeoutException("query timed out");
    }

    @GetMapping("/resource-failure")
    public void resourceFailure() {
        throw new DataAccessResourceFailureException("connection refused");
    }

    @GetMapping("/unexpected")
    public void unexpected() {
        throw new IllegalStateException("secret internal detail");
    }
}
