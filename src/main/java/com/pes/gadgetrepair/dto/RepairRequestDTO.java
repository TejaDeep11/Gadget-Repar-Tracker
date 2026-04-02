package com.pes.gadgetrepair.dto;

import com.pes.gadgetrepair.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 Design Principles Used:

 1. Single Responsibility Principle (SRP)
    - This class is responsible only for transferring repair request data
      between the UI layer and service layer.

 2. Separation of Concerns
    - Prevents direct exposure of database entities to the UI.

 3. Open Closed Principle
    - New fields can be added without modifying service logic.

 MVC Role:
 Controller  →  DTO  →  Service  →  Entity

 Purpose in System:
 Captures user input when a customer submits a repair request.

 Related UML Classes:
 RepairRequest
 Gadget
 Customer
*/

public class RepairRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Brand cannot be empty")
    private String brand;

    @NotBlank(message = "Model cannot be empty")
    private String model;

    @NotNull(message = "Device type must be selected")
    private DeviceType deviceType;

    @NotBlank(message = "Problem description cannot be empty")
    private String problemDescription;

    public RepairRequestDTO() {}

    public RepairRequestDTO(Long customerId,
                            String brand,
                            String model,
                            DeviceType deviceType,
                            String problemDescription) {
        this.customerId = customerId;
        this.brand = brand;
        this.model = model;
        this.deviceType = deviceType;
        this.problemDescription = problemDescription;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }
}