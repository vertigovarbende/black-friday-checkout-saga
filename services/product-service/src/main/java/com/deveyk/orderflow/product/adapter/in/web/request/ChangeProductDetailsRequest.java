package com.deveyk.orderflow.product.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

// PATCH: verilmeyen (null) alan değişmez. Adın boş olmaması ve fiyatın pozitifliği domain'in kuralı (PRD_003, PRD_006).
public record ChangeProductDetailsRequest(

        @Size(max = 200, message = "Name can be at most 200 characters")
        String name,

        @Valid
        MoneyRequest unitPrice

) {

}
