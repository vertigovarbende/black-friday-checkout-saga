package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.response.MoneyResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderLineResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;

public final class OrderToResponseMapper {

    private OrderToResponseMapper() {
    }

    public static PlaceOrderResponse toPlaceOrderResponse(final Order order) {
        return PlaceOrderResponse.builder()
                .orderId(order.getId().value())
                .status(order.getStatus().name())
                .build();
    }

    public static OrderResponse toOrderResponse(final Order order) {
        return OrderResponse.builder()
                .orderId(order.getId().value())
                .status(order.getStatus().name())
                .cancellationReason(order.getCancellationReason().map(Enum::name).orElse(null))
                .placedAt(order.getPlacedAt())
                .lastModifiedAt(order.getLastModifiedAt())
                .lines(order.getLines().stream().map(OrderToResponseMapper::toLineResponse).toList())
                .total(toMoneyResponse(order.getTotal()))
                .build();
    }

    private static OrderLineResponse toLineResponse(final OrderLine line) {
        return OrderLineResponse.builder()
                .productId(line.getProductId().value())
                .productName(line.getProductName())
                .unitPrice(toMoneyResponse(line.getUnitPrice()))
                .quantity(line.getQuantity().value())
                .lineTotal(toMoneyResponse(line.getLineTotal()))
                .build();
    }

    private static MoneyResponse toMoneyResponse(final Money money) {
        return MoneyResponse.builder()
                .amount(money.amount().toPlainString())
                .currency(money.currency().getCurrencyCode())
                .build();
    }
}
