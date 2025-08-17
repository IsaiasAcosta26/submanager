//package com.submanager.submanager.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public Map<String, String> handleAuthErrors(Exception ex) {
//        return Map.of("error", "Credenciales inválidas o usuario no existe");
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public Map<String, String> handleConflict(IllegalArgumentException ex) {
//        return Map.of("error", ex.getMessage());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
//        return errors;
//    }
//}
