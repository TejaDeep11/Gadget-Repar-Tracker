package com.pes.gadgetrepair.service.impl;

import com.pes.gadgetrepair.adapter.PaymentGatewayAdapter;
import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.exception.PaymentFailedException;
import com.pes.gadgetrepair.exception.RepairRequestNotFoundException;
import com.pes.gadgetrepair.model.Invoice;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.repository.InvoiceRepository;
import com.pes.gadgetrepair.repository.RepairRequestRepository;
import com.pes.gadgetrepair.service.BillingService;
import com.pes.gadgetrepair.strategy.PaymentStrategy;
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
    private final PaymentGatewayAdapter paymentGatewayAdapter;

    public BillingServiceImpl(
            InvoiceRepository invoiceRepository,
            RepairRequestRepository repairRequestRepository,
            PaymentGatewayAdapter paymentGatewayAdapter
    ) {
        this.invoiceRepository = invoiceRepository;
        this.repairRequestRepository = repairRequestRepository;
        this.paymentGatewayAdapter = paymentGatewayAdapter;
    }

    @Override
    public Invoice generateInvoice(Long repairRequestId, double amount) {

        RepairRequest request = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new RepairRequestNotFoundException("Repair request not found"));

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
                .orElseThrow(() -> new PaymentFailedException("Invoice not found"));

        invoice.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));

        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice processPayment(Long invoiceId, PaymentStrategy paymentStrategy) {
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PaymentFailedException("Invoice not found"));

        try {
            // Apply the payment strategy
            paymentStrategy.processPayment(invoice);
            
            // Also use external gateway adapter as fallback/confirmation
            paymentGatewayAdapter.processPayment(invoice);
            
            return invoiceRepository.save(invoice);
        } catch (Exception e) {
            throw new PaymentFailedException("Payment processing failed: " + e.getMessage());
        }
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}