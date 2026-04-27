package com.aims.payment_service.repository;

import com.aims.payment_service.entity.TransactionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionInfoRepository extends JpaRepository<TransactionInfo, String> {

    /** Tìm transaction theo orderId (invoiceId) */
    Optional<TransactionInfo> findByOrderId(String orderId);
}
