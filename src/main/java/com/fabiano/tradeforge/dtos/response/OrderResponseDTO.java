package com.fabiano.tradeforge.dtos.response;

import com.fabiano.tradeforge.enums.OrderSide;
import com.fabiano.tradeforge.enums.OrderStatus;
import com.fabiano.tradeforge.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponseDTO(
        Long id,
        String assetSymbol,
        OrderSide side,
        OrderType type,
        Integer quantity,
        BigDecimal limitPrice,
        OrderStatus status,
        Instant createdAt,
        Instant executedAt
) {
}
