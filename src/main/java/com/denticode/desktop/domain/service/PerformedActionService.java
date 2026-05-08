package com.denticode.desktop.domain.service;

import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.PerformedAction;
import com.denticode.desktop.domain.model.ProcedureType;
import com.denticode.desktop.infra.repo.AppointmentRepository;
import com.denticode.desktop.infra.repo.PerformedActionRepository;
import com.denticode.desktop.infra.repo.ProcedureTypeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PerformedActionService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PerformedActionRepository actions;
    private final AppointmentRepository appointments;
    private final ProcedureTypeRepository procedures;
    private final EventBus events;

    public PerformedActionService(PerformedActionRepository actions,
                                  AppointmentRepository appointments,
                                  ProcedureTypeRepository procedures,
                                  EventBus events) {
        this.actions = actions;
        this.appointments = appointments;
        this.procedures = procedures;
        this.events = events;
    }

    public List<PerformedAction> byAppointment(Long appointmentId) {
        return actions.byAppointment(appointmentId);
    }

    public PerformedAction add(Long appointmentId, Long procedureId, Doctor performingDoctor,
                               int quantity, BigDecimal unitPrice,
                               String tooth, String surfaces, String anesthesia,
                               List<Long> facilityIds, String notes) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be >= 1");
        if (unitPrice == null || unitPrice.signum() < 0) throw new IllegalArgumentException("Unit price must be >= 0");
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        ProcedureType proc = procedures.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found"));

        PerformedAction a = new PerformedAction();
        a.setAppointment(appt);
        a.setProcedureType(proc);
        a.setPerformingDoctor(performingDoctor);
        a.setActionDateTime(LocalDateTime.now());
        a.setToothInvolved(blankToNull(tooth));
        a.setSurfacesInvolved(blankToNull(surfaces));
        a.setAnesthesiaUsed(blankToNull(anesthesia));
        a.setDescriptionNotes(blankToNull(notes));
        a.setQuantity(quantity);
        a.setUnitPrice(unitPrice);
        a.setFacilitiesUsed(serializeFacilities(facilityIds));
        a.recomputeTotal();

        PerformedAction saved = actions.persist(a);
        events.publish(EventBus.PERFORMED_ACTION_CHANGED, saved.getId());
        return saved;
    }

    public void delete(Long id) {
        actions.deleteById(id);
        events.publish(EventBus.PERFORMED_ACTION_CHANGED, id);
    }

    public List<Long> facilitiesOf(PerformedAction a) {
        if (a == null || a.getFacilitiesUsed() == null || a.getFacilitiesUsed().isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(a.getFacilitiesUsed(), new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String serializeFacilities(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(ids);
        } catch (Exception e) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
