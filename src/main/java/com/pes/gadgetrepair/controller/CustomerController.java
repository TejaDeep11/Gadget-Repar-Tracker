package com.pes.gadgetrepair.controller;

import com.pes.gadgetrepair.config.UserSessionManager;
import com.pes.gadgetrepair.dto.RepairRequestDTO;
import com.pes.gadgetrepair.enums.DeviceType;
import com.pes.gadgetrepair.model.RepairRequest;
import com.pes.gadgetrepair.service.RepairService;
import com.pes.gadgetrepair.service.BillingService;
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
 Handles customer interactions with the system.

 Features:
 - Submit repair request
 - Track repair status
*/

@Controller
public class CustomerController {

    private final RepairService repairService;
    private final BillingService billingService;
    private final ApplicationContext context;
    private final UserSessionManager sessionManager;

    public CustomerController(RepairService repairService, BillingService billingService, ApplicationContext context, UserSessionManager sessionManager) {
        this.repairService = repairService;
        this.billingService = billingService;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    @FXML
    private TextField deviceNameField;

    @FXML
    private ComboBox<String> deviceTypeCombo;

    @FXML
    private TextField issueDescriptionField;

    @FXML
    private TextField estimatedCostField;

    @FXML
    private TableView<RepairRequest> repairRequestsTable;

    @FXML
    private TableColumn<RepairRequest, Long> idColumn;

    @FXML
    private TableColumn<RepairRequest, String> deviceColumn;

    @FXML
    private TableColumn<RepairRequest, String> issueColumn;

    @FXML
    private TableColumn<RepairRequest, String> statusColumn;

    @FXML
    private TableColumn<RepairRequest, String> dateColumn;

    @FXML
    private TableView<com.pes.gadgetrepair.model.Invoice> invoiceTable;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.Invoice, Long> invoiceIdCol;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.Invoice, Long> repairIdCol;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.Invoice, Double> amountCol;

    @FXML
    private TableColumn<com.pes.gadgetrepair.model.Invoice, String> statusPayCol;

    @FXML
    private void initialize() {
        // Populate device type combo box
        deviceTypeCombo.getItems().addAll(
            DeviceType.SMARTPHONE.name(),
            DeviceType.LAPTOP.name(),
            DeviceType.TABLET.name(),
            DeviceType.SMARTWATCH.name(),
            DeviceType.DESKTOP.name(),
            DeviceType.OTHER.name()
        );

        // Set up RepairRequest TableColumn cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        deviceColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getGadget() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGadget().getBrand());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        issueColumn.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().name())
        );
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().toString())
        );

        // Set up Invoice TableColumn cell value factories
        invoiceIdCol.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        repairIdCol.setCellValueFactory(cellData -> {
            if(cellData.getValue().getRepairRequest() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRepairRequest().getRequestId());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusPayCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus().name())
        );
    }

    @FXML
    private void handleSubmitRepair(ActionEvent event) {
        try {
            String deviceName = deviceNameField.getText();
            String deviceType = deviceTypeCombo.getValue();
            String issueDescription = issueDescriptionField.getText();

            if(deviceName.isEmpty() || deviceType == null || issueDescription.isEmpty()) {
                System.out.println("Please fill all required fields");
                return;
            }

            RepairRequestDTO dto = new RepairRequestDTO();
            
            // Set customer ID from current session
            Long customerId = sessionManager.getCurrentUserId();
            if(customerId == null) {
                System.out.println("Error: No logged-in user found");
                return;
            }
            
            dto.setCustomerId(customerId);
            dto.setBrand(deviceName);
            dto.setModel("Unknown"); // Default model since form doesn't capture it
            dto.setDeviceType(DeviceType.valueOf(deviceType));
            dto.setProblemDescription(issueDescription);
            
            RepairRequest repairRequest = submitRepairRequest(dto);
            System.out.println("Repair request submitted successfully: " + repairRequest.getRequestId());

            // Clear fields
            deviceNameField.clear();
            deviceTypeCombo.setValue(null);
            issueDescriptionField.clear();
            estimatedCostField.clear();

        } catch(Exception e) {
            System.out.println("Error submitting repair request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadRequests(ActionEvent event) {
        try {
            List<RepairRequest> allRequests = viewAllRequests();
            // Filter out DELIVERED repairs - they're only shown in Invoice history
            List<RepairRequest> activeRequests = allRequests.stream()
                .filter(r -> !r.getStatus().equals(com.pes.gadgetrepair.enums.RepairStatus.DELIVERED))
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Loaded " + activeRequests.size() + " active repair requests");
            
            // Convert to ObservableList and populate the table
            ObservableList<RepairRequest> observableRequests = FXCollections.observableArrayList(activeRequests);
            repairRequestsTable.setItems(observableRequests);
            
            // Also load invoices when requests are loaded
            loadInvoices();
            
        } catch(Exception e) {
            System.out.println("Error loading requests: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handlePayInvoice(ActionEvent event) {
        try {
            com.pes.gadgetrepair.model.Invoice selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
            if(selectedInvoice == null) {
                System.out.println("Please select an invoice to pay");
                return;
            }

            // Check if already paid
            if(selectedInvoice.getPaymentStatus().equals(com.pes.gadgetrepair.enums.PaymentStatus.PAID)) {
                javafx.scene.control.Alert alreadyPaidAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alreadyPaidAlert.setTitle("Already Paid");
                alreadyPaidAlert.setHeaderText("Invoice Already Paid");
                alreadyPaidAlert.setContentText("This invoice has already been marked as paid and is awaiting manager approval.");
                alreadyPaidAlert.showAndWait();
                return;
            }

            // Show payment dialog
            javafx.scene.control.Alert paymentDialog = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            paymentDialog.setTitle("Invoice Payment");
            paymentDialog.setHeaderText("Pay Invoice #" + selectedInvoice.getInvoiceId());
            paymentDialog.setContentText("Amount: $" + selectedInvoice.getAmount() + "\nRepair ID: " + selectedInvoice.getRepairRequest().getRequestId() + "\n\nProceed with payment?");
            
            java.util.Optional<javafx.scene.control.ButtonType> result = paymentDialog.showAndWait();
            if(result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                // Update repair status to PENDING_PAYMENT (waiting for manager approval)
                RepairRequest repair = selectedInvoice.getRepairRequest();
                repairService.updateRepairStatus(repair.getRequestId(), com.pes.gadgetrepair.enums.RepairStatus.PENDING_PAYMENT.name());
                
                // Update invoice payment status to PAID
                billingService.updatePaymentStatus(selectedInvoice.getInvoiceId(), com.pes.gadgetrepair.enums.PaymentStatus.PAID.name());
                
                System.out.println("Invoice #" + selectedInvoice.getInvoiceId() + " marked as PAID. Awaiting manager approval...");
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Payment Submitted");
                successAlert.setHeaderText("Payment Successful");
                successAlert.setContentText("Invoice has been marked as paid and is awaiting manager approval.");
                successAlert.showAndWait();
                
                // Reload invoices
                loadInvoices();
            }
        } catch(Exception e) {
            System.out.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadInvoices() {
        try {
            Long customerId = sessionManager.getCurrentUserId();
            if(customerId == null) {
                System.out.println("Error: No logged-in user found");
                return;
            }
            
            // Get all repairs for this customer
            List<RepairRequest> customerRepairs = viewAllRequests();
            
            // Create a list to store invoices for this customer
            java.util.List<com.pes.gadgetrepair.model.Invoice> customerInvoices = new java.util.ArrayList<>();
            
            // Get all invoices and filter for those belonging to this customer's repairs
            java.util.List<com.pes.gadgetrepair.model.Invoice> allInvoices = billingService.getAllInvoices();
            for(com.pes.gadgetrepair.model.Invoice inv : allInvoices) {
                if(inv.getRepairRequest() != null && inv.getRepairRequest().getCustomer().getId().equals(customerId)) {
                    customerInvoices.add(inv);
                }
            }
            
            System.out.println("Loaded " + customerInvoices.size() + " invoices for customer");
            
            // Convert to ObservableList and populate the table
            ObservableList<com.pes.gadgetrepair.model.Invoice> observableInvoices = FXCollections.observableArrayList(customerInvoices);
            invoiceTable.setItems(observableInvoices);
            
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

    public RepairRequest submitRepairRequest(RepairRequestDTO dto) {
        return repairService.createRepairRequest(dto);
    }

    public Optional<RepairRequest> getRepairStatus(Long requestId) {
        return repairService.findRepairRequestById(requestId);
    }

    public List<RepairRequest> viewAllRequests() {
        // Get current logged-in customer ID from session
        Long currentCustomerId = sessionManager.getCurrentUser().getId();
        List<RepairRequest> allRequests = repairService.getAllRepairRequests();
        // Filter to show only this customer's repair requests
        return allRequests.stream()
            .filter(r -> r.getCustomer() != null && r.getCustomer().getId().equals(currentCustomerId))
            .collect(java.util.stream.Collectors.toList());
    }
}