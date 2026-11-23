package com.deveyk.orderflow.order.adapter.out.messaging;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCreatedPayload;

final class OrderToOrderCreatedPayloadMapper {

    private OrderToOrderCreatedPayloadMapper() {
    }

    static OrderCreatedPayload toPayload(final Order order) {
        return OrderCreatedPayload.builder()
                .orderId(order.getId().value())
                .customerId(order.getCustomerId().value())
                .placedAt(order.getPlacedAt())
                .totalAmount(toMoneyAmount(order.getTotal()))
                .lines(order.getLines().stream().map(OrderToOrderCreatedPayloadMapper::toLine).toList())
                .build();
    }

    private static OrderCreatedPayload.Line toLine(final OrderLine line) {
        return OrderCreatedPayload.Line.builder()
                .productId(line.getProductId().value())
                .quantity(line.getQuantity().value())
                .unitPrice(toMoneyAmount(line.getUnitPrice()))
                .build();
    }

    private static MoneyAmount toMoneyAmount(final Money money) {
        return MoneyAmount.of(money.amount(), money.currency());
    }
}
