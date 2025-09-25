package com.jw.holidayguard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/principal")
    public ResponseEntity<?> getUserPrincipal(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("isAuthenticated", false));
        }

        Map<String, Object> principalData = Map.of(
            "username", authentication.getName(),
            "roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );

        return ResponseEntity.ok(principalData);
    }
}
