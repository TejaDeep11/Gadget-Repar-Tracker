package com.pes.gadgetrepair.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 Design Principles Used:
 Liskov Substitution Principle

 UML Mapping:
 Manager → inherits User
*/

@Entity
@Table(name = "manager")
public class Manager extends User {

    private String departmentId;

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}