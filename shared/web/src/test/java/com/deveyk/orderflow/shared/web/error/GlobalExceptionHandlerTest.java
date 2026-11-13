package com.deveyk.orderflow.shared.web.error;

import com.deveyk.orderflow.shared.web.testsupport.ErrorProbeController;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ErrorProbeController.class)
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @ParameterizedTest(name = "{0} → {1} {2}")
    @CsvSource({
            "/probe/validation,     400, TST_001",
            "/probe/not-found,      404, TST_002",
            "/probe/conflict,       409, TST_003",
            "/probe/invalid-state,  409, TST_004",
            "/probe/business-rule,  422, TST_005",
            "/probe/technical,      503, TST_006",
            "/probe/root,           500, TST_007"
    })
    @DisplayName("Her exception ailesi kendi HTTP status'uyla ve exception'ın koduyla döner")
    void givenFamilyException_whenRequest_thenFamilyStatusAndCode(final String path, final int status, final String code)
            throws Exception {
        // When / Then
        mockMvc.perform(get(path))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.header").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @ParameterizedTest(name = "{0} {1} → {2} {3}")
    @CsvSource({
            "GET,    /probe/items/abc,                400, GEN_004",
            "GET,    /probe/header,                   400, GEN_009",
            "GET,    /probe/method-validation?size=0, 400, GEN_002",
            "DELETE, /probe/body,                     405, GEN_006",
            "GET,    /does-not-exist,                 404, GEN_005",
            "GET,    /probe/data-integrity,           409, GEN_007",
            "GET,    /probe/optimistic-lock,          409, GEN_010",
            "GET,    /probe/transient,                503, GEN_011",
            "GET,    /probe/resource-failure,         503, GEN_011",
            "GET,    /probe/unexpected,               500, GEN_001"
    })
    @DisplayName("Her framework exception'ı tablodaki HTTP status ve GEN koduyla döner")
    void givenFrameworkException_whenRequest_thenMappedStatusAndCode(final String method, final String path,
                                                                     final int status, final String code)
            throws Exception {
        // When / Then
        mockMvc.perform(request(HttpMethod.valueOf(method), path))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    @DisplayName("Geçersiz istek gövdesinde alan hataları annotation'daki mesajla subErrors'ta döner")
    void givenInvalidBody_whenPost_thenSubErrorsWithAnnotationMessages() throws Exception {
        // Given
        final String body = "{\"productId\": null, \"quantity\": 0}";

        // When / Then
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_002"))
                .andExpect(jsonPath("$.subErrors.length()").value(2))
                .andExpect(jsonPath("$.subErrors[?(@.field == 'quantity')].message", contains("Quantity must be at least 1")))
                .andExpect(jsonPath("$.subErrors[?(@.field == 'quantity')].value", contains("0")))
                .andExpect(jsonPath("$.subErrors[?(@.field == 'productId')].message", contains("Product id is required")));
    }

    @Test
    @DisplayName("ConstraintViolationException alan hatalarını annotation tipiyle birlikte subErrors'ta döner")
    void givenConstraintViolation_whenRequest_thenSubErrorsWithAnnotationType() throws Exception {
        // When / Then
        mockMvc.perform(get("/probe/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_002"))
                .andExpect(jsonPath("$.subErrors[0].field").value("productId"))
                .andExpect(jsonPath("$.subErrors[0].message").value("Product id is required"))
                .andExpect(jsonPath("$.subErrors[0].type").value("NotNull"));
    }

    @Test
    @DisplayName("Okunamayan gövde 400 MALFORMED_REQUEST, desteklenmeyen içerik tipi 415 döner")
    void givenMalformedOrUnsupportedBody_whenPost_thenMalformedOrUnsupported() throws Exception {
        // When / Then
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_003"));
        mockMvc.perform(post("/probe/body").contentType(MediaType.TEXT_PLAIN).content("text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("GEN_008"));
    }

    @Test
    @DisplayName("503 cevapları Retry-After header'ı taşır")
    void givenTechnicalFailure_whenRequest_thenRetryAfterHeader() throws Exception {
        // When / Then
        mockMvc.perform(get("/probe/technical"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", GlobalExceptionHandler.RETRY_AFTER_SECONDS));
        mockMvc.perform(get("/probe/transient"))
                .andExpect(header().string("Retry-After", GlobalExceptionHandler.RETRY_AFTER_SECONDS));
    }

    @Test
    @DisplayName("Beklenmeyen bir hatada iç mesaj istemciye sızmaz")
    void givenUnexpectedException_whenRequest_thenGenericMessage() throws Exception {
        // When / Then
        mockMvc.perform(get("/probe/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(CommonErrorCode.INTERNAL_SERVER_ERROR.defaultMessage()))
                .andExpect(jsonPath("$.message", not(containsString("secret"))));
    }

    @Test
    @DisplayName("İstemci hataları INFO, sunucu hataları ERROR seviyesinde loglanır")
    void givenClientAndServerErrors_whenRequest_thenLoggedAtInfoAndError(final CapturedOutput output) throws Exception {
        // When
        mockMvc.perform(get("/probe/not-found"));
        mockMvc.perform(get("/probe/unexpected"));

        // Then
        assertThat(output.getOut()).containsPattern("INFO.*responseCode:TST_002 \\| path:/probe/not-found");
        assertThat(output.getOut()).containsPattern("ERROR.*responseCode:GEN_001 \\| path:/probe/unexpected");
    }

    @Test
    @DisplayName("Her hata orderflow.errors sayacını kod ve aileyle bir artırır")
    void givenError_whenRequest_thenErrorCounterIncremented() throws Exception {
        // Given
        final double before = errorCount("TST_002", "not_found");

        // When
        mockMvc.perform(get("/probe/not-found"));

        // Then
        assertThat(errorCount("TST_002", "not_found")).isEqualTo(before + 1);
    }

    private double errorCount(final String code, final String family) {

        final Counter counter = meterRegistry.find(GlobalExceptionHandler.ERROR_COUNTER)
                .tag("code", code)
                .tag("family", family)
                .counter();

        return counter == null ? 0 : counter.count();
    }

    @TestConfiguration
    static class MetricsTestConfiguration {

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
