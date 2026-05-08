package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AppointmentRepository extends BaseRepository<Appointment, Long> {

    public AppointmentRepository(Database db) {
        super(db, Appointment.class);
    }

    public List<Appointment> all() {
        return read(em -> em.createQuery(
                        "from Appointment a " +
                                "left join fetch a.patient " +
                                "left join fetch a.primaryDoctor " +
                                "order by a.scheduledAt desc", Appointment.class)
                .getResultList());
    }

    public List<Appointment> byPatient(Long patientId) {
        return read(em -> em.createQuery(
                        "from Appointment a " +
                                "left join fetch a.patient " +
                                "left join fetch a.primaryDoctor " +
                                "where a.patient.id = :pid " +
                                "order by a.scheduledAt desc", Appointment.class)
                .setParameter("pid", patientId)
                .getResultList());
    }

    public List<Appointment> byDoctor(Long doctorId) {
        return read(em -> em.createQuery(
                        "from Appointment a " +
                                "left join fetch a.patient " +
                                "left join fetch a.primaryDoctor " +
                                "where a.primaryDoctor.id = :did " +
                                "order by a.scheduledAt desc", Appointment.class)
                .setParameter("did", doctorId)
                .getResultList());
    }

    public List<Appointment> betweenDates(LocalDateTime from, LocalDateTime to) {
        return read(em -> em.createQuery(
                        "from Appointment a " +
                                "where a.scheduledAt >= :from and a.scheduledAt < :to " +
                                "order by a.scheduledAt asc", Appointment.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList());
    }

    public Appointment findWithActions(Long id) {
        return read(em -> em.createQuery(
                        "from Appointment a " +
                                "left join fetch a.performedActions " +
                                "left join fetch a.patient " +
                                "left join fetch a.primaryDoctor " +
                                "where a.id = :id", Appointment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    public Map<AppointmentStatus, Long> countByStatus() {
        List<Object[]> rows = read(em -> em.createQuery(
                        "select a.status, count(a) from Appointment a group by a.status",
                        Object[].class)
                .getResultList());
        java.util.EnumMap<AppointmentStatus, Long> map = new java.util.EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus s : AppointmentStatus.values()) map.put(s, 0L);
        for (Object[] row : rows) {
            map.put((AppointmentStatus) row[0], (Long) row[1]);
        }
        return map;
    }
}
