package com.pes.gadgetrepair.observer;

import com.pes.gadgetrepair.model.Customer;
import com.pes.gadgetrepair.model.RepairRequest;

/*
 Observer Implementation

 Responsibility:
 Sends notification to the customer when the
 repair status changes.

 In a real system this could send:
 - Email
 - SMS
 - Push notification
*/

public class CustomerNotificationObserver implements NotificationObserver {

    private final Customer customer;

    public CustomerNotificationObserver(Customer customer) {
        this.customer = customer;
    }

    @Override
    public void update(RepairRequest repairRequest) {

        System.out.println(
                "Notification to Customer: " + customer.getName()
                        + " | Repair Request ID: " + repairRequest.getRequestId()
                        + " | New Status: " + repairRequest.getStatus()
        );
    }
}