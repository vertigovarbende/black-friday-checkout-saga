package com.deveyk.orderflow.order.application.order.port;

import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;

import java.util.Optional;

public interface OrderReadPort {

    Optional<Order> findById(OrderId orderId);
}
