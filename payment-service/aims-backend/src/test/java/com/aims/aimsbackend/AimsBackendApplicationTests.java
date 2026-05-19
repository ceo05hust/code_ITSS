package com.aims.aimsbackend;

import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.repository.order.InvoiceRepository;
import com.aims.aimsbackend.repository.order.TransactionInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AimsBackendApplicationTests {

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private TransactionInfoRepository transactionInfoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void printDatabaseContents() {
		System.out.println("=== INVOICES IN DB ===");
		List<Invoice> invoices = invoiceRepository.findAll();
		invoices.forEach(inv -> System.out.println("Invoice ID: " + inv.getInvoiceId() + " | Ref: " + inv.getPaymentReference() + " | Amount: " + inv.getTotalAmount()));

		System.out.println("=== TRANSACTIONS IN DB ===");
		List<TransactionInfo> txns = transactionInfoRepository.findAll();
		txns.forEach(txn -> System.out.println("Txn ID: " + txn.getTransactionId() + " | Amount: " + txn.getAmount() + " | Status: " + txn.getStatus() + " | Content: " + txn.getTransactionContent()));
	}

}

