package com.pes.gadgetrepair.service;

import com.pes.gadgetrepair.model.User;

import java.util.Optional;

/*
 Design Principles Used:

 1. Dependency Inversion Principle
    - Controllers depend on this interface instead of implementation.

 2. Single Responsibility Principle
    - Handles business operations related to system users.

 3. Interface Segregation Principle
    - Defines only operations related to user management.

 Purpose in System:
 Provides user authentication and user management operations.

 Related Entities:
 User, Customer, Technician, Manager
*/

public interface UserService {

    User authenticate(String email,String password);

    User saveUser(User user);

}