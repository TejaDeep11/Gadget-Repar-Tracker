package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 Design Principles Used:
 1. Dependency Inversion Principle
 2. Single Responsibility Principle

 Purpose:
 Provides CRUD operations for Customer entities specifically.
 Extends base repository to handle Customer-specific queries.

 UML Mapping:
 Customer Entity
*/

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
}
