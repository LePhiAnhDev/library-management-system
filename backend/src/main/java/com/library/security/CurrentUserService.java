package com.library.security;

import com.library.domain.user.User;
import com.library.domain.user.UserRepository;
import com.library.domain.user.UserStatus;
import com.library.security.clerk.ClerkUserClient;
import com.library.security.clerk.ClerkUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Resolves and provisions the internal staff profile for the authenticated Clerk user.
 * Provisioning runs outside any write transaction (the Clerk API call sits between the read
 * and the save), and tolerates a concurrent first request via the unique constraint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final ClerkUserClient clerkUserClient;

    /**
     * Returns the existing user for the token subject, creating it from Clerk on first sight.
     */
    public User provision(Jwt jwt) {
        String clerkUserId = jwt.getSubject();
        return userRepository.findByClerkUserId(clerkUserId)
                .orElseGet(() -> createFromClerk(clerkUserId));
    }

    private User createFromClerk(String clerkUserId) {
        ClerkUserInfo info = clerkUserClient.fetchUser(clerkUserId).orElse(null);
        User user = User.builder()
                .clerkUserId(clerkUserId)
                .email(info != null ? info.email() : null)
                .fullName(info != null ? info.fullName() : null)
                .avatarUrl(info != null ? info.avatarUrl() : null)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException race) {
            // A concurrent first request already inserted the row; read it back.
            return userRepository.findByClerkUserId(clerkUserId).orElseThrow(() -> race);
        }
    }

    /**
     * Current staff member for the request. Reads the value cached by the provisioning filter,
     * falling back to resolving from the security context if it is not present.
     */
    public User getCurrentUser() {
        User cached = CurrentUserHolder.get();
        if (cached != null) {
            return cached;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            User user = provision(jwtAuth.getToken());
            CurrentUserHolder.set(user);
            return user;
        }
        throw new IllegalStateException("Không có người dùng đã xác thực trong ngữ cảnh hiện tại");
    }
}
