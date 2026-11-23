package com.deveyk.orderflow.order.adapter.in.web.controller;

import com.deveyk.orderflow.order.adapter.in.web.facade.OrderFacade;
import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.shared.observability.OrderflowHeaders;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
class OrderController {

    static final String ORDER_PATH = "/{orderId}";

    private final OrderFacade orderFacade;

    @PostMapping
    ResponseEntity<PlaceOrderResponse> placeOrder(@RequestHeader(OrderflowHeaders.USER_ID) final UUID userId,
                                                  @Valid @RequestBody final PlaceOrderRequest request) {

        final PlaceOrderResponse response = orderFacade.placeOrder(userId, request);

        final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path(ORDER_PATH)
                .buildAndExpand(response.orderId())
                .toUri();

        return ResponseEntity.accepted().location(location).body(response);
    }


    @GetMapping(ORDER_PATH)
    ResponseEntity<OrderResponse> getOrder(@RequestHeader(OrderflowHeaders.USER_ID) final UUID userId,
                                           @PathVariable final UUID orderId) {
        final OrderResponse response = orderFacade.getOrder(userId, orderId);
        return ResponseEntity.ok(response);
    }
}
