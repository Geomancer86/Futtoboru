package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Player Profession v1.0
 * 
 * Represents a player's day job (for amateur/semi-professional players).
 * Provides attribute bonuses based on the profession's characteristics.
 * 
 * Based on historical 1888-89 data where players had to live within 6 miles
 * of the ground and often worked in local industries.
 * 
 * Examples:
 * - Quarryman: +3 Strength, +2 Endurance (very physically demanding)
 * - Schoolmaster: +2 Perception, +2 Leadership (teaching requires these skills)
 * - Railway Worker: +1 Strength, +2 Endurance, +1 Teamwork
 * 
 * @author Geomancer86
 */
public class PlayerProfession implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String description;
    
    /**
     * Profession Category
     * For grouping and regional assignment
     */
    private String category; // "TEXTILE", "RAILWAY", "MINING", "CONSTRUCTION", etc.
    
    /**
     * Physical Attribute Bonuses
     * Range: -3 to +3 (typically 0 to +3)
     */
    private Integer strengthBonus = 0;
    private Integer enduranceBonus = 0;
    private Integer staminaBonus = 0;
    private Integer speedBonus = 0;
    private Integer accelerationBonus = 0;
    private Integer jumpingBonus = 0;
    private Integer dexterityBonus = 0;
    
    /**
     * Mental Attribute Bonuses
     * Range: -3 to +3 (typically 0 to +3)
     */
    private Integer concentrationBonus = 0;
    private Integer courageBonus = 0;
    private Integer determinationBonus = 0;
    private Integer leadershipBonus = 0;
    private Integer perceptionBonus = 0;
    private Integer positioningBonus = 0;
    private Integer teamworkBonus = 0;
    
    /**
     * Historical Context
     */
    private String historicalNotes; // Description of profession in 1888-89
    private Boolean isCommon = true; // Common profession for the era
    
    /**
     * Regional Availability
     * Which regions/clubs this profession is common in
     * Format: comma-separated club IDs or region names
     */
    private String regionalAvailability; // e.g., "1,2,3" or "BLACKBURN,PRESTON"
    
    // Constructors
    public PlayerProfession() {
    }
    
    public PlayerProfession(Long id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }
    
    /**
     * Get total bonus points (for balancing)
     */
    public int getTotalBonusPoints() {
        return Math.abs(strengthBonus) + Math.abs(enduranceBonus) + Math.abs(staminaBonus) +
               Math.abs(speedBonus) + Math.abs(accelerationBonus) + Math.abs(jumpingBonus) +
               Math.abs(dexterityBonus) + Math.abs(concentrationBonus) + Math.abs(courageBonus) +
               Math.abs(determinationBonus) + Math.abs(leadershipBonus) + Math.abs(perceptionBonus) +
               Math.abs(positioningBonus) + Math.abs(teamworkBonus);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStrengthBonus() {
        return strengthBonus != null ? strengthBonus : 0;
    }

    public void setStrengthBonus(Integer strengthBonus) {
        this.strengthBonus = strengthBonus;
    }

    public Integer getEnduranceBonus() {
        return enduranceBonus != null ? enduranceBonus : 0;
    }

    public void setEnduranceBonus(Integer enduranceBonus) {
        this.enduranceBonus = enduranceBonus;
    }

    public Integer getStaminaBonus() {
        return staminaBonus != null ? staminaBonus : 0;
    }

    public void setStaminaBonus(Integer staminaBonus) {
        this.staminaBonus = staminaBonus;
    }

    public Integer getSpeedBonus() {
        return speedBonus != null ? speedBonus : 0;
    }

    public void setSpeedBonus(Integer speedBonus) {
        this.speedBonus = speedBonus;
    }

    public Integer getAccelerationBonus() {
        return accelerationBonus != null ? accelerationBonus : 0;
    }

    public void setAccelerationBonus(Integer accelerationBonus) {
        this.accelerationBonus = accelerationBonus;
    }

    public Integer getJumpingBonus() {
        return jumpingBonus != null ? jumpingBonus : 0;
    }

    public void setJumpingBonus(Integer jumpingBonus) {
        this.jumpingBonus = jumpingBonus;
    }

    public Integer getDexterityBonus() {
        return dexterityBonus != null ? dexterityBonus : 0;
    }

    public void setDexterityBonus(Integer dexterityBonus) {
        this.dexterityBonus = dexterityBonus;
    }

    public Integer getConcentrationBonus() {
        return concentrationBonus != null ? concentrationBonus : 0;
    }

    public void setConcentrationBonus(Integer concentrationBonus) {
        this.concentrationBonus = concentrationBonus;
    }

    public Integer getCourageBonus() {
        return courageBonus != null ? courageBonus : 0;
    }

    public void setCourageBonus(Integer courageBonus) {
        this.courageBonus = courageBonus;
    }

    public Integer getDeterminationBonus() {
        return determinationBonus != null ? determinationBonus : 0;
    }

    public void setDeterminationBonus(Integer determinationBonus) {
        this.determinationBonus = determinationBonus;
    }

    public Integer getLeadershipBonus() {
        return leadershipBonus != null ? leadershipBonus : 0;
    }

    public void setLeadershipBonus(Integer leadershipBonus) {
        this.leadershipBonus = leadershipBonus;
    }

    public Integer getPerceptionBonus() {
        return perceptionBonus != null ? perceptionBonus : 0;
    }

    public void setPerceptionBonus(Integer perceptionBonus) {
        this.perceptionBonus = perceptionBonus;
    }

    public Integer getPositioningBonus() {
        return positioningBonus != null ? positioningBonus : 0;
    }

    public void setPositioningBonus(Integer positioningBonus) {
        this.positioningBonus = positioningBonus;
    }

    public Integer getTeamworkBonus() {
        return teamworkBonus != null ? teamworkBonus : 0;
    }

    public void setTeamworkBonus(Integer teamworkBonus) {
        this.teamworkBonus = teamworkBonus;
    }

    public String getHistoricalNotes() {
        return historicalNotes;
    }

    public void setHistoricalNotes(String historicalNotes) {
        this.historicalNotes = historicalNotes;
    }

    public Boolean getIsCommon() {
        return isCommon != null ? isCommon : true;
    }

    public void setIsCommon(Boolean isCommon) {
        this.isCommon = isCommon;
    }

    public String getRegionalAvailability() {
        return regionalAvailability;
    }

    public void setRegionalAvailability(String regionalAvailability) {
        this.regionalAvailability = regionalAvailability;
    }
}
