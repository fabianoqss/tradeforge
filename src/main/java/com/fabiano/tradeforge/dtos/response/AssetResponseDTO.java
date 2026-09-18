package com.fabiano.tradeforge.dtos.response;

import com.fabiano.tradeforge.enums.AssetType;

public record AssetResponseDTO(
        String symbol,
        String name,
        AssetType assetType,
        String exchange,
        String currency,
        Boolean tradable
) {
}
