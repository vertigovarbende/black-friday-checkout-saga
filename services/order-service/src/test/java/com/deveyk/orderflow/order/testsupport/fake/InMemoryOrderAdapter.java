package com.deveyk.orderflow.order.testsupport.fake;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Sadece InMemoryOrderAdapterContractTest'ten geçtiği için var (07 §7). Her okuma yeni bir kopya döner;
// böylece iki ayrı okuma, gerçek adapter'daki gibi birbirinden bağımsız iki nesnedir.
public class InMemoryOrderAdapter implements OrderReadPort, OrderSavePort {

    private final Map<OrderId, Order> orders = new HashMap<>();

    @Override
    public Optional<Order> findById(final OrderId orderId) {
        return Optional.ofNullable(orders.get(orderId)).map(stored -> copy(stored, stored.getVersion()));
    }

    @Override
    public void create(final Order order) {
        if (orders.containsKey(order.getId())) {
            throw new IllegalStateException("Order " + order.getId().value() + " already exists");
        }
        orders.put(order.getId(), copy(order, 0L));
    }

    @Override
    public void update(final Order order) {
        final Order stored = orders.get(order.getId());
        if (stored == null) {
            throw new OrderNotFoundException(order.getId());
        }

        if (stored.getVersion() != order.getVersion()) {
            throw new OptimisticLockingFailureException("Order " + order.getId().value()
                    + " was modified concurrently: read version " + order.getVersion()
                    + ", current version " + stored.getVersion());
        }

        orders.put(order.getId(), copy(order, order.getVersion() + 1));
    }

    public int storedCount() {
        return orders.size();
    }

    private static Order copy(final Order order, final long version) {
        return Order.rehydrate()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .lines(order.getLines())
                .placedAt(order.getPlacedAt())
                .status(order.getStatus())
                .cancellationReason(order.getCancellationReason().orElse(null))
                .lastModifiedAt(order.getLastModifiedAt())
                .version(version)
                .build();
    }
}
