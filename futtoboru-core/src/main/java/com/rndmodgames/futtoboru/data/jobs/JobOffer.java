package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Person;

/**
 * Job Offer v1
 * 
 * Represents a job offer made to a player after application.
 * v1.0: Simplified - no interviews, offers made directly after application.
 * 
 * @author Geomancer86
 */
public class JobOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Related job opening
     */
    private Long jobOpeningId; // Store ID to avoid circular references
    
    /**
     * Recipient
     */
    private Long recipientId; // Store ID to avoid circular references
    
    /**
     * Dates
     */
    private LocalDateTime offerDate;
    private LocalDateTime expirationDate; // Default: 7 days from offer
    
    /**
     * Status
     */
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, NEGOTIATING, EXPIRED
    
    /**
     * Terms (v1.0: basic)
     */
    private BigDecimal salary;
    private Integer contractLengthMonths;
    
    /**
     * Negotiation (v1.0: simple)
     */
    private Long counterOfferId; // null if not negotiating, points to counter-offer
    private Integer negotiationRound; // 0 = original offer, 1-3 = counter-offers
    private boolean isCounterOffer; // true if this is a counter-offer
    
    /**
     * Original offer (if this is a counter-offer)
     */
    private Long originalOfferId; // null if this is the original offer
    
    // Constructors
    public JobOffer() {
        this.status = OfferStatus.PENDING;
        this.offerDate = LocalDateTime.now();
        this.expirationDate = this.offerDate.plusDays(JobConstants.OFFER_EXPIRATION_DAYS); // Default 7 days
        this.negotiationRound = 0;
        this.isCounterOffer = false;
    }
    
    public JobOffer(com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening, Person recipient) {
        this();
        this.jobOpeningId = jobOpening.getId();
        this.recipientId = recipient.getId();
        this.salary = jobOpening.getSalary();
        this.contractLengthMonths = jobOpening.getContractLengthMonths();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getJobOpeningId() {
        return jobOpeningId;
    }
    
    public void setJobOpeningId(Long jobOpeningId) {
        this.jobOpeningId = jobOpeningId;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public LocalDateTime getOfferDate() {
        return offerDate;
    }
    
    public void setOfferDate(LocalDateTime offerDate) {
        this.offerDate = offerDate;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public OfferStatus getStatus() {
        return status;
    }
    
    public void setStatus(OfferStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSalary() {
        return salary;
    }
    
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
    
    public Integer getContractLengthMonths() {
        return contractLengthMonths;
    }
    
    public void setContractLengthMonths(Integer contractLengthMonths) {
        this.contractLengthMonths = contractLengthMonths;
    }
    
    public Long getCounterOfferId() {
        return counterOfferId;
    }
    
    public void setCounterOfferId(Long counterOfferId) {
        this.counterOfferId = counterOfferId;
    }
    
    public Integer getNegotiationRound() {
        if (negotiationRound == null) {
            negotiationRound = 0;
        }
        return negotiationRound;
    }
    
    public void setNegotiationRound(Integer negotiationRound) {
        this.negotiationRound = negotiationRound;
    }
    
    public boolean isCounterOffer() {
        return isCounterOffer;
    }
    
    public void setCounterOffer(boolean isCounterOffer) {
        this.isCounterOffer = isCounterOffer;
    }
    
    public Long getOriginalOfferId() {
        return originalOfferId;
    }
    
    public void setOriginalOfferId(Long originalOfferId) {
        this.originalOfferId = originalOfferId;
    }
    
    /**
     * Utility: Check if offer has expired
     * 
     * @param currentGameDate The current game date (not real-world date)
     */
    public boolean isExpired(LocalDateTime currentGameDate) {
        if (currentGameDate == null) {
            // Fallback to real-world time if game date not provided (shouldn't happen)
            return expirationDate != null &&
                   expirationDate.isBefore(LocalDateTime.now()) && 
                   status == OfferStatus.PENDING;
        }
        return expirationDate != null &&
               expirationDate.isBefore(currentGameDate) && 
               status == OfferStatus.PENDING;
    }
    
    /**
     * Utility: Check if offer can be negotiated
     * 
     * @param currentGameDate The current game date (not real-world date)
     */
    public boolean canNegotiate(LocalDateTime currentGameDate) {
        return status == OfferStatus.PENDING && 
               !isExpired(currentGameDate) && 
               getNegotiationRound() < JobConstants.MAX_NEGOTIATION_ROUNDS;
    }
}

