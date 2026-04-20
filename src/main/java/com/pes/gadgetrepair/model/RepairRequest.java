package com.pes.gadgetrepair.model;

import com.pes.gadgetrepair.enums.RepairStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_requests")
public class RepairRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician technician;

    @OneToOne
    @JoinColumn(name = "gadget_gadget_id")
    private Gadget gadget;

    private String problemDescription;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RepairStatus status;

    private LocalDateTime createdAt;

    public RepairRequest() {}

    public RepairRequest(Customer customer, Gadget gadget, String problemDescription) {
        this.customer = customer;
        this.gadget = gadget;
        this.problemDescription = problemDescription;
        this.status = RepairStatus.REQUEST_SUBMITTED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getRequestId() {
        return requestId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Technician getTechnician() {
        return technician;
    }

    public Gadget getGadget() {
        return gadget;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }
}