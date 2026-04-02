package com.pes.gadgetrepair.strategy;

import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.model.Invoice;

/*
 Strategy Implementation:
 Card Payment

 Behavior:
 Simulates card payment processing.
*/

public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Invoice invoice) {

        // Simulated card processing
        invoice.setPaymentStatus(PaymentStatus.PAID);

        System.out.println("Payment completed using CARD for Invoice ID: "
                + invoice.getInvoiceId());
    }
}