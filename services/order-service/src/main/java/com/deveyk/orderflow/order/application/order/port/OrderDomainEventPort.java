package com.deveyk.orderflow.order.application.order.port;

import com.deveyk.orderflow.order.domain.order.model.Order;

public interface OrderDomainEventPort {

    void publishOrderCreated(Order order);
}
