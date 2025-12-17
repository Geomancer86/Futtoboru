package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Region Modifier v1.0
 * 
 * Provides attribute modifiers based on a player's region (within a country).
 * Reflects local characteristics and industries that influence player attributes.
 * 
 * Modifiers are typically +1 to +2 (smaller than nationality modifiers).
 * 
 * Examples (1888-89 England):
 * - Lancashire: +1 Strength, +1 Endurance (textile mills, industrial)
 * - Birmingham: +1 Strength, +1 Courage (metalworking, foundries)
 * - Derby/Nottingham: +1 Dexterity, +1 Concentration (lace industry)
 * 
 * @author Geomancer86
 */
public class RegionModifier implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String regionName; // "LANCASHIRE", "BIRMINGHAM", etc.
    private Long countryId; // Which country this region belongs to
    
    /**
     * Physical Attribute Modifiers
     * Range: -1 to +2 (typically 0 to +2)
     */
    private Integer strengthModifier = 0;
    private Integer enduranceModifier = 0;
    private Integer staminaModifier = 0;
    private Integer speedModifier = 0;
    private Integer accelerationModifier = 0;
    private Integer jumpingModifier = 0;
    private Integer dexterityModifier = 0;
    
    /**
     * Mental Attribute Modifiers
     * Range: -1 to +2 (typically 0 to +2)
     */
    private Integer concentrationModifier = 0;
    private Integer courageModifier = 0;
    private Integer determinationModifier = 0;
    private Integer leadershipModifier = 0;
    private Integer perceptionModifier = 0;
    private Integer positioningModifier = 0;
    private Integer teamworkModifier = 0;
    
    /**
     * Applicable Cities/Clubs
     * Comma-separated list of city names or club IDs
     */
    private String applicableCities;
    
    /**
     * Historical Context
     */
    private String historicalNotes; // Description of region in 1888-89
    
    // Constructors
    public RegionModifier() {
    }
    
    public RegionModifier(String regionName, Long countryId) {
        this.regionName = regionName;
        this.countryId = countryId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Integer getStrengthModifier() {
        return strengthModifier != null ? strengthModifier : 0;
    }

    public void setStrengthModifier(Integer strengthModifier) {
        this.strengthModifier = strengthModifier;
    }

    public Integer getEnduranceModifier() {
        return enduranceModifier != null ? enduranceModifier : 0;
    }

    public void setEnduranceModifier(Integer enduranceModifier) {
        this.enduranceModifier = enduranceModifier;
    }

    public Integer getStaminaModifier() {
        return staminaModifier != null ? staminaModifier : 0;
    }

    public void setStaminaModifier(Integer staminaModifier) {
        this.staminaModifier = staminaModifier;
    }

    public Integer getSpeedModifier() {
        return speedModifier != null ? speedModifier : 0;
    }

    public void setSpeedModifier(Integer speedModifier) {
        this.speedModifier = speedModifier;
    }

    public Integer getAccelerationModifier() {
        return accelerationModifier != null ? accelerationModifier : 0;
    }

    public void setAccelerationModifier(Integer accelerationModifier) {
        this.accelerationModifier = accelerationModifier;
    }

    public Integer getJumpingModifier() {
        return jumpingModifier != null ? jumpingModifier : 0;
    }

    public void setJumpingModifier(Integer jumpingModifier) {
        this.jumpingModifier = jumpingModifier;
    }

    public Integer getDexterityModifier() {
        return dexterityModifier != null ? dexterityModifier : 0;
    }

    public void setDexterityModifier(Integer dexterityModifier) {
        this.dexterityModifier = dexterityModifier;
    }

    public Integer getConcentrationModifier() {
        return concentrationModifier != null ? concentrationModifier : 0;
    }

    public void setConcentrationModifier(Integer concentrationModifier) {
        this.concentrationModifier = concentrationModifier;
    }

    public Integer getCourageModifier() {
        return courageModifier != null ? courageModifier : 0;
    }

    public void setCourageModifier(Integer courageModifier) {
        this.courageModifier = courageModifier;
    }

    public Integer getDeterminationModifier() {
        return determinationModifier != null ? determinationModifier : 0;
    }

    public void setDeterminationModifier(Integer determinationModifier) {
        this.determinationModifier = determinationModifier;
    }

    public Integer getLeadershipModifier() {
        return leadershipModifier != null ? leadershipModifier : 0;
    }

    public void setLeadershipModifier(Integer leadershipModifier) {
        this.leadershipModifier = leadershipModifier;
    }

    public Integer getPerceptionModifier() {
        return perceptionModifier != null ? perceptionModifier : 0;
    }

    public void setPerceptionModifier(Integer perceptionModifier) {
        this.perceptionModifier = perceptionModifier;
    }

    public Integer getPositioningModifier() {
        return positioningModifier != null ? positioningModifier : 0;
    }

    public void setPositioningModifier(Integer positioningModifier) {
        this.positioningModifier = positioningModifier;
    }

    public Integer getTeamworkModifier() {
        return teamworkModifier != null ? teamworkModifier : 0;
    }

    public void setTeamworkModifier(Integer teamworkModifier) {
        this.teamworkModifier = teamworkModifier;
    }

    public String getApplicableCities() {
        return applicableCities;
    }

    public void setApplicableCities(String applicableCities) {
        this.applicableCities = applicableCities;
    }

    public String getHistoricalNotes() {
        return historicalNotes;
    }

    public void setHistoricalNotes(String historicalNotes) {
        this.historicalNotes = historicalNotes;
    }
}
