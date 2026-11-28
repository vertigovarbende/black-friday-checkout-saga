package com.deveyk.orderflow.product.adapter.in.web.facade;

import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.response.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductFacade {

    List<ProductResponse> listProducts();

    ProductResponse changeProductDetails(UUID productId, ChangeProductDetailsRequest request);
}
