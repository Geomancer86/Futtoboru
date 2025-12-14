package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job Offer Status
 * 
 * @author Geomancer86
 */
public enum OfferStatus {
    PENDING,        // Awaiting player response
    ACCEPTED,       // Offer accepted
    REJECTED,       // Offer rejected
    NEGOTIATING,    // Counter-offer submitted
    EXPIRED         // Offer expired
}

