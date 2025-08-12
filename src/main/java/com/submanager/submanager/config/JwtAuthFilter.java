//package com.submanager.submanager.config;
//
//import com.submanager.submanager.repository.UserRepository;
//import com.submanager.submanager.util.JwtUtil;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends GenericFilter {
//
//    private final JwtUtil jwtUtil;
//    private final UserRepository userRepository;
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String header = httpRequest.getHeader("Authorization");
//
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            String username = jwtUtil.extractUsername(token);
//
//            userRepository.findByUsername(username).ifPresent(user -> {
//                if (jwtUtil.isTokenValid(token, user)) {
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(user, null, null);
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                }
//            });
//        }
//
//        chain.doFilter(request, response);
//    }
//}
