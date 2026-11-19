package com.deveyk.orderflow.order.application.product.port;

import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;

import java.util.Collection;
import java.util.List;

public interface ProductSnapshotReadPort {

    // Bulunamayan id'ler sonuçta yer almaz; exception fırlatılmaz.
    List<ProductSnapshot> findAllByIds(Collection<ProductId> productIds);
}
