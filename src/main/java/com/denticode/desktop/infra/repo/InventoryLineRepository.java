package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.MaterialInventoryLine;

import java.util.List;
import java.util.Optional;

public class InventoryLineRepository extends BaseRepository<MaterialInventoryLine, Long> {

    public InventoryLineRepository(Database db) {
        super(db, MaterialInventoryLine.class);
    }

    public List<MaterialInventoryLine> byConsultory(Long consultoryId) {
        return read(em -> em.createQuery(
                        "from MaterialInventoryLine l where l.consultory.id = :id " +
                                "order by l.facility.categoryKey, l.facility.displayName",
                        MaterialInventoryLine.class)
                .setParameter("id", consultoryId)
                .getResultList());
    }

    public Optional<MaterialInventoryLine> findPair(Long consultoryId, Long facilityId) {
        return read(em -> em.createQuery(
                        "from MaterialInventoryLine l " +
                                "where l.consultory.id = :cid and l.facility.id = :fid",
                        MaterialInventoryLine.class)
                .setParameter("cid", consultoryId)
                .setParameter("fid", facilityId)
                .getResultStream()
                .findFirst());
    }

    public List<MaterialInventoryLine> all() {
        return read(em -> em.createQuery(
                        "from MaterialInventoryLine l " +
                                "order by l.consultory.name, l.facility.displayName",
                        MaterialInventoryLine.class)
                .getResultList());
    }
}
