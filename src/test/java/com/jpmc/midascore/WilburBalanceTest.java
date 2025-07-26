package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
public class WilburBalanceTest {
    private static final Logger logger = LoggerFactory.getLogger(WilburBalanceTest.class);
    
    @Autowired
    private DatabaseConduit databaseConduit;
    
    @Autowired
    private UserPopulator userPopulator;
    
    @Autowired
    private FileLoader fileLoader;
    
    @Test
    void testWilburBalance() {
        // Populate users first
        userPopulator.populate();
        
        // Get wilbur's initial balance (ID 9)
        UserRecord wilbur = databaseConduit.findUserById(9L);
        logger.info("Wilbur's initial balance: {}", wilbur != null ? wilbur.getBalance() : "NOT FOUND");
        
        // Process all transactions from the test data for Task Four
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");
        int transactionCount = 0;
        int successCount = 0;
        
        for (String transactionLine : transactionLines) {
            transactionCount++;
            String[] transactionData = transactionLine.split(", ");
            Transaction transaction = new Transaction(
                Long.parseLong(transactionData[0]), 
                Long.parseLong(transactionData[1]), 
                Float.parseFloat(transactionData[2])
            );
            
            boolean success = databaseConduit.processTransaction(transaction);
            if (success) {
                successCount++;
            }
            logger.info("Transaction {}: {} - {}", transactionCount, transaction, success ? "SUCCESS" : "REJECTED");
            
            // Log wilbur's balance after each transaction involving him
            if (transaction.getSenderId() == 9L || transaction.getRecipientId() == 9L) {
                UserRecord currentWilbur = databaseConduit.findUserById(9L);
                if (currentWilbur != null) {
                    logger.warn("WILBUR BALANCE AFTER TRANSACTION {}: {}", transactionCount, currentWilbur.getBalance());
                }
            }
        }
        
        // Get wilbur's final balance
        UserRecord finalWilbur = databaseConduit.findUserById(9L);
        float finalBalance = finalWilbur != null ? finalWilbur.getBalance() : 0;
        int roundedBalance = (int) Math.floor(finalBalance);
        
        logger.info("=== FINAL RESULTS ===");
        logger.info("Total transactions processed: {}", transactionCount);
        logger.info("Successful transactions: {}", successCount);
        logger.info("Wilbur's final balance: {}", finalBalance);
        logger.info("Wilbur's balance rounded down: {}", roundedBalance);
        logger.info("===================");
    }
}
