package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.Doctor;

import java.util.List;
import java.util.Optional;

public class DoctorRepository extends BaseRepository<Doctor, Long> {

    public DoctorRepository(Database db) {
        super(db, Doctor.class);
    }

    public List<Doctor> activeDoctors() {
        return read(em -> em.createQuery(
                        "from Doctor d where d.active = true order by d.lastName, d.firstName", Doctor.class)
                .getResultList());
    }

    public Optional<Doctor> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return read(em -> em.createQuery("from Doctor d where d.user.id = :uid", Doctor.class)
                .setParameter("uid", userId)
                .getResultList()
                .stream()
                .findFirst());
    }
}
