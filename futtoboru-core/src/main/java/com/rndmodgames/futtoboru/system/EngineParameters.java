package com.rndmodgames.futtoboru.system;

/**
 * Engine Parameters v1
 * 
 * @author Geomancer86
 */
public class EngineParameters {

    // TODO: separate friendly matches types and official matches types
    // Match type constants: 1 = FRIENDLY_MATCH, 2 = LEAGUE_MATCH
    public static String [] matchTypes = new String [] {"Friendly Match", "League Match"};
    
    // TODO: implement neutral venues
    public static String [] matchVenueTypes = new String [] {"Home", "Away"};
    
    //
    public static String [] matchRulesTypes = new String [] {"90 Minutes Only"};
    
    // Return match type label
    public static String getMatchType(int matchType) {
        
        // Validate matchType is within bounds
        if (matchType < 1 || matchType > matchTypes.length) {
            return "Unknown Match Type (" + matchType + ")";
        }
        
        return matchTypes[matchType - 1];
    }
}