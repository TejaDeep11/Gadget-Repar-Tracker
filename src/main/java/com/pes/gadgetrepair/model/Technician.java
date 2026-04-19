package com.pes.gadgetrepair.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 Design Principles Used:
 1. Liskov Substitution Principle
 2. Single Responsibility Principle

 UML Mapping:
 Technician → inherits User
*/

@Entity
@Table(name = "technician")
public class Technician extends User {

    private String specialization;

    private Double efficiencyRating;

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Double getEfficiencyRating() {
        return efficiencyRating;
    }

    public void setEfficiencyRating(Double efficiencyRating) {
        this.efficiencyRating = efficiencyRating;
    }
}