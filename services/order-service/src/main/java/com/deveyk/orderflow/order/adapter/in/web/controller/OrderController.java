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

    private final OrderFacade orderFacade;

    // Komut: 202 + Location + PENDING. Sonraki durum değişiklikleri asenkron gelir (ADR-0011).
    @PostMapping
    ResponseEntity<PlaceOrderResponse> placeOrder(@RequestHeader(OrderflowHeaders.USER_ID) final UUID userId,
                                                  @Valid @RequestBody final PlaceOrderRequest request) {
        final PlaceOrderResponse response = orderFacade.placeOrder(userId, request);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{orderId}")
                .buildAndExpand(response.orderId())
                .toUri();
        return ResponseEntity.accepted().location(location).body(response);
    }

    // Replica'dan okunur; sipariş verildikten hemen sonra kısa bir süre 404 dönebilir (ADR-0011).
    @GetMapping("/{orderId}")
    OrderResponse getOrder(@RequestHeader(OrderflowHeaders.USER_ID) final UUID userId,
                           @PathVariable final UUID orderId) {
        return orderFacade.getOrder(userId, orderId);
    }
}
