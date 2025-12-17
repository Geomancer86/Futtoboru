package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Player Contract v1.0
 * 
 * Comprehensive contract system supporting multiple contract types,
 * bonuses, clauses, and negotiable terms.
 * 
 * Based on research from Football Manager, FIFA Manager, Championship Manager,
 * and historical 1888-89 data.
 * 
 * @author Geomancer86
 */
public class PlayerContract implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // ========================================
    // BASIC INFORMATION
    // ========================================
    private Long id;
    private Long playerId;
    private Long clubId;
    private Integer contractType; // AMATEUR, SEMI_PRO, PROFESSIONAL, etc.
    
    // ========================================
    // CONTRACT TERMS
    // ========================================
    private LocalDateTime startDate;
    private LocalDateTime endDate; // null for indefinite
    private Integer contractLengthMonths; // null for indefinite
    
    // ========================================
    // FINANCIAL TERMS
    // ========================================
    private BigDecimal weeklyWage = BigDecimal.ZERO;
    private BigDecimal signingBonus = BigDecimal.ZERO;
    private BigDecimal loyaltyBonus = BigDecimal.ZERO;
    private BigDecimal agentFee = BigDecimal.ZERO;
    
    // ========================================
    // PERFORMANCE BONUSES (per occurrence)
    // ========================================
    private BigDecimal appearanceFee = BigDecimal.ZERO;
    private BigDecimal unusedSubstituteFee = BigDecimal.ZERO;
    private BigDecimal goalBonus = BigDecimal.ZERO;
    private BigDecimal assistBonus = BigDecimal.ZERO;
    private BigDecimal cleanSheetBonus = BigDecimal.ZERO;
    private BigDecimal manOfTheMatchBonus = BigDecimal.ZERO;
    
    // ========================================
    // ACHIEVEMENT BONUSES (one-time)
    // ========================================
    private BigDecimal leagueWinBonus = BigDecimal.ZERO;
    private BigDecimal leagueRunnerUpBonus = BigDecimal.ZERO;
    private BigDecimal cupWinBonus = BigDecimal.ZERO;
    private BigDecimal cupRunnerUpBonus = BigDecimal.ZERO;
    private BigDecimal promotionBonus = BigDecimal.ZERO;
    private BigDecimal relegationWageDrop = BigDecimal.ZERO; // Percentage (0-100)
    
    // ========================================
    // STANDING BONUSES
    // ========================================
    private BigDecimal top3FinishBonus = BigDecimal.ZERO;
    private BigDecimal topHalfFinishBonus = BigDecimal.ZERO;
    private BigDecimal avoidRelegationBonus = BigDecimal.ZERO;
    
    // ========================================
    // SEASONAL PERFORMANCE BONUSES
    // ========================================
    private BigDecimal topGoalscorerBonus = BigDecimal.ZERO;
    private BigDecimal mostAssistsBonus = BigDecimal.ZERO;
    private BigDecimal playerOfTheYearBonus = BigDecimal.ZERO;
    private BigDecimal mostCleanSheetsBonus = BigDecimal.ZERO;
    
    // ========================================
    // BEHAVIOR BONUSES
    // ========================================
    private BigDecimal goodConductBonus = BigDecimal.ZERO; // No cards/disciplinary issues
    private BigDecimal gentlemanBonus = BigDecimal.ZERO; // Fair play, sportsmanship
    private BigDecimal partnershipBonus = BigDecimal.ZERO; // Team chemistry, assists to specific players
    
    // ========================================
    // CONTRACT CLAUSES
    // ========================================
    private BigDecimal minimumFeeRelease = null; // null = no clause
    private BigDecimal relegationReleaseFee = null;
    private BigDecimal promotionReleaseFee = null;
    private BigDecimal sellOnFeePercentage = null; // 0-100
    private Boolean hasExtensionOption = false;
    private Integer extensionOptionMonths = null;
    
    // ========================================
    // CONTRACT STATUS
    // ========================================
    private Boolean isActive = true;
    private Boolean isNegotiating = false;
    private LocalDateTime lastNegotiationDate = null;
    
    // ========================================
    // HISTORICAL TRACKING
    // ========================================
    private BigDecimal totalWagesPaid = BigDecimal.ZERO;
    private BigDecimal totalBonusesPaid = BigDecimal.ZERO;
    private Integer appearances = 0;
    private Integer goals = 0;
    private Integer assists = 0;
    private Integer cleanSheets = 0;
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    public PlayerContract() {
    }
    
    public PlayerContract(Long playerId, Long clubId, Integer contractType) {
        this.playerId = playerId;
        this.clubId = clubId;
        this.contractType = contractType;
        this.startDate = LocalDateTime.now();
    }
    
    // ========================================
    // CALCULATION METHODS
    // ========================================
    
    /**
     * Calculate weekly cost (base wage only)
     * Does not include bonuses (calculated separately)
     */
    public BigDecimal calculateWeeklyCost() {
        return weeklyWage != null ? weeklyWage : BigDecimal.ZERO;
    }
    
    /**
     * Estimate total contract value
     * Includes wages, signing bonus, loyalty bonus
     * Does not include performance bonuses (variable)
     */
    public BigDecimal calculateTotalValue() {
        BigDecimal total = BigDecimal.ZERO;
        
        // Base wages
        if (contractLengthMonths != null && weeklyWage != null) {
            // Approximate: months * 4.33 weeks per month
            BigDecimal totalWages = weeklyWage.multiply(BigDecimal.valueOf(contractLengthMonths * 4.33));
            total = total.add(totalWages);
        }
        
        // Signing bonus
        if (signingBonus != null) {
            total = total.add(signingBonus);
        }
        
        // Loyalty bonus
        if (loyaltyBonus != null) {
            total = total.add(loyaltyBonus);
        }
        
        return total;
    }
    
    /**
     * Check if contract is expired
     */
    public boolean isExpired(LocalDateTime currentDate) {
        if (endDate == null) {
            return false; // Indefinite contract
        }
        return currentDate.isAfter(endDate);
    }
    
    /**
     * Check if contract is expiring soon
     */
    public boolean isExpiringSoon(LocalDateTime currentDate, int monthsWarning) {
        if (endDate == null) {
            return false; // Indefinite contract
        }
        LocalDateTime warningDate = endDate.minusMonths(monthsWarning);
        return currentDate.isAfter(warningDate) && !isExpired(currentDate);
    }
    
    /**
     * Get remaining contract length in months
     */
    public Integer getRemainingMonths(LocalDateTime currentDate) {
        if (endDate == null) {
            return null; // Indefinite
        }
        if (currentDate.isAfter(endDate)) {
            return 0; // Expired
        }
        long months = java.time.temporal.ChronoUnit.MONTHS.between(currentDate, endDate);
        return (int) months;
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Integer getContractType() {
        return contractType;
    }

    public void setContractType(Integer contractType) {
        this.contractType = contractType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getContractLengthMonths() {
        return contractLengthMonths;
    }

    public void setContractLengthMonths(Integer contractLengthMonths) {
        this.contractLengthMonths = contractLengthMonths;
    }

    public BigDecimal getWeeklyWage() {
        return weeklyWage != null ? weeklyWage : BigDecimal.ZERO;
    }

    public void setWeeklyWage(BigDecimal weeklyWage) {
        this.weeklyWage = weeklyWage;
    }

    public BigDecimal getSigningBonus() {
        return signingBonus != null ? signingBonus : BigDecimal.ZERO;
    }

    public void setSigningBonus(BigDecimal signingBonus) {
        this.signingBonus = signingBonus;
    }

    public BigDecimal getLoyaltyBonus() {
        return loyaltyBonus != null ? loyaltyBonus : BigDecimal.ZERO;
    }

    public void setLoyaltyBonus(BigDecimal loyaltyBonus) {
        this.loyaltyBonus = loyaltyBonus;
    }

    public BigDecimal getAgentFee() {
        return agentFee != null ? agentFee : BigDecimal.ZERO;
    }

    public void setAgentFee(BigDecimal agentFee) {
        this.agentFee = agentFee;
    }

    public BigDecimal getAppearanceFee() {
        return appearanceFee != null ? appearanceFee : BigDecimal.ZERO;
    }

    public void setAppearanceFee(BigDecimal appearanceFee) {
        this.appearanceFee = appearanceFee;
    }

    public BigDecimal getUnusedSubstituteFee() {
        return unusedSubstituteFee != null ? unusedSubstituteFee : BigDecimal.ZERO;
    }

    public void setUnusedSubstituteFee(BigDecimal unusedSubstituteFee) {
        this.unusedSubstituteFee = unusedSubstituteFee;
    }

    public BigDecimal getGoalBonus() {
        return goalBonus != null ? goalBonus : BigDecimal.ZERO;
    }

    public void setGoalBonus(BigDecimal goalBonus) {
        this.goalBonus = goalBonus;
    }

    public BigDecimal getAssistBonus() {
        return assistBonus != null ? assistBonus : BigDecimal.ZERO;
    }

    public void setAssistBonus(BigDecimal assistBonus) {
        this.assistBonus = assistBonus;
    }

    public BigDecimal getCleanSheetBonus() {
        return cleanSheetBonus != null ? cleanSheetBonus : BigDecimal.ZERO;
    }

    public void setCleanSheetBonus(BigDecimal cleanSheetBonus) {
        this.cleanSheetBonus = cleanSheetBonus;
    }

    public BigDecimal getManOfTheMatchBonus() {
        return manOfTheMatchBonus != null ? manOfTheMatchBonus : BigDecimal.ZERO;
    }

    public void setManOfTheMatchBonus(BigDecimal manOfTheMatchBonus) {
        this.manOfTheMatchBonus = manOfTheMatchBonus;
    }

    public BigDecimal getLeagueWinBonus() {
        return leagueWinBonus != null ? leagueWinBonus : BigDecimal.ZERO;
    }

    public void setLeagueWinBonus(BigDecimal leagueWinBonus) {
        this.leagueWinBonus = leagueWinBonus;
    }

    public BigDecimal getLeagueRunnerUpBonus() {
        return leagueRunnerUpBonus != null ? leagueRunnerUpBonus : BigDecimal.ZERO;
    }

    public void setLeagueRunnerUpBonus(BigDecimal leagueRunnerUpBonus) {
        this.leagueRunnerUpBonus = leagueRunnerUpBonus;
    }

    public BigDecimal getCupWinBonus() {
        return cupWinBonus != null ? cupWinBonus : BigDecimal.ZERO;
    }

    public void setCupWinBonus(BigDecimal cupWinBonus) {
        this.cupWinBonus = cupWinBonus;
    }

    public BigDecimal getCupRunnerUpBonus() {
        return cupRunnerUpBonus != null ? cupRunnerUpBonus : BigDecimal.ZERO;
    }

    public void setCupRunnerUpBonus(BigDecimal cupRunnerUpBonus) {
        this.cupRunnerUpBonus = cupRunnerUpBonus;
    }

    public BigDecimal getPromotionBonus() {
        return promotionBonus != null ? promotionBonus : BigDecimal.ZERO;
    }

    public void setPromotionBonus(BigDecimal promotionBonus) {
        this.promotionBonus = promotionBonus;
    }

    public BigDecimal getRelegationWageDrop() {
        return relegationWageDrop != null ? relegationWageDrop : BigDecimal.ZERO;
    }

    public void setRelegationWageDrop(BigDecimal relegationWageDrop) {
        this.relegationWageDrop = relegationWageDrop;
    }

    public BigDecimal getTop3FinishBonus() {
        return top3FinishBonus != null ? top3FinishBonus : BigDecimal.ZERO;
    }

    public void setTop3FinishBonus(BigDecimal top3FinishBonus) {
        this.top3FinishBonus = top3FinishBonus;
    }

    public BigDecimal getTopHalfFinishBonus() {
        return topHalfFinishBonus != null ? topHalfFinishBonus : BigDecimal.ZERO;
    }

    public void setTopHalfFinishBonus(BigDecimal topHalfFinishBonus) {
        this.topHalfFinishBonus = topHalfFinishBonus;
    }

    public BigDecimal getAvoidRelegationBonus() {
        return avoidRelegationBonus != null ? avoidRelegationBonus : BigDecimal.ZERO;
    }

    public void setAvoidRelegationBonus(BigDecimal avoidRelegationBonus) {
        this.avoidRelegationBonus = avoidRelegationBonus;
    }

    public BigDecimal getTopGoalscorerBonus() {
        return topGoalscorerBonus != null ? topGoalscorerBonus : BigDecimal.ZERO;
    }

    public void setTopGoalscorerBonus(BigDecimal topGoalscorerBonus) {
        this.topGoalscorerBonus = topGoalscorerBonus;
    }

    public BigDecimal getMostAssistsBonus() {
        return mostAssistsBonus != null ? mostAssistsBonus : BigDecimal.ZERO;
    }

    public void setMostAssistsBonus(BigDecimal mostAssistsBonus) {
        this.mostAssistsBonus = mostAssistsBonus;
    }

    public BigDecimal getPlayerOfTheYearBonus() {
        return playerOfTheYearBonus != null ? playerOfTheYearBonus : BigDecimal.ZERO;
    }

    public void setPlayerOfTheYearBonus(BigDecimal playerOfTheYearBonus) {
        this.playerOfTheYearBonus = playerOfTheYearBonus;
    }

    public BigDecimal getMostCleanSheetsBonus() {
        return mostCleanSheetsBonus != null ? mostCleanSheetsBonus : BigDecimal.ZERO;
    }

    public void setMostCleanSheetsBonus(BigDecimal mostCleanSheetsBonus) {
        this.mostCleanSheetsBonus = mostCleanSheetsBonus;
    }

    public BigDecimal getGoodConductBonus() {
        return goodConductBonus != null ? goodConductBonus : BigDecimal.ZERO;
    }

    public void setGoodConductBonus(BigDecimal goodConductBonus) {
        this.goodConductBonus = goodConductBonus;
    }

    public BigDecimal getGentlemanBonus() {
        return gentlemanBonus != null ? gentlemanBonus : BigDecimal.ZERO;
    }

    public void setGentlemanBonus(BigDecimal gentlemanBonus) {
        this.gentlemanBonus = gentlemanBonus;
    }

    public BigDecimal getPartnershipBonus() {
        return partnershipBonus != null ? partnershipBonus : BigDecimal.ZERO;
    }

    public void setPartnershipBonus(BigDecimal partnershipBonus) {
        this.partnershipBonus = partnershipBonus;
    }

    public BigDecimal getMinimumFeeRelease() {
        return minimumFeeRelease;
    }

    public void setMinimumFeeRelease(BigDecimal minimumFeeRelease) {
        this.minimumFeeRelease = minimumFeeRelease;
    }

    public BigDecimal getRelegationReleaseFee() {
        return relegationReleaseFee;
    }

    public void setRelegationReleaseFee(BigDecimal relegationReleaseFee) {
        this.relegationReleaseFee = relegationReleaseFee;
    }

    public BigDecimal getPromotionReleaseFee() {
        return promotionReleaseFee;
    }

    public void setPromotionReleaseFee(BigDecimal promotionReleaseFee) {
        this.promotionReleaseFee = promotionReleaseFee;
    }

    public BigDecimal getSellOnFeePercentage() {
        return sellOnFeePercentage;
    }

    public void setSellOnFeePercentage(BigDecimal sellOnFeePercentage) {
        this.sellOnFeePercentage = sellOnFeePercentage;
    }

    public Boolean getHasExtensionOption() {
        return hasExtensionOption != null ? hasExtensionOption : false;
    }

    public void setHasExtensionOption(Boolean hasExtensionOption) {
        this.hasExtensionOption = hasExtensionOption;
    }

    public Integer getExtensionOptionMonths() {
        return extensionOptionMonths;
    }

    public void setExtensionOptionMonths(Integer extensionOptionMonths) {
        this.extensionOptionMonths = extensionOptionMonths;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsNegotiating() {
        return isNegotiating != null ? isNegotiating : false;
    }

    public void setIsNegotiating(Boolean isNegotiating) {
        this.isNegotiating = isNegotiating;
    }

    public LocalDateTime getLastNegotiationDate() {
        return lastNegotiationDate;
    }

    public void setLastNegotiationDate(LocalDateTime lastNegotiationDate) {
        this.lastNegotiationDate = lastNegotiationDate;
    }

    public BigDecimal getTotalWagesPaid() {
        return totalWagesPaid != null ? totalWagesPaid : BigDecimal.ZERO;
    }

    public void setTotalWagesPaid(BigDecimal totalWagesPaid) {
        this.totalWagesPaid = totalWagesPaid;
    }

    public BigDecimal getTotalBonusesPaid() {
        return totalBonusesPaid != null ? totalBonusesPaid : BigDecimal.ZERO;
    }

    public void setTotalBonusesPaid(BigDecimal totalBonusesPaid) {
        this.totalBonusesPaid = totalBonusesPaid;
    }

    public Integer getAppearances() {
        return appearances != null ? appearances : 0;
    }

    public void setAppearances(Integer appearances) {
        this.appearances = appearances;
    }

    public Integer getGoals() {
        return goals != null ? goals : 0;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getAssists() {
        return assists != null ? assists : 0;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getCleanSheets() {
        return cleanSheets != null ? cleanSheets : 0;
    }

    public void setCleanSheets(Integer cleanSheets) {
        this.cleanSheets = cleanSheets;
    }
}
