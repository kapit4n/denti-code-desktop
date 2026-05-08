package com.denticode.desktop.domain.service;

import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.domain.model.Payment;
import com.denticode.desktop.domain.model.PaymentMethod;
import com.denticode.desktop.infra.repo.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PaymentService {

    private final PaymentRepository payments;
    private final EventBus events;

    public PaymentService(PaymentRepository payments, EventBus events) {
        this.payments = payments;
        this.events = events;
    }

    public List<Payment> byPatient(Long patientId) {
        return payments.byPatient(patientId);
    }

    public BigDecimal totalPaid(Long patientId) {
        return payments.sumByPatient(patientId);
    }

    public Payment record(Patient patient, BigDecimal amount, PaymentMethod method,
                          LocalDateTime when, String note) {
        if (patient == null) throw new IllegalArgumentException("Patient is required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        Payment p = new Payment();
        p.setPatient(patient);
        p.setAmount(amount);
        p.setMethod(method);
        p.setNote(note == null || note.isBlank() ? null : note.trim());
        p.setPaidAt(when == null ? LocalDateTime.now() : when);
        Payment saved = payments.persist(p);
        events.publish(EventBus.PAYMENT_CHANGED, saved.getId());
        return saved;
    }

    public void delete(Long id) {
        payments.deleteById(id);
        events.publish(EventBus.PAYMENT_CHANGED, id);
    }
}
