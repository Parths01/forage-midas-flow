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
public class DatabaseValidationTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseValidationTest.class);
    
    @Autowired
    private DatabaseConduit databaseConduit;
    
    @Autowired
    private UserPopulator userPopulator;
    
    @Autowired
    private FileLoader fileLoader;
    
    @Test
    void testWaldorfBalance() {
        // Populate users first
        userPopulator.populate();
        
        // Get waldorf's initial balance
        UserRecord waldorf = databaseConduit.findUserById(5L); // waldorf is user ID 5
        logger.info("Waldorf's initial balance: {}", waldorf != null ? waldorf.getBalance() : "NOT FOUND");
        
        // Process all transactions from the test data
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
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
        }
        
        // Get waldorf's final balance
        UserRecord finalWaldorf = databaseConduit.findUserById(5L);
        float finalBalance = finalWaldorf != null ? finalWaldorf.getBalance() : 0;
        int roundedBalance = (int) Math.floor(finalBalance);
        
        logger.info("=== FINAL RESULTS ===");
        logger.info("Total transactions processed: {}", transactionCount);
        logger.info("Successful transactions: {}", successCount);
        logger.info("Waldorf's final balance: {}", finalBalance);
        logger.info("Waldorf's balance rounded down: {}", roundedBalance);
        logger.info("===================");
    }
}
