package com.pes.gadgetrepair.service.impl;

import com.pes.gadgetrepair.exception.InventoryException;
import com.pes.gadgetrepair.model.Part;
import com.pes.gadgetrepair.repository.PartRepository;
import com.pes.gadgetrepair.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
Design Principles Used:
1. Single Responsibility Principle
2. Dependency Inversion Principle

Role:
Handles spare parts inventory management.
*/

@Service
public class InventoryServiceImpl implements InventoryService {

    private final PartRepository partRepository;

    public InventoryServiceImpl(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Override
    public Part addPart(Part part) {
        return partRepository.save(part);
    }

    @Override
    public Optional<Part> findPartById(Long partId) {
        return partRepository.findById(partId);
    }

    @Override
    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    @Override
    public Part updateStock(Long partId, int quantity) {

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new InventoryException("Part not found"));

        part.setQuantity(part.getQuantity() + quantity);

        return partRepository.save(part);
    }
}