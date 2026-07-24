package com.library.security;

import com.library.domain.user.User;

/**
 * Holds the resolved internal user for the duration of a request, populated by the provisioning
 * filter so services can read the current staff member without another lookup.
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
