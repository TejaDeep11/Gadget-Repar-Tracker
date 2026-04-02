package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 Design Principles Used:

 1. Dependency Inversion Principle
    - Services depend on repository interfaces, not concrete implementations.

 2. Single Responsibility Principle
    - Handles persistence operations for User entities.

 Design Pattern:
 Repository Pattern (Spring Data JPA)

 Purpose:
 Provides CRUD operations for all types of users
 (Customer, Technician, Manager).

 UML Mapping:
 User Entity
*/

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

}