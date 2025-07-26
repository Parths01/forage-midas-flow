package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    private final List<Transaction> receivedTransactions = new ArrayList<>();
    private final DatabaseConduit databaseConduit;
    
    public TransactionListener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }
    
    @KafkaListener(topics = "${general.kafka-topic}")
    public void handleTransaction(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
        receivedTransactions.add(transaction);
        logger.info("Transaction count: {}, Amount: {}", receivedTransactions.size(), transaction.getAmount());
        
        // Process the transaction through DatabaseConduit
        boolean success = databaseConduit.processTransaction(transaction);
        if (success) {
            logger.info("Transaction processed successfully");
        } else {
            logger.warn("Transaction rejected");
        }
        
        // Check waldorf's balance after each transaction involving ID 5
        if (transaction.getSenderId() == 5L || transaction.getRecipientId() == 5L) {
            var waldorf = databaseConduit.findUserById(5L);
            if (waldorf != null) {
                logger.warn("WALDORF BALANCE UPDATE: {} (transaction: {})", waldorf.getBalance(), transaction);
            }
        }
        
        // For debugging purposes - this is where we can set breakpoints
        // to inspect the transaction amounts
        if (receivedTransactions.size() <= 4) {
            logger.warn("TASK TWO DEBUG - Transaction #{}: Amount = {}", receivedTransactions.size(), transaction.getAmount());
        }
    }
    
    public List<Transaction> getReceivedTransactions() {
        return new ArrayList<>(receivedTransactions);
    }
    
    public void clearReceivedTransactions() {
        receivedTransactions.clear();
    }
}
