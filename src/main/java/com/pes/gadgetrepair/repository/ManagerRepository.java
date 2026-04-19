package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 Design Principles Used:
 1. Dependency Inversion Principle
 2. Single Responsibility Principle

 Purpose:
 Provides CRUD operations for Manager entities specifically.
 Extends base repository to handle Manager-specific queries.

 UML Mapping:
 Manager Entity
*/

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

    Optional<Manager> findByEmail(String email);
}
