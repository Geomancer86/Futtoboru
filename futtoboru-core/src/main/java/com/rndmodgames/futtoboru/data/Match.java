package com.rndmodgames.futtoboru.data;

import java.beans.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Match v1
 * 
 *  - Proposed or Scheduled Matches
 *  
 *  - Results are a separate object
 *  
 *      - But we can keep the aggregate score (GOALS) and the result (WIN - LOSE - DRAW) for quicker calculations / display
 *      
 *      - We can also keep the total attendance and money but we will need a separate object to keep track of tickets
 *          sold and money collection/income/revenue per day
 * 
 * TODO: research about original historic friendly matches, if they were played and happily tied or they always played penalties, etc.
 * 
 * @author Geomancer86
 */
public class Match implements Serializable {

    //
    private static final long serialVersionUID = 493790683292639017L;

    private Long id;
    
    // Avoid circular reference
    private Long homeClubId;
    private Long awayClubId;
    
    private LocalDateTime proposeDateTime;
    private LocalDateTime matchDateTime;
    
    // TODO: League and Season x League
    
    //
    private Integer matchType;
    private Integer matchRules;
    
    //
    private Boolean isProposed = false;
    private Boolean isAccepted = false;
    private Boolean isPlayed = false;
    
    // Keep it basic, for now there is no penalty kicks/score/tiebreaker
    private Integer homeGoals = 0;
    private Integer awayGoals = 0;
    
    // Basic attendance stats to avoid overselling a Match
    private Integer attendance = 0;
    
    //
    public static final int FRIENDLY_MATCH = 1;

    // 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHomeClubId() {
        return homeClubId;
    }

    public void setHomeClubId(Long homeClubId) {
        this.homeClubId = homeClubId;
    }

    public Long getAwayClubId() {
        return awayClubId;
    }

    public void setAwayClubId(Long awayClubId) {
        this.awayClubId = awayClubId;
    }

    public LocalDateTime getProposeDateTime() {
        return proposeDateTime;
    }

    public void setProposeDateTime(LocalDateTime proposeDateTime) {
        this.proposeDateTime = proposeDateTime;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }

    public Integer getMatchType() {
        return matchType;
    }

    public void setMatchType(Integer matchType) {
        this.matchType = matchType;
    }

    public Integer getMatchRules() {
        return matchRules;
    }

    public void setMatchRules(Integer matchRules) {
        this.matchRules = matchRules;
    }

    public Boolean getIsProposed() {
        return isProposed;
    }

    public void setIsProposed(Boolean isProposed) {
        this.isProposed = isProposed;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public Boolean getIsPlayed() {
        return isPlayed;
    }

    public void setIsPlayed(Boolean isPlayed) {
        this.isPlayed = isPlayed;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }
}