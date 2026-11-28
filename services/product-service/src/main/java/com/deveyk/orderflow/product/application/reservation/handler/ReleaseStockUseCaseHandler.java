package com.deveyk.orderflow.product.application.reservation.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockSagaEventPort;
import com.deveyk.orderflow.product.application.reservation.usecase.ReleaseStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleaseStockUseCaseHandler implements UseCaseHandler<StockReservation, ReleaseStockUseCase> {

    private final StockReservationReadPort stockReservationReadPort;
    private final StockReservationSavePort stockReservationSavePort;
    private final ProductReadPort productReadPort;
    private final ProductSavePort productSavePort;
    private final StockSagaEventPort stockSagaEventPort;
    private final Clock clock;

    // Cevap her durumda yayınlanır (checkout-saga.md §5); COMMITTED rezervasyon InvalidReservationStateException → DLT.
    @Override
    @Transactional
    public StockReservation handle(final ReleaseStockUseCase useCase) {

        final Optional<StockReservation> existing = stockReservationReadPort.findById(useCase.orderId());

        // Reserve'den önce gelen release (saga T2'de timeout): kapanış kaydı; sonra gelen reserve stoğa dokunmaz.
        if (existing.isEmpty()) {
            final StockReservation closed = StockReservation.closed(useCase.orderId(), clock.instant());
            stockReservationSavePort.create(closed);
            stockSagaEventPort.publishStockReleased(closed);
            log.info("Stock release before reserve, closing record created orderId={}", closed.getId().value());
            return closed;
        }

        final StockReservation reservation = existing.get();
        final Map<ProductId, Product> products = productReadPort.findAllByIdsAsMap(reservation.productIds());

        if (reservation.release(products, clock.instant())) {
            stockReservationSavePort.update(reservation);
            products.values().forEach(productSavePort::update);
            log.info("Stock released orderId={} lines={}", reservation.getId().value(), reservation.getLines().size());
        } else {
            log.info("Stock release repeated, reply published again orderId={} status={}", reservation.getId().value(), reservation.getStatus());
        }

        stockSagaEventPort.publishStockReleased(reservation);
        return reservation;
    }
}
