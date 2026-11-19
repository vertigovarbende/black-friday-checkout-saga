package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.contract.OrderPortContract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderAdapterContractIT extends OrderTestContainerConfiguration implements OrderPortContract {

    @Autowired
    private OrderAdapter orderAdapter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    public OrderReadPort readPort() {
        return orderAdapter;
    }

    @Override
    public OrderSavePort savePort() {
        return orderAdapter;
    }

    @Override
    public void inTransaction(final Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
    }

    // Sözleşmenin dışında, sadece JPA adapter'ına özgü: fake'te detach olan bir entity yok.
    @Test
    @DisplayName("update transaction dışında çağrılamaz; değişiklik sessizce kaybolmasın")
    void givenNoTransaction_whenUpdate_thenRejected() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When / Then
        assertThatThrownBy(() -> orderAdapter.update(order)).isInstanceOf(IllegalTransactionStateException.class);
    }
}
