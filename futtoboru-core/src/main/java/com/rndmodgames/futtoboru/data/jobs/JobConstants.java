package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job System Constants (v1.0)
 * 
 * @author Geomancer86
 */
public class JobConstants {
    
    // Application limits (set to very high number to effectively allow unlimited applications)
    public static final int MAX_APPLICATIONS_PER_WEEK = 999; // Effectively unlimited
    
    // Offer expiration
    public static final int OFFER_EXPIRATION_DAYS = 7;
    
    // Negotiation limits
    public static final int MAX_NEGOTIATION_ROUNDS = 3;
    
    // Job opening expiration
    public static final int JOB_OPENING_DEFAULT_DEADLINE_DAYS = 30;

    /** J-01: an advert runs 21 to 42 days (seeded per opening); at its deadline the club appoints someone (D31) */
    public static final int JOB_OPENING_MIN_DEADLINE_DAYS = 21;
    public static final int JOB_OPENING_MAX_DEADLINE_DAYS = 42;

    /**
     * J-01 (D31): the daily chance that an AI club's staff member leaves his post (about three seasons in a post on
     * average). Placeholder, not researched (D30).
     */
    public static final double AI_STAFF_DEPARTURE_PER_DAY = 1.0 / (3 * 365);
    
    // Application processing delay (v1.0: simplified - instant for MVP, can add delay later)
    public static final int APPLICATION_PROCESSING_DELAY_DAYS = 0; // 0 = instant
    
    // AI negotiation tolerance (percentage)
    public static final double AI_NEGOTIATION_TOLERANCE = 0.10; // 10% above/below original offer
}

