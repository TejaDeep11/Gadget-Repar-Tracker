package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.RepairLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 Design Principles:
 SRP

 Purpose:
 Persistence operations for repair logs.
*/

@Repository
public interface RepairLogRepository extends JpaRepository<RepairLog, Long> {

}