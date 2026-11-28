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
    WITHDRAWN,          // Player withdrew application
    DECLINED,           // J-01: offer received and turned down by the applicant
    EXPIRED             // J-01: offer received and left unanswered until it lapsed
}

