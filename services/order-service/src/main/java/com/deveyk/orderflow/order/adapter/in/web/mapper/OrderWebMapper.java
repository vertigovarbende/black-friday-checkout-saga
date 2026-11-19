package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.MoneyResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderLineResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderWebMapper {

    @Mapping(target = "customerId", source = "userId")
    @Mapping(target = "lines", source = "request.lines")
    PlaceOrderUseCase toPlaceOrderUseCase(UUID userId, PlaceOrderRequest request);

    PlaceOrderUseCase.Line toUseCaseLine(PlaceOrderRequest.Line line);

    @Mapping(target = "customerId", source = "userId")
    @Mapping(target = "orderId", source = "orderId")
    GetOrderUseCase toGetOrderUseCase(UUID userId, UUID orderId);

    @Mapping(target = "orderId", source = "id")
    PlaceOrderResponse toPlaceOrderResponse(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "cancellationReason", expression = "java(order.getCancellationReason().map(Enum::name).orElse(null))")
    OrderResponse toOrderResponse(Order order);

    OrderLineResponse toOrderLineResponse(OrderLine line);

    default MoneyResponse toMoneyResponse(final Money money) {
        return new MoneyResponse(money.amount().toPlainString(), money.currency().getCurrencyCode());
    }

    default CustomerId toCustomerId(final UUID value) {
        return new CustomerId(value);
    }

    default OrderId toOrderId(final UUID value) {
        return new OrderId(value);
    }

    default ProductId toProductId(final UUID value) {
        return new ProductId(value);
    }

    default Quantity toQuantity(final int value) {
        return new Quantity(value);
    }

    default UUID fromOrderId(final OrderId orderId) {
        return orderId.value();
    }

    default UUID fromProductId(final ProductId productId) {
        return productId.value();
    }

    default int fromQuantity(final Quantity quantity) {
        return quantity.value();
    }
}
