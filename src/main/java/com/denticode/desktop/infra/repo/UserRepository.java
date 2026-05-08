package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.User;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository<User, Long> {

    public UserRepository(Database db) {
        super(db, User.class);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String normalized = email.trim().toLowerCase();
        return read(em -> em.createQuery(
                        "from User u where lower(u.email) = :email", User.class)
                .setParameter("email", normalized)
                .getResultList()
                .stream()
                .findFirst());
    }

    public List<User> all() {
        return findAll();
    }
}
