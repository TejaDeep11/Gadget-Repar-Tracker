package com.pes.gadgetrepair.strategy;

import com.pes.gadgetrepair.model.Invoice;

/*
 Design Pattern:
 Behavioral Pattern → Strategy Pattern

 Purpose:
 Defines a common interface for all payment strategies.

 Why this is useful:
 Allows the payment method to be selected dynamically
 without modifying the billing service.

 SOLID Principles Applied:

 1. Open Closed Principle
    - New payment methods can be added without modifying existing code.

 2. Single Responsibility Principle
    - Each strategy handles only one payment behavior.

 3. Dependency Inversion Principle
    - High-level modules depend on abstraction.

 Usage Flow:
 BillingService → PaymentStrategy → Specific Payment Implementation
*/

public interface PaymentStrategy {

    void processPayment(Invoice invoice);

}