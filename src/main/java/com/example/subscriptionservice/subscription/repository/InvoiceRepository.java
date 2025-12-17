package com.example.subscriptionservice.subscription.repository;

import com.example.subscriptionservice.subscription.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByUserIdOrderByIssueDateDesc(Long userId);
}