package com.deveyk.orderflow.product.application.reservation.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockSagaEventPort;
import com.deveyk.orderflow.product.application.reservation.usecase.ReserveStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.model.RequestedLine;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveStockUseCaseHandler implements UseCaseHandler<StockReservation, ReserveStockUseCase> {

    private final StockReservationReadPort stockReservationReadPort;
    private final StockReservationSavePort stockReservationSavePort;
    private final ProductReadPort productReadPort;
    private final ProductSavePort productSavePort;
    private final StockSagaEventPort stockSagaEventPort;
    private final Clock clock;

    // Listener'dan inbox'ın transaction'ı içinde çağrılır: inbox kaydı, rezervasyon, ürünler ve cevabın outbox kaydı
    // birlikte commit edilir.
    @Override
    @Transactional
    public StockReservation handle(final ReserveStockUseCase useCase) {

        final Optional<StockReservation> existing = stockReservationReadPort.findById(useCase.orderId());

        // Aynı niyet farklı eventId ile (durum koruması, checkout-saga.md §5): durum değişmez, cevap yeniden yayınlanır.
        if (existing.isPresent()) {
            final StockReservation reservation = existing.get();
            publishReply(reservation);
            log.info("Stock reserve request repeated, reply published again orderId={} status={}", reservation.getId().value(), reservation.getStatus());
            return reservation;
        }

        final List<ProductId> productIds = useCase.lines().stream().map(RequestedLine::productId).toList();
        final Map<ProductId, Product> products = productReadPort.findAllByIdsAsMap(productIds);

        final StockReservation reservation = StockReservation.request(useCase.orderId(), useCase.lines(), products, clock.instant());
        stockReservationSavePort.create(reservation);
        products.values().forEach(productSavePort::update);   // ret durumunda hiçbiri değişmemiştir
        publishReply(reservation);

        log.info("Stock reservation requested orderId={} status={} rejection={}", reservation.getId().value(),
                reservation.getStatus(), reservation.getRejection().map(rejection -> rejection.reason().name()).orElse(null));
        return reservation;
    }

    private void publishReply(final StockReservation reservation) {
        reservation.rejectionOfReserveRequest().ifPresentOrElse(
                rejection -> stockSagaEventPort.publishStockReserveFailed(reservation.getId(), rejection),
                () -> stockSagaEventPort.publishStockReserved(reservation)
        );
    }
}
