package com.pes.gadgetrepair.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/*
 Design Principles:
 SRP

 UML Mapping:
 RepairLog
*/

@Entity
@Table(name = "repair_logs")
public class RepairLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    private RepairRequest repairRequest;

    private String updateMessage;

    private LocalDateTime timestamp;

    public RepairLog() {}

    public RepairLog(RepairRequest repairRequest, String updateMessage) {
        this.repairRequest = repairRequest;
        this.updateMessage = updateMessage;
        this.timestamp = LocalDateTime.now();
    }
}