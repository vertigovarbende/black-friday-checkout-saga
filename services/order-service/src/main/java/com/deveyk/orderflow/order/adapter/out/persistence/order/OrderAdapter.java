package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class OrderAdapter implements OrderReadPort, OrderSavePort {

    private final OrderRepository orderRepository;

    @Override
    public Optional<Order> findById(final OrderId orderId) {
        return orderRepository.findWithLinesById(orderId.value()).map(OrderEntityToDomainMapper::toDomain);
    }

    @Override
    public void create(final Order order) {
        orderRepository.save(OrderDomainToEntityMapper.toNewEntity(order));
    }

    // Transaction dışında çağrılırsa entity hemen detach olur ve değişiklik sessizce kaybolur; bu yüzden MANDATORY.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final Order order) {

        final OrderEntity entity = orderRepository.findById(order.getId().value())
                .orElseThrow(() -> new OrderNotFoundException(order.getId()));

        if (entity.getVersion() != order.getVersion()) {

            String msg = "Order " + order.getId().value() + " was modified concurrently: read version " + order.getVersion() + ", current version " + entity.getVersion();
            throw new OptimisticLockingFailureException(msg);

        }

        entity.applyState(order.getStatus(), order.getCancellationReason().orElse(null), order.getLastModifiedAt());
    }
}
