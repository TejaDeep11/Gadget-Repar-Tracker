package com.pes.gadgetrepair.enums;

/*
 Design Principles Used:
 1. Open Closed Principle
    - New device categories can be introduced without modifying logic.

 2. Type Safety
    - Prevents invalid device types from being used.

 Purpose in System:
 Used by Gadget entity to classify the device.

 UML Mapping:
 Gadget → deviceType
*/

public enum DeviceType {

    SMARTPHONE,
    LAPTOP,
    TABLET,
    SMARTWATCH,
    DESKTOP,
    OTHER
}