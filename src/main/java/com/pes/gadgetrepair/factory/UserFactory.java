package com.pes.gadgetrepair.factory;

import com.pes.gadgetrepair.enums.UserRole;
import com.pes.gadgetrepair.model.Customer;
import com.pes.gadgetrepair.model.Manager;
import com.pes.gadgetrepair.model.Technician;
import com.pes.gadgetrepair.model.User;

/*
 Factory Pattern Implementation

 Purpose:
 Creates User objects based on role.

 Design Pattern:
 Creational Pattern → Factory

 Used in:
 Registration or Admin user creation
*/

public class UserFactory {

    public static User createUser(UserRole role,
                                  String name,
                                  String email,
                                  String phone,
                                  String password) {

        User user = null;

        switch (role) {

            case CUSTOMER:
                user = new Customer();
                break;

            case TECHNICIAN:
                user = new Technician();
                break;

            case MANAGER:
                user = new Manager();
                break;

            default:
                throw new IllegalArgumentException("Invalid user role");
        }

        // Set common attributes
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(password);
        user.setRole(role);

        return user;
    }

}