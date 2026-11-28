package com.deveyk.orderflow.product.domain.product.exception;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;

import java.io.Serial;

// Product.reserve'ün değişmezi. Saga'daki yetersiz stok bu exception değildir, REJECTED rezervasyondur (05 §8):
// StockReservation.request önce canReserve'e bakar, doğru kullanımda bu hiç fırlamaz.
public final class InsufficientStockException extends OrderflowBusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(final ProductId productId, final Quantity available, final Quantity requested) {
        super(ProductErrorCode.INSUFFICIENT_STOCK, productId.value().toString(),
                String.valueOf(available.value()), String.valueOf(requested.value()));
    }
}
