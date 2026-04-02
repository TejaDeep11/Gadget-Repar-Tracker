package com.pes.gadgetrepair.model;

import com.pes.gadgetrepair.enums.DeviceType;
import jakarta.persistence.*;

/*
 Design Principles:
 Single Responsibility Principle

 UML Mapping:
 Gadget Entity
*/

@Entity
@Table(name = "gadgets")
public class Gadget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gadgetId;

    private String brand;

    private String model;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public Gadget() {}

    public Gadget(String brand, String model, DeviceType deviceType) {
        this.brand = brand;
        this.model = model;
        this.deviceType = deviceType;
    }

    public Long getGadgetId() {
        return gadgetId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }
}