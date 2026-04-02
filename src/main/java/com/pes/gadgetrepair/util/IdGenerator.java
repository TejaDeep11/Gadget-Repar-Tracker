package com.pes.gadgetrepair.util;

import java.util.UUID;

/*
 Utility Class

 Purpose:
 Generates unique identifiers for system entities
 such as invoices, repair requests, etc.

 SOLID Principles:

 1. Single Responsibility Principle
    - Responsible only for ID generation.

 2. Utility Design
    - Static methods used without object creation.

 Usage Example:

 String id = IdGenerator.generateId("INV");
*/

public final class IdGenerator {

    private IdGenerator() {
        // Prevent instantiation
    }

    public static String generateId(String prefix) {

        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + uniquePart;
    }
}