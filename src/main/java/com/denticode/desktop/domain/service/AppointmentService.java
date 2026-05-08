package com.denticode.desktop.domain.service;

import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.infra.repo.AppointmentRepository;
import com.denticode.desktop.infra.repo.DoctorRepository;
import com.denticode.desktop.infra.repo.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AppointmentService {

    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final EventBus events;

    public AppointmentService(AppointmentRepository appointments,
                              PatientRepository patients,
                              DoctorRepository doctors,
                              EventBus events) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
        this.events = events;
    }

    public List<Appointment> all() {
        return appointments.all();
    }

    public List<Appointment> byPatient(Long patientId) {
        return appointments.byPatient(patientId);
    }

    public List<Appointment> byDoctor(Long doctorId) {
        return appointments.byDoctor(doctorId);
    }

    public Optional<Appointment> findWithActions(Long id) {
        return Optional.ofNullable(appointments.findWithActions(id));
    }

    public Optional<Appointment> find(Long id) {
        return appointments.findById(id);
    }

    public List<Appointment> today() {
        LocalDate today = LocalDate.now();
        return appointments.betweenDates(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public Map<AppointmentStatus, Long> countByStatus() {
        return appointments.countByStatus();
    }

    public long count() {
        return appointments.count();
    }

    public Appointment create(Long patientId, Long doctorId, LocalDateTime when,
                              Integer durationMinutes, String purpose, String notes,
                              AppointmentStatus initial) {
        if (when == null) throw new IllegalArgumentException("Scheduled time is required");
        Patient patient = patients.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Doctor doctor = doctors.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        AppointmentStatus status = initial == null ? AppointmentStatus.SCHEDULED : initial;
        if (!EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED).contains(status)) {
            throw new IllegalArgumentException("Initial status must be SCHEDULED or CONFIRMED");
        }
        if (durationMinutes != null && durationMinutes < 1) {
            throw new IllegalArgumentException("Duration must be at least 1 minute");
        }

        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setPrimaryDoctor(doctor);
        a.setScheduledAt(when);
        a.setEstimatedDurationMinutes(durationMinutes == null ? 30 : durationMinutes);
        a.setPurpose(purpose);
        a.setNotes(notes);
        a.setStatus(status);

        Appointment saved = appointments.persist(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    public Appointment updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        a.setStatus(newStatus);
        Appointment saved = appointments.save(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    public Appointment reschedule(Long appointmentId, LocalDateTime newWhen) {
        if (newWhen == null || !newWhen.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("New time must be in the future");
        }
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        a.setScheduledAt(newWhen);
        a.setStatus(AppointmentStatus.SCHEDULED);
        Appointment saved = appointments.save(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    public Appointment patch(Long id, LocalDateTime when, Integer duration, String purpose,
                             String notes, AppointmentStatus status) {
        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (when != null) a.setScheduledAt(when);
        if (duration != null) a.setEstimatedDurationMinutes(duration);
        if (purpose != null) a.setPurpose(purpose);
        if (notes != null) a.setNotes(notes);
        if (status != null) a.setStatus(status);
        Appointment saved = appointments.save(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    /**
     * Patient self-service: confirm an upcoming appointment that is currently
     * Scheduled or Rescheduled.
     */
    public Appointment patientConfirm(Long appointmentId) {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (a.getStatus() != AppointmentStatus.SCHEDULED && a.getStatus() != AppointmentStatus.RESCHEDULED) {
            throw new IllegalStateException("Only Scheduled or Rescheduled visits can be confirmed");
        }
        if (a.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Visit is in the past");
        }
        a.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointments.save(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    public Appointment patientCancel(Long appointmentId) {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (a.getStatus().isTerminal()) {
            throw new IllegalStateException("Visit is already in a terminal state");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointments.save(a);
        events.publish(EventBus.APPOINTMENT_CHANGED, saved.getId());
        return saved;
    }

    /** Helpful builder for the doctor's "New visit" modal default time. */
    public static LocalDateTime defaultProposedTime() {
        LocalDate today = LocalDate.now();
        return today.atTime(LocalTime.of(9, 0));
    }
}
