package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Message Priority v1
 * 
 * Priority levels for inbox messages.
 * 
 * @author Geomancer86
 */
public enum MessagePriority {
    
    /**
     * Urgent messages - appear at top, red indicator
     * Examples: Match starting soon, contract expiring
     */
    URGENT,
    
    /**
     * Normal priority - default
     * Examples: League announcements, fixture releases
     */
    NORMAL,
    
    /**
     * Low priority - grayed out, appears at bottom
     * Examples: General news, historical information
     */
    LOW
}

