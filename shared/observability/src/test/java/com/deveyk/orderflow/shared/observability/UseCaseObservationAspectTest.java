package com.deveyk.orderflow.shared.observability;

import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.observability.testsupport.ProbeErrorCode;
import com.deveyk.orderflow.shared.observability.testsupport.ProbeUseCase;
import com.deveyk.orderflow.shared.observability.testsupport.ProbeUseCaseHandler;
import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UseCaseObservationAspectTest {

    private TestObservationRegistry observationRegistry;

    private UseCaseHandler<String, ProbeUseCase> handler;

    @BeforeEach
    void setUpProxiedHandler() {
        observationRegistry = TestObservationRegistry.create();
        final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new ProbeUseCaseHandler());
        proxyFactory.addAspect(new UseCaseObservationAspect(observationRegistry));
        handler = proxyFactory.getProxy();
    }

    @Test
    @DisplayName("Başarılı handle bir orderflow.usecase observation'ı üretir: usecase adı ve outcome=success")
    void givenSucceedingHandler_whenHandle_thenObservationWithSuccessOutcome() {
        // When
        final String result = handler.handle(ProbeUseCase.succeeding());

        // Then
        assertThat(result).isEqualTo("done");

        TestObservationRegistryAssert.assertThat(observationRegistry)
                .hasObservationWithNameEqualTo(UseCaseObservationAspect.OBSERVATION_NAME)
                .that()
                .hasContextualNameEqualTo("ProbeUseCase")
                .hasLowCardinalityKeyValue(UseCaseObservationAspect.USECASE_KEY, "ProbeUseCase")
                .hasLowCardinalityKeyValue(UseCaseObservationAspect.OUTCOME_KEY, UseCaseObservationAspect.SUCCESS)
                .hasBeenStarted()
                .hasBeenStopped();
    }

    @Test
    @DisplayName("Handler exception fırlatırsa outcome ailenin adıdır, hata observation'a yazılır ve exception aynen geçer")
    void givenFailingHandler_whenHandle_thenObservationWithFamilyOutcomeAndExceptionRethrown() {
        // Given
        final RuntimeException failure = new OrderflowResourceNotFoundException(ProbeErrorCode.PROBE) { };

        // When / Then
        assertThatThrownBy(() -> handler.handle(ProbeUseCase.failingWith(failure))).isSameAs(failure);

        TestObservationRegistryAssert.assertThat(observationRegistry)
                .hasObservationWithNameEqualTo(UseCaseObservationAspect.OBSERVATION_NAME)
                .that()
                .hasLowCardinalityKeyValue(UseCaseObservationAspect.OUTCOME_KEY, ErrorFamily.NOT_FOUND)
                .hasError()
                .hasBeenStopped();
    }

    @Test
    @DisplayName("Aileye ait olmayan bir hata outcome=unexpected olarak sayılır")
    void givenUnexpectedFailure_whenHandle_thenUnexpectedOutcome() {
        // When / Then
        assertThatThrownBy(() -> handler.handle(ProbeUseCase.failingWith(new IllegalStateException("boom"))))
                .isInstanceOf(IllegalStateException.class);

        TestObservationRegistryAssert.assertThat(observationRegistry)
                .hasObservationWithNameEqualTo(UseCaseObservationAspect.OBSERVATION_NAME)
                .that()
                .hasLowCardinalityKeyValue(UseCaseObservationAspect.OUTCOME_KEY, ErrorFamily.UNEXPECTED);
    }
}
