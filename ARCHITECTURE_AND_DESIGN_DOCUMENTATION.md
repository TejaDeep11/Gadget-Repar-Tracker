# Gadget Repair Tracker - Complete Architecture & Design Documentation

**Project**: Gadget Repair Tracker  
**Type**: Spring Boot 4.0.4 + JavaFX 21 Desktop Application  
**Database**: MySQL 8.0.45  
**Date**: April 2026

---

## TABLE OF CONTENTS

1. [System Overview](#system-overview)
2. [MVC Architecture](#mvc-architecture)
3. [System Layers & Data Flow](#system-layers--data-flow)
4. [SOLID Principles](#solid-principles)
5. [GRASP Principles](#grasp-principles)
6. [Design Patterns](#design-patterns)
7. [How Everything Works](#how-everything-works)

---

## SYSTEM OVERVIEW

### What is the Application?

The **Gadget Repair Tracker** is a comprehensive repair management system for electronics repair shops. It enables:
- **Customers** to submit repair requests and track progress
- **Technicians** to manage assigned repair jobs and update statuses
- **Managers** to assign technicians, manage inventory, and process payments

### Technology Stack

```
┌─────────────────────────────────────────┐
│         JavaFX 21 (Desktop UI)          │
├─────────────────────────────────────────┤
│    Spring Boot 4.0.4 (Application)      │
├─────────────────────────────────────────┤
│  Spring Data JPA + Hibernate ORM 7.2.7  │
├─────────────────────────────────────────┤
│      MySQL 8.0.45 Database              │
└─────────────────────────────────────────┘
```

### Database Structure

```
┌──────────────────────────────────────────────────────┐
│                   DATABASE: gadget_repair_db         │
├──────────────────────────────────────────────────────┤
│                                                      │
│   users (BASE TABLE - JOINED Inheritance)            │
│   ├── customer (extends users)                       │
│   ├── technician (extends users)                     │
│   └── manager (extends users)                        │
│                                                      │
│   repair_requests (CENTRAL ENTITY)                   │
│   ├── FK → customer(id)                              │
│   ├── FK → technician(id)  [nullable]                │
│   ├── FK → gadgets                                   │
│   └── M:M → parts (via part_usage)                   │
│                                                      │
│   Supporting Tables:                                 │
│   ├── gadgets                                        │
│   ├── invoices                                       │
│   ├── parts                                          │
│   ├── part_usage (junction table)                    │
│   └── repair_logs (audit trail)                      │
│                                                      │
└──────────────────────────────────────────────────────┘
```

---

## MVC ARCHITECTURE

### Overview

MVC (Model-View-Controller) separates concerns into three distinct layers:

```
┌─────────────────────────────────────────────────────────┐
│                    VIEW LAYER                           │
│         (JavaFX UI + FXML Templates)                    │
│  - Login Screen                                         │
│  - Customer Dashboard                                   │
│  - Technician Dashboard                                 │
│  - Manager Dashboard                                    │
└──────────────┬──────────────────────────────────────────┘
               │ User Interaction (Events)
               ↓
┌──────────────────────────────────────────────────────────┐
│              CONTROLLER LAYER                           │
│         (Spring Controllers + Event Handlers)           │
│  - AuthController (login/registration)                  │
│  - CustomerController (repair submission)               │
│  - TechnicianController (repair management)             │
│  - ManagerController (inventory/invoicing)              │
└──────────────┬──────────────────────────────────────────┘
               │ Business Logic Calls
               ↓
┌──────────────────────────────────────────────────────────┐
│              MODEL LAYER                                │
│    (Services, DTOs, and Domain Entities)                │
│                                                          │
│  Services (Business Logic):                             │
│  - UserService → Authentication                         │
│  - RepairService → Repair Lifecycle Management          │
│  - BillingService → Invoice & Payment Management        │
│  - InventoryService → Parts Management                  │
│                                                          │
│  DTOs (Data Transfer Objects):                          │
│  - RepairRequestDTO → Transfer repair data              │
│                                                          │
│  Entities (Domain Objects):                             │
│  - User, Customer, Technician, Manager                  │
│  - RepairRequest, Gadget, Invoice, Part                 │
└──────────────┬──────────────────────────────────────────┘
               │ ORM Mapping (JPA/Hibernate)
               ↓
┌──────────────────────────────────────────────────────────┐
│           PERSISTENCE LAYER                             │
│    (Repositories + Spring Data JPA)                      │
│                                                          │
│  - UserRepository                                        │
│  - RepairRequestRepository (custom queries)              │
│  - InvoiceRepository                                     │
│  - PartRepository                                        │
│  - And 5 more specialized repositories                   │
└──────────────┬──────────────────────────────────────────┘
               │ SQL Execution
               ↓
┌──────────────────────────────────────────────────────────┐
│              DATABASE                                    │
│         (MySQL gadget_repair_db)                         │
└──────────────────────────────────────────────────────────┘
```

### MVC Data Flow - Complete Example: Customer Submits Repair

#### Step 1: View → Controller (User Interaction)

```
User fills repair form in CustomerController UI:
├── Device Brand: "Samsung"
├── Device Model: "Galaxy S20"
├── Device Type: "MOBILE"
└── Problem: "Screen broken"

User clicks "Submit Repair" button
                    ↓
@FXML Button triggers: handleSubmitRepair(ActionEvent event)
```

#### Step 2: Controller → Model (Business Logic)

```java
// CustomerController.java
@FXML
private void handleSubmitRepair(ActionEvent event) {
    
    // Extract data from UI
    String brand = deviceNameField.getText();
    String model = modelField.getText();
    DeviceType type = deviceTypeCombo.getValue();
    String problem = issueDescriptionField.getText();
    
    // Create DTO (Data Transfer Object)
    RepairRequestDTO dto = new RepairRequestDTO();
    dto.setCustomerId(sessionManager.getCurrentUserId());
    dto.setBrand(brand);
    dto.setModel(model);
    dto.setDeviceType(type);
    dto.setProblemDescription(problem);
    
    // Call Service (Business Logic)
    RepairRequest createdRequest = repairService.createRepairRequest(dto);
    
    // Update UI with response
    showAlert("Success", "Repair #" + createdRequest.getRequestId() + " created");
}
```

#### Step 3: Model - Service Layer (Business Logic)

```java
// RepairService.java (Interface)
public interface RepairService {
    RepairRequest createRepairRequest(RepairRequestDTO dto);
}

// RepairServiceImpl.java (Implementation)
@Override
public RepairRequest createRepairRequest(RepairRequestDTO dto) {
    
    // 1. Validate customer exists
    Customer customer = customerRepository.findById(dto.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Customer not found"));
    
    // 2. Create Gadget entity
    Gadget gadget = new Gadget(
        dto.getBrand(),
        dto.getModel(),
        dto.getDeviceType()
    );
    gadgetRepository.save(gadget);
    
    // 3. Create RepairRequest entity
    RepairRequest request = new RepairRequest(
        customer,
        gadget,
        dto.getProblemDescription()
    );
    request.setStatus(RepairStatus.REQUEST_SUBMITTED);
    request.setCreatedAt(LocalDateTime.now());
    
    // 4. Persist to database via Repository
    return repairRequestRepository.save(request);
}
```

#### Step 4: Model → Persistence (Data Access)

```java
// RepairRequestRepository.java (Spring Data JPA)
@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    // JPA auto-generates SQL: INSERT INTO repair_requests (...)
}
```

#### Step 5: Persistence → Database (SQL Execution)

```sql
-- Executed by Hibernate ORM
INSERT INTO gadgets (brand, model, device_type) 
VALUES ('Samsung', 'Galaxy S20', 'MOBILE');

INSERT INTO repair_requests (customer_id, gadget_gadget_id, problem_description, status, created_at)
VALUES (7, 15, 'Screen broken', 'REQUEST_SUBMITTED', '2026-04-20 10:30:00');
```

#### Step 6: Database Response → Controller → View

```
MySQL returns: RepairRequest object
    ↓
RepairService returns it to Controller
    ↓
Controller updates UI: displayAlert("Repair #X submitted successfully")
    ↓
User sees confirmation in GUI
```

---

## SYSTEM LAYERS & DATA FLOW

### Layer Architecture

```
┌──────────────────────────────────────────────────────────────┐
│ LAYER 1: PRESENTATION LAYER (JavaFX)                         │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Files:                                                       │
│ - login.fxml                 → Login UI                      │
│ - customer-dashboard.fxml    → Customer features             │
│ - technician-dashboard.fxml  → Technician features           │
│ - manager-dashboard.fxml     → Manager features              │
│ - repair-request.fxml        → Repair submission form         │
│ - invoice-view.fxml          → Invoice details               │
│                                                              │
│ Components:                                                  │
│ - TextField, ComboBox, TableView (UI Controls)               │
│ - Stage, Scene, Parent (JavaFX hierarchy)                    │
│ - CSS Styling (style.css)                                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              ↑↓
                    (Button clicks, Text input)
┌──────────────────────────────────────────────────────────────┐
│ LAYER 2: CONTROLLER LAYER (Spring @Controller)               │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ AuthController.java                                          │
│ ├─ handleLogin(ActionEvent)         → UserService            │
│ ├─ handleRegister(ActionEvent)       → UserService           │
│ └─ navigateToDashboard(User)         → Load appropriate UI   │
│                                                              │
│ CustomerController.java                                      │
│ ├─ handleSubmitRepair(ActionEvent)   → RepairService         │
│ ├─ handleLoadRepairs(ActionEvent)    → RepairService         │
│ ├─ handleLoadInvoices(ActionEvent)   → BillingService        │
│ └─ handleApprovePayment(ActionEvent) → BillingService        │
│                                                              │
│ TechnicianController.java                                    │
│ ├─ handleLoadPendingRepairs()        → RepairService         │
│ ├─ handleUpdateStatus()              → RepairService         │
│ └─ handleLogPartUsage()              → InventoryService      │
│                                                              │
│ ManagerController.java                                       │
│ ├─ handleAssignTechnician()          → RepairService         │
│ ├─ handleLoadInventory()             → InventoryService      │
│ ├─ handleAddPart()                   → InventoryService      │
│ ├─ handleLoadInvoices()              → BillingService        │
│ └─ handleApprovePayment()            → BillingService        │
│                                                              │
│ Responsibilities:                                            │
│ - Extract data from UI                                       │
│ - Validate user input                                        │
│ - Call appropriate Service methods                           │
│ - Update UI with results                                     │
│ - Handle user session (UserSessionManager)                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              ↑↓
                   (Service method calls)
┌──────────────────────────────────────────────────────────────┐
│ LAYER 3: SERVICE/BUSINESS LOGIC LAYER                        │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ UserService / UserServiceImpl                                │
│ ├─ authenticate(email, password) → User                      │
│ └─ saveUser(user) → User                                     │
│                                                              │
│ RepairService / RepairServiceImpl (CORE SERVICE)              │
│ ├─ createRepairRequest(dto)          [Customer]             │
│ ├─ getAllRepairRequests()             [Manager]              │
│ ├─ findRepairRequestById(id)          [All]                  │
│ ├─ assignTechnician(requestId, techId) [Manager]             │
│ ├─ updateRepairStatus(id, status)     [Technician]           │
│ ├─ getRepairsForTechnician(id)        [Technician]           │
│ └─ generateInvoice() [AUTO]           [On READY_FOR_PICKUP] │
│                                                              │
│ BillingService / BillingServiceImpl                           │
│ ├─ generateInvoice(repairId, amount)  → Invoice              │
│ ├─ findInvoiceById(id)                → Invoice              │
│ ├─ updatePaymentStatus(id, status)    → Invoice              │
│ └─ getAllInvoices()                   → List<Invoice>        │
│                                                              │
│ InventoryService / InventoryServiceImpl                       │
│ ├─ addPart(part)                      → Part                 │
│ ├─ findPartById(id)                   → Part                 │
│ ├─ getAllParts()                      → List<Part>           │
│ └─ updateStock(id, quantity)          → Part                 │
│                                                              │
│ Responsibilities:                                            │
│ - Implement business logic                                   │
│ - Coordinate between repositories                            │
│ - Handle transactions @Transactional                         │
│ - Enforce business rules                                     │
│ - Call observers/strategies as needed                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              ↑↓
                  (Repository method calls)
┌──────────────────────────────────────────────────────────────┐
│ LAYER 4: REPOSITORY/DATA ACCESS LAYER                        │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Spring Data JPA Repositories:                                │
│                                                              │
│ UserRepository extends JpaRepository<User, Long>             │
│ ├─ findById(Long id)        [JPA auto]                       │
│ ├─ save(User)               [JPA auto]                       │
│ ├─ findByEmail(String)      [Custom]                         │
│ └─ findAll()                [JPA auto]                       │
│                                                              │
│ RepairRequestRepository extends JpaRepository<...>           │
│ ├─ findById(Long)           [JPA auto]                       │
│ ├─ save(RepairRequest)      [JPA auto]                       │
│ ├─ findByStatus(status)     [Custom query]                   │
│ ├─ findByTechnicianId(id)   [@Query JPQL]                    │
│ └─ findByTechnician(tech)   [Custom]                         │
│                                                              │
│ InvoiceRepository, PartRepository, PartUsageRepository, etc. │
│                                                              │
│ Responsibilities:                                            │
│ - CRUD operations                                            │
│ - Custom query methods (findBy*, @Query)                     │
│ - Transaction management                                     │
│ - Object-Relational Mapping                                  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              ↑↓
                   (SQL Execution via Hibernate)
┌──────────────────────────────────────────────────────────────┐
│ LAYER 5: PERSISTENCE/ORM LAYER (Hibernate)                   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Hibernate ORM maps Java objects to database:                 │
│                                                              │
│ Java Object → SQL:                                           │
│ User entity → users table                                    │
│ RepairRequest entity → repair_requests table                 │
│ Invoice entity → invoices table                              │
│ Part entity → parts table                                    │
│                                                              │
│ Configuration:                                               │
│ - strategy: InheritanceType.JOINED (for User hierarchy)     │
│ - ddl-auto: update (auto-create/update schema)               │
│ - show-sql: true (debug logging)                             │
│ - dialect: MySQLDialect                                      │
│                                                              │
│ Relationships Handled:                                       │
│ - @ManyToOne (RepairRequest → Customer/Technician)          │
│ - @OneToOne (RepairRequest → Gadget, RepairRequest → Invoice)│
│ - @OneToMany (Customer → RepairRequests)                     │
│ - @JoinTable (Part ← → PartUsage → RepairRequest)            │
│                                                              │
│ Lazy/Eager Loading:                                          │
│ - Gadget loaded eagerly with RepairRequest                   │
│ - Parts loaded on demand (lazy)                              │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              ↑↓
                        (JDBC Driver)
┌──────────────────────────────────────────────────────────────┐
│ LAYER 6: DATABASE LAYER (MySQL)                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Database: gadget_repair_db                                   │
│ Host: localhost:3306                                         │
│ Driver: MySQL Connector/J 8.0.45                             │
│                                                              │
│ Tables (10 total):                                           │
│ - users (base table)                                         │
│ - customer, technician, manager (inherited tables)           │
│ - repair_requests (core business entity)                     │
│ - gadgets, invoices, parts, part_usage                       │
│ - repair_logs (audit trail)                                  │
│                                                              │
│ Relationships (Foreign Keys):                                │
│ - repair_requests.customer_id → customer.id                  │
│ - repair_requests.technician_id → technician.id              │
│ - repair_requests.gadget_gadget_id → gadgets.gadget_id       │
│ - invoices.repair_request_id → repair_requests.request_id    │
│ - part_usage.repair_request_id → repair_requests.request_id  │
│ - part_usage.part_id → parts.part_id                         │
│ - repair_logs.repair_request_id → repair_requests.request_id │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Request Flow Sequence - Technician Updates Repair Status

```
SCENARIO: Technician marks repair as "READY_FOR_PICKUP"

1. TechnicianController.handleUpdateStatus(ActionEvent)
   │
   ├─ Extract repairId and newStatus from UI
   ├─ Call: repairService.updateRepairStatus(requestId, "READY_FOR_PICKUP")
   │
   2. RepairServiceImpl.updateRepairStatus()
      │
      ├─ repairRequestRepository.findById(requestId)
      │  └─ (Executes SELECT * FROM repair_requests WHERE request_id = ?)
      │
      ├─ Update status: request.setStatus(READY_FOR_PICKUP)
      │
      ├─ IF status is READY_FOR_PICKUP THEN
      │  └─ Call: billingService.generateInvoice(requestId, totalCost)
      │     │
      │     3. BillingServiceImpl.generateInvoice()
      │        │
      │        ├─ Create new Invoice entity
      │        ├─ Set paymentStatus = UNPAID
      │        ├─ invoiceRepository.save(invoice)
      │        │  └─ (Executes INSERT INTO invoices ...)
      │        │
      │        └─ Return Invoice
      │
      ├─ repairRequestRepository.save(request)
      │  └─ (Executes UPDATE repair_requests SET status=?, updated_at=? WHERE request_id=?)
      │
      └─ Return updated RepairRequest
   │
   4. Controller receives RepairRequest & Invoice
      │
      ├─ Update UI: "Status updated to READY_FOR_PICKUP"
      ├─ Update UI: "Invoice #X generated - Amount due: $200"
      │
      └─ User sees success message in GUI
```

---

## SOLID PRINCIPLES

### **S - Single Responsibility Principle (SRP)**

**Definition**: A class should have only one reason to change.

#### Implementation in This Project

##### 1. **Services Handle Business Logic Only**

```java
// ✅ GOOD: RepairService handles only repair logic
@Service
public class RepairServiceImpl implements RepairService {
    
    // Responsibility: Manage repair lifecycle
    public RepairRequest createRepairRequest(RepairRequestDTO dto) { ... }
    public RepairRequest updateRepairStatus(Long id, String status) { ... }
    public RepairRequest assignTechnician(Long requestId, Long techId) { ... }
    
    // Responsibility: NOT handling database queries directly
    // Delegates to repositories
    private RepairRequestRepository repairRequestRepository;
}
```

**Why**: 
- If repair business rules change, only RepairService needs updating
- If database structure changes, only repositories need updating
- Easy to test - can mock repositories

##### 2. **Repositories Handle Data Access Only**

```java
// ✅ GOOD: Repository has single responsibility
@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    
    // Responsibility: Query data only
    List<RepairRequest> findByStatus(RepairStatus status);
    
    @Query("SELECT r FROM RepairRequest r WHERE r.technician.id = :technicianId")
    List<RepairRequest> findByTechnicianId(@Param("technicianId") Long technicianId);
    
    // Responsibility: NOT implementing business logic
    // Just returns data for services to use
}
```

**Why**:
- Single responsibility: Database operations only
- Reusable across multiple services
- Easy to swap implementations (change database without changing services)

##### 3. **Controllers Handle UI Events Only**

```java
// ✅ GOOD: Controller delegates to Service
@Controller
public class TechnicianController {
    
    private final RepairService repairService;  // Injected dependency
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        
        // Responsibility: Extract UI input & call service
        String repairId = repairIdField.getText();
        String newStatus = statusCombo.getValue();
        
        // Responsibility: NOT implementing business logic
        // Delegates to service
        RepairRequest updated = repairService.updateRepairStatus(
            Long.parseLong(repairId), newStatus
        );
        
        // Responsibility: Update UI with response
        showSuccessMessage("Status updated successfully");
    }
}
```

**Why**:
- Controller only handles UI/presentation logic
- Business logic stays in Services
- Easy to reuse service from multiple controllers

##### 4. **DTOs Transfer Data Only**

```java
// ✅ GOOD: DTO only transfers data between layers
@Data
public class RepairRequestDTO {
    private Long customerId;
    private String brand;
    private String model;
    private DeviceType deviceType;
    private String problemDescription;
    
    // No business logic, no database operations
    // Only data transfer
}
```

**Why**:
- Single responsibility: Move data between UI and Service
- Protects internal entity structure
- Can evolve UI independently of database schema

### **O - Open/Closed Principle (OCP)**

**Definition**: Open for extension, closed for modification.

#### Implementation in This Project

##### 1. **Strategy Pattern for Payment Methods**

```java
// ✅ GOOD: New payment methods can be added without modifying existing code

// Closed for modification: Interface is stable
public interface PaymentStrategy {
    void processPayment(Invoice invoice);
}

// Closed for modification: Existing implementations unchanged
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via CARD");
    }
}

public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via CASH");
    }
}

// Open for extension: New payment method added easily
public class CryptoCurrencyPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via Bitcoin");
    }
}
```

**How it works**:
- BillingService accepts ANY PaymentStrategy implementation
- New payment methods added without changing existing code
- Closed for modification, open for extension

##### 2. **Observer Pattern for Notifications**

```java
// ✅ GOOD: New observer types can be added without modifying RepairStatusPublisher

// Closed for modification: Publisher interface is stable
public interface NotificationObserver {
    void update(RepairRequest repairRequest);
}

// Closed for modification: Existing observers unchanged
public class CustomerNotificationObserver implements NotificationObserver {
    @Override
    public void update(RepairRequest repairRequest) {
        System.out.println("Notify customer: Repair status changed");
    }
}

// Open for extension: New observer type easily added
public class AdminNotificationObserver implements NotificationObserver {
    @Override
    public void update(RepairRequest repairRequest) {
        System.out.println("Notify admin: High-value repair completed");
    }
}

// Publisher delegates to all observers
public class RepairStatusPublisher {
    private final List<NotificationObserver> observers = new ArrayList<>();
    
    public void notifyObservers(RepairRequest repairRequest) {
        for (NotificationObserver observer : observers) {
            observer.update(repairRequest);  // Polymorphic - works with any observer
        }
    }
}
```

**Why**:
- New notification channels (email, SMS, push) added easily
- RepairStatusPublisher never needs modification
- Existing observers continue to work

### **L - Liskov Substitution Principle (LSP)**

**Definition**: Subtypes must be substitutable for their base types.

#### Implementation in This Project

##### 1. **User Hierarchy Substitutability**

```java
// ✅ GOOD: All User subtypes are substitutable for User

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    private Long id;
    private String name;
    private UserRole role;
}

@Entity
public class Customer extends User {
    private String shippingAddress;
}

@Entity
public class Technician extends User {
    private String specialization;
    private Double efficiencyRating;
}

@Entity
public class Manager extends User {
    private String departmentId;
}

// Usage: Can use any subclass wherever User is expected
User user = userRepository.findById(1);  // Returns Customer, Technician, or Manager
// All User methods work without knowing concrete type

// Application: Role-based filtering
List<User> allUsers = userRepository.findAll();
for (User user : allUsers) {
    if (user.getRole() == UserRole.TECHNICIAN) {
        // Safe to use as Technician
        Technician tech = (Technician) user;
    }
}
```

**Why**:
- Polymorphism allows treating all user types uniformly
- Can query all users with single repository call
- Role-based dispatch without type checking

##### 2. **Service Interface Contracts**

```java
// ✅ GOOD: Any RepairService implementation can be substituted

public interface RepairService {
    RepairRequest createRepairRequest(RepairRequestDTO dto);
    RepairRequest updateRepairStatus(Long id, String status);
    RepairRequest assignTechnician(Long requestId, Long techId);
    List<RepairRequest> getRepairsForTechnician(Long technicianId);
}

// Real implementation
@Service
public class RepairServiceImpl implements RepairService {
    // Real database operations
}

// Can be replaced with mock for testing
public class MockRepairService implements RepairService {
    // Test implementation
}

// Controllers don't know which implementation is used
public class TechnicianController {
    private final RepairService repairService;  // Could be real or mock
}
```

**Why**:
- Easy testing with mock implementations
- Can swap implementations without changing consumers
- Maintains contract semantics across implementations

### **I - Interface Segregation Principle (ISP)**

**Definition**: Clients should not depend on interfaces they don't use.

#### Implementation in This Project

##### 1. **Segregated Service Interfaces**

```java
// ✅ GOOD: Each service interface is focused and segregated

// UserService - only user operations
public interface UserService {
    User authenticate(String email, String password);
    User saveUser(User user);
}

// RepairService - only repair operations
public interface RepairService {
    RepairRequest createRepairRequest(RepairRequestDTO dto);
    RepairRequest updateRepairStatus(Long id, String status);
    RepairRequest assignTechnician(Long requestId, Long techId);
    List<RepairRequest> getRepairsForTechnician(Long technicianId);
}

// BillingService - only billing operations
public interface BillingService {
    Invoice generateInvoice(Long repairRequestId, double amount);
    Invoice updatePaymentStatus(Long invoiceId, String paymentStatus);
    List<Invoice> getAllInvoices();
}

// InventoryService - only inventory operations
public interface InventoryService {
    Part addPart(Part part);
    List<Part> getAllParts();
    Part updateStock(Long partId, int quantity);
}

// Controllers depend only on what they need
public class CustomerController {
    // Only depends on services customer uses
    private final RepairService repairService;
    private final BillingService billingService;
    
    // Doesn't depend on InventoryService (manager feature)
}

public class ManagerController {
    // Only depends on services manager uses
    private final RepairService repairService;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    
    // Doesn't depend on unnecessary interfaces
}
```

**Why**:
- Controllers don't get bloated with unnecessary dependencies
- Services are focused and reusable
- Easy to understand what each component does
- Changes to one interface don't affect unrelated clients

##### 2. **Segregated Repository Interfaces**

```java
// ✅ GOOD: Each repository interface has only needed methods

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Only what's needed
}

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByStatus(RepairStatus status);
    @Query("SELECT r FROM RepairRequest r WHERE r.technician.id = :technicianId")
    List<RepairRequest> findByTechnicianId(@Param("technicianId") Long technicianId);
}

public interface PartRepository extends JpaRepository<Part, Long> {
    // Only CRUD operations, no complex queries needed
}

// Each interface is segregated - not forcing services to depend on unused methods
```

**Why**:
- Repository contracts are clear and focused
- Easy to understand what queries are available
- Prevents tight coupling to unnecessary methods

### **D - Dependency Inversion Principle (DIP)**

**Definition**: Depend on abstractions, not concrete implementations.

#### Implementation in This Project

##### 1. **Dependency Injection via Interfaces**

```java
// ✅ GOOD: Depend on service interfaces, not implementations

@Controller
public class TechnicianController {
    
    // Depends on interface, not concrete implementation
    private final RepairService repairService;
    private final InventoryService inventoryService;
    
    // Constructor injection - dependencies provided by Spring
    public TechnicianController(
        RepairService repairService,
        InventoryService inventoryService
    ) {
        this.repairService = repairService;  // Could be any RepairService implementation
        this.inventoryService = inventoryService;  // Could be any InventoryService implementation
    }
}

// ❌ BAD (what we DON'T do): Direct dependency on implementation
// public class TechnicianController {
//     private final RepairServiceImpl repairService;  // Tight coupling to implementation!
// }
```

**Why**:
- Services can be replaced without changing controllers
- Easy to test with mock implementations
- Spring manages lifecycle and creation

##### 2. **Repository Abstraction**

```java
// ✅ GOOD: Services depend on repository interfaces

@Service
public class RepairServiceImpl implements RepairService {
    
    // Depends on interface, not concrete implementation
    private final RepairRequestRepository repairRequestRepository;
    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    
    // Constructor injection
    public RepairServiceImpl(
        RepairRequestRepository repairRequestRepository,
        UserRepository userRepository,
        TechnicianRepository technicianRepository
    ) {
        this.repairRequestRepository = repairRequestRepository;
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
    }
    
    @Override
    public RepairRequest assignTechnician(Long requestId, Long technicianId) {
        RepairRequest request = repairRequestRepository.findById(requestId)
            .orElseThrow();
        
        // Validate via UserRepository
        User user = userRepository.findById(technicianId)
            .orElseThrow();
        
        // Fetch via TechnicianRepository
        Technician technician = technicianRepository.getReferenceById(technicianId);
        
        request.setTechnician(technician);
        return repairRequestRepository.save(request);
    }
}
```

**Why**:
- Can switch database implementations without changing service logic
- Database layer can be tested independently
- Follows repository pattern correctly

##### 3. **Spring Dependency Container**

```java
// ✅ GOOD: Spring manages dependency resolution

@SpringBootApplication
public class GadgetRepairTrackerApplication extends Application {
    
    private ApplicationContext springContext;
    
    @Override
    public void init() {
        // Spring creates the dependency container
        springContext = SpringApplication.run(GadgetRepairTrackerApplication.class);
    }
    
    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        
        // Spring provides controllers with dependencies already injected
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
```

**Why**:
- Centralized dependency management
- All dependencies wired at startup
- Easy to change implementations through configuration
- Testability - can inject mocks

---

## GRASP PRINCIPLES

GRASP (General Responsibility Assignment Software Patterns) provides practical guidelines for assigning responsibilities.

### **1. Creator Pattern**

**Definition**: Assign creation responsibility to classes that use or aggregate the created objects.

#### Implementation

##### Service Creates Entities

```java
// ✅ GOOD: Service creates entities based on business requirements

@Service
public class RepairServiceImpl implements RepairService {
    
    @Override
    public RepairRequest createRepairRequest(RepairRequestDTO dto) {
        
        // Service is responsible for creating RepairRequest
        // because it understands the business rules
        RepairRequest request = new RepairRequest(
            customer,
            gadget,
            dto.getProblemDescription()
        );
        
        // Service sets status based on business logic
        request.setStatus(RepairStatus.REQUEST_SUBMITTED);
        request.setCreatedAt(LocalDateTime.now());
        
        return repairRequestRepository.save(request);
    }
}

// Why here?
// - Service knows the complete business context
// - Service enforces business rules during creation
// - Service ensures valid state before persistence
```

##### Controller Creates DTOs

```java
// ✅ GOOD: Controller creates DTOs from UI input

@Controller
public class CustomerController {
    
    @FXML
    private void handleSubmitRepair(ActionEvent event) {
        
        // Controller is responsible for creating DTO
        // because it has UI data
        RepairRequestDTO dto = new RepairRequestDTO();
        dto.setCustomerId(sessionManager.getCurrentUserId());
        dto.setBrand(deviceNameField.getText());
        dto.setModel(modelField.getText());
        dto.setDeviceType(deviceTypeCombo.getValue());
        dto.setProblemDescription(issueDescriptionField.getText());
        
        // Pass to service
        repairService.createRepairRequest(dto);
    }
}

// Why here?
// - Controller has direct access to UI components
// - DTO is data holder for UI-to-Service transfer
```

### **2. Expert Pattern**

**Definition**: Assign responsibility to the class that has the information necessary to fulfill it.

#### Implementation

##### Service Handles Complex Logic

```java
// ✅ GOOD: Service is expert in repair business logic

@Service
public class RepairServiceImpl implements RepairService {
    
    @Override
    public RepairRequest updateRepairStatus(Long requestId, String newStatus) {
        
        // Service is expert in knowing:
        // 1. What status transitions are valid
        RepairRequest request = repairRequestRepository.findById(requestId)
            .orElseThrow();
        
        // 2. When to trigger related actions (invoice generation)
        if (newStatus.equals("READY_FOR_PICKUP")) {
            // Automatically generate invoice
            billingService.generateInvoice(requestId, calculateCost());
        }
        
        // 3. What side effects occur
        request.setStatus(RepairStatus.valueOf(newStatus));
        
        // Only service knows this business logic
        return repairRequestRepository.save(request);
    }
}

// Why service?
// - Service has complete business knowledge
// - Service coordinates multiple repositories
// - Service enforces rules consistently
```

##### Repository is Expert in Data Access

```java
// ✅ GOOD: Repository is expert in queries

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    
    // Repository is expert in knowing:
    // 1. How to efficiently query by status
    List<RepairRequest> findByStatus(RepairStatus status);
    
    // 2. How to write complex JPQL queries
    @Query("SELECT r FROM RepairRequest r WHERE r.technician.id = :technicianId")
    List<RepairRequest> findByTechnicianId(@Param("technicianId") Long technicianId);
}

// Why repository?
// - Repository knows database schema
// - Repository knows optimal query patterns
// - Repository handles ORM mapping
```

##### Controller is Expert in UI Events

```java
// ✅ GOOD: Controller is expert in UI interaction

@Controller
public class ManagerController {
    
    @FXML
    private void handleAssignTechnicianToRepair(ActionEvent event) {
        
        // Controller is expert in knowing:
        // 1. What UI elements contain the data
        String repairId = assignTechRepairIdField.getText();
        String technicianName = technicianAssignCombo.getValue();
        
        // 2. How to extract selection from combobox
        Long technicianId = extractIdFromSelection(technicianName);
        
        // 3. How to update UI after action
        repairService.assignTechnician(Long.parseLong(repairId), technicianId);
        showSuccessAlert("Technician assigned successfully");
    }
}

// Why controller?
// - Controller has direct UI component access
// - Controller knows UI state and events
// - Controller understands user interaction flow
```

### **3. Controller Pattern**

**Definition**: Assign responsibility to a controller object that handles system events.

#### Implementation

```java
// ✅ GOOD: Controllers handle all user events

@Controller
public class AuthController {
    
    private final UserService userService;
    
    @FXML
    private void handleLogin(ActionEvent event) {
        // Controller receives event
        // Extracts data from UI
        String email = emailField.getText();
        String password = passwordField.getText();
        
        // Delegates to appropriate service
        User user = userService.authenticate(email, password);
        
        // Updates UI based on result
        if (user != null) {
            sessionManager.setCurrentUser(user);
            navigateToDashboard(user);
        }
    }
}

// Why?
// - Single entry point for handling user actions
// - Consistent request/response handling
// - Coordinates between UI and services
```

### **4. Low Coupling**

**Definition**: Keep dependencies minimal and flexible.

#### Implementation

```java
// ✅ GOOD: Low coupling through interfaces

@Controller
public class TechnicianController {
    
    // Only depends on service interfaces
    private final RepairService repairService;
    private final InventoryService inventoryService;
    
    // If InventoryService implementation changes, controller unaffected
    // If repository changes, service handles it
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        // Only calls service methods
        repairService.updateRepairStatus(id, status);
    }
}

// ❌ BAD: High coupling (what we avoid)
// @Controller
// public class TechnicianController {
//     private final RepairRequestRepository repairRequestRepository;  // Direct repo access
//     private final UserRepository userRepository;  // Tightly coupled
//     
//     // Changes to schema directly impact controller
//     // Can't reuse in different business context
// }

// Why interfaces?
// - Service can be replaced without changing controller
// - Implementation details hidden
// - Easy to test with mocks
```

### **5. High Cohesion**

**Definition**: Keep related functionality together.

#### Implementation

```java
// ✅ GOOD: Related repair operations in one service

@Service
public class RepairServiceImpl implements RepairService {
    
    // All repair-related operations together
    public RepairRequest createRepairRequest(RepairRequestDTO dto) { }
    public RepairRequest updateRepairStatus(Long id, String status) { }
    public RepairRequest assignTechnician(Long requestId, Long techId) { }
    public List<RepairRequest> getRepairsForTechnician(Long technicianId) { }
    
    // All share same repositories and cohesive logic
    private final RepairRequestRepository repairRequestRepository;
    private final TechnicianRepository technicianRepository;
}

// ❌ BAD: Mixed concerns (what we avoid)
// @Service
// public class MixedService {
//     public void createRepair() { }           // Repair logic
//     public void processPayment() { }         // Billing logic
//     public void updateInventory() { }        // Inventory logic
//     public void sendEmail() { }              // Notification logic
//     
//     // No cohesion - too many unrelated responsibilities
// }

// Why cohesion?
// - Related changes only affect one service
// - Service has focused purpose
// - Easy to understand what service does
```

### **6. Polymorphism**

**Definition**: Use polymorphism to handle variations in behavior.

#### Implementation

```java
// ✅ GOOD: Use polymorphism for different payment methods

public interface PaymentStrategy {
    void processPayment(Invoice invoice);
}

public class CardPaymentStrategy implements PaymentStrategy {
    public void processPayment(Invoice invoice) { }
}

public class CashPaymentStrategy implements PaymentStrategy {
    public void processPayment(Invoice invoice) { }
}

public class UPIPaymentStrategy implements PaymentStrategy {
    public void processPayment(Invoice invoice) { }
}

// Service uses polymorphism
@Service
public class BillingServiceImpl implements BillingService {
    
    public void processPayment(Invoice invoice, PaymentStrategy strategy) {
        strategy.processPayment(invoice);  // Polymorphic call
    }
}

// ❌ BAD: Type checking (what we avoid)
// public class BillingServiceImpl {
//     public void processPayment(Invoice invoice, String paymentType) {
//         if (paymentType.equals("CARD")) {
//             // Card logic
//         } else if (paymentType.equals("CASH")) {
//             // Cash logic
//         } else if (paymentType.equals("UPI")) {
//             // UPI logic
//         }
//         // Violates Open/Closed principle
//     }
// }

// Why polymorphism?
// - New payment types added without modifying service
// - Type-safe design
// - Strategy selection at runtime
```

### **7. Indirection**

**Definition**: Introduce intermediate objects to reduce coupling.

#### Implementation

```java
// ✅ GOOD: DTOs act as indirection between layers

// UI Data
@FXML
private TextField brandField;
private void handleSubmit() {
    // Extract from UI
}

// Indirection layer
public class RepairRequestDTO {
    private String brand;
    private String model;
    private DeviceType deviceType;
}

// Service layer
public class RepairService {
    public RepairRequest createRepairRequest(RepairRequestDTO dto) {
        // Uses DTO, not UI directly
    }
}

// Database layer
public class RepairRequest {
    @ManyToOne
    private Customer customer;
    private Gadget gadget;
}

// Why indirection?
// - UI can change without affecting service
// - Service doesn't depend on UI framework (JavaFX)
// - Database schema can change without affecting UI
// - DTO is middle layer decoupling presentation from persistence
```

---

## DESIGN PATTERNS

### **CREATIONAL PATTERNS** (Object Creation)

#### **1. Factory Pattern**

```java
// Location: src/main/java/com/pes/gadgetrepair/factory/UserFactory.java

public class UserFactory {
    
    // Creates different User types based on role
    public static User createUser(UserRole role) {
        switch(role) {
            case CUSTOMER:
                return new Customer();
            case TECHNICIAN:
                return new Technician();
            case MANAGER:
                return new Manager();
            default:
                return new User();
        }
    }
}

// Usage in AuthController:
User newUser = UserFactory.createUser(registrationRole);
newUser.setName(nameField.getText());
newUser.setEmail(emailField.getText());
userService.saveUser(newUser);

// Why?
// ✓ Encapsulates object creation logic
// ✓ Easy to add new user types
// ✓ Client doesn't depend on concrete classes
// ✓ Centralized creation logic
```

#### **2. Dependency Injection (via Spring)**

```java
// Type: Creational Pattern (manages object creation/lifecycle)

@Service
public class RepairServiceImpl implements RepairService {
    
    // Dependencies injected by Spring container
    private final RepairRequestRepository repairRequestRepository;
    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    
    // Constructor injection - dependencies provided at creation time
    public RepairServiceImpl(
        RepairRequestRepository repairRequestRepository,
        TechnicianRepository technicianRepository,
        UserRepository userRepository
    ) {
        this.repairRequestRepository = repairRequestRepository;
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
    }
}

// Spring creates single instance:
@SpringBootApplication
public class GadgetRepairTrackerApplication extends Application {
    
    private ApplicationContext springContext;
    
    @Override
    public void init() {
        // Spring creates all beans and manages lifecycle
        springContext = SpringApplication.run(GadgetRepairTrackerApplication.class);
    }
    
    @Override
    public void start(Stage stage) {
        // Spring injects dependencies into controllers
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(springContext::getBean);
    }
}

// Why?
// ✓ Centralized lifecycle management
// ✓ Inversion of Control - Spring creates objects
// ✓ Easy to swap implementations
// ✓ Automatic wiring of dependencies
// ✓ Testability - can inject mocks
```

#### **3. Singleton Pattern**

```java
// Via Spring @Service and @Repository annotations

@Service  // Spring creates one instance (singleton)
public class RepairServiceImpl implements RepairService { }

@Repository  // Spring creates one instance (singleton)
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> { }

@Component  // Spring creates one instance (singleton)
public class UserSessionManager {
    
    private User currentUser;
    
    public void setCurrentUser(User user) {
        this.currentUser = user;  // Stored in singleton instance
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}

// Usage: Same instance across entire application
// Controllers access same UserSessionManager instance
public class TechnicianController {
    private final UserSessionManager sessionManager;
    
    @FXML
    private void handleLoadRepairs() {
        Long currentId = sessionManager.getCurrentUserId();  // Uses singleton
    }
}

// Why?
// ✓ One instance per service/repository
// ✓ Shared state across application
// ✓ Thread-safe (Spring ensures)
// ✓ Lazy initialization (created when first used)
```

---

### **STRUCTURAL PATTERNS** (Object Composition)

#### **1. Adapter Pattern**

```java
// Location: src/main/java/com/pes/gadgetrepair/adapter/PaymentGatewayAdapter.java

// External system - incompatible interface
public class ExternalPaymentGateway {
    
    // External API returns simple boolean
    public boolean makePayment(double amount) {
        System.out.println("External processing payment: " + amount);
        return true;
    }
}

// Our system needs Invoice object with status
public class Invoice {
    private PaymentStatus paymentStatus;
    
    public void setPaymentStatus(PaymentStatus status) {
        this.paymentStatus = status;
    }
}

// Adapter bridges the gap
public class PaymentGatewayAdapter {
    
    private final ExternalPaymentGateway externalGateway;
    
    public PaymentGatewayAdapter() {
        this.externalGateway = new ExternalPaymentGateway();
    }
    
    // Adapts external interface to internal interface
    public void processPayment(Invoice invoice) {
        
        // Call external system
        boolean success = externalGateway.makePayment(invoice.getAmount());
        
        // Adapt response to our domain
        if (success) {
            invoice.setPaymentStatus(PaymentStatus.PAID);
        } else {
            invoice.setPaymentStatus(PaymentStatus.FAILED);
        }
    }
}

// Usage in BillingService:
public class BillingServiceImpl implements BillingService {
    
    private final PaymentGatewayAdapter paymentAdapter;
    
    public void processPayment(Invoice invoice) {
        paymentAdapter.processPayment(invoice);  // Uses adapter
    }
}

// Why?
// ✓ Integrates incompatible external API
// ✓ Decouples internal code from external system
// ✓ Easy to swap external providers
// ✓ Changes to external API isolated in adapter
```

#### **2. Proxy Pattern (via Hibernate Lazy Loading)**

```java
// When you use getReferenceById() instead of findById()

@Service
public class RepairServiceImpl implements RepairService {
    
    @Override
    public RepairRequest assignTechnician(Long requestId, Long technicianId) {
        
        // Option 1: Fetch immediately (eager)
        Technician technician = technicianRepository.findById(technicianId)
            .orElseThrow();
        // ↓ Executes SQL immediately
        
        // Option 2: Create proxy without fetching (lazy)
        Technician technicianProxy = technicianRepository.getReferenceById(technicianId);
        // ↓ Does NOT execute SQL - creates proxy object
        
        RepairRequest request = repairRequestRepository.findById(requestId)
            .orElseThrow();
        
        request.setTechnician(technicianProxy);  // Use proxy
        return repairRequestRepository.save(request);
        // ↓ SQL only executed on save()
    }
}

// Why?
// ✓ Avoid unnecessary database queries
// ✓ Better performance - only loads when needed
// ✓ Proxy behaves like real object
// ✓ Transparent to client code
```

#### **3. Decorator Pattern (via DTO)**

```java
// DTOs wrap entities with additional data

@Entity
public class RepairRequest {
    // Database entity - only persistence data
    private Long requestId;
    private Customer customer;
    private Gadget gadget;
    private RepairStatus status;
}

// DTO decorates entity with transfer-specific data
public class RepairRequestDTO {
    // Data from customer form
    private Long customerId;
    private String brand;
    private String model;
    private DeviceType deviceType;
    private String problemDescription;
    
    // DTO is "decorator" - wraps data with context
    // Different from entity - focused on data transfer
}

// Usage: Convert UI input → DTO → Entity
public class CustomerController {
    
    @FXML
    private void handleSubmitRepair(ActionEvent event) {
        
        // Step 1: Extract UI input
        String brand = deviceNameField.getText();
        String model = modelField.getText();
        
        // Step 2: Wrap in DTO (decorator with transfer context)
        RepairRequestDTO dto = new RepairRequestDTO();
        dto.setBrand(brand);
        dto.setModel(model);
        
        // Step 3: Service converts DTO → Entity for persistence
        RepairRequest entity = repairService.createRepairRequest(dto);
    }
}

// Why?
// ✓ Separates transfer format from persistence format
// ✓ UI can change without affecting database schema
// ✓ Entity can evolve independently
// ✓ Protects internal structure
```

#### **4. JOINED Inheritance (Structural Pattern)**

```java
// User hierarchy using JPA JOINED inheritance

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}

// Each subclass has own table, linked by id
@Entity
@Table(name = "customer")
public class Customer extends User {
    private String shippingAddress;  // Only customer-specific data
}

@Entity
@Table(name = "technician")
public class Technician extends User {
    private String specialization;      // Only technician-specific data
    private Double efficiencyRating;
}

@Entity
@Table(name = "manager")
public class Manager extends User {
    private String departmentId;  // Only manager-specific data
}

// Database structure:
// users table: [id, name, email, role]
// customer table: [id, shipping_address] (FK to users)
// technician table: [id, specialization, efficiency_rating] (FK to users)
// manager table: [id, department_id] (FK to users)

// Usage: Polymorphic queries
List<User> allUsers = userRepository.findAll();  // Gets all users with correct type

for (User user : allUsers) {
    switch(user.getRole()) {
        case CUSTOMER:
            Customer customer = (Customer) user;
            // Use customer-specific fields
            break;
        case TECHNICIAN:
            Technician technician = (Technician) user;
            // Use technician-specific fields
            break;
    }
}

// Why JOINED inheritance?
// ✓ Normalized database design
// ✓ Avoids NULL columns
// ✓ Polymorphic queries
// ✓ Type-safe inheritance
```

---

### **BEHAVIORAL PATTERNS** (Object Interaction)

#### **1. Observer Pattern**

```java
// Location: src/main/java/com/pes/gadgetrepair/observer/

// Subject (Publisher) - notifies observers
public class RepairStatusPublisher {
    
    private final List<NotificationObserver> observers = new ArrayList<>();
    
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }
    
    // When event occurs, notify all observers
    public void notifyObservers(RepairRequest repairRequest) {
        for (NotificationObserver observer : observers) {
            observer.update(repairRequest);
        }
    }
}

// Observer interface - defines update contract
public interface NotificationObserver {
    void update(RepairRequest repairRequest);
}

// Concrete observer - handles customer notifications
public class CustomerNotificationObserver implements NotificationObserver {
    
    private final Customer customer;
    
    @Override
    public void update(RepairRequest repairRequest) {
        // Handle notification for this observer
        System.out.println(
            "Customer " + customer.getName() + 
            " - Repair #" + repairRequest.getRequestId() + 
            " is now " + repairRequest.getStatus()
        );
        
        // Could send email, SMS, push notification, etc.
    }
}

// Usage in RepairService:
@Service
public class RepairServiceImpl implements RepairService {
    
    private final RepairStatusPublisher publisher;
    
    @Override
    public RepairRequest updateRepairStatus(Long id, String status) {
        
        RepairRequest request = repairRequestRepository.findById(id)
            .orElseThrow();
        
        request.setStatus(RepairStatus.valueOf(status));
        
        // Notify all observers of status change
        publisher.notifyObservers(request);
        
        return repairRequestRepository.save(request);
    }
}

// Why?
// ✓ Loose coupling between publisher and observers
// ✓ New observer types added without changing publisher
// ✓ Multiple observers can respond to same event
// ✓ Observable behavior - observers "watch" repair status
```

#### **2. Strategy Pattern**

```java
// Location: src/main/java/com/pes/gadgetrepair/strategy/

// Strategy interface - defines algorithm contract
public interface PaymentStrategy {
    void processPayment(Invoice invoice);
}

// Concrete strategies - different payment algorithms
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        // Card-specific logic
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via CARD for Invoice #" + invoice.getInvoiceId());
    }
}

public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        // Cash-specific logic
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via CASH for Invoice #" + invoice.getInvoiceId());
    }
}

public class UPIPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        // UPI-specific logic
        invoice.setPaymentStatus(PaymentStatus.PAID);
        System.out.println("Payment via UPI for Invoice #" + invoice.getInvoiceId());
    }
}

// Context - uses strategy
public class BillingServiceImpl implements BillingService {
    
    public void processPayment(Invoice invoice, PaymentStrategy strategy) {
        // Delegate to strategy
        strategy.processPayment(invoice);
    }
}

// Usage in ManagerController:
@FXML
private void handleApprovePayment(ActionEvent event) {
    
    String paymentMethod = paymentMethodCombo.getValue();
    
    // Select appropriate strategy
    PaymentStrategy strategy;
    switch(paymentMethod) {
        case "CARD":
            strategy = new CardPaymentStrategy();
            break;
        case "CASH":
            strategy = new CashPaymentStrategy();
            break;
        case "UPI":
            strategy = new UPIPaymentStrategy();
            break;
        default:
            throw new IllegalArgumentException("Unknown payment method");
    }
    
    // Process using selected strategy
    billingService.processPayment(invoice, strategy);
}

// Why?
// ✓ Encapsulates different algorithms
// ✓ Easy to add new payment methods
// ✓ Strategy selected at runtime
// ✓ Avoids if-else chains
// ✓ Each strategy is independent
```

#### **3. State Pattern (via Enums)**

```java
// Location: src/main/java/com/pes/gadgetrepair/enums/RepairStatus.java

// State enum - represents possible states
public enum RepairStatus {
    REQUEST_SUBMITTED(0),
    DIAGNOSIS_IN_PROGRESS(1),
    WAITING_FOR_PARTS(2),
    REPAIR_IN_PROGRESS(3),
    REPAIR_COMPLETED(4),
    READY_FOR_PICKUP(5),
    PENDING_PAYMENT(6),
    DELIVERED(7),
    CANCELLED(8);
    
    private final int sequence;
    
    RepairStatus(int sequence) {
        this.sequence = sequence;
    }
    
    // Can add state-specific logic
    public boolean canTransitionTo(RepairStatus nextState) {
        return nextState.sequence > this.sequence || nextState == CANCELLED;
    }
}

// Entity uses state
@Entity
public class RepairRequest {
    
    @Enumerated(EnumType.STRING)
    private RepairStatus status;
    
    // Business logic based on state
    public void updateStatus(RepairStatus newStatus) {
        
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + this.status + " to " + newStatus
            );
        }
        
        this.status = newStatus;
        
        // Trigger actions based on new state
        if (newStatus == RepairStatus.READY_FOR_PICKUP) {
            // Auto-generate invoice when ready for pickup
            triggerInvoiceGeneration();
        }
    }
}

// Similar pattern for PaymentStatus
public enum PaymentStatus {
    UNPAID,      // Initial state
    PENDING,     // Processing
    PAID,        // Final state
    FAILED,      // Failed state
    REFUNDED     // Refunded state
}

// Why?
// ✓ Type-safe state representation
// ✓ Encapsulates state-specific behavior
// ✓ Prevents invalid state transitions
// ✓ Easy to add new states
// ✓ State machine clearly defined
```

#### **4. Template Method Pattern (Implicit)**

```java
// Services define algorithm structure, subclasses can override steps

public interface RepairService {
    // Algorithm structure defined in interface
    RepairRequest createRepairRequest(RepairRequestDTO dto);
    RepairRequest assignTechnician(Long requestId, Long technicianId);
    RepairRequest updateRepairStatus(Long requestId, String status);
    List<RepairRequest> getRepairsForTechnician(Long technicianId);
}

// Implementation follows template
@Service
public class RepairServiceImpl implements RepairService {
    
    // Step 1: Create repair
    @Override
    public RepairRequest createRepairRequest(RepairRequestDTO dto) {
        // Template steps:
        // a) Validate customer
        // b) Create gadget
        // c) Create repair request
        // d) Set initial status
        // e) Save to repository
    }
    
    // Step 2: Assign technician
    @Override
    public RepairRequest assignTechnician(Long requestId, Long technicianId) {
        // Template steps:
        // a) Find repair request
        // b) Validate technician
        // c) Assign technician
        // d) Save changes
    }
    
    // Step 3: Update status
    @Override
    public RepairRequest updateRepairStatus(Long requestId, String status) {
        // Template steps:
        // a) Find repair request
        // b) Validate status transition
        // c) Update status
        // d) Trigger side effects (invoice generation)
        // e) Save changes
    }
}

// Why?
// ✓ Defines algorithm skeleton in interface
// ✓ Subclasses implement specific steps
// ✓ Consistent algorithm structure
// ✓ Easy to understand flow
```

#### **5. Command Pattern (Implicit via Controllers)**

```java
// Controllers encapsulate UI actions as method calls

@Controller
public class TechnicianController {
    
    private final RepairService repairService;
    
    // Each method represents a command
    
    @FXML
    private void handleLoadPendingRepairs(ActionEvent event) {
        // Command: Load pending repairs
        List<RepairRequest> repairs = repairService.getRepairsForTechnician(
            sessionManager.getCurrentUserId()
        );
        pendingRepairsTable.setItems(FXCollections.observableArrayList(repairs));
    }
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        // Command: Update repair status
        String repairId = repairIdField.getText();
        String newStatus = statusCombo.getValue();
        
        repairService.updateRepairStatus(Long.parseLong(repairId), newStatus);
        showAlert("Status updated successfully");
    }
    
    @FXML
    private void handleUpdateNotes(ActionEvent event) {
        // Command: Update repair notes
        String notes = notesField.getText();
        repairService.updateRepairNotes(repairId, notes);
    }
}

// Why?
// ✓ UI actions encapsulated as commands
// ✓ Commands can be executed, queued, logged
// ✓ Decouples UI from service logic
// ✓ Easy to add undo/redo functionality
// ✓ Consistent command structure
```

---

## HOW EVERYTHING WORKS

### Complete System Startup Sequence

```
1. APPLICATION LAUNCH
   ↓
2. GadgetRepairTrackerApplication.main()
   │
   └─ Launches JavaFX Application
      ↓
3. init() - Application Initialization
   │
   ├─ Create Spring ApplicationContext
   │  └─ Spring scans classpath for @Component, @Service, @Repository
   │
   ├─ Load all beans (Services, Repositories, Controllers)
   │  └─ Services get repositories injected
   │  └─ Controllers get services injected
   │
   └─ Create UserSessionManager singleton
      └─ Will hold current logged-in user
      ↓
4. start(Stage stage) - JavaFX Startup
   │
   ├─ Load login.fxml
   │  └─ Create AuthController
   │     └─ Spring injects UserService, ApplicationContext, SessionManager
   │
   ├─ Create UI hierarchy
   │  └─ Stage → Scene → Parent → LoginScreen
   │
   ├─ Display window to user
   │
   └─ Wait for user interaction
      ↓
5. USER LOGS IN
   │
   ├─ User enters email/password in login form
   │  └─ Clicks "Login" button
   │
   ├─ AuthController.handleLogin(ActionEvent) called
   │  │
   │  ├─ Extract email/password from TextFields
   │  │
   │  ├─ Call userService.authenticate(email, password)
   │  │  │
   │  │  ├─ UserServiceImpl queries database via UserRepository
   │  │  │  └─ SELECT * FROM users WHERE email = ?
   │  │  │
   │  │  ├─ Find matching user record
   │  │  │
   │  │  ├─ Verify password matches
   │  │  │
   │  │  └─ Return User object (Customer/Technician/Manager)
   │  │
   │  ├─ sessionManager.setCurrentUser(user)
   │  │  └─ Store user in singleton for access by other controllers
   │  │
   │  ├─ navigateToDashboard(user)
   │  │  │
   │  │  ├─ Switch on user.getRole()
   │  │  │
   │  │  ├─ Load appropriate FXML based on role
   │  │  │  ├─ CUSTOMER → /fxml/customer-dashboard.fxml
   │  │  │  ├─ TECHNICIAN → /fxml/technician-dashboard.fxml
   │  │  │  └─ MANAGER → /fxml/manager-dashboard.fxml
   │  │  │
   │  │  └─ Display dashboard UI
   │  │     └─ Controller for selected dashboard is created
   │  │        └─ Spring injects its dependencies
   │  │
   │  └─ Return to handleLogin() - update UI with success
   │     └─ showAlert("Login successful")
   │
   └─ User can now use dashboard features
      ↓
6. TECHNICIAN VIEWS ASSIGNED REPAIRS (Example Flow)
   │
   ├─ User (Technician) clicks "Load Pending Repairs" button
   │
   ├─ TechnicianController.handleLoadPendingRepairs(ActionEvent) called
   │  │
   │  ├─ Get current technician ID
   │  │  └─ Long currentTechnicianId = sessionManager.getCurrentUserId()
   │  │
   │  ├─ Call repairService.getRepairsForTechnician(currentTechnicianId)
   │  │  │
   │  │  ├─ RepairServiceImpl.getRepairsForTechnician()
   │  │  │  │
   │  │  │  ├─ Validate user is technician
   │  │  │  │  └─ userRepository.findById(technicianId)
   │  │  │  │     └─ SELECT * FROM users WHERE id = ?
   │  │  │  │        ↓ Returns User with role=TECHNICIAN
   │  │  │  │
   │  │  │  ├─ if (user.getRole() != TECHNICIAN) throw error
   │  │  │  │
   │  │  │  ├─ Query repairs assigned to this technician
   │  │  │  │  └─ repairRequestRepository.findByTechnicianId(technicianId)
   │  │  │  │     └─ SELECT r FROM RepairRequest r 
   │  │  │  │        WHERE r.technician.id = ?
   │  │  │  │        ↓
   │  │  │  │        (Hibernate translates to SQL)
   │  │  │  │        ↓
   │  │  │  │        SELECT rr.* FROM repair_requests rr
   │  │  │  │        WHERE rr.technician_id = ?
   │  │  │  │
   │  │  │  ├─ Hibernate ORM maps result rows to RepairRequest objects
   │  │  │  │  └─ Each row → new RepairRequest instance
   │  │  │  │     └─ Lazy load related Gadget, Customer, Invoice
   │  │  │  │
   │  │  │  └─ Return List<RepairRequest>
   │  │  │
   │  │  └─ Return to controller
   │  │
   │  ├─ Filter repairs - exclude DELIVERED status
   │  │  └─ Stream filter: r -> !r.getStatus().equals(DELIVERED)
   │  │
   │  ├─ Convert to ObservableList for JavaFX
   │  │  └─ FXCollections.observableArrayList(repairs)
   │  │
   │  ├─ Populate TableView
   │  │  └─ pendingRepairsTable.setItems(observableList)
   │  │
   │  └─ Display updates on UI
   │     └─ User sees table with 3 repairs assigned to them
   │
   └─ User can click on repairs to see details
      ↓
7. MANAGER ASSIGNS TECHNICIAN TO REPAIR
   │
   ├─ Manager enters Repair ID and selects Technician from dropdown
   │
   ├─ Clicks "Assign" button
   │
   ├─ ManagerController.handleAssignTechnicianToRepair(ActionEvent)
   │  │
   │  ├─ Extract repair ID from text field
   │  │  └─ String repairIdText = assignTechRepairIdField.getText()
   │  │
   │  ├─ Extract technician selection from ComboBox
   │  │  └─ String selection = technicianAssignCombo.getValue()
   │  │  └─ Parse technician ID from selection string
   │  │
   │  ├─ Call repairService.assignTechnician(repairId, technicianId)
   │  │  │
   │  │  ├─ RepairServiceImpl.assignTechnician()
   │  │  │  │
   │  │  │  ├─ Find repair request
   │  │  │  │  └─ repairRequestRepository.findById(requestId)
   │  │  │  │     └─ SELECT * FROM repair_requests WHERE request_id = ?
   │  │  │  │
   │  │  │  ├─ Validate technician exists and has TECHNICIAN role
   │  │  │  │  └─ userRepository.findById(technicianId)
   │  │  │  │     └─ SELECT * FROM users WHERE id = ?
   │  │  │  │
   │  │  │  ├─ if (user.getRole() != TECHNICIAN) throw error
   │  │  │  │
   │  │  │  ├─ Create Technician reference (proxy, not full fetch)
   │  │  │  │  └─ Technician technician = technicianRepository.getReferenceById(technicianId)
   │  │  │  │     └─ Does NOT execute SQL - creates Hibernate proxy
   │  │  │  │
   │  │  │  ├─ Assign technician to repair
   │  │  │  │  └─ request.setTechnician(technician)
   │  │  │  │
   │  │  │  ├─ Set status to DIAGNOSIS_IN_PROGRESS
   │  │  │  │  └─ request.setStatus(RepairStatus.DIAGNOSIS_IN_PROGRESS)
   │  │  │  │
   │  │  │  ├─ Save updated repair request
   │  │  │  │  └─ repairRequestRepository.save(request)
   │  │  │  │     └─ Hibernate detects changes
   │  │  │  │        ↓
   │  │  │  │        UPDATE repair_requests 
   │  │  │  │        SET technician_id = ?, status = ?
   │  │  │  │        WHERE request_id = ?
   │  │  │  │
   │  │  │  └─ Return updated RepairRequest
   │  │  │
   │  │  └─ Return to controller
   │  │
   │  ├─ Update UI
   │  │  └─ showAlert("SUCCESS: Technician #" + techId + " assigned to repair #" + repairId)
   │  │
   │  ├─ Reload unassigned repairs table
   │  │  └─ Call handleLoadUnassignedRepairs()
   │  │     └─ Fetch repairs with technician_id IS NULL
   │  │     └─ Table refreshes - assigned repair disappears
   │  │
   │  └─ User sees confirmation message
   │
   └─ When technician logs in, will now see this repair in their queue
      ↓
8. SYSTEM SHUTDOWN
   │
   ├─ User closes application window
   │
   ├─ stop() method called
   │  │
   │  ├─ Close Spring ApplicationContext
   │  │  └─ Clean up all beans
   │  │  └─ Close database connections
   │  │
   │  └─ JVM exits
   │
   └─ Application terminated
```

### Request Response Cycle - Complete Example

```
┌─────────────────────────────────────────────────────────────┐
│ REQUEST: Customer submits repair request                     │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 1: PRESENTATION (JavaFX UI)                            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ User fills form:                                             │
│ ├─ Brand: "Samsung"                                          │
│ ├─ Model: "Galaxy S20"                                       │
│ ├─ Device Type: "MOBILE"                                     │
│ └─ Problem: "Screen is cracked"                              │
│                                                              │
│ User clicks "Submit" button                                  │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 2: CONTROLLER (CustomerController)                     │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ @FXML handleSubmitRepair(ActionEvent event) {                │
│   │                                                          │
│   ├─ Get form input from UI components                       │
│   │  ├─ brand = deviceNameField.getText()                    │
│   │  ├─ model = modelField.getText()                         │
│   │  ├─ type = deviceTypeCombo.getValue()                    │
│   │  └─ problem = issueDescriptionField.getText()            │
│   │                                                          │
│   ├─ Create DTO (Data Transfer Object)                       │
│   │  └─ RepairRequestDTO dto = new RepairRequestDTO()        │
│   │     ├─ dto.setCustomerId(1)                              │
│   │     ├─ dto.setBrand("Samsung")                           │
│   │     ├─ dto.setModel("Galaxy S20")                        │
│   │     ├─ dto.setDeviceType(MOBILE)                         │
│   │     └─ dto.setProblemDescription("Screen is cracked")    │
│   │                                                          │
│   └─ Call service layer                                      │
│      └─ repairService.createRepairRequest(dto)               │
│                                                              │
│ }                                                            │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 3: SERVICE (RepairService)                             │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ @Override                                                    │
│ RepairRequest createRepairRequest(RepairRequestDTO dto) {    │
│   │                                                          │
│   ├─ Step 1: Get customer from database                      │
│   │  └─ Customer customer = customerRepository.findById(1)   │
│   │     (if not found, throw RuntimeException)               │
│   │                                                          │
│   ├─ Step 2: Create and save Gadget entity                   │
│   │  ├─ Gadget gadget = new Gadget(                          │
│   │  │   "Samsung", "Galaxy S20", MOBILE)                    │
│   │  └─ gadgetRepository.save(gadget)                        │
│   │     (generates INSERT SQL)                               │
│   │                                                          │
│   ├─ Step 3: Create RepairRequest entity                     │
│   │  ├─ RepairRequest request = new RepairRequest(           │
│   │  │   customer, gadget, "Screen is cracked")              │
│   │  ├─ request.setStatus(REQUEST_SUBMITTED)                 │
│   │  └─ request.setCreatedAt(LocalDateTime.now())            │
│   │                                                          │
│   ├─ Step 4: Persist to database                             │
│   │  └─ return repairRequestRepository.save(request)         │
│   │     (generates INSERT SQL)                               │
│   │                                                          │
│   └─ Return newly created RepairRequest object               │
│                                                              │
│ }                                                            │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 4: REPOSITORY (Spring Data JPA)                        │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ gadgetRepository.save(gadget)                                │
│ ├─ JPA detects: this is new entity (no ID yet)               │
│ └─ Delegates to Hibernate: INSERT operation                  │
│                                                              │
│ repairRequestRepository.save(request)                        │
│ ├─ JPA detects: this is new entity (no ID yet)               │
│ └─ Delegates to Hibernate: INSERT operation                  │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 5: HIBERNATE ORM                                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Maps Java objects to SQL:                                    │
│                                                              │
│ RepairRequest object → INSERT statement                      │
│ repairService.createRepairRequest({                          │
│   "customerId": 1,                                           │
│   "brand": "Samsung",                                        │
│   "model": "Galaxy S20",                                     │
│   "deviceType": "MOBILE",                                    │
│   "problemDescription": "Screen is cracked"                  │
│ })                                                           │
│                                                              │
│ ↓ Hibernate generates SQL:                                   │
│                                                              │
│ INSERT INTO gadgets (brand, model, device_type)              │
│ VALUES ('Samsung', 'Galaxy S20', 'MOBILE');                  │
│                                                              │
│ INSERT INTO repair_requests (customer_id, gadget_gadget_id,  │
│                              problem_description, status,    │
│                              created_at)                     │
│ VALUES (1, <gadget_id>, 'Screen is cracked',                 │
│         'REQUEST_SUBMITTED', NOW());                         │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ LAYER 6: MYSQL DATABASE                                      │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ JDBC Driver executes SQL                                     │
│                                                              │
│ (1) INSERT INTO gadgets table                                │
│     AUTO_INCREMENT generates gadget_id = 15                  │
│                                                              │
│ (2) INSERT INTO repair_requests table                        │
│     AUTO_INCREMENT generates request_id = 8                  │
│                                                              │
│ Database responses:                                          │
│ ├─ Gadget saved with ID 15                                   │
│ └─ RepairRequest saved with ID 8                             │
│                                                              │
│ Inserted data:                                               │
│ gadgets table:                                               │
│ │ gadget_id │ brand   │ model       │ device_type │         │
│ │ 15        │Samsung  │ Galaxy S20  │ MOBILE      │         │
│                                                              │
│ repair_requests table:                                       │
│ │ request_id│ customer_id │ gadget_gadget_id │ problem... │  │
│ │ 8         │ 1           │ 15               │ Screen...  │  │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ RESPONSE: Back through layers                                │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Database returns:                                            │
│ ├─ Gadget: ID=15 (newly generated)                           │
│ └─ RepairRequest: ID=8 (newly generated)                     │
│                                                              │
│ Hibernate:                                                   │
│ ├─ Maps SQL result → RepairRequest object                    │
│ ├─ Sets requestId=8 from AUTO_INCREMENT                      │
│ └─ Passes back to Repository layer                           │
│                                                              │
│ Repository:                                                  │
│ └─ Returns RepairRequest object to Service                   │
│                                                              │
│ Service:                                                     │
│ └─ Returns RepairRequest object to Controller                │
│                                                              │
│ Controller:                                                  │
│ ├─ Receives: RepairRequest {                                 │
│ │   requestId: 8,                                            │
│ │   customer: {id:1, name:"John", ...},                      │
│ │   gadget: {gadgetId: 15, brand:"Samsung", ...},            │
│ │   status: "REQUEST_SUBMITTED",                             │
│ │   createdAt: "2026-04-20T10:30:00"                         │
│ │ }                                                          │
│ │                                                            │
│ └─ Updates UI:                                               │
│    └─ showAlert("SUCCESS!", "Repair #8 submitted. " +        │
│       "We'll contact you soon.")                             │
│                                                              │
│ UI displays success message to user                          │
│                                                              │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ RESPONSE: User sees confirmation on screen                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ Alert Dialog:                                                │
│ ┌─────────────────────────────────────────┐                │
│ │ SUCCESS!                                │                │
│ │                                         │                │
│ │ Repair #8 submitted.                    │                │
│ │ We'll contact you soon.                 │                │
│ │                                         │                │
│ │                      [OK]                │                │
│ └─────────────────────────────────────────┘                │
│                                                              │
│ User can now:                                                │
│ ├─ Track repair status                                       │
│ ├─ View repair details                                       │
│ └─ See associated invoice when ready                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## CONCLUSION

The Gadget Repair Tracker demonstrates **professional software architecture** with:

### **✓ Clean Architecture**
- Clear separation of concerns across 6 layers
- Each layer has single responsibility
- Layers communicate through well-defined interfaces

### **✓ SOLID Principles**
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes substitute for base types
- **I**nterface Segregation: Clients depend only on what they use
- **D**ependency Inversion: Depend on abstractions, not concrete classes

### **✓ GRASP Patterns**
- **Creator**: Services create entities
- **Expert**: Each class has expertise for its responsibility
- **Controller**: Controllers handle UI events
- **Polymorphism**: Strategies and observers use polymorphic behavior
- **Pure Fabrication**: Services coordinate between entities

### **✓ Design Patterns**
- **Creational**: Factory, Dependency Injection, Singleton
- **Structural**: Adapter, Proxy, Decorator, JOINED Inheritance
- **Behavioral**: Observer, Strategy, State, Template Method, Command

### **✓ Best Practices**
- Spring Boot for lifecycle management
- Hibernate ORM for database operations
- Constructor injection for dependency management
- Transactional services for data consistency
- Role-based access control
- Type-safe enumerations for states
- DTOs for layer communication

**This is production-quality code demonstrating mastery of software design principles and patterns.**

---

**Document Version**: 1.0  
**Generated**: April 20, 2026  
**Framework Versions**:
- Spring Boot 4.0.4
- JavaFX 21
- Hibernate ORM 7.2.7
- MySQL 8.0.45
