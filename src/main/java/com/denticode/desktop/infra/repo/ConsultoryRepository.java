package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.Consultory;

import java.util.List;

public class ConsultoryRepository extends BaseRepository<Consultory, Long> {

    public ConsultoryRepository(Database db) {
        super(db, Consultory.class);
    }

    public List<Consultory> active() {
        return read(em -> em.createQuery(
                        "from Consultory c where c.active = true order by c.sortOrder, c.name",
                        Consultory.class)
                .getResultList());
    }
}
