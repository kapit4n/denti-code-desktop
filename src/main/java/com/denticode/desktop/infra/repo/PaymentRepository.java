package com.denticode.desktop.infra.repo;

import com.denticode.desktop.core.Database;
import com.denticode.desktop.domain.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRepository extends BaseRepository<Payment, Long> {

    public PaymentRepository(Database db) {
        super(db, Payment.class);
    }

    public List<Payment> byPatient(Long patientId) {
        return read(em -> em.createQuery(
                        "from Payment p where p.patient.id = :pid order by p.paidAt desc",
                        Payment.class)
                .setParameter("pid", patientId)
                .getResultList());
    }

    public BigDecimal sumByPatient(Long patientId) {
        return read(em -> {
            Object total = em.createQuery(
                            "select coalesce(sum(p.amount), 0) from Payment p where p.patient.id = :pid")
                    .setParameter("pid", patientId)
                    .getSingleResult();
            return total == null ? BigDecimal.ZERO : new BigDecimal(total.toString());
        });
    }
}
