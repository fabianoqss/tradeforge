package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.UserResponseDTO;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.repositories.RoleRepository;
import com.fabiano.tradeforge.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Jwt jwtWithUsername(String username) {
        return Jwt.withTokenValue("token-value")
                .header("alg", "none")
                .claim("username", username)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void getMeShouldReturnAuthenticatedUserData() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, roleRepository, passwordEncoder);

        User user = new User();
        user.setId(1L);
        user.setName("Fabiano Quirino");
        user.setNickname("fabiano");
        user.setEmail("fabiano@tradeforge.com");

        Mockito.when(userRepository.findByEmail("fabiano@tradeforge.com"))
                .thenReturn(Optional.of(user));

        Jwt jwt = jwtWithUsername("fabiano@tradeforge.com");
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(jwt, null));

        UserResponseDTO dto = userService.getMe();

        assertEquals(new UserResponseDTO(1L, "Fabiano Quirino", "fabiano", "fabiano@tradeforge.com"), dto);
    }

    @Test
    void authenticatedShouldThrowWhenThereIsNoAuthentication() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, roleRepository, passwordEncoder);

        SecurityContextHolder.clearContext();

        assertThrows(UsernameNotFoundException.class, userService::authenticated);
    }

    @Test
    void authenticatedShouldThrowWhenUserFromTokenDoesNotExist() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, roleRepository, passwordEncoder);

        Mockito.when(userRepository.findByEmail("ghost@tradeforge.com"))
                .thenReturn(Optional.empty());

        Jwt jwt = jwtWithUsername("ghost@tradeforge.com");
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertThrows(UsernameNotFoundException.class, userService::authenticated);
    }
}
