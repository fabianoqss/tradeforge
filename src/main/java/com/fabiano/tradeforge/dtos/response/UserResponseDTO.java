package com.fabiano.tradeforge.dtos.response;

public record UserResponseDTO(
        Long id,
        String name,
        String nickname,
        String email
) {
}
