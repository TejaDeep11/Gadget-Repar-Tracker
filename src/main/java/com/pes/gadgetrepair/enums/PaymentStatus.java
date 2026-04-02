package com.pes.gadgetrepair.enums;

/*
 Design Principles Used:
 1. Single Responsibility Principle
    - Only responsible for representing payment state.

 2. Open Closed Principle
    - Additional payment states can be added if the system evolves.

 Purpose in System:
 Used by Invoice entity to track payment completion.

 UML Mapping:
 Invoice → paymentStatus
*/

public enum PaymentStatus {

    UNPAID,
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}