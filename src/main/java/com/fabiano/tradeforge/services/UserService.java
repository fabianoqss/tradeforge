package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.UserRequestDTO;
import com.fabiano.tradeforge.dtos.response.UserResponseDTO;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Role;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.projections.UserDetailsProjection;
import com.fabiano.tradeforge.repositories.RoleRepository;
import com.fabiano.tradeforge.repositories.UserRepository;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import com.fabiano.tradeforge.services.exceptions.UserAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> result = userRepository
                .searchUserAndRolesByEmail(username);
        if(result.size() == 0){
            throw new UsernameNotFoundException("Email not found");
        }
        User user = new User();
        user.setEmail(result.get(0).getUsername());
        user.setPassword(result.get(0).getPassword());
        user.setEnabled(result.get(0).getEnabled());
        for(UserDetailsProjection projection : result){
            user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
        }
        return user;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByCpf(userRequestDTO.CPF())){
            throw new UserAlreadyExistsException("User Already Exists");
        }

        User user = new User();
        copyDtoToEntity(user, userRequestDTO);

        Portfolio portfolio = new Portfolio(user, new BigDecimal("10000.00"), Instant.now());
        user.setPortfolio(portfolio);

        Role role = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER not found"));
        user.addRole(role);

        user = userRepository.save(user);

        return  new UserResponseDTO(user.getId(), user.getName(), user.getNickname(), user.getEmail());
    }

    @Transactional
    public void deleteUser(Long id){
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User Not Found");
        }
        try{
            userRepository.deleteById(id);
        }catch(DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("Referential integrity violation");
        }

    }

    @Transactional
    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        User entity = authenticated();
        return new UserResponseDTO(entity.getId(), entity.getName(), entity.getNickname(), entity.getEmail());
    }

    @Transactional(readOnly = true)
    public User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return userRepository.findByEmail(username).get();
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("Invalid user");
        }
    }

    private void copyDtoToEntity(User user, UserRequestDTO userRequestDTO) {
        user.setEmail(userRequestDTO.email());
        user.setNickname(userRequestDTO.nickname());
        user.setName(userRequestDTO.name());
        user.setPassword(passwordEncoder.encode(userRequestDTO.password()));
        user.setCpf(userRequestDTO.CPF());
    }
}
