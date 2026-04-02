package com.pes.gadgetrepair.repository;

import com.pes.gadgetrepair.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 Design Principles:
 Single Responsibility Principle

 Purpose:
 Database operations for invoices.
*/

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

}