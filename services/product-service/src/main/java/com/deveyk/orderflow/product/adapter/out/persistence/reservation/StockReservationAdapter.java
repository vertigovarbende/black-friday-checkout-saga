package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationNotFoundException;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class StockReservationAdapter implements StockReservationReadPort, StockReservationSavePort {

    private final StockReservationRepository stockReservationRepository;

    @Override
    public Optional<StockReservation> findById(final OrderId orderId) {
        return stockReservationRepository.findWithLinesById(orderId.value()).map(StockReservationEntityToDomainMapper::toDomain);
    }

    // version null → persist (INSERT'ten önce SELECT yok). Aynı orderId ikinci kez oluşturulursa PK ihlali flush'ta
    // DataIntegrityViolationException olur (iki thread aynı anda ilk reserve'ü işlerse); retry'da ikincisi kaydı görür.
    @Override
    public void create(final StockReservation reservation) {
        stockReservationRepository.save(StockReservationDomainToEntityMapper.toNewEntity(reservation));
    }

    // Transaction dışında çağrılırsa entity hemen detach olur ve değişiklik sessizce kaybolur; bu yüzden MANDATORY.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final StockReservation reservation) {

        final StockReservationEntity entity = stockReservationRepository.findById(reservation.getId().value())
                .orElseThrow(() -> new StockReservationNotFoundException(reservation.getId()));

        if (entity.getVersion() != reservation.getVersion()) {

            String msg = "Stock reservation " + reservation.getId().value() + " was modified concurrently: read version " + reservation.getVersion() + ", current version " + entity.getVersion();
            throw new OptimisticLockingFailureException(msg);

        }

        entity.applyState(reservation.getStatus(), reservation.getLastModifiedAt());
    }
}
