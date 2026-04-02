package com.pes.gadgetrepair.adapter;

/*
 External System Simulation

 This represents a third-party payment service that
 our system cannot modify.

 In a real system this could be:
 - Razorpay
 - Stripe
 - PayPal
*/

public class ExternalPaymentGateway {

    public boolean makePayment(double amount) {

        // Simulated external API call
        System.out.println("External payment gateway processing payment: " + amount);

        // Simulate successful transaction
        return true;
    }
}