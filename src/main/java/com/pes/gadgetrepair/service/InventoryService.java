package com.pes.gadgetrepair.service;

import com.pes.gadgetrepair.model.Part;

import java.util.List;
import java.util.Optional;

/*
 Design Principles Used:

 1. Single Responsibility Principle
    - Manages spare parts inventory.

 2. Open Closed Principle
    - New inventory operations can be added without modifying existing logic.

 Purpose in System:
 Handles spare parts inventory used during repairs.

 Related UML Entities:
 Part
 PartUsage
*/

public interface InventoryService {

    Part addPart(Part part);

    Optional<Part> findPartById(Long partId);

    List<Part> getAllParts();

    Part updateStock(Long partId, int quantity);

}