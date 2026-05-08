package com.denticode.desktop.core;

import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.domain.model.Role;
import com.denticode.desktop.domain.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

/**
 * Bootstraps demo data on first launch when {@code app.demoMode=true} and
 * the database is empty. Idempotent: if any user exists, the seeder is a no-op.
 *
 * Mirrors the seed users from the Next.js auth-srv so login flows match the web app.
 */
public final class Seeder {

    private final AppContext app;

    public Seeder(AppContext app) {
        this.app = app;
    }

    public void seedIfEmpty() {
        if (app.database().read(em -> em.createQuery("select count(u) from User u", Long.class)
                .getSingleResult()) > 0) {
            return;
        }

        seedAdmin();
        Doctor susan = seedDoctor("susan.storm@denti-code.com", "Susan", "Storm",
                "+1-555-0101", "DEN-1234", "Room 1", "General Dentistry");
        Doctor peter = seedDoctor("peter.parker@denti-code.com", "Peter", "Parker",
                "+1-555-0102", "DEN-1235", "Room 2", "Endodontics");

        Patient p1 = seedPatient("patient1@example.com", "Mary", "Jane",
                LocalDate.of(1995, 3, 14), "+1-555-2001", "Female",
                "10 Oak Street", "No known allergies.");
        Patient p2 = seedPatient("patient2@example.com", "John", "Walker",
                LocalDate.of(1988, 11, 2), "+1-555-2002", "Male",
                "25 Maple Avenue", "Hypertension; on lisinopril.");
        Patient p3 = seedPatient("patient3@example.com", "Aisha", "Khan",
                LocalDate.of(2001, 6, 21), "+1-555-2003", "Female",
                "7 Pine Road", null);
        seedPatient("patient4@example.com", "Carlos", "Mendoza",
                LocalDate.of(1979, 1, 5), "+1-555-2004", "Male",
                null, "Mild penicillin allergy.");
        seedPatient("patient5@example.com", "Lin", "Wei",
                LocalDate.of(2010, 9, 30), "+1-555-2005", "Other",
                "42 Birch Lane", null);

        seedAppointment(p1, susan, LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0),
                "Routine cleaning", AppointmentStatus.SCHEDULED);
        seedAppointment(p2, peter, LocalDateTime.now().plusDays(2).withHour(10).withMinute(30).withSecond(0).withNano(0),
                "Root canal follow-up", AppointmentStatus.CONFIRMED);
        seedAppointment(p3, susan, LocalDateTime.now().minusDays(7).withHour(15).withMinute(0).withSecond(0).withNano(0),
                "Periodic exam", AppointmentStatus.COMPLETED);
        seedAppointment(p1, peter, LocalDateTime.now().plusDays(7).withHour(14).withMinute(0).withSecond(0).withNano(0),
                "Composite filling", AppointmentStatus.SCHEDULED);
    }

    private void seedAdmin() {
        User u = app.authService().createUser(
                "admin@denti-code.com", "Password123!", "Admin",
                EnumSet.of(Role.ADMIN));
        u.setPreferredLocale(app.config().defaultLocale());
        app.database().transaction(em -> em.merge(u));
    }

    private Doctor seedDoctor(String email, String first, String last,
                              String phone, String license, String room, String specialization) {
        User u = app.authService().createUser(
                email, "Password123!", "Dr. " + first + " " + last,
                EnumSet.of(Role.DOCTOR));
        Doctor d = new Doctor();
        d.setUser(u);
        d.setFirstName(first);
        d.setLastName(last);
        d.setEmail(email);
        d.setContactPhone(phone);
        d.setLicenseNumber(license);
        d.setOfficeRoom(room);
        d.setSpecialization(specialization);
        d.setActive(true);
        return app.database().transaction(em -> {
            em.persist(d);
            return d;
        });
    }

    private Patient seedPatient(String email, String first, String last,
                                LocalDate dob, String phone, String gender,
                                String address, String history) {
        User u = app.authService().createUser(
                email, "Password123!", first + " " + last,
                EnumSet.of(Role.PATIENT));
        Patient p = new Patient();
        p.setUser(u);
        p.setFirstName(first);
        p.setLastName(last);
        p.setDateOfBirth(dob);
        p.setContactPhone(phone);
        p.setEmail(email);
        p.setGender(gender);
        p.setAddress(address);
        p.setMedicalHistorySummary(history);
        return app.database().transaction(em -> {
            em.persist(p);
            return p;
        });
    }

    private void seedAppointment(Patient p, Doctor d, LocalDateTime when,
                                 String purpose, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setPatient(p);
        a.setPrimaryDoctor(d);
        a.setScheduledAt(when);
        a.setEstimatedDurationMinutes(45);
        a.setPurpose(purpose);
        a.setStatus(status);
        app.database().transaction(em -> {
            em.persist(a);
            return a;
        });
    }
}
