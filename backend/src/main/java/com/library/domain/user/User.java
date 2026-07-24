package com.library.domain.user;

import com.library.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

/**
 * Internal staff profile, provisioned one-to-one from a Clerk account via clerk_user_id (the token sub).
 * Holds no password and no role; it exists so business records can reference the staff member
 * who handled an operation (borrow, return, settle a fine).
 */
@Entity
@Table(name = "users")
@BatchSize(size = 64)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "clerk_user_id", nullable = false, unique = true, updatable = false)
    private String clerkUserId;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;
}
