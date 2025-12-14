package com.rndmodgames.futtoboru.data;

/**
 * Attribute Change Tracking Constants (v1.0)
 * 
 * Defines time periods and thresholds for tracking player attribute changes.
 * Based on industry standards: Football Manager uses 30-day periods for attribute changes.
 * 
 * @author Geomancer86
 */
public class AttributeTrackingConstants {
    
    /**
     * Default time periods (in days) for attribute change tracking
     */
    public static final int DEFAULT_SHORT_TERM_DAYS = 14;      // 2 weeks - recent form
    public static final int DEFAULT_MEDIUM_TERM_DAYS = 30;     // 1 month - recommended default
    public static final int DEFAULT_LONG_TERM_DAYS = 90;       // 3 months - long-term trends
    
    /**
     * Default period to use for attribute change display
     * Set to medium-term (30 days) as recommended by analysis
     */
    public static final int DEFAULT_CHANGE_PERIOD_DAYS = DEFAULT_MEDIUM_TERM_DAYS;
    
    /**
     * Minimum change threshold (in attribute points) to display changes
     * Changes smaller than this are considered "stable" and shown as [=]
     */
    public static final float MIN_CHANGE_THRESHOLD = 0.5f;      // 0.5 points
    
    /**
     * Significant change threshold (in attribute points)
     * Changes larger than this show double arrows (↑↑ or ↓↓)
     */
    public static final float SIGNIFICANT_CHANGE_THRESHOLD = 2.0f;  // 2.0 points
    
    /**
     * Snapshot frequency - how often to create attribute snapshots
     */
    public static final int SNAPSHOT_INTERVAL_DAYS = 7;         // Weekly snapshots
    
    /**
     * Maximum snapshots to keep per player
     * 52 weeks = 1 year of history
     */
    public static final int MAX_SNAPSHOTS_PER_PLAYER = 52;     // 1 year (52 weeks)
    
    /**
     * Maximum age of snapshots to keep (in days)
     * Snapshots older than this will be cleaned up
     */
    public static final int MAX_SNAPSHOT_AGE_DAYS = 365;       // 1 year
}

