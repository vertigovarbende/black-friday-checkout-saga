package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.time.Instant;
import java.util.List;

public class OrderBuilder implements TestDataBuilder<Order> {

    private OrderId id;
    private CustomerId customerId;
    private List<OrderLine> lines;
    private Instant placedAt;
    private OrderStatus status;
    private CancellationReason cancellationReason;
    private Instant lastModifiedAt;
    private long version;

    public OrderBuilder withValidValues() {
        this.id = OrderId.newId();
        this.customerId = new CustomerId(OrderValidTestData.Customer.ID);
        this.lines = List.of(new OrderLineBuilder().withValidValues().build());
        this.placedAt = OrderValidTestData.NOW;
        this.status = OrderStatus.PENDING;
        this.cancellationReason = null;
        this.lastModifiedAt = OrderValidTestData.NOW;
        this.version = 0L;
        return this;
    }

    public OrderBuilder withId(final OrderId id) {
        this.id = id;
        return this;
    }

    public OrderBuilder withCustomerId(final CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderBuilder withStatus(final OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderBuilder withCancellationReason(final CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
        return this;
    }

    public OrderBuilder withLines(final List<OrderLine> lines) {
        this.lines = lines;
        return this;
    }

    @Override
    public Order build() {
        return Order.rehydrate()
                .id(this.id)
                .customerId(this.customerId)
                .lines(this.lines)
                .placedAt(this.placedAt)
                .status(this.status)
                .cancellationReason(this.cancellationReason)
                .lastModifiedAt(this.lastModifiedAt)
                .version(this.version)
                .build();
    }
}
