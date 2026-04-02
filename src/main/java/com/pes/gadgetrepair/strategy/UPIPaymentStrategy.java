package com.pes.gadgetrepair.strategy;

import com.pes.gadgetrepair.enums.PaymentStatus;
import com.pes.gadgetrepair.model.Invoice;

/*
 Strategy Implementation:
 UPI Payment

 Behavior:
 Handles digital wallet / UPI transactions.
*/

public class UPIPaymentStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Invoice invoice) {

        // Simulated UPI transaction
        invoice.setPaymentStatus(PaymentStatus.PAID);

        System.out.println("Payment completed using UPI for Invoice ID: "
                + invoice.getInvoiceId());
    }
}