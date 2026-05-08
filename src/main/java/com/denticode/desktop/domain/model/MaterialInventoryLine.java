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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "material_inventory_lines",
        uniqueConstraints = @UniqueConstraint(columnNames = {"consultory_id", "facility_id"})
)
public class MaterialInventoryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "consultory_id")
    private Consultory consultory;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id")
    private TreatmentFacility facility;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Consultory getConsultory() { return consultory; }
    public void setConsultory(Consultory consultory) { this.consultory = consultory; }
    public TreatmentFacility getFacility() { return facility; }
    public void setFacility(TreatmentFacility facility) { this.facility = facility; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
