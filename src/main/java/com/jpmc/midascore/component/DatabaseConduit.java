package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConduit.class);
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    @Transactional
    public boolean processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction);
        
        // Find sender and recipient by their IDs
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        
        // Validate transaction
        if (sender == null) {
            logger.warn("Invalid transaction: Sender with ID {} not found", transaction.getSenderId());
            return false;
        }
        
        if (recipient == null) {
            logger.warn("Invalid transaction: Recipient with ID {} not found", transaction.getRecipientId());
            return false;
        }
        
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Invalid transaction: Sender {} has insufficient balance. Required: {}, Available: {}", 
                    sender.getName(), transaction.getAmount(), sender.getBalance());
            return false;
        }
        
        // Process the transaction
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());
        
        // Save updated user records
        userRepository.save(sender);
        userRepository.save(recipient);
        
        // Create and save transaction record
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
        transactionRepository.save(transactionRecord);
        
        logger.info("Transaction processed successfully: {} sent {} to {}", 
                sender.getName(), transaction.getAmount(), recipient.getName());
        logger.info("New balances - {}: {}, {}: {}", 
                sender.getName(), sender.getBalance(), recipient.getName(), recipient.getBalance());
        
        return true;
    }
    
    public UserRecord findUserByName(String name) {
        return userRepository.findByName(name);
    }
    
    public UserRecord findUserById(long id) {
        return userRepository.findById(id);
    }
}
