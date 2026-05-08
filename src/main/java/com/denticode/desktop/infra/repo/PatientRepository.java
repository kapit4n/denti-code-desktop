package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.Patient;

import java.util.List;
import java.util.Optional;

public class PatientRepository extends BaseRepository<Patient, Long> {

    public PatientRepository(Database db) {
        super(db, Patient.class);
    }

    public List<Patient> search(String query) {
        if (query == null || query.isBlank()) {
            return read(em -> em.createQuery(
                            "from Patient p order by p.lastName, p.firstName", Patient.class)
                    .getResultList());
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return read(em -> em.createQuery(
                        "from Patient p " +
                                "where lower(p.firstName) like :q " +
                                "   or lower(p.lastName)  like :q " +
                                "   or lower(coalesce(p.email, '')) like :q " +
                                "   or lower(p.contactPhone) like :q " +
                                "order by p.lastName, p.firstName", Patient.class)
                .setParameter("q", like)
                .getResultList());
    }

    public Optional<Patient> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return read(em -> em.createQuery("from Patient p where p.user.id = :uid", Patient.class)
                .setParameter("uid", userId)
                .getResultList()
                .stream()
                .findFirst());
    }
}
