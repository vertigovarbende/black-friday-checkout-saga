package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;

import java.util.List;

final class OrderDomainToEntityMapper {

    private OrderDomainToEntityMapper() {
    }

    static OrderEntity toNewEntity(final Order order) {
        final OrderEntity entity = OrderEntity.builder()
                .id(order.getId().value())
                .customerId(order.getCustomerId().value())
                .status(order.getStatus())
                .cancellationReason(order.getCancellationReason().orElse(null))
                .placedAt(order.getPlacedAt())
                .lastModifiedAt(order.getLastModifiedAt())
                .build();

        final List<OrderLine> lines = order.getLines();
        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            final OrderLine line = lines.get(lineNo);
            entity.addLine(OrderLineEntity.builder()
                    .id(line.getId().value())
                    .order(entity)
                    .lineNo(lineNo)
                    .productId(line.getProductId().value())
                    .productName(line.getProductName())
                    .unitPrice(line.getUnitPrice().amount())
                    .currency(line.getUnitPrice().currency().getCurrencyCode())
                    .quantity(line.getQuantity().value())
                    .build());
        }
        return entity;
    }
}
