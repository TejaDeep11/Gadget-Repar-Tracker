package com.pes.gadgetrepair.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 Design Principles Used:
 1. Liskov Substitution Principle
    - Customer extends User.

 UML Mapping:
 Customer → inherits User
*/

@Entity
@Table(name = "customer")
public class Customer extends User {

    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}