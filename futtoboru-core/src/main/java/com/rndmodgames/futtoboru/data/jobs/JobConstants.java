package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job System Constants (v1.0)
 * 
 * @author Geomancer86
 */
public class JobConstants {
    
    // Application limits
    public static final int MAX_APPLICATIONS_PER_WEEK = 3;
    
    // Offer expiration
    public static final int OFFER_EXPIRATION_DAYS = 7;
    
    // Negotiation limits
    public static final int MAX_NEGOTIATION_ROUNDS = 3;
    
    // Job opening expiration
    public static final int JOB_OPENING_DEFAULT_DEADLINE_DAYS = 30;
    
    // Application processing delay (v1.0: simplified - instant for MVP, can add delay later)
    public static final int APPLICATION_PROCESSING_DELAY_DAYS = 0; // 0 = instant
    
    // AI negotiation tolerance (percentage)
    public static final double AI_NEGOTIATION_TOLERANCE = 0.10; // 10% above/below original offer
}

