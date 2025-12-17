package com.rndmodgames.futtoboru.data;

/**
 * Contract Type Constants v1.0
 * 
 * Defines contract types available in the game.
 * Based on historical 1888-89 data and modern football management games.
 * 
 * @author Geomancer86
 */
public class ContractType {
    
    /**
     * Amateur Contract
     * - No wages
     * - No training requirement
     * - Can leave anytime
     * - Can have other employment
     */
    public static final int AMATEUR = 0;
    
    /**
     * Semi-Professional Contract
     * - Low wages (£0.25-0.50/week)
     * - Half-day training (2-3 sessions/week)
     * - Can have other employment
     * - Limited bonuses
     */
    public static final int SEMI_PROFESSIONAL = 1;
    
    /**
     * Professional Contract
     * - Regular wages (£0.50-5.00/week)
     * - Full-time training (daily sessions)
     * - Dedicated to football
     * - Full bonus structure
     */
    public static final int PROFESSIONAL = 2;
    
    /**
     * Scholarship Contract (Future)
     * - Very low wage (£0.10-0.25/week)
     * - Training required
     * - Limited to youth players
     */
    public static final int SCHOLARSHIP = 3;
    
    /**
     * Non-Contract (Trialist)
     * - No wage
     * - Short-term (weeks)
     * - For trials/assessments
     */
    public static final int NON_CONTRACT = 4;
    
    /**
     * Get contract type name
     */
    public static String getName(Integer type) {
        if (type == null) {
            return "Unknown";
        }
        
        switch (type) {
            case AMATEUR:
                return "Amateur";
            case SEMI_PROFESSIONAL:
                return "Semi-Professional";
            case PROFESSIONAL:
                return "Professional";
            case SCHOLARSHIP:
                return "Scholarship";
            case NON_CONTRACT:
                return "Non-Contract";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Check if contract type requires training
     */
    public static boolean requiresTraining(Integer type) {
        if (type == null) {
            return false;
        }
        return type == SEMI_PROFESSIONAL || type == PROFESSIONAL || type == SCHOLARSHIP;
    }
    
    /**
     * Check if contract type is full-time
     */
    public static boolean isFullTime(Integer type) {
        if (type == null) {
            return false;
        }
        return type == PROFESSIONAL;
    }
    
    /**
     * Check if contract type allows other employment
     */
    public static boolean allowsOtherEmployment(Integer type) {
        if (type == null) {
            return true;
        }
        return type == AMATEUR || type == SEMI_PROFESSIONAL;
    }
    
    /**
     * Get typical weekly wage range for contract type (1888-89)
     */
    public static String getWageRange(Integer type) {
        if (type == null) {
            return "Unknown";
        }
        
        switch (type) {
            case AMATEUR:
                return "£0/week";
            case SEMI_PROFESSIONAL:
                return "£0.25-0.50/week (2s 6d - 5s)";
            case PROFESSIONAL:
                return "£0.50-5.00/week (10s - £1+)";
            case SCHOLARSHIP:
                return "£0.10-0.25/week";
            case NON_CONTRACT:
                return "£0/week";
            default:
                return "Unknown";
        }
    }
}
