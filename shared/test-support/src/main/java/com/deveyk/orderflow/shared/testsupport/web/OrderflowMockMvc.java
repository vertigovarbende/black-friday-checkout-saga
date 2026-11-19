package com.deveyk.orderflow.shared.testsupport.web;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class OrderflowMockMvc {

    private final MockMvc mockMvc;

    public OrderflowMockMvc(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions perform(final RequestBuilder request) throws Exception {
        return mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print());
    }

    public ResultActions perform(final RequestBuilder request, final ErrorCode expected) throws Exception {
        return perform(request)
                .andExpect(OrderflowMockResultMatchersBuilders.timestamp().isNotEmpty())
                .andExpect(OrderflowMockResultMatchersBuilders.code().value(expected.code()))
                .andExpect(OrderflowMockResultMatchersBuilders.header().value(expected.header()))
                .andExpect(OrderflowMockResultMatchersBuilders.message().isString())
                .andExpect(OrderflowMockResultMatchersBuilders.path().isString());
    }
}
