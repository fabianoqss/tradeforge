package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.UserRequestDTO;
import com.fabiano.tradeforge.dtos.response.UserResponseDTO;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.repositories.UserRepository;
import com.fabiano.tradeforge.services.exceptions.UserAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByCpf(userRequestDTO.CPF())){
            throw new UserAlreadyExistsException("User Already Exists");
        }

        User user = new User();
        copyDtoToEntity(user, userRequestDTO);
        user = userRepository.save(user);


        return  new UserResponseDTO(user.getName(), user.getEmail(), user.getPassword());
    }

    public void copyDtoToEntity(User user, UserRequestDTO userRequestDTO) {
        user.setEmail(userRequestDTO.email());
        user.setNickname(userRequestDTO.nickname());
        user.setName(userRequestDTO.name());
        user.setPassword(userRequestDTO.password());
        user.setCpf(userRequestDTO.CPF());
    }


}
