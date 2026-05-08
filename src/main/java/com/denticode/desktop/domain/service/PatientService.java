package com.denticode.desktop.domain.service;

import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.infra.repo.PatientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class PatientService {

    private final PatientRepository patients;
    private final EventBus events;

    public PatientService(PatientRepository patients, EventBus events) {
        this.patients = patients;
        this.events = events;
    }

    public List<Patient> search(String query) {
        return patients.search(query);
    }

    public Optional<Patient> find(Long id) {
        return patients.findById(id);
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patients.findByUserId(userId);
    }

    public Patient create(Patient draft) {
        validate(draft, true);
        Patient saved = patients.persist(draft);
        events.publish(EventBus.PATIENT_CHANGED, saved.getId());
        return saved;
    }

    public Patient update(Patient draft) {
        validate(draft, false);
        Patient saved = patients.save(draft);
        events.publish(EventBus.PATIENT_CHANGED, saved.getId());
        return saved;
    }

    public void delete(Long id) {
        patients.deleteById(id);
        events.publish(EventBus.PATIENT_CHANGED, id);
    }

    public long count() {
        return patients.count();
    }

    private void validate(Patient p, boolean creating) {
        if (p.getFirstName() == null || p.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required");
        if (p.getLastName() == null || p.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (p.getDateOfBirth() == null || p.getDateOfBirth().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Valid date of birth is required");
        if (p.getContactPhone() == null || p.getContactPhone().isBlank())
            throw new IllegalArgumentException("Contact phone is required");
    }
}
