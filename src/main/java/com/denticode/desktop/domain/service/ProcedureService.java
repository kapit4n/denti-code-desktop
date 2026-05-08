package com.denticode.desktop.domain.service;

import com.denticode.desktop.domain.model.ProcedureType;
import com.denticode.desktop.domain.model.TreatmentFacility;
import com.denticode.desktop.infra.repo.ProcedureTypeRepository;
import com.denticode.desktop.infra.repo.TreatmentFacilityRepository;

import java.util.List;

public final class ProcedureService {

    private final ProcedureTypeRepository procedures;
    private final TreatmentFacilityRepository facilities;

    public ProcedureService(ProcedureTypeRepository procedures,
                            TreatmentFacilityRepository facilities) {
        this.procedures = procedures;
        this.facilities = facilities;
    }

    public List<ProcedureType> activeProcedures() {
        return procedures.active();
    }

    public List<TreatmentFacility> activeFacilities() {
        return facilities.active();
    }
}
