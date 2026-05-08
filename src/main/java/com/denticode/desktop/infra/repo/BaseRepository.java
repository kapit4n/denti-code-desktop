package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Tiny generic repository over a JPA-mapped entity. Concrete repositories add
 * domain-specific queries on top.
 */
public abstract class BaseRepository<T, ID> {

    protected final Database db;
    protected final Class<T> entityType;

    protected BaseRepository(Database db, Class<T> entityType) {
        this.db = db;
        this.entityType = entityType;
    }

    public Optional<T> findById(ID id) {
        return db.read(em -> Optional.ofNullable(em.find(entityType, id)));
    }

    public List<T> findAll() {
        return db.read(em -> em.createQuery("from " + entityType.getSimpleName() + " e", entityType).getResultList());
    }

    public T save(T entity) {
        return db.transaction(em -> em.merge(entity));
    }

    public T persist(T entity) {
        return db.transaction(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public void deleteById(ID id) {
        db.transaction(em -> {
            T managed = em.find(entityType, id);
            if (managed != null) em.remove(managed);
            return null;
        });
    }

    public long count() {
        return db.read(em -> em.createQuery("select count(e) from " + entityType.getSimpleName() + " e", Long.class)
                .getSingleResult());
    }

    protected <R> R inTx(java.util.function.Function<EntityManager, R> work) {
        return db.transaction(work);
    }

    protected <R> R read(java.util.function.Function<EntityManager, R> work) {
        return db.read(work);
    }
}
