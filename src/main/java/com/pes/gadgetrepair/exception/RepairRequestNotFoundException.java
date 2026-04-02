package com.pes.gadgetrepair.exception;

/*
 Custom Exception

 Purpose:
 Thrown when a repair request cannot be located.

 Related Entity:
 RepairRequest
*/

public class RepairRequestNotFoundException extends RuntimeException {

    public RepairRequestNotFoundException(Long requestId) {
        super("Repair request not found with ID: " + requestId);
    }

    public RepairRequestNotFoundException(String message) {
        super(message);
    }
}