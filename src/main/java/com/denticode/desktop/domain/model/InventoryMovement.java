package com.denticode.desktop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;

import java.time.Instant;
import com.denticode.desktop.infra.persistence.InstantEpochMillisConverter;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "consultory_id")
    private Consultory consultory;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id")
    private TreatmentFacility facility;

    /** Positive number of units. The sign of the change is encoded by {@link #type}. */
    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InventoryMovementType type;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Consultory getConsultory() { return consultory; }
    public void setConsultory(Consultory consultory) { this.consultory = consultory; }
    public TreatmentFacility getFacility() { return facility; }
    public void setFacility(TreatmentFacility facility) { this.facility = facility; }
    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public InventoryMovementType getType() { return type; }
    public void setType(InventoryMovementType type) { this.type = type; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
