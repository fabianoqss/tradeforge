package com.fabiano.tradeforge.controllers;

import com.fabiano.tradeforge.dtos.request.UserRequestDTO;
import com.fabiano.tradeforge.dtos.response.UserResponseDTO;
import com.fabiano.tradeforge.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {
	
	private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        return  null;
    }
	
}
