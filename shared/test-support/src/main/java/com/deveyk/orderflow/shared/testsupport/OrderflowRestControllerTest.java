package com.deveyk.orderflow.shared.testsupport;

import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvc;
import com.deveyk.orderflow.shared.web.SharedWebAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(SharedWebAutoConfiguration.class)
public abstract class OrderflowRestControllerTest extends LogTrackerConfiguration {

    protected OrderflowMockMvc orderflowMockMvc;

    @BeforeEach
    protected void setUpOrderflowMockMvc(@Autowired final MockMvc mockMvc) {
        this.orderflowMockMvc = new OrderflowMockMvc(mockMvc);
    }
}
