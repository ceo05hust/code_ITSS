package com.aims.aimsbackend.repository.order;

import com.aims.aimsbackend.entity.order.TransactionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionInfoRepository extends JpaRepository<TransactionInfo, Long> {
    Optional<TransactionInfo> findByExternalTransactionId(String externalTransactionId);
}