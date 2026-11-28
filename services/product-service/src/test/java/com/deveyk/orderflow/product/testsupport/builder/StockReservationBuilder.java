package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationLine;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StockReservationBuilder implements TestDataBuilder<StockReservation> {

    private OrderId id;
    private List<ReservationLine> lines = new ArrayList<>();
    private ReservationStatus status;
    private StockRejection rejection;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private long version;

    // RESERVED, tek satır: ProductValidTestData.Product'tan 2 adet
    public StockReservationBuilder withValidValues() {
        this.id = ProductValidTestData.Order.ORDER_ID;
        this.lines = new ArrayList<>(List.of(new ReservationLine(ProductValidTestData.Product.PRODUCT_ID, ProductValidTestData.Order.QUANTITY)));
        this.status = ReservationStatus.RESERVED;
        this.rejection = null;
        this.createdAt = ProductValidTestData.NOW;
        this.lastModifiedAt = ProductValidTestData.NOW;
        this.version = 0L;
        return this;
    }

    // REJECTED, sebebi verilen satırın ürünü
    public StockReservationBuilder withRejection(final RejectionReason reason) {
        this.status = ReservationStatus.REJECTED;
        this.rejection = new StockRejection(reason, this.lines.getFirst().productId());
        return this;
    }

    // Satırsız RELEASED kapanış kaydı
    public StockReservationBuilder withClosingRecord() {
        this.status = ReservationStatus.RELEASED;
        this.lines = new ArrayList<>();
        return this;
    }

    public StockReservationBuilder withId(final OrderId id) {
        this.id = id;
        return this;
    }

    public StockReservationBuilder withLine(final ProductId productId, final int quantity) {
        this.lines.add(new ReservationLine(productId, new Quantity(quantity)));
        return this;
    }

    public StockReservationBuilder withLines(final List<ReservationLine> lines) {
        this.lines = lines == null ? null : new ArrayList<>(lines);
        return this;
    }

    public StockReservationBuilder withStatus(final ReservationStatus status) {
        this.status = status;
        return this;
    }

    public StockReservationBuilder withRejection(final StockRejection rejection) {
        this.rejection = rejection;
        return this;
    }

    public StockReservationBuilder withCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public StockReservationBuilder withLastModifiedAt(final Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    public StockReservationBuilder withVersion(final long version) {
        this.version = version;
        return this;
    }

    @Override
    public StockReservation build() {
        return StockReservation.rehydrate()
                .id(this.id)
                .lines(this.lines)
                .status(this.status)
                .rejection(this.rejection)
                .createdAt(this.createdAt)
                .lastModifiedAt(this.lastModifiedAt)
                .version(this.version)
                .build();
    }
}
