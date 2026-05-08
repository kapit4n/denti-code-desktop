package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.ProcedureType;

import java.util.List;

public class ProcedureTypeRepository extends BaseRepository<ProcedureType, Long> {

    public ProcedureTypeRepository(Database db) {
        super(db, ProcedureType.class);
    }

    public List<ProcedureType> active() {
        return read(em -> em.createQuery(
                        "from ProcedureType p where p.active = true order by p.name", ProcedureType.class)
                .getResultList());
    }
}
