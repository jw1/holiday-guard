package com.jw.holidayguard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * A service to abstract the retrieval of the current user's information
 * from the Spring Security context. This allows downstream services to be
 * decoupled from the specifics of the security implementation.
 */
@Service
public class CurrentUserService {

    /**
     * Gets the username of the currently authenticated principal.
     *
     * @return The username, or a default string "anonymous" if no one is authenticated.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
