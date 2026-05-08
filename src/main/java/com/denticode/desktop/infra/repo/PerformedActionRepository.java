package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.PerformedAction;

import java.util.List;

public class PerformedActionRepository extends BaseRepository<PerformedAction, Long> {

    public PerformedActionRepository(Database db) {
        super(db, PerformedAction.class);
    }

    public List<PerformedAction> byAppointment(Long appointmentId) {
        return read(em -> em.createQuery(
                        "from PerformedAction p where p.appointment.id = :id order by p.actionDateTime asc",
                        PerformedAction.class)
                .setParameter("id", appointmentId)
                .getResultList());
    }
}
