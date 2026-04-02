package com.pes.gadgetrepair.service.impl;

import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.model.Invoice;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.repository.InvoiceRepository;
import com.pes.gadgetrepair.repository.RepairRequestRepository;
import com.pes.gadgetrepair.service.BillingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
Design Principles Used:
1. Single Responsibility Principle
2. Dependency Inversion Principle

Role:
Handles invoice generation and payment updates.
*/

@Service
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final RepairRequestRepository repairRequestRepository;

    public BillingServiceImpl(
            InvoiceRepository invoiceRepository,
            RepairRequestRepository repairRequestRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.repairRequestRepository = repairRequestRepository;
    }

    @Override
    public Invoice generateInvoice(Long repairRequestId, double amount) {

        RepairRequest request = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new RuntimeException("Repair request not found"));

        Invoice invoice = new Invoice();
        invoice.setRepairRequest(request);
        invoice.setAmount(amount);
        invoice.setPaymentStatus(PaymentStatus.PENDING);

        return invoiceRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    @Override
    public Invoice updatePaymentStatus(Long invoiceId, String paymentStatus) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));

        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}