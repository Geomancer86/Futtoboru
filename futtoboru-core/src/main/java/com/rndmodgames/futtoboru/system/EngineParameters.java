package com.rndmodgames.futtoboru.system;

/**
 * Engine Parameters v1
 * 
 * @author Geomancer86
 */
public class EngineParameters {

    // TODO: separate friendly matches types and official matches types
    public static String [] matchTypes = new String [] {"Friendly Match"};
    
    // TODO: implement neutral venues
    public static String [] matchVenueTypes = new String [] {"Home", "Away"};
    
    //
    public static String [] matchRulesTypes = new String [] {"90 Minutes Only"};
    
    // Return match type label
    public static String getMatchType(int matchType) {
        
        return matchTypes[matchType - 1];
    }
}