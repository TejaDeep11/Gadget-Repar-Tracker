package com.pes.gadgetrepair.adapter;

import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.model.Invoice;

/*
 Design Pattern:
 Structural Pattern → Adapter Pattern

 Purpose:
 Adapts the external payment gateway interface
 to match the system's internal payment workflow.

 Why Adapter is Needed:
 External APIs often have incompatible interfaces.
 Adapter allows integration without modifying existing code.

 SOLID Principles Applied:

 1. Single Responsibility Principle
    - Responsible only for adapting external payment calls.

 2. Open Closed Principle
    - External systems can change without affecting internal logic.

 3. Dependency Inversion Principle
    - High level modules depend on abstraction rather than external APIs.

 Integration Flow:

 BillingService
      ↓
 PaymentGatewayAdapter
      ↓
 ExternalPaymentGateway
*/

public class PaymentGatewayAdapter {

    private final ExternalPaymentGateway externalGateway;

    public PaymentGatewayAdapter() {
        this.externalGateway = new ExternalPaymentGateway();
    }

    public void processPayment(Invoice invoice) {

        boolean success = externalGateway.makePayment(invoice.getAmount());

        if (success) {
            invoice.setPaymentStatus(PaymentStatus.PAID);

            System.out.println(
                    "Payment successful via external gateway for Invoice ID: "
                            + invoice.getInvoiceId()
            );
        } else {
            invoice.setPaymentStatus(PaymentStatus.FAILED);

            System.out.println(
                    "Payment failed for Invoice ID: "
                            + invoice.getInvoiceId()
            );
        }
    }
}