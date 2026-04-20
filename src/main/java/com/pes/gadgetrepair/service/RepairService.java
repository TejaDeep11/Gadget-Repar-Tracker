package com.pes.gadgetrepair.service;

import com.pes.gadgetrepair.dto.RepairRequestDTO;
import com.pes.gadgetrepair.model.RepairRequest;

import java.util.List;
import java.util.Optional;

/*
 Design Principles Used:

 1. Single Responsibility Principle
    - Responsible for repair lifecycle management.

 2. Dependency Inversion Principle
    - Controllers depend on abstraction.

 Purpose in System:
 Handles operations related to repair requests such as
 creation, assignment, tracking, and updates.

 Related UML Entities:
 RepairRequest
 RepairLog
 Gadget
*/

public interface RepairService {

    RepairRequest createRepairRequest(RepairRequestDTO repairRequestDTO);

    Optional<RepairRequest> findRepairRequestById(Long requestId);

    List<RepairRequest> getAllRepairRequests();

    RepairRequest assignTechnician(Long requestId, Long technicianId);

    RepairRequest updateRepairStatus(Long requestId, String status);

      List<RepairRequest> getRepairsForTechnician(Long technicianId);

}