package com.pes.gadgetrepair.controller;

import com.pes.gadgetrepair.config.UserSessionManager;
import com.pes.gadgetrepair.model.Invoice;
import com.pes.gadgetrepair.model.Part;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.service.BillingService;
import com.pes.gadgetrepair.service.InventoryService;
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

import java.util.List;
import java.util.Optional;

/*
 Controller Layer

 Responsibility:
 Allows managers to manage inventory and billing.

 Features:
 - Add spare parts
 - Update stock
 - Generate invoices
*/

@Controller
public class ManagerController {

    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final RepairService repairService;
    private final ApplicationContext context;
    private final UserSessionManager sessionManager;

    public ManagerController(
            InventoryService inventoryService,
            BillingService billingService,
            RepairService repairService,
            ApplicationContext context,
            UserSessionManager sessionManager
    ) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.repairService = repairService;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    @FXML
    private TableView<Part> inventoryTable;

    @FXML
    private TableColumn<Part, Long> partIdColumn;

    @FXML
    private TableColumn<Part, String> partNameColumn;

    @FXML
    private TableColumn<Part, Integer> quantityColumn;

    @FXML
    private TableColumn<Part, Double> priceColumn;

    @FXML
    private TableColumn<Part, String> supplierColumn;

    @FXML
    private TableView<Invoice> invoicesTable;

    @FXML
    private TableColumn<Invoice, Long> invoiceIdColumn;

    @FXML
    private TableColumn<Invoice, Long> repairIdColumn;

    @FXML
    private TableColumn<Invoice, Double> amountColumn;

    @FXML
    private TableColumn<Invoice, String> invoiceDateColumn;

    @FXML
    private TableColumn<Invoice, String> invoiceStatusColumn;

    @FXML
    private TableView<RepairRequest> historyTable;

    @FXML
    private TableColumn<RepairRequest, Long> historyIdCol;

    @FXML
    private TableColumn<RepairRequest, String> historyDeviceCol;

    @FXML
    private TableColumn<RepairRequest, String> historyCustomerCol;

    @FXML
    private TableColumn<RepairRequest, Double> historyAmountCol;

    @FXML
    private TableColumn<RepairRequest, String> historyCompletedDateCol;

    @FXML
    private TextField partNameField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField supplierField;

    @FXML
    private ComboBox<String> partQuantityCombo;

    @FXML
    private void initialize() {
        // Set up Part table cell value factories
        partIdColumn.setCellValueFactory(new PropertyValueFactory<>("partId"));
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        // Set up Invoice table cell value factories
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        repairIdColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getRepairRequest() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRepairRequest().getRequestId());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        invoiceDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("N/A")
        );
        invoiceStatusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus().name())
        );

        // Set up History table cell value factories
        historyIdCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        historyDeviceCol.setCellValueFactory(cellData -> {
            if(cellData.getValue().getGadget() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGadget().getBrand());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        historyCustomerCol.setCellValueFactory(cellData -> {
            if(cellData.getValue().getCustomer() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        historyAmountCol.setCellValueFactory(cellData -> {
            try {
                // Get the repair request ID
                Long repairId = cellData.getValue().getRequestId();
                // Find the associated invoice
                java.util.List<Invoice> allInvoices = billingService.getAllInvoices();
                java.util.Optional<Invoice> invoiceOptional = allInvoices.stream()
                    .filter(inv -> inv.getRepairRequest() != null && 
                                   inv.getRepairRequest().getRequestId().equals(repairId))
                    .findFirst();
                
                if(invoiceOptional.isPresent()) {
                    return new javafx.beans.property.SimpleObjectProperty<>(invoiceOptional.get().getAmount());
                }
            } catch(Exception e) {
                System.out.println("Error getting amount for history: " + e.getMessage());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(0.0);
        });
        historyCompletedDateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().toString())
        );
    }

    @FXML
    private void handleLoadInventory(ActionEvent event) {
        try {
            List<Part> parts = viewAllParts();
            System.out.println("Loaded " + parts.size() + " parts");
            
            // Debug: Print quantities for each part
            for(Part part : parts) {
                System.out.println("Part: " + part.getPartName() + " | Quantity: " + part.getQuantity() + " | Price: " + part.getPrice());
            }
            
            // Convert to ObservableList and populate the table
            ObservableList<Part> observableParts = FXCollections.observableArrayList(parts);
            inventoryTable.setItems(observableParts);
            inventoryTable.refresh(); // Force refresh to ensure data displays
            
        } catch(Exception e) {
            System.out.println("Error loading inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPart(ActionEvent event) {
        try {
            String name = partNameField.getText().trim();
            String priceText = priceField.getText().trim();
            String supplier = supplierField.getText().trim();

            // Quantity is now OPTIONAL - will be added/updated later
            if(name.isEmpty() || priceText.isEmpty() || supplier.isEmpty()) {
                System.out.println("ERROR: Please fill all required fields - Name, Price, and Supplier (Quantity is optional)");
                return;
            }

            // Validate price
            double price;
            try {
                price = Double.parseDouble(priceText);
                if(price < 0) {
                    System.out.println("ERROR: Price cannot be negative, got: " + price);
                    return;
                }
            } catch(NumberFormatException e) {
                System.out.println("ERROR: Price must be a valid number, got: " + priceText);
                return;
            }

            // Create part without quantity (optional step)
            Part part = new Part(name, price, supplier);
            addPart(part);
            System.out.println("SUCCESS: Part '" + name + "' added successfully! Use 'Update Part Quantity' tab to set quantity.");

            // Clear fields
            partNameField.clear();
            quantityField.clear();
            priceField.clear();
            supplierField.clear();

        } catch(Exception e) {
            System.out.println("ERROR adding part: " + e.getMessage());
            System.out.println("Error adding part: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateInvoice(ActionEvent event) {
        try {
            System.out.println("Generating invoice...");
            // TODO: Implement invoice generation UI
        } catch(Exception e) {
            System.out.println("Error generating invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateQuantity(ActionEvent event) {
        try {
            String selectedPartName = partQuantityCombo.getValue();
            if(selectedPartName == null || selectedPartName.isEmpty()) {
                System.out.println("ERROR: Please select a part from the ComboBox");
                return;
            }

            String quantityText = quantityField.getText().trim();
            if(quantityText.isEmpty()) {
                System.out.println("ERROR: Please enter a quantity value");
                return;
            }

            int newQuantity;
            try {
                newQuantity = Integer.parseInt(quantityText);
                if(newQuantity < 0) {
                    System.out.println("ERROR: Quantity cannot be negative, got: " + newQuantity);
                    return;
                }
            } catch(NumberFormatException e) {
                System.out.println("ERROR: Quantity must be a valid number, got: " + quantityText);
                return;
            }
            
            // Find the part by name
            List<Part> allParts = inventoryService.getAllParts();
            Optional<Part> partOptional = allParts.stream()
                    .filter(p -> p.getPartName().equals(selectedPartName))
                    .findFirst();
            
            if(partOptional.isPresent()) {
                Part selectedPart = partOptional.get();
                selectedPart.setQuantity(newQuantity);
                inventoryService.addPart(selectedPart); // Save updated part
                
                System.out.println("SUCCESS: Part '" + selectedPart.getPartName() + "' quantity updated to " + newQuantity);
                quantityField.clear();
                partQuantityCombo.setValue(null);
                loadPartQuantityCombo(); // Reload combo
                handleLoadInventory(new ActionEvent()); // Reload inventory table
            } else {
                System.out.println("ERROR: Part '" + selectedPartName + "' not found in database");
            }
            
        } catch(Exception e) {
            System.out.println("ERROR updating quantity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPartQuantityCombo() {
        try {
            List<Part> parts = inventoryService.getAllParts();
            ObservableList<String> partNames = FXCollections.observableArrayList();
            for(Part part : parts) {
                partNames.add(part.getPartName());
            }
            partQuantityCombo.setItems(partNames);
            System.out.println("Loaded " + partNames.size() + " parts for quantity combo");
        } catch(Exception e) {
            System.out.println("Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadPartQuantityCombo(ActionEvent event) {
        loadPartQuantityCombo();
    }

    @FXML
    private void handleLoadHistory(ActionEvent event) {
        try {
            // Load all repairs with DELIVERED status (completed and paid)
            List<RepairRequest> allRepairs = repairService.getAllRepairRequests();
            List<Invoice> allInvoices = billingService.getAllInvoices();
            
            List<RepairRequest> history = allRepairs.stream()
                .filter(r -> r.getStatus().equals(com.pes.gadgetrepair.enums.RepairStatus.DELIVERED) ||
                             r.getStatus().equals(com.pes.gadgetrepair.enums.RepairStatus.READY_FOR_PICKUP))
                // Also filter: only show if payment is PAID
                .filter(r -> {
                    java.util.Optional<Invoice> invoiceOptional = allInvoices.stream()
                        .filter(inv -> inv.getRepairRequest() != null && 
                                       inv.getRepairRequest().getRequestId().equals(r.getRequestId()))
                        .findFirst();
                    
                    if(invoiceOptional.isPresent()) {
                        return invoiceOptional.get().getPaymentStatus().equals(com.pes.gadgetrepair.enums.PaymentStatus.PAID);
                    }
                    return false; // No invoice found, don't show
                })
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Loaded " + history.size() + " completed and paid repairs");
            
            // Convert to ObservableList and populate the table
            ObservableList<RepairRequest> observableHistory = FXCollections.observableArrayList(history);
            historyTable.setItems(observableHistory);
            
        } catch(Exception e) {
            System.out.println("Error loading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApprovePayment(ActionEvent event) {
        try {
            Invoice selectedInvoice = invoicesTable.getSelectionModel().getSelectedItem();
            if(selectedInvoice == null) {
                System.out.println("Please select an invoice to approve");
                return;
            }

            // Only approve if payment status is PAID
            if(!selectedInvoice.getPaymentStatus().equals(com.pes.gadgetrepair.enums.PaymentStatus.PAID)) {
                System.out.println("Invoice payment status is not PAID. Current status: " + selectedInvoice.getPaymentStatus().name());
                javafx.scene.control.Alert notPaidAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                notPaidAlert.setTitle("Cannot Approve");
                notPaidAlert.setHeaderText("Invoice Not Marked as Paid");
                notPaidAlert.setContentText("Only invoices marked as PAID by customers can be approved. Current status: " + selectedInvoice.getPaymentStatus().name());
                notPaidAlert.showAndWait();
                return;
            }

            // Show confirmation dialog
            javafx.scene.control.Alert confirmDialog = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Approve Payment");
            confirmDialog.setHeaderText("Approve Invoice #" + selectedInvoice.getInvoiceId());
            confirmDialog.setContentText("Amount: $" + selectedInvoice.getAmount() + 
                                        "\nRepair ID: " + selectedInvoice.getRepairRequest().getRequestId() +
                                        "\n\nApprove this payment?");
            
            java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();
            if(result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                // Update repair status to DELIVERED (manager approval)
                RepairRequest repair = selectedInvoice.getRepairRequest();
                repairService.updateRepairStatus(repair.getRequestId(), com.pes.gadgetrepair.enums.RepairStatus.DELIVERED.name());
                
                System.out.println("Payment approved! Repair #" + repair.getRequestId() + " status set to DELIVERED.");
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Payment Approved");
                successAlert.setHeaderText("Success");
                successAlert.setContentText("Payment has been approved. Repair is now marked as DELIVERED.");
                successAlert.showAndWait();
                
                // Reload invoices
                handleLoadInvoices(new ActionEvent());
            }
        } catch(Exception e) {
            System.out.println("Error approving payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadInvoices(ActionEvent event) {
        try {
            java.util.List<Invoice> allInvoices = billingService.getAllInvoices();
            System.out.println("Loaded " + allInvoices.size() + " invoices");
            
            for(Invoice inv : allInvoices) {
                System.out.println("Invoice #" + inv.getInvoiceId() + " | Repair: " + inv.getRepairRequest().getRequestId() + 
                                   " | Amount: $" + inv.getAmount() + " | Status: " + inv.getPaymentStatus().name());
            }
            
            // Convert to ObservableList and populate the table
            ObservableList<Invoice> observableInvoices = FXCollections.observableArrayList(allInvoices);
            invoicesTable.setItems(observableInvoices);
            invoicesTable.refresh();
            
        } catch(Exception e) {
            System.out.println("Error loading invoices: " + e.getMessage());
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

    public Part addPart(Part part) {

        return inventoryService.addPart(part);
    }

    public Part updateStock(Long partId, int quantity) {

        return inventoryService.updateStock(partId, quantity);
    }

    public List<Part> viewAllParts() {

        return inventoryService.getAllParts();
    }

    public Invoice generateInvoice(Long repairRequestId, double amount) {

        return billingService.generateInvoice(repairRequestId, amount);
    }

    public Optional<Invoice> getInvoice(Long invoiceId) {

        return billingService.findInvoiceById(invoiceId);
    }
}