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
    
    @KafkaListener(topics = "${general.kafka-topic}")
    public void handleTransaction(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
        receivedTransactions.add(transaction);
        logger.info("Transaction count: {}, Amount: {}", receivedTransactions.size(), transaction.getAmount());
        
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
