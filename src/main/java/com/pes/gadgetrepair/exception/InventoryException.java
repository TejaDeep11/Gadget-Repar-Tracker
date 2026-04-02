package com.pes.gadgetrepair.exception;

/*
 Custom Exception

 Purpose:
 Handles errors related to inventory management.

 Example Cases:
 - Part not found
 - Insufficient stock
*/

public class InventoryException extends RuntimeException {

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}