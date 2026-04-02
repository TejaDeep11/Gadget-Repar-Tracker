package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 Design Principles:
 SRP

 Purpose:
 Handles inventory parts persistence.
*/

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

}