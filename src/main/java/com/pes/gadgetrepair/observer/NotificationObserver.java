package com.pes.gadgetrepair.observer;

import com.pes.gadgetrepair.model.RepairRequest;

/*
 Design Pattern:
 Behavioral Pattern → Observer Pattern

 Purpose:
 Defines the observer interface for receiving
 repair status updates.

 SOLID Principles Applied:

 1. Dependency Inversion Principle
    - High-level modules depend on abstraction.

 2. Open Closed Principle
    - New observers can be added without modifying existing code.

 Usage Flow:
 RepairStatusPublisher → NotificationObserver → Concrete Observers
*/

public interface NotificationObserver {

    void update(RepairRequest repairRequest);

}