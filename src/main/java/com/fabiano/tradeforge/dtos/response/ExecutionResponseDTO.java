package com.fabiano.tradeforge.dtos.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ExecutionResponseDTO(
        Long id,
        Long orderId,
        BigDecimal executedPrice,
        Integer executedQuantity,
        Instant executedAt
) {
}
