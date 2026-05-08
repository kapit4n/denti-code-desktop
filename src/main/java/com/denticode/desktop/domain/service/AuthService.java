package com.denticode.desktop.domain.service;

import com.denticode.desktop.domain.model.Role;
import com.denticode.desktop.domain.model.User;
import com.denticode.desktop.infra.repo.UserRepository;
import com.denticode.desktop.infra.security.PasswordHasher;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Authenticates against locally stored users. Mirrors the {@code authApiSlice}
 * login mutation from the Next.js app, but everything stays on-device.
 */
public final class AuthService {

    private final UserRepository users;
    private final PasswordHasher hasher;

    public AuthService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public Optional<User> authenticate(String email, String password) {
        return users.findByEmail(email).filter(u -> u.isActive() && hasher.verify(password, u.getPasswordHash()));
    }

    public User createUser(String email, String password, String displayName, Set<Role> roles) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password too short");
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setDisplayName(displayName);
        u.setPasswordHash(hasher.hash(password));
        u.setRoles(EnumSet.copyOf(roles));
        return users.persist(u);
    }

    public void changePassword(User user, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password too short");
        }
        user.setPasswordHash(hasher.hash(newPassword));
        users.save(user);
    }
}
