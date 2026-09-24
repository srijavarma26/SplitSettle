package com.splitsettle.userservice.service;

import com.splitsettle.userservice.dto.AuthResponse;
import com.splitsettle.userservice.dto.LoginRequest;
import com.splitsettle.userservice.dto.RegisterRequest;
import com.splitsettle.userservice.dto.UserDTO;
import com.splitsettle.userservice.entity.User;
import com.splitsettle.userservice.exception.InvalidCredentialsException;
import com.splitsettle.userservice.exception.UserAlreadyExistsException;
import com.splitsettle.userservice.exception.UserNotFoundException;
import com.splitsettle.userservice.repository.UserRepository;
import com.splitsettle.userservice.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, mapToDTO(user));
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
