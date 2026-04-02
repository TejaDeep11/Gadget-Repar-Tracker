package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Gadget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 Design Principles:
 Repository Pattern

 Purpose:
 Handles persistence of Gadget entities.
*/

@Repository
public interface GadgetRepository extends JpaRepository<Gadget, Long> {

}