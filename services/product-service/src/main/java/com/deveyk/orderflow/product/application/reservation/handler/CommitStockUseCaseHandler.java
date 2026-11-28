package com.deveyk.orderflow.product.application.reservation.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.application.reservation.usecase.CommitStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationNotFoundException;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitStockUseCaseHandler implements UseCaseHandler<StockReservation, CommitStockUseCase> {

    private final StockReservationReadPort stockReservationReadPort;
    private final StockReservationSavePort stockReservationSavePort;
    private final ProductReadPort productReadPort;
    private final ProductSavePort productSavePort;
    private final Clock clock;

    // Rezervasyon yoksa not-found → kısa retry → DLT; RELEASED / REJECTED InvalidReservationStateException → DLT.
    @Override
    @Transactional
    public StockReservation handle(final CommitStockUseCase useCase) {

        final StockReservation reservation = stockReservationReadPort.findById(useCase.orderId())
                .orElseThrow(() -> new StockReservationNotFoundException(useCase.orderId()));

        final Map<ProductId, Product> products = productReadPort.findAllByIdsAsMap(reservation.productIds());

        if (!reservation.commit(products, clock.instant())) {
            log.info("Stock commit repeated orderId={}", reservation.getId().value());
            return reservation;
        }

        stockReservationSavePort.update(reservation);
        products.values().forEach(productSavePort::update);

        log.info("Stock committed orderId={} lines={}", reservation.getId().value(), reservation.getLines().size());
        return reservation;
    }
}
