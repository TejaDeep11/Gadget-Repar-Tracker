package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.enums.RepairStatus;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 Design Principles:
 1. Single Responsibility Principle
 2. Repository Pattern

 Purpose:
 Handles database operations related to repair requests.

 UML Mapping:
 RepairRequest
*/

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {

    List<RepairRequest> findByStatus(RepairStatus status);

    @Query("SELECT r FROM RepairRequest r WHERE r.technician.id = :technicianId")
    List<RepairRequest> findByTechnicianId(@Param("technicianId") Long technicianId);

    List<RepairRequest> findByTechnician(Technician technician);

}