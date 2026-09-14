package com.fabiano.tradeforge.dtos.request;

public record UserRequestDTO(
        String CPF,
        String password,
        String email,
        String nickname,
        String name

) {
}
