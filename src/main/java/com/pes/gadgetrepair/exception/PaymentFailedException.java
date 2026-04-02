package com.pes.gadgetrepair.exception;

/*
 Custom Exception

 Purpose:
 Thrown when a payment operation fails.

 Used With:
 PaymentGatewayAdapter
 BillingService
*/

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(Long invoiceId) {
        super("Payment failed for invoice ID: " + invoiceId);
    }
}