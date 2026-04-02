package com.pes.gadgetrepair.enums;

/*
 Design Principles Used:
 1. Open Closed Principle (OCP)
    - New repair states can be added without modifying existing logic.

 2. Single Responsibility Principle (SRP)
    - This enum only represents the lifecycle status of a repair request.

 Purpose in System:
 Used by RepairRequest entity to track the progress of a repair.

 UML Mapping:
 State Diagram → Repair Lifecycle
*/

public enum RepairStatus {

    REQUEST_SUBMITTED,
    DIAGNOSIS_IN_PROGRESS,
    WAITING_FOR_PARTS,
    REPAIR_IN_PROGRESS,
    REPAIR_COMPLETED,
    READY_FOR_PICKUP,
    PENDING_PAYMENT,
    DELIVERED,
    CANCELLED
}