package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job Application Status
 * 
 * @author Geomancer86
 */
public enum ApplicationStatus {
    PENDING,            // Awaiting club decision
    REJECTED,           // Application rejected
    OFFER_RECEIVED,     // Offer made (v1.0: simplified, no interview)
    ACCEPTED,           // Offer accepted
    WITHDRAWN           // Player withdrew application
}

