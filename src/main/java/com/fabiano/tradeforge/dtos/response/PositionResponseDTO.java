package com.fabiano.tradeforge.dtos.response;

import java.math.BigDecimal;

public record PositionResponseDTO(
        String assetSymbol,
        Integer quantity,
        BigDecimal averagePrice,
        BigDecimal realizedPnl
) {
}
