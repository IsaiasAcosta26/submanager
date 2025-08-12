//package com.submanager.submanager.service;
//
//import com.submanager.submanager.dto.AuthResponseDTO;
//import com.submanager.submanager.dto.LoginRequestDTO;
//import com.submanager.submanager.dto.RegisterRequestDTO;
//import com.submanager.submanager.model.entity.User;
//import com.submanager.submanager.repository.UserRepository;
//import com.submanager.submanager.util.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//    private final AuthenticationManager authManager;
//
//    public AuthResponseDTO register(RegisterRequestDTO request) {
//        User user = User.builder()
//                .username(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
//                .premium(false)
//                .build();
//
//        userRepository.save(user);
//        String token = jwtUtil.generateToken(user);
//        return new AuthResponseDTO(token);
//    }
//
//    public AuthResponseDTO login(LoginRequestDTO request) {
//        authManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//        );
//
//        User user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        String token = jwtUtil.generateToken(user);
//        return new AuthResponseDTO(token);
//    }
//}
