package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.UserRequestDTO;
import com.fabiano.tradeforge.dtos.response.UserResponseDTO;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Role;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.repositories.RoleRepository;
import com.fabiano.tradeforge.repositories.UserRepository;
import com.fabiano.tradeforge.services.exceptions.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByCpf(userRequestDTO.CPF())){
            throw new UserAlreadyExistsException("User Already Exists");
        }

        User user = new User();
        copyDtoToEntity(user, userRequestDTO);

        Portfolio portfolio = new Portfolio(user, 10000.00, Instant.now());
        user.setPortfolio(portfolio);

        Role role = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER not found"));
        user.addRole(role);

        user = userRepository.save(user);

        return  new UserResponseDTO(user.getName(), user.getNickname(), user.getEmail());
    }

    public void copyDtoToEntity(User user, UserRequestDTO userRequestDTO) {
        user.setEmail(userRequestDTO.email());
        user.setNickname(userRequestDTO.nickname());
        user.setName(userRequestDTO.name());
        user.setPassword(passwordEncoder.encode(userRequestDTO.password()));
        user.setCpf(userRequestDTO.CPF());
    }


}
