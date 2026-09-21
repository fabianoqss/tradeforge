package com.fabiano.tradeforge.dtos.request;

import com.fabiano.tradeforge.enums.OrderSide;
import com.fabiano.tradeforge.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequestDTO(

        @NotBlank
        String assetSymbol,

        @NotNull
        OrderSide side,

        @NotNull
        OrderType type,

        @NotNull
        @Positive
        Integer quantity,

        BigDecimal limitPrice

) {
}
