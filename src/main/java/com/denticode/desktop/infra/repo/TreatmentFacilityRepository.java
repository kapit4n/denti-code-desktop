package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.TreatmentFacility;

import java.util.List;

public class TreatmentFacilityRepository extends BaseRepository<TreatmentFacility, Long> {

    public TreatmentFacilityRepository(Database db) {
        super(db, TreatmentFacility.class);
    }

    public List<TreatmentFacility> active() {
        return read(em -> em.createQuery(
                        "from TreatmentFacility f where f.active = true order by f.sortOrder, f.displayName",
                        TreatmentFacility.class)
                .getResultList());
    }
}
