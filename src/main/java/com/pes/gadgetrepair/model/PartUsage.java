package com.pes.gadgetrepair.model;

import jakarta.persistence.*;

/*
 UML Mapping:
 PartUsage tracks which parts are used for which repairs
 and automatically decreases inventory when assigned.
*/

@Entity
@Table(name = "part_usage")
public class PartUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Part part;

    @ManyToOne
    private RepairRequest repairRequest;

    private int quantityUsed;

    public PartUsage() {}

    public PartUsage(Part part, RepairRequest repairRequest, int quantityUsed) {
        this.part = part;
        this.repairRequest = repairRequest;
        this.quantityUsed = quantityUsed;
    }

    public Long getId() {
        return id;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public RepairRequest getRepairRequest() {
        return repairRequest;
    }

    public void setRepairRequest(RepairRequest repairRequest) {
        this.repairRequest = repairRequest;
    }

    public int getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(int quantityUsed) {
        this.quantityUsed = quantityUsed;
    }
}