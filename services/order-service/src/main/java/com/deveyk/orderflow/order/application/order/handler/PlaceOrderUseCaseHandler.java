package com.deveyk.orderflow.order.application.order.handler;

import com.deveyk.orderflow.order.application.order.port.OrderDomainEventPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.order.exception.ProductNotAvailableException;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceOrderUseCaseHandler implements UseCaseHandler<Order, PlaceOrderUseCase> {

    private final ProductSnapshotReadPort productSnapshotReadPort;
    private final OrderSavePort orderSavePort;
    private final OrderDomainEventPort orderDomainEventPort;
    private final Clock clock;

    @Override
    @Transactional
    public Order handle(final PlaceOrderUseCase useCase) {

        final Set<ProductId> productIds = useCase.lines().stream()
                .map(PlaceOrderUseCase.Line::productId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final Map<ProductId, ProductSnapshot> products = productSnapshotReadPort.findAllByIds(productIds).stream()
                .collect(Collectors.toMap(ProductSnapshot::getId, Function.identity()));

        final List<ProductId> missing = productIds.stream()
                .filter(productId -> !products.containsKey(productId))
                .toList();

        if (!missing.isEmpty()) {
            throw new ProductNotAvailableException(missing);
        }

        final List<OrderLine> lines = useCase.lines().stream()
                .map(line -> toOrderLine(products.get(line.productId()), line))
                .toList();

        final Order order = Order.place(useCase.customerId(), lines, clock.instant());
        orderSavePort.create(order);
        orderDomainEventPort.publishOrderCreated(order);

        log.info("Order placed orderId={} customerId={} lines={}", order.getId().value(), order.getCustomerId().value(), order.getLines().size());
        return order;
    }

    private static OrderLine toOrderLine(final ProductSnapshot product, final PlaceOrderUseCase.Line line) {
        return OrderLine.create(product.getId(), product.getName(), product.getUnitPrice(), line.quantity());
    }
}
