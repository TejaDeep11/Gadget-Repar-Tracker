package com.pes.gadgetrepair.model;

import jakarta.persistence.*;

/*
 Design Principles Used:

 1. Single Responsibility Principle
    - Represents spare parts used in repair operations.

 2. Encapsulation
    - Fields are private and accessed through getters/setters.

 UML Mapping:
 Part entity used for inventory management.
*/

@Entity
@Table(name = "parts")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partId;

    private String partName;

    @Column(name = "stock_quantity", nullable = true)
    private Integer quantity;

    private double price;

    private String supplier;

    public Part() {}

    public Part(String partName, double price) {
        this.partName = partName;
        this.quantity = null; // Quantity set later
        this.price = price;
    }

    public Part(String partName, double price, String supplier) {
        this.partName = partName;
        this.quantity = null;
        this.price = price;
        this.supplier = supplier;
    }

    public Part(String partName, Integer quantity, double price) {
        this.partName = partName;
        this.quantity = quantity;
        this.price = price;
    }

    public Part(String partName, Integer quantity, double price, String supplier) {
        this.partName = partName;
        this.quantity = quantity;
        this.price = price;
        this.supplier = supplier;
    }

    public Long getPartId() {
        return partId;
    }

    public String getPartName() {
        return partName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}