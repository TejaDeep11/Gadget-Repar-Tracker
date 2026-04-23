package com.pes.gadgetrepair.service.impl;

import com.pes.gadgetrepair.dto.RepairRequestDTO;
import com.pes.gadgetrepair.enums.RepairStatus;
import com.pes.gadgetrepair.enums.UserRole;
import com.pes.gadgetrepair.exception.RepairRequestNotFoundException;
import com.pes.gadgetrepair.exception.UserNotFoundException;
import com.pes.gadgetrepair.model.*;
import com.pes.gadgetrepair.observer.CustomerNotificationObserver;
import com.pes.gadgetrepair.observer.RepairStatusPublisher;
import com.pes.gadgetrepair.repository.CustomerRepository;
import com.pes.gadgetrepair.repository.GadgetRepository;
import com.pes.gadgetrepair.repository.RepairLogRepository;
import com.pes.gadgetrepair.repository.RepairRequestRepository;
import com.pes.gadgetrepair.repository.TechnicianRepository;
import com.pes.gadgetrepair.repository.UserRepository;
import com.pes.gadgetrepair.service.RepairService;
import com.pes.gadgetrepair.service.BillingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
Design Principles Used:
1. Single Responsibility Principle
2. Dependency Inversion Principle
3. Separation of Concerns

Role:
Handles repair lifecycle management.
*/

@Service
public class RepairServiceImpl implements RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final GadgetRepository gadgetRepository;
    private final BillingService billingService;
    private final RepairStatusPublisher statusPublisher;
    private final RepairLogRepository repairLogRepository;

    public RepairServiceImpl(
            RepairRequestRepository repairRequestRepository,
            CustomerRepository customerRepository,
            TechnicianRepository technicianRepository,
            UserRepository userRepository,
            GadgetRepository gadgetRepository,
            BillingService billingService,
            RepairStatusPublisher statusPublisher,
            RepairLogRepository repairLogRepository
    ) {
        this.repairRequestRepository = repairRequestRepository;
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.gadgetRepository = gadgetRepository;
        this.billingService = billingService;
        this.statusPublisher = statusPublisher;
        this.repairLogRepository = repairLogRepository;
    }

    @Override
    public RepairRequest createRepairRequest(RepairRequestDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        Gadget gadget = new Gadget(
                dto.getBrand(),
                dto.getModel(),
                dto.getDeviceType()
        );

        gadgetRepository.save(gadget);

        RepairRequest request = new RepairRequest(
                customer,
                gadget,
                dto.getProblemDescription()
        );

        RepairRequest savedRequest = repairRequestRepository.save(request);
        
        // Attach observer to notify customer of status changes
        statusPublisher.addObserver(new CustomerNotificationObserver(customer));
        
        // Log the repair creation
        repairLogRepository.save(
                new RepairLog(savedRequest, "Repair request created by customer")
        );
        
        return savedRequest;
    }

    @Override
    public Optional<RepairRequest> findRepairRequestById(Long requestId) {
        return repairRequestRepository.findById(requestId);
    }

    @Override
    public List<RepairRequest> getAllRepairRequests() {
        return repairRequestRepository.findAll();
    }

    @Override
    public RepairRequest assignTechnician(Long requestId, Long technicianId) {

        RepairRequest request = repairRequestRepository.findById(requestId)
                .orElseThrow(() -> new RepairRequestNotFoundException("Repair request not found"));

        User user = userRepository.findById(technicianId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        if(user.getRole() != UserRole.TECHNICIAN) {
            throw new UserNotFoundException("User is not a technician");
        }

        // Use getReference to create a Technician proxy with just the ID
        // This avoids unnecessary DB lookup while satisfying FK constraint
        Technician technician = technicianRepository.getReferenceById(technicianId);
        request.setTechnician(technician);
        request.setStatus(RepairStatus.DIAGNOSIS_IN_PROGRESS);

        RepairRequest savedRequest = repairRequestRepository.save(request);
        
        // Log technician assignment (use user.getName() instead of technician.getName() to avoid lazy loading)
        repairLogRepository.save(
                new RepairLog(savedRequest, "Technician assigned: " + user.getName())
        );
        
        // Notify observers of status change
        notifyObservers(savedRequest);
        
        return savedRequest;
    }

    @Override
    public RepairRequest updateRepairStatus(Long requestId, String status) {

        RepairRequest request = repairRequestRepository.findById(requestId)
                .orElseThrow(() -> new RepairRequestNotFoundException("Repair request not found"));

        request.setStatus(RepairStatus.valueOf(status));
        RepairRequest updatedRequest = repairRequestRepository.save(request);
        
        // Log the status change
        repairLogRepository.save(
                new RepairLog(updatedRequest, "Status updated to: " + status)
        );
        
        // Notify observers of status change
        notifyObservers(updatedRequest);
        
        // Auto-generate invoice when repair is Ready for Pickup
        if(status.equals(RepairStatus.READY_FOR_PICKUP.name())) {
            // Check if invoice already exists for this repair
            try {
                java.util.List<Invoice> allInvoices = billingService.getAllInvoices();
                boolean invoiceExists = allInvoices.stream()
                    .anyMatch(inv -> inv.getRepairRequest().getRequestId().equals(requestId));
                
                if(!invoiceExists) {
                    // Generate invoice with estimated cost (you can adjust this calculation)
                    double estimatedCost = 500.0; // Default estimate - can be based on device type or labor
                    billingService.generateInvoice(requestId, estimatedCost);
                    System.out.println("Invoice automatically generated for repair #" + requestId);
                }
            } catch(Exception e) {
                System.out.println("Error generating invoice: " + e.getMessage());
            }
        }
        
        return updatedRequest;
    }

    @Override
    public List<RepairRequest> getRepairsForTechnician(Long technicianId) {
        User user = userRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if(user.getRole() != UserRole.TECHNICIAN) {
            throw new UserNotFoundException("User is not a technician");
        }
        return repairRequestRepository.findByTechnicianId(technicianId);
    }

    @Override
    public void notifyObservers(RepairRequest repairRequest) {
        // Notify all registered observers about the repair status change
        statusPublisher.notifyObservers(repairRequest);
        System.out.println("Repair status change notification sent for repair #" + repairRequest.getRequestId());
    }
}