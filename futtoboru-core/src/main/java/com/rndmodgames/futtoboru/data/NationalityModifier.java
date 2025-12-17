package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Nationality Modifier v1.0
 * 
 * Provides attribute modifiers based on a player's nationality.
 * Reflects historical footballing strength and characteristics of different nations.
 * 
 * Modifiers range from -1 to +3, balanced to reflect real-life footballing power.
 * 
 * Examples (1888-89):
 * - England: +2 Strength, +2 Determination (dominant, industrial)
 * - Scotland: +2 Technical, +2 Perception (skillful, tactical)
 * - Wales: +2 Courage, +1 Determination (fighting spirit)
 * - Ireland: +1 Courage, +1 Determination (less developed)
 * 
 * @author Geomancer86
 */
public class NationalityModifier implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long countryId;
    private String countryName;
    
    /**
     * Physical Attribute Modifiers
     * Range: -1 to +3 (typically 0 to +3)
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
     * Range: -1 to +3 (typically 0 to +3)
     */
    private Integer concentrationModifier = 0;
    private Integer courageModifier = 0;
    private Integer determinationModifier = 0;
    private Integer leadershipModifier = 0;
    private Integer perceptionModifier = 0;
    private Integer positioningModifier = 0;
    private Integer teamworkModifier = 0;
    
    /**
     * Historical Context
     */
    private String historicalNotes; // Footballing strength in 1888-89
    private Integer footballingStrength; // 1-10 scale (10 = strongest nation)
    
    // Constructors
    public NationalityModifier() {
    }
    
    public NationalityModifier(Long countryId, String countryName) {
        this.countryId = countryId;
        this.countryName = countryName;
    }
    
    /**
     * Get total modifier points (for balancing)
     */
    public int getTotalModifierPoints() {
        return Math.abs(strengthModifier) + Math.abs(enduranceModifier) + Math.abs(staminaModifier) +
               Math.abs(speedModifier) + Math.abs(accelerationModifier) + Math.abs(jumpingModifier) +
               Math.abs(dexterityModifier) + Math.abs(concentrationModifier) + Math.abs(courageModifier) +
               Math.abs(determinationModifier) + Math.abs(leadershipModifier) + Math.abs(perceptionModifier) +
               Math.abs(positioningModifier) + Math.abs(teamworkModifier);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
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

    public String getHistoricalNotes() {
        return historicalNotes;
    }

    public void setHistoricalNotes(String historicalNotes) {
        this.historicalNotes = historicalNotes;
    }

    public Integer getFootbalingStrength() {
        return footballingStrength != null ? footballingStrength : 5;
    }

    public void setFootbalingStrength(Integer footballingStrength) {
        this.footballingStrength = footballingStrength;
    }
}
