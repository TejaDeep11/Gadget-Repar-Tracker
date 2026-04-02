package com.pes.gadgetrepair.model;

import com.pes.gadgetrepair.enums.PaymentStatus;
import jakarta.persistence.*;

/*
 Design Principles Used:

 1. Single Responsibility Principle
    - Represents billing information for a repair request.

 2. Encapsulation
    - Fields are private and accessed through getters/setters.

 UML Mapping:
 Invoice entity linked with RepairRequest.
*/

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @OneToOne
    @JoinColumn(name = "repair_request_id")
    private RepairRequest repairRequest;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Invoice() {}

    public Invoice(RepairRequest repairRequest, double amount, PaymentStatus paymentStatus) {
        this.repairRequest = repairRequest;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public RepairRequest getRepairRequest() {
        return repairRequest;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setRepairRequest(RepairRequest repairRequest) {
        this.repairRequest = repairRequest;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}