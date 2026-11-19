package com.deveyk.orderflow.order.application.order.handler;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetOrderUseCaseHandler implements UseCaseHandler<Order, GetOrderUseCase> {

    private final OrderReadPort orderReadPort;

    // readOnly → replica (ADR-0004). Başka müşterinin siparişi yokmuş gibi davranılır (404);
    // auth gelirse bu durum 403'e dönebilir (ROADMAP, kapsam dışı: auth).
    @Override
    @Transactional(readOnly = true)
    public Order handle(final GetOrderUseCase useCase) {
        return orderReadPort.findById(useCase.orderId())
                .filter(order -> order.getCustomerId().equals(useCase.customerId()))
                .orElseThrow(() -> new OrderNotFoundException(useCase.orderId()));
    }
}
