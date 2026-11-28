package com.deveyk.orderflow.shared.testsupport.web;

import com.deveyk.orderflow.shared.observability.OrderflowHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

public final class OrderflowMockMvcRequestBuilders {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private OrderflowMockMvcRequestBuilders() {
    }

    public static MockHttpServletRequestBuilder get(final String endpoint, final UUID userId) {
        return MockMvcRequestBuilders.get(endpoint)
                .header(OrderflowHeaders.USER_ID, userId.toString())
                .accept(MediaType.APPLICATION_JSON);
    }

    // Header'sız istek: X-User-Id istemeyen endpoint'ler (product-service admin API'si) ve header kontrollerinin testleri için.
    public static MockHttpServletRequestBuilder get(final String endpoint) {
        return MockMvcRequestBuilders.get(endpoint)
                .accept(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder post(final String endpoint, final UUID userId, final Object body) {
        return post(endpoint, body)
                .header(OrderflowHeaders.USER_ID, userId.toString());
    }

    // Header'sız ya da elle verilen header'la istek; header kontrollerinin testleri için.
    public static MockHttpServletRequestBuilder post(final String endpoint, final Object body) {
        return MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(JSON_MAPPER.writeValueAsString(body));
    }

    public static MockHttpServletRequestBuilder patch(final String endpoint, final Object body) {
        return MockMvcRequestBuilders.patch(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(JSON_MAPPER.writeValueAsString(body));
    }
}
