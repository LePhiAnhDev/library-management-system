package com.library.security;

import com.library.domain.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs after the bearer token filter. For authenticated requests it ensures the internal user
 * exists (provisioning from Clerk on first sight) and caches it for the request so downstream
 * services never trigger a Clerk call inside a write transaction. Registered in SecurityConfig.
 */
@Slf4j
@RequiredArgsConstructor
public class ClerkUserProvisioningFilter extends OncePerRequestFilter {

    private final CurrentUserService currentUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            try {
                User user = currentUserService.provision(jwtAuth.getToken());
                CurrentUserHolder.set(user);
            } catch (Exception ex) {
                log.warn("Provision người dùng thất bại cho sub {}: {}", jwtAuth.getName(), ex.getMessage());
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
        }
    }
}
