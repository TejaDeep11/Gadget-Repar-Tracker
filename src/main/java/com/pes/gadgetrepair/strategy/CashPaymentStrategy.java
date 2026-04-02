package com.pes.gadgetrepair.strategy;

import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.model.Invoice;

/*
 Strategy Implementation:
 Cash Payment

 Behavior:
 Marks invoice as PAID when cash payment is completed.
*/

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Invoice invoice) {

        // In real systems this would interact with POS hardware
        invoice.setPaymentStatus(PaymentStatus.PAID);

        System.out.println("Payment completed using CASH for Invoice ID: "
                + invoice.getInvoiceId());
    }
}