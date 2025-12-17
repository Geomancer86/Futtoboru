package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Club Expenses v1.0
 * 
 * Tracks detailed expense breakdown for a club over a specific period.
 * Provides accounting-style precision for financial management.
 * 
 * Based on historical 1888-89 Everton FC expense records.
 * 
 * @author Geomancer86
 */
public class ClubExpenses implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long clubId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String periodType; // "WEEKLY", "MONTHLY"
    
    /**
     * Detailed Expense Categories (1888-89 Historical)
     */
    
    // Player Wages (Future: from PlayerContract system)
    private BigDecimal playerWages = BigDecimal.ZERO;
    
    // Staff Wages
    private BigDecimal staffWages = BigDecimal.ZERO; // Trainer, groundsmen, etc.
    
    // Stadium & Ground Maintenance
    private BigDecimal stadiumMaintenance = BigDecimal.ZERO; // Stands, facilities
    private BigDecimal pitchMaintenance = BigDecimal.ZERO; // Pitch upkeep
    
    // Materials & Equipment
    private BigDecimal materialsEquipment = BigDecimal.ZERO; // Boots, balls, kit
    
    // Travel Expenses
    private BigDecimal travelExpenses = BigDecimal.ZERO; // Away match travel
    
    // Matchday Expenses
    private BigDecimal matchdayExpenses = BigDecimal.ZERO; // Police, commission, referee
    
    // Administrative Expenses
    private BigDecimal administrativeExpenses = BigDecimal.ZERO; // Printing, postage, advertising
    
    // Stadium Rent
    private BigDecimal stadiumRent = BigDecimal.ZERO; // Annual rent / 52 (if rented)
    
    // Other Expenses
    private BigDecimal otherExpenses = BigDecimal.ZERO; // Medical, insurance, training, entertainment
    
    // Calculated Total
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    
    /**
     * Metadata for calculation context
     */
    private Integer rosterSize; // For scaling calculations
    private Integer stadiumCapacity; // For scaling calculations
    private BigDecimal stadiumValue; // For maintenance calculations
    
    // Constructors
    public ClubExpenses() {
    }
    
    public ClubExpenses(Long clubId, LocalDateTime periodStart, LocalDateTime periodEnd, String periodType) {
        this.clubId = clubId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.periodType = periodType;
    }
    
    /**
     * Calculate total expenses from all categories
     */
    public BigDecimal calculateTotal() {
        totalExpenses = BigDecimal.ZERO
            .add(getPlayerWages())
            .add(getStaffWages())
            .add(getStadiumMaintenance())
            .add(getPitchMaintenance())
            .add(getMaterialsEquipment())
            .add(getTravelExpenses())
            .add(getMatchdayExpenses())
            .add(getAdministrativeExpenses())
            .add(getStadiumRent())
            .add(getOtherExpenses());
        return totalExpenses;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public BigDecimal getPlayerWages() {
        return playerWages != null ? playerWages : BigDecimal.ZERO;
    }

    public void setPlayerWages(BigDecimal playerWages) {
        this.playerWages = playerWages;
    }

    public BigDecimal getStaffWages() {
        return staffWages != null ? staffWages : BigDecimal.ZERO;
    }

    public void setStaffWages(BigDecimal staffWages) {
        this.staffWages = staffWages;
    }

    public BigDecimal getStadiumMaintenance() {
        return stadiumMaintenance != null ? stadiumMaintenance : BigDecimal.ZERO;
    }

    public void setStadiumMaintenance(BigDecimal stadiumMaintenance) {
        this.stadiumMaintenance = stadiumMaintenance;
    }

    public BigDecimal getPitchMaintenance() {
        return pitchMaintenance != null ? pitchMaintenance : BigDecimal.ZERO;
    }

    public void setPitchMaintenance(BigDecimal pitchMaintenance) {
        this.pitchMaintenance = pitchMaintenance;
    }

    public BigDecimal getMaterialsEquipment() {
        return materialsEquipment != null ? materialsEquipment : BigDecimal.ZERO;
    }

    public void setMaterialsEquipment(BigDecimal materialsEquipment) {
        this.materialsEquipment = materialsEquipment;
    }

    public BigDecimal getTravelExpenses() {
        return travelExpenses != null ? travelExpenses : BigDecimal.ZERO;
    }

    public void setTravelExpenses(BigDecimal travelExpenses) {
        this.travelExpenses = travelExpenses;
    }

    public BigDecimal getMatchdayExpenses() {
        return matchdayExpenses != null ? matchdayExpenses : BigDecimal.ZERO;
    }

    public void setMatchdayExpenses(BigDecimal matchdayExpenses) {
        this.matchdayExpenses = matchdayExpenses;
    }

    public BigDecimal getAdministrativeExpenses() {
        return administrativeExpenses != null ? administrativeExpenses : BigDecimal.ZERO;
    }

    public void setAdministrativeExpenses(BigDecimal administrativeExpenses) {
        this.administrativeExpenses = administrativeExpenses;
    }

    public BigDecimal getStadiumRent() {
        return stadiumRent != null ? stadiumRent : BigDecimal.ZERO;
    }

    public void setStadiumRent(BigDecimal stadiumRent) {
        this.stadiumRent = stadiumRent;
    }

    public BigDecimal getOtherExpenses() {
        return otherExpenses != null ? otherExpenses : BigDecimal.ZERO;
    }

    public void setOtherExpenses(BigDecimal otherExpenses) {
        this.otherExpenses = otherExpenses;
    }

    public BigDecimal getTotalExpenses() {
        if (totalExpenses == null || totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return calculateTotal();
        }
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public Integer getRosterSize() {
        return rosterSize;
    }

    public void setRosterSize(Integer rosterSize) {
        this.rosterSize = rosterSize;
    }

    public Integer getStadiumCapacity() {
        return stadiumCapacity;
    }

    public void setStadiumCapacity(Integer stadiumCapacity) {
        this.stadiumCapacity = stadiumCapacity;
    }

    public BigDecimal getStadiumValue() {
        return stadiumValue;
    }

    public void setStadiumValue(BigDecimal stadiumValue) {
        this.stadiumValue = stadiumValue;
    }
}
