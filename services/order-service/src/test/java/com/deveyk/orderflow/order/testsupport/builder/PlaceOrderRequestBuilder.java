package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlaceOrderRequestBuilder implements TestDataBuilder<PlaceOrderRequest> {

    private List<PlaceOrderRequest.Line> lines = new ArrayList<>();

    public PlaceOrderRequestBuilder withValidValues() {
        this.lines = new ArrayList<>(List.of(new PlaceOrderRequest.Line(OrderValidTestData.Product.ID, 1)));
        return this;
    }

    // Bütün satırları değiştirir; boş liste ya da null da verilebilir (validation testleri)
    public PlaceOrderRequestBuilder withLines(final List<PlaceOrderRequest.Line> lines) {
        this.lines = lines;
        return this;
    }

    // Satır ekler: new PlaceOrderRequestBuilder().withLine(a, 2).withLine(b, 1)
    public PlaceOrderRequestBuilder withLine(final UUID productId, final int quantity) {
        this.lines.add(new PlaceOrderRequest.Line(productId, quantity));
        return this;
    }

    @Override
    public PlaceOrderRequest build() {
        return new PlaceOrderRequest(this.lines);
    }
}
