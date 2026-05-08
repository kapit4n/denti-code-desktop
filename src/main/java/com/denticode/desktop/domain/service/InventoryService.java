package com.denticode.desktop.domain.service;

import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Consultory;
import com.denticode.desktop.domain.model.InventoryMovement;
import com.denticode.desktop.domain.model.InventoryMovementType;
import com.denticode.desktop.domain.model.MaterialInventoryLine;
import com.denticode.desktop.domain.model.TreatmentFacility;
import com.denticode.desktop.infra.repo.ConsultoryRepository;
import com.denticode.desktop.infra.repo.InventoryLineRepository;
import com.denticode.desktop.infra.repo.InventoryMovementRepository;

import java.util.List;
import java.util.Optional;

public final class InventoryService {

    private final ConsultoryRepository consultories;
    private final InventoryLineRepository lines;
    private final InventoryMovementRepository movements;
    private final EventBus events;

    public InventoryService(ConsultoryRepository consultories,
                            InventoryLineRepository lines,
                            InventoryMovementRepository movements,
                            EventBus events) {
        this.consultories = consultories;
        this.lines = lines;
        this.movements = movements;
        this.events = events;
    }

    public List<Consultory> consultories() {
        return consultories.active();
    }

    public Consultory createConsultory(String name, String shortCode) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        Consultory c = new Consultory();
        c.setName(name.trim());
        c.setShortCode(shortCode == null || shortCode.isBlank() ? null : shortCode.trim());
        c.setActive(true);
        Consultory saved = consultories.persist(c);
        events.publish(EventBus.INVENTORY_CHANGED, saved.getId());
        return saved;
    }

    public List<MaterialInventoryLine> linesByConsultory(Long consultoryId) {
        return lines.byConsultory(consultoryId);
    }

    public List<MaterialInventoryLine> allLines() {
        return lines.all();
    }

    public List<InventoryMovement> recentMovements() {
        return movements.recent(60);
    }

    public MaterialInventoryLine createLine(Consultory consultory, TreatmentFacility facility, int initialQty) {
        if (initialQty < 0) throw new IllegalArgumentException("Initial quantity must be >= 0");
        Optional<MaterialInventoryLine> existing = lines.findPair(consultory.getId(), facility.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("This material is already tracked at that consultory");
        }
        MaterialInventoryLine line = new MaterialInventoryLine();
        line.setConsultory(consultory);
        line.setFacility(facility);
        line.setQuantity(initialQty);
        MaterialInventoryLine saved = lines.persist(line);

        if (initialQty > 0) {
            recordMovement(consultory, facility, initialQty, InventoryMovementType.RECEIVE, "Initial stock");
        }
        events.publish(EventBus.INVENTORY_CHANGED, saved.getId());
        return saved;
    }

    public MaterialInventoryLine adjust(MaterialInventoryLine line, int amount,
                                        InventoryMovementType type, String note) {
        if (amount < 1) throw new IllegalArgumentException("Amount must be >= 1");
        int delta = type == InventoryMovementType.RECEIVE ? amount : -amount;
        int next = line.getQuantity() + delta;
        if (next < 0) throw new IllegalStateException("Resulting stock cannot be negative");
        line.setQuantity(next);
        MaterialInventoryLine saved = lines.save(line);
        recordMovement(saved.getConsultory(), saved.getFacility(), amount, type, note);
        events.publish(EventBus.INVENTORY_CHANGED, saved.getId());
        return saved;
    }

    public void deleteLine(MaterialInventoryLine line) {
        if (line.getQuantity() != 0) {
            throw new IllegalStateException("Stock must be zero before removing the line");
        }
        lines.deleteById(line.getId());
        events.publish(EventBus.INVENTORY_CHANGED, line.getId());
    }

    private void recordMovement(Consultory consultory, TreatmentFacility facility,
                                int amount, InventoryMovementType type, String note) {
        InventoryMovement m = new InventoryMovement();
        m.setConsultory(consultory);
        m.setFacility(facility);
        m.setQuantityChange(amount);
        m.setType(type);
        m.setNote(note);
        movements.persist(m);
    }
}
