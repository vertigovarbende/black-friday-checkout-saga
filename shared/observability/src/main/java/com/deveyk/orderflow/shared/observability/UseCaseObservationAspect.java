package com.deveyk.orderflow.shared.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UseCaseObservationAspect {

    public static final String OBSERVATION_NAME = "orderflow.usecase";
    public static final String USECASE_KEY = "usecase";
    public static final String OUTCOME_KEY = "outcome";
    public static final String SUCCESS = "success";

    private final ObservationRegistry observationRegistry;

    public UseCaseObservationAspect(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Around("execution(* com.deveyk.orderflow.shared.application.UseCaseHandler+.handle(..))")
    public Object observe(final ProceedingJoinPoint joinPoint) throws Throwable {

        final String useCase = useCaseName(joinPoint);

        final Observation observation = Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
                .contextualName(useCase)
                .lowCardinalityKeyValue(USECASE_KEY, useCase)
                .start();

        try (Observation.Scope ignored = observation.openScope()) {

            final Object result = joinPoint.proceed();
            observation.lowCardinalityKeyValue(OUTCOME_KEY, SUCCESS);
            return result;

        } catch (final Throwable throwable) {

            observation.lowCardinalityKeyValue(OUTCOME_KEY, ErrorFamily.of(throwable));
            observation.error(throwable);
            throw throwable;

        } finally {
            observation.stop();
        }
    }

    private static String useCaseName(final ProceedingJoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            return "unknown";
        }
        return args[0].getClass().getSimpleName();
    }
}
