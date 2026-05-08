package com.denticode.desktop.infra.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper over jBCrypt so the rest of the app doesn't depend on the
 * concrete library directly. Cost 10 is a good default for an offline desktop app.
 */
public final class PasswordHasher {

    private static final int DEFAULT_COST = 10;

    public String hash(String plain) {
        if (plain == null) throw new IllegalArgumentException("password is required");
        return BCrypt.hashpw(plain, BCrypt.gensalt(DEFAULT_COST));
    }

    public boolean verify(String plain, String hash) {
        if (plain == null || hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
