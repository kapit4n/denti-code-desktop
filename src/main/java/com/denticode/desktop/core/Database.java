package com.denticode.desktop.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds the application's single {@link DataSource} and JPA
 * {@link EntityManagerFactory}. Runs Flyway migrations before exposing the
 * factory so the schema is always in sync with the latest baseline.
 */
public final class Database implements AutoCloseable {

    private final HikariDataSource dataSource;
    private final EntityManagerFactory emf;

    public Database(Config config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.jdbcUrl());
        hikari.setMaximumPoolSize(4);
        hikari.setMinimumIdle(1);
        hikari.setPoolName("denti-code-pool");
        hikari.setConnectionInitSql("PRAGMA foreign_keys = ON");
        this.dataSource = new HikariDataSource(hikari);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        // SQLite's type system can represent 64-bit rowids as INTEGER even when the domain uses Long.
        // Flyway is the schema source of truth; avoid failing app startup on benign type affinities.
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.show_sql", "false");
        jpaProps.put("hibernate.format_sql", "false");
        jpaProps.put("jakarta.persistence.nonJtaDataSource", dataSource);
        this.emf = Persistence.createEntityManagerFactory("denti-code", jpaProps);
    }

    public EntityManagerFactory entityManagerFactory() {
        return emf;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    /**
     * Run {@code work} inside a JPA transaction and return its result.
     */
    public <T> T transaction(Function<EntityManager, T> work) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Read-only convenience: opens an EM, runs the work, closes it.
     */
    public <T> T read(Function<EntityManager, T> work) {
        EntityManager em = emf.createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    @Override
    public void close() {
        if (emf.isOpen()) emf.close();
        if (!dataSource.isClosed()) dataSource.close();
    }
}
