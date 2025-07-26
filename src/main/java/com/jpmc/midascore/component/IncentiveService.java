package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
public class IncentiveService {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";
    
    private final RestTemplate restTemplate;
    
    public IncentiveService() {
        this.restTemplate = new RestTemplate();
    }
    
    public float getIncentive(Transaction transaction) {
        try {
            logger.info("Calling incentive API for transaction: {}", transaction);
            Incentive incentive = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            
            if (incentive != null) {
                logger.info("Received incentive: {}", incentive.getAmount());
                return incentive.getAmount();
            } else {
                logger.warn("Received null incentive response");
                return 0.0f;
            }
        } catch (RestClientException e) {
            logger.error("Error calling incentive API: {}", e.getMessage());
            // Return 0 incentive if API call fails
            return 0.0f;
        }
    }
}
