package com.denticode.desktop.core;

import com.denticode.desktop.domain.model.Role;
import com.denticode.desktop.domain.model.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Holds the currently authenticated user. Views can bind to {@link #userProperty()}
 * to react to login / logout. There is at most one session at a time.
 */
public final class Session {

    private final ObjectProperty<User> user = new SimpleObjectProperty<>(this, "user", null);

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User u) {
        user.set(u);
    }

    public boolean isAuthenticated() {
        return user.get() != null;
    }

    public boolean hasRole(Role role) {
        User u = user.get();
        return u != null && u.getRoles().contains(role);
    }

    public Role primaryRole() {
        User u = user.get();
        if (u == null || u.getRoles().isEmpty()) return null;
        if (u.getRoles().contains(Role.ADMIN)) return Role.ADMIN;
        if (u.getRoles().contains(Role.DOCTOR)) return Role.DOCTOR;
        if (u.getRoles().contains(Role.PATIENT)) return Role.PATIENT;
        return u.getRoles().iterator().next();
    }

    public void clear() {
        user.set(null);
    }
}
