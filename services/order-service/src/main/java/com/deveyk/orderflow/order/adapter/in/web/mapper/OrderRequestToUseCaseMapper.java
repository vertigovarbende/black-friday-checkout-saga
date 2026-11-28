package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.OrderId;

import java.util.UUID;

public final class OrderRequestToUseCaseMapper {

    private OrderRequestToUseCaseMapper() {
    }

    // X-User-Id bu bağlamda müşteridir.
    public static PlaceOrderUseCase toPlaceOrderUseCase(final UUID userId, final PlaceOrderRequest request) {
        return new PlaceOrderUseCase(new CustomerId(userId), request.lines().stream()
                .map(OrderRequestToUseCaseMapper::toUseCaseLine)
                .toList());
    }

    public static GetOrderUseCase toGetOrderUseCase(final UUID userId, final UUID orderId) {
        return new GetOrderUseCase(new OrderId(orderId), new CustomerId(userId));
    }

    private static PlaceOrderUseCase.Line toUseCaseLine(final PlaceOrderRequest.Line line) {
        return new PlaceOrderUseCase.Line(new ProductId(line.productId()), new Quantity(line.quantity()));
    }
}
