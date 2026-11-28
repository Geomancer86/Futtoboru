package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
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
    
    // Competition tracking (v1.0 - Season Completion & Cup Draws)
    private Long competitionId;        // Competition this match belongs to (League or Cup)
    private Long competitionEditionId; // Edition this match belongs to (Season or Staging)
    private Integer round;             // Round number (for cups, null for leagues)
    
    // Bracket tracking (v1.0 - Complete Cup Bracket System)
    private Long parentMatch1Id;       // First parent match (winner advances to this match)
    private Long parentMatch2Id;       // Second parent match (winner advances to this match)
    private Integer bracketPosition;   // Position in round (1, 2, 3, ...)
    private String bracketPath;       // Unique path like "R1M1" (Round 1 Match 1)
    
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
    
    // Scorers tracking (v1.0 - Awards System)
    private java.util.List<Long> homeScorerIds = new java.util.ArrayList<>();
    private java.util.List<Long> awayScorerIds = new java.util.ArrayList<>();

    // G-01: the team sheet each side played with, frozen at kick-off (null until played; optional in saves)
    private TeamSheet homeSheet;
    private TeamSheet awaySheet;

    // Goal minutes (G-03): one minute (1-90, half-time after 45) per goal of that side, ascending, so goal i of the
    // side was scored by its scorer i. Empty for a goalless side and for matches played before G-03 (old saves).
    private java.util.List<Integer> homeGoalMinutes = new java.util.ArrayList<>();
    private java.util.List<Integer> awayGoalMinutes = new java.util.ArrayList<>();
    /** G-02: the men hurt in this match, per side (optional: an older save or an unplayed match has none). */
    private java.util.List<MatchInjury> homeInjuries = new java.util.ArrayList<>();
    private java.util.List<MatchInjury> awayInjuries = new java.util.ArrayList<>();

    // Basic attendance stats to avoid overselling a Match
    private Integer attendance = 0;
    
    // Match revenue (ticket sales for home club) (v1.0)
    private BigDecimal matchRevenue;

    /**
     * P2-14: the ground data id of a neutral venue (a cup semi-final or final, engine/cup/NeutralVenues); null or
     * empty = the home club's ground. Saved with the match (an older save has none: home ground).
     */
    private String venue;

    /**
     * E-17 (D19-9): every event of the match as the ECS engine saw it, in the compact saved form of
     * {@code match.ecs.events.MatchEventLog} (a short versioned Base64 string, about 2 KB a match); read back with
     * {@code engine.report.MatchEvents.of(match)} for the ticker and the report. Null for a match the instant engine
     * played, an unplayed match and an older save. Player refs in it name the slots of {@link #getHomeSheet()} /
     * {@link #getAwaySheet()}.
     */
    private String eventLog;

    // Match Type Constants
    public static final int FRIENDLY_MATCH = 1;
    public static final int LEAGUE_MATCH = 2;
    public static final int CUP_MATCH = 3;

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

    public java.util.List<MatchInjury> getHomeInjuries() {
        if (homeInjuries == null) {
            homeInjuries = new java.util.ArrayList<>();
        }
        return homeInjuries;
    }

    public void setHomeInjuries(java.util.List<MatchInjury> homeInjuries) {
        this.homeInjuries = homeInjuries;
    }

    public java.util.List<MatchInjury> getAwayInjuries() {
        if (awayInjuries == null) {
            awayInjuries = new java.util.ArrayList<>();
        }
        return awayInjuries;
    }

    public void setAwayInjuries(java.util.List<MatchInjury> awayInjuries) {
        this.awayInjuries = awayInjuries;
    }

    public java.util.List<Long> getHomeScorerIds() {
        return homeScorerIds;
    }

    public void setHomeScorerIds(java.util.List<Long> homeScorerIds) {
        this.homeScorerIds = homeScorerIds;
    }

    public java.util.List<Long> getAwayScorerIds() {
        return awayScorerIds;
    }

    public void setAwayScorerIds(java.util.List<Long> awayScorerIds) {
        this.awayScorerIds = awayScorerIds;
    }

    /**
     * G-01: the home side's team sheet as it stood at kick-off (who played, in which place, shape, style, captain);
     * null before the match is played. The line-up the match engines read (see {@link TeamSheet}).
     */
    public TeamSheet getHomeSheet() {
        return homeSheet;
    }

    public void setHomeSheet(TeamSheet homeSheet) {
        this.homeSheet = homeSheet;
    }

    /** G-01: the away side's team sheet as it stood at kick-off; null before the match is played. */
    public TeamSheet getAwaySheet() {
        return awaySheet;
    }

    public void setAwaySheet(TeamSheet awaySheet) {
        this.awaySheet = awaySheet;
    }

    /** G-03: the minute of each home goal, ascending (empty when unknown). Never null. */
    public java.util.List<Integer> getHomeGoalMinutes() {
        if (homeGoalMinutes == null) {
            homeGoalMinutes = new java.util.ArrayList<>();
        }
        return homeGoalMinutes;
    }

    public void setHomeGoalMinutes(java.util.List<Integer> homeGoalMinutes) {
        this.homeGoalMinutes = homeGoalMinutes;
    }

    /** G-03: the minute of each away goal, ascending (empty when unknown). Never null. */
    public java.util.List<Integer> getAwayGoalMinutes() {
        if (awayGoalMinutes == null) {
            awayGoalMinutes = new java.util.ArrayList<>();
        }
        return awayGoalMinutes;
    }

    public void setAwayGoalMinutes(java.util.List<Integer> awayGoalMinutes) {
        this.awayGoalMinutes = awayGoalMinutes;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }
    
    // Competition tracking getters and setters
    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public Long getCompetitionEditionId() {
        return competitionEditionId;
    }

    public void setCompetitionEditionId(Long competitionEditionId) {
        this.competitionEditionId = competitionEditionId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    /**
     * Match Revenue Getters and Setters (v1.0)
     * 
     * Revenue from ticket sales (for home club)
     */
    public BigDecimal getMatchRevenue() {
        return matchRevenue != null ? matchRevenue : BigDecimal.ZERO;
    }

    public void setMatchRevenue(BigDecimal matchRevenue) {
        this.matchRevenue = matchRevenue;
    }
    
    // Bracket tracking getters and setters (v1.0 - Complete Cup Bracket System)
    public Long getParentMatch1Id() {
        return parentMatch1Id;
    }
    
    public void setParentMatch1Id(Long parentMatch1Id) {
        this.parentMatch1Id = parentMatch1Id;
    }
    
    public Long getParentMatch2Id() {
        return parentMatch2Id;
    }
    
    public void setParentMatch2Id(Long parentMatch2Id) {
        this.parentMatch2Id = parentMatch2Id;
    }
    
    public Integer getBracketPosition() {
        return bracketPosition;
    }
    
    public void setBracketPosition(Integer bracketPosition) {
        this.bracketPosition = bracketPosition;
    }
    
    public String getBracketPath() {
        return bracketPath;
    }
    
    public void setBracketPath(String bracketPath) {
        this.bracketPath = bracketPath;
    }

    /** @return the neutral ground's data id, or null when the match is at the home club's ground (P2-14) */
    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    /** E-17: the match's events in their compact saved form, null when none (instant engine, older save). */
    public String getEventLog() {
        return eventLog;
    }

    public void setEventLog(String eventLog) {
        this.eventLog = eventLog;
    }
}
