package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.PartUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 Design Principles:
 SRP

 Purpose:
 Tracks part usage in repair operations.
*/

@Repository
public interface PartUsageRepository extends JpaRepository<PartUsage, Long> {

}