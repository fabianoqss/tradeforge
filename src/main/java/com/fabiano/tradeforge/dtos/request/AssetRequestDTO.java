package com.fabiano.tradeforge.dtos.request;

import com.fabiano.tradeforge.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequestDTO(

        @NotBlank
        String symbol,

        @NotBlank
        String name,

        @NotNull
        AssetType assetType,

        @NotBlank
        String exchange,

        @NotBlank
        String currency,

        @NotNull
        Boolean tradable

) {
}
