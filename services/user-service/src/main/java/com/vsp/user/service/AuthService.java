package com.vsp.user.service;

import com.vsp.user.model.entity.User;
import com.vsp.user.repository.UserRepository;
import com.vsp.user.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Map<String, Object> register(String email, String username, String displayName, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setVerified(false);
        User saved = userRepository.save(user);
        return authResponse(saved);
    }

    public Map<String, Object> login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return authResponse(user);
    }

    private Map<String, Object> authResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "username", user.getUsername()
        );
    }
}
