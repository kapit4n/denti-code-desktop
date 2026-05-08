package com.denticode.desktop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.denticode.desktop.infra.persistence.LocalDateTimeIsoStringConverter;

@Entity
@Table(name = "performed_actions")
public class PerformedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performed_action_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "procedure_type_id")
    private ProcedureType procedureType;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "performing_doctor_id")
    private Doctor performingDoctor;

    @Column(name = "action_at", nullable = false)
    @Convert(converter = LocalDateTimeIsoStringConverter.class)
    private LocalDateTime actionDateTime = LocalDateTime.now();

    @Column(name = "tooth_involved")
    private String toothInvolved;

    @Column(name = "surfaces_involved")
    private String surfacesInvolved;

    @Column(name = "anesthesia_used")
    private String anesthesiaUsed;

    /** JSON array of TreatmentFacility ids, e.g. "[1,4]". */
    @Column(name = "facilities_used", columnDefinition = "TEXT")
    private String facilitiesUsed;

    @Column(name = "description_notes", columnDefinition = "TEXT")
    private String descriptionNotes;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    public BigDecimal recomputeTotal() {
        BigDecimal qty = BigDecimal.valueOf(quantity);
        BigDecimal total = unitPrice == null ? BigDecimal.ZERO : unitPrice.multiply(qty);
        this.totalPrice = total;
        return total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public ProcedureType getProcedureType() { return procedureType; }
    public void setProcedureType(ProcedureType procedureType) { this.procedureType = procedureType; }
    public Doctor getPerformingDoctor() { return performingDoctor; }
    public void setPerformingDoctor(Doctor performingDoctor) { this.performingDoctor = performingDoctor; }
    public LocalDateTime getActionDateTime() { return actionDateTime; }
    public void setActionDateTime(LocalDateTime actionDateTime) { this.actionDateTime = actionDateTime; }
    public String getToothInvolved() { return toothInvolved; }
    public void setToothInvolved(String toothInvolved) { this.toothInvolved = toothInvolved; }
    public String getSurfacesInvolved() { return surfacesInvolved; }
    public void setSurfacesInvolved(String surfacesInvolved) { this.surfacesInvolved = surfacesInvolved; }
    public String getAnesthesiaUsed() { return anesthesiaUsed; }
    public void setAnesthesiaUsed(String anesthesiaUsed) { this.anesthesiaUsed = anesthesiaUsed; }
    public String getFacilitiesUsed() { return facilitiesUsed; }
    public void setFacilitiesUsed(String facilitiesUsed) { this.facilitiesUsed = facilitiesUsed; }
    public String getDescriptionNotes() { return descriptionNotes; }
    public void setDescriptionNotes(String descriptionNotes) { this.descriptionNotes = descriptionNotes; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
