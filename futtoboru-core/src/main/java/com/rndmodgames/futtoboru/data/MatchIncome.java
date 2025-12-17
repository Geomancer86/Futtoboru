package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Match Income v1
 * 
 * Tracks income generated from a specific match.
 * This is separate from the Match object to allow detailed tracking
 * and historical analysis.
 * 
 * @author Geomancer86
 */
public class MatchIncome implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long matchId;
    private Long clubId; // Home club (the one receiving income)
    private BigDecimal ticketRevenue;
    private Integer attendance;
    private LocalDateTime matchDate;
    private Integer matchType; // Match.FRIENDLY_MATCH, Match.LEAGUE_MATCH, Match.CUP_MATCH
    
    // Additional revenue sources (future expansion)
    private BigDecimal hospitalityRevenue = BigDecimal.ZERO;
    private BigDecimal merchandiseRevenue = BigDecimal.ZERO;
    
    // Constructors
    public MatchIncome() {
    }
    
    public MatchIncome(Long matchId, Long clubId, BigDecimal ticketRevenue, 
                      Integer attendance, LocalDateTime matchDate, Integer matchType) {
        this.matchId = matchId;
        this.clubId = clubId;
        this.ticketRevenue = ticketRevenue;
        this.attendance = attendance;
        this.matchDate = matchDate;
        this.matchType = matchType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public BigDecimal getTicketRevenue() {
        return ticketRevenue != null ? ticketRevenue : BigDecimal.ZERO;
    }

    public void setTicketRevenue(BigDecimal ticketRevenue) {
        this.ticketRevenue = ticketRevenue;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public Integer getMatchType() {
        return matchType;
    }

    public void setMatchType(Integer matchType) {
        this.matchType = matchType;
    }

    public BigDecimal getHospitalityRevenue() {
        return hospitalityRevenue != null ? hospitalityRevenue : BigDecimal.ZERO;
    }

    public void setHospitalityRevenue(BigDecimal hospitalityRevenue) {
        this.hospitalityRevenue = hospitalityRevenue;
    }

    public BigDecimal getMerchandiseRevenue() {
        return merchandiseRevenue != null ? merchandiseRevenue : BigDecimal.ZERO;
    }

    public void setMerchandiseRevenue(BigDecimal merchandiseRevenue) {
        this.merchandiseRevenue = merchandiseRevenue;
    }
    
    /**
     * Get total revenue from all sources
     */
    public BigDecimal getTotalRevenue() {
        return getTicketRevenue()
            .add(getHospitalityRevenue())
            .add(getMerchandiseRevenue());
    }
}
