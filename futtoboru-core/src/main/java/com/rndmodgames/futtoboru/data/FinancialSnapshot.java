package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Financial Snapshot v1
 * 
 * Tracks club financial state at a specific point in time.
 * Used for financial history and charting.
 * 
 * Snapshots can be created:
 * - Daily (for detailed tracking)
 * - Weekly (for performance balance)
 * - Monthly (for long-term trends)
 * 
 * @author Geomancer86
 */
public class FinancialSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long clubId;
    private LocalDateTime snapshotDate;
    private BigDecimal balance;
    
    // Period totals (income/expenditure since last snapshot)
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expenditure = BigDecimal.ZERO;
    
    // Income breakdown
    private BigDecimal matchDayIncome = BigDecimal.ZERO;
    private BigDecimal ticketSales = BigDecimal.ZERO;
    private BigDecimal otherIncome = BigDecimal.ZERO;
    
    // Expenditure breakdown
    private BigDecimal playerWages = BigDecimal.ZERO;
    private BigDecimal staffWages = BigDecimal.ZERO;
    private BigDecimal facilityCosts = BigDecimal.ZERO;
    private BigDecimal otherExpenditure = BigDecimal.ZERO;
    
    // Period type
    private String periodType; // "DAILY", "WEEKLY", "MONTHLY"
    
    // Constructors
    public FinancialSnapshot() {
    }
    
    public FinancialSnapshot(Long clubId, LocalDateTime snapshotDate, BigDecimal balance) {
        this.clubId = clubId;
        this.snapshotDate = snapshotDate;
        this.balance = balance;
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

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getIncome() {
        return income != null ? income : BigDecimal.ZERO;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpenditure() {
        return expenditure != null ? expenditure : BigDecimal.ZERO;
    }

    public void setExpenditure(BigDecimal expenditure) {
        this.expenditure = expenditure;
    }

    public BigDecimal getMatchDayIncome() {
        return matchDayIncome != null ? matchDayIncome : BigDecimal.ZERO;
    }

    public void setMatchDayIncome(BigDecimal matchDayIncome) {
        this.matchDayIncome = matchDayIncome;
    }

    public BigDecimal getTicketSales() {
        return ticketSales != null ? ticketSales : BigDecimal.ZERO;
    }

    public void setTicketSales(BigDecimal ticketSales) {
        this.ticketSales = ticketSales;
    }

    public BigDecimal getOtherIncome() {
        return otherIncome != null ? otherIncome : BigDecimal.ZERO;
    }

    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
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

    public BigDecimal getFacilityCosts() {
        return facilityCosts != null ? facilityCosts : BigDecimal.ZERO;
    }

    public void setFacilityCosts(BigDecimal facilityCosts) {
        this.facilityCosts = facilityCosts;
    }

    public BigDecimal getOtherExpenditure() {
        return otherExpenditure != null ? otherExpenditure : BigDecimal.ZERO;
    }

    public void setOtherExpenditure(BigDecimal otherExpenditure) {
        this.otherExpenditure = otherExpenditure;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }
    
    /**
     * Calculate profit/loss for this period
     */
    public BigDecimal getProfitLoss() {
        return getIncome().subtract(getExpenditure());
    }
}
