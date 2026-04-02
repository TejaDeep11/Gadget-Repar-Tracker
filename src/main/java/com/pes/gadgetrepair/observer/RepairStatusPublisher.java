package com.pes.gadgetrepair.observer;

import com.pes.gadgetrepair.model.RepairRequest;

import java.util.ArrayList;
import java.util.List;

/*
 Design Pattern:
 Observer Pattern → Subject / Publisher

 Responsibility:
 Maintains list of observers and notifies them
 whenever a repair status changes.

 SOLID Principles Applied:

 1. Single Responsibility Principle
    - Manages observers and notifications.

 2. Open Closed Principle
    - New observer types can be added easily.
*/

public class RepairStatusPublisher {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(RepairRequest repairRequest) {

        for (NotificationObserver observer : observers) {
            observer.update(repairRequest);
        }
    }
}