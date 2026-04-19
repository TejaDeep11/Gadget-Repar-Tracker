package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 Design Principles Used:
 1. Dependency Inversion Principle
 2. Single Responsibility Principle

 Purpose:
 Provides CRUD operations for Technician entities specifically.
 Extends base repository to handle Technician-specific queries.

 UML Mapping:
 Technician Entity
*/

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    Optional<Technician> findByEmail(String email);
}
