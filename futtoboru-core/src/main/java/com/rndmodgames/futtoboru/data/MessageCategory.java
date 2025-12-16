package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Message Category v1
 * 
 * Categories for organizing inbox messages.
 * 
 * @author Geomancer86
 */
public enum MessageCategory {
    
    /**
     * League-related messages
     * - League creation
     * - Fixture releases
     * - Season reminders
     * - Matchday notifications
     */
    LEAGUE,
    
    /**
     * Cup competition messages
     * - Cup draw announcements
     * - Draw results
     * - Round progression
     */
    CUP,
    
    /**
     * Authority/Association messages
     * - Rule changes
     * - Important dates
     * - General announcements
     */
    AUTHORITY,
    
    /**
     * Match-related messages
     * - Match previews
     * - Match results
     * - Match reports
     */
    MATCH,
    
    /**
     * Job-related messages
     * - Job offers
     * - Application status
     * - Contract negotiations
     */
    JOB,
    
    /**
     * System messages
     * - General notifications
     * - Game events
     */
    SYSTEM
}

