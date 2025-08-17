//package com.submanager.submanager.controller;
//
//import com.submanager.submanager.dto.AuthResponseDTO;
//import com.submanager.submanager.dto.LoginRequestDTO;
//import com.submanager.submanager.dto.RegisterRequestDTO;
//import com.submanager.submanager.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
//        return ResponseEntity.ok(authService.register(request));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//}
