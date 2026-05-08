package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.InventoryMovement;

import java.util.List;

public class InventoryMovementRepository extends BaseRepository<InventoryMovement, Long> {

    public InventoryMovementRepository(Database db) {
        super(db, InventoryMovement.class);
    }

    public List<InventoryMovement> recent(int limit) {
        return read(em -> em.createQuery(
                        "from InventoryMovement m order by m.createdAt desc",
                        InventoryMovement.class)
                .setMaxResults(limit)
                .getResultList());
    }
}
