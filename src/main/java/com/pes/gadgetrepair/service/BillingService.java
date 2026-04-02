package com.pes.gadgetrepair.service;

import com.pes.gadgetrepair.model.Invoice;

import java.util.List;
import java.util.Optional;

/*
 Design Principles Used:

 1. Single Responsibility Principle
    - Responsible for billing and invoice management.

 2. Dependency Inversion Principle
    - Controllers depend on interface.

 Purpose in System:
 Handles invoice generation and payment updates.

 Related UML Entities:
 Invoice
 RepairRequest
 PaymentStatus
*/

public interface BillingService {

    Invoice generateInvoice(Long repairRequestId, double amount);

    Optional<Invoice> findInvoiceById(Long invoiceId);

    Invoice updatePaymentStatus(Long invoiceId, String paymentStatus);

    List<Invoice> getAllInvoices();

}