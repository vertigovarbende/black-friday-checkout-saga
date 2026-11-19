package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderLineId;

import java.util.Currency;

final class OrderEntityToDomainMapper {

    private OrderEntityToDomainMapper() {
    }

    static Order toDomain(final OrderEntity entity) {
        return Order.rehydrate()
                .id(new OrderId(entity.getId()))
                .customerId(new CustomerId(entity.getCustomerId()))
                .lines(entity.getLines().stream().map(OrderEntityToDomainMapper::toDomain).toList())
                .placedAt(entity.getPlacedAt())
                .status(entity.getStatus())
                .cancellationReason(entity.getCancellationReason())
                .lastModifiedAt(entity.getLastModifiedAt())
                .version(entity.getVersion())
                .build();
    }

    private static OrderLine toDomain(final OrderLineEntity entity) {
        return OrderLine.rehydrate()
                .id(new OrderLineId(entity.getId()))
                .productId(new ProductId(entity.getProductId()))
                .productName(entity.getProductName())
                .unitPrice(new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency())))
                .quantity(new Quantity(entity.getQuantity()))
                .build();
    }
}
