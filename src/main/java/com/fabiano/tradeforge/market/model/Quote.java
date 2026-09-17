package com.fabiano.tradeforge.market.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Quote(
        String symbol,
        BigDecimal price,
        Instant timestamp
) {
}
