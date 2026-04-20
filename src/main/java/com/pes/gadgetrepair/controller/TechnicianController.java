package com.pes.gadgetrepair.controller;

import com.pes.gadgetrepair.config.UserSessionManager;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.enums.RepairStatus;
import com.pes.gadgetrepair.service.RepairService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

/*
 Controller Layer

 Responsibility:
 Allows technicians to manage repair jobs.

 Features:
 - Assign technician
 - Update repair status
*/

@Controller
public class TechnicianController {

    private final RepairService repairService;
    private final ApplicationContext context;
    private final UserSessionManager sessionManager;
    private final com.pes.gadgetrepair.service.InventoryService inventoryService;

    public TechnicianController(
            RepairService repairService,
            ApplicationContext context,
            UserSessionManager sessionManager,
            com.pes.gadgetrepair.service.InventoryService inventoryService
    ) {
        this.repairService = repairService;
        this.context = context;
        this.sessionManager = sessionManager;
        this.inventoryService = inventoryService;
    }

    @FXML
    private TableView<RepairRequest> pendingRepairsTable;

    @FXML
    private TableColumn<RepairRequest, Long> techIdColumn;

    @FXML
    private TableColumn<RepairRequest, String> techDeviceColumn;

    @FXML
    private TableColumn<RepairRequest, String> techIssueColumn;

    @FXML
    private TableColumn<RepairRequest, String> techCustomerColumn;

    @FXML
    private TableColumn<RepairRequest, String> techDateColumn;

    @FXML
    private TextField repairIdField;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private TextField notesField;

    @FXML
    private TextField assignRepairIdField;

    @FXML
    private ComboBox<String> partsCombo;

    @FXML
    private TextField partQuantityField;

    @FXML
    private TableView<com.pes.gadgetrepair.model.PartUsage> assignedPartsTable;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.PartUsage, String> partNameCol;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.PartUsage, Integer> usedQtyCol;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.PartUsage, Double> unitPriceCol;

    @FXML
    private void initialize() {
        // Populate repair status combo box (excluding DELIVERED and PENDING_PAYMENT - manager-only actions)
        statusCombo.getItems().addAll(
            RepairStatus.REQUEST_SUBMITTED.name(),
            RepairStatus.DIAGNOSIS_IN_PROGRESS.name(),
            RepairStatus.WAITING_FOR_PARTS.name(),
            RepairStatus.REPAIR_IN_PROGRESS.name(),
            RepairStatus.REPAIR_COMPLETED.name(),
            RepairStatus.READY_FOR_PICKUP.name(),
            RepairStatus.CANCELLED.name()
        );

        // Set up TableColumn cell value factories for pending repairs
        techIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        techDeviceColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getGadget() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGadget().getBrand());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        techIssueColumn.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        techCustomerColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getCustomer() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        techDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().toString())
        );

        // Set up Assigned Parts table columns
        partNameCol.setCellValueFactory(cellData -> {
            if(cellData.getValue().getPart() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPart().getPartName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        usedQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        unitPriceCol.setCellValueFactory(cellData -> {
            if(cellData.getValue().getPart() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPart().getPrice());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(0.0);
        });

        // Load available parts into combo box
        loadAvailableParts();
    }

    private void loadAvailableParts() {
        try {
            java.util.List<com.pes.gadgetrepair.model.Part> parts = inventoryService.getAllParts();
            java.util.List<String> partNames = new java.util.ArrayList<>();
            for(com.pes.gadgetrepair.model.Part part : parts) {
                partNames.add(part.getPartName());
            }
            partsCombo.getItems().addAll(partNames);
            System.out.println("Loaded " + partNames.size() + " available parts");
        } catch(Exception e) {
            System.out.println("Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadPendingRepairs(ActionEvent event) {
        try {
            Long currentTechnicianId = sessionManager.getCurrentUserId();
            if(currentTechnicianId == null) {
                System.out.println("ERROR: No technician logged in");
                return;
            }

            java.util.List<RepairRequest> assignedRepairs = repairService.getRepairsForTechnician(currentTechnicianId);
            java.util.List<RepairRequest> pendingRepairs = assignedRepairs.stream()
                .filter(r -> !r.getStatus().equals(RepairStatus.DELIVERED))
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Loaded " + pendingRepairs.size() + " repairs assigned to technician #" + currentTechnicianId);
            
            // Convert to ObservableList and populate the table
            ObservableList<RepairRequest> observableRepairs = FXCollections.observableArrayList(pendingRepairs);
            pendingRepairsTable.setItems(observableRepairs);
            
        } catch(Exception e) {
            System.out.println("Error loading assigned repairs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        try {
            String repairIdText = repairIdField.getText();
            String newStatus = statusCombo.getValue();
            String notes = notesField.getText();

            if(repairIdText.isEmpty() || newStatus == null) {
                System.out.println("Please fill all required fields");
                return;
            }

            Long repairId = Long.parseLong(repairIdText);
            updateRepairStatus(repairId, newStatus);
            
            System.out.println("Repair status updated successfully");
            repairIdField.clear();
            notesField.clear();

        } catch(Exception e) {
            System.out.println("Error updating repair status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAssignPart(ActionEvent event) {
        try {
            String repairIdText = assignRepairIdField.getText().trim();
            String selectedPartComboValue = partsCombo.getValue();
            String qtyText = partQuantityField.getText().trim();

            if(repairIdText.isEmpty() || selectedPartComboValue == null || qtyText.isEmpty()) {
                System.out.println("ERROR: Please fill all required fields - Repair ID, Part, and Quantity");
                return;
            }

            // Extract part name (no longer includes quantity in combo)
            final String partNameForLookup = selectedPartComboValue.trim();
            System.out.println("DEBUG: Selected part: '" + partNameForLookup + "'");

            // Validate repair ID
            Long repairId;
            try {
                repairId = Long.parseLong(repairIdText);
            } catch(NumberFormatException e) {
                System.out.println("ERROR: Repair ID must be a number, got: " + repairIdText);
                return;
            }

            // Validate quantity - must be a positive integer
            int quantity;
            try {
                quantity = Integer.parseInt(qtyText);
                if(quantity <= 0) {
                    System.out.println("ERROR: Quantity must be greater than 0, got: " + quantity);
                    return;
                }
            } catch(NumberFormatException e) {
                System.out.println("ERROR: Quantity must be a number, got: " + qtyText);
                return;
            }

            // Get repair request
            java.util.Optional<RepairRequest> repairOptional = repairService.findRepairRequestById(repairId);
            if(!repairOptional.isPresent()) {
                System.out.println("ERROR: Repair request not found with ID: " + repairId);
                return;
            }
            
            RepairRequest repair = repairOptional.get();

            // Find part by name
            java.util.List<com.pes.gadgetrepair.model.Part> allParts = inventoryService.getAllParts();
            java.util.Optional<com.pes.gadgetrepair.model.Part> partOptional = allParts.stream()
                    .filter(p -> p.getPartName().equals(partNameForLookup))
                    .findFirst();

            if(!partOptional.isPresent()) {
                System.out.println("ERROR: Part not found with name: " + partNameForLookup);
                return;
            }

            com.pes.gadgetrepair.model.Part part = partOptional.get();

            // Check if part has enough quantity
            if(part.getQuantity() == null) {
                System.out.println("ERROR: Part '" + partNameForLookup + "' has no quantity set. Manager must set quantity first.");
                return;
            }

            if(part.getQuantity() < quantity) {
                System.out.println("ERROR: Insufficient quantity for part '" + partNameForLookup + "' - Available: " + part.getQuantity() + ", Requested: " + quantity);
                return;
            }

            // Decrease inventory
            inventoryService.updateStock(part.getPartId(), -quantity);
            System.out.println("SUCCESS: Inventory decreased for part: " + partNameForLookup + ", quantity decreased by: " + quantity);

            // Create PartUsage record
            com.pes.gadgetrepair.model.PartUsage partUsage = new com.pes.gadgetrepair.model.PartUsage();
            partUsage.setRepairRequest(repair);
            partUsage.setPart(part);
            partUsage.setQuantityUsed(quantity);

            // TODO: Save PartUsage (might need a service method)
            System.out.println("SUCCESS: " + quantity + " of " + partNameForLookup + " assigned to repair #" + repairId);

            // Clear fields
            assignRepairIdField.clear();
            partsCombo.setValue(null);
            partQuantityField.clear();

        } catch(NumberFormatException e) {
            System.out.println("ERROR: Number format error - " + e.getMessage());
        } catch(Exception e) {
            System.out.println("ERROR assigning part: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle("Gadget Repair Tracker");
            stage.show();

        } catch(Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public RepairRequest assignTechnician(Long requestId, Long technicianId) {

        return repairService.assignTechnician(requestId, technicianId);
    }

    public RepairRequest updateRepairStatus(Long requestId, String status) {

        return repairService.updateRepairStatus(requestId, status);
    }
}