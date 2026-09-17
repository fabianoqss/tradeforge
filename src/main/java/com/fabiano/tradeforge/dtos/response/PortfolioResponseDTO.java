package com.fabiano.tradeforge.dtos.response;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponseDTO(
        BigDecimal cashBalance,
        List<PositionResponseDTO> positions
) {
}
