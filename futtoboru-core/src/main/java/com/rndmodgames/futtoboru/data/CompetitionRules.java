package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Competition Rules v1.0
 * 
 * Defines the rules for a competition, including:
 * - Points system (points for win/draw/loss)
 * - Tie-breaking criteria (order of precedence)
 * - Match format (single/double round-robin)
 * 
 * @author Geomancer86
 */
public class CompetitionRules implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Points System
    private Integer pointsForWin = 3;
    private Integer pointsForDraw = 1;
    private Integer pointsForLoss = 0;
    
    // Tie-Breaking Criteria (in order of precedence)
    public enum TieBreaker {
        POINTS,              // Total points
        GOAL_DIFFERENCE,     // Goals for - Goals against
        GOAL_AVERAGE,        // Goals for / Goals against
        GOALS_SCORED,        // Total goals scored
        GOALS_CONCEDED,      // Total goals conceded (ascending - fewer is better)
        HEAD_TO_HEAD,        // Direct match results between tied teams
        ALPHABETICAL         // Club name alphabetical order (rare fallback)
    }
    
    private List<TieBreaker> tieBreakingOrder = new ArrayList<>();
    
    // Match Format
    public enum MatchFormat {
        SINGLE_ROUND_ROBIN,  // Each team plays each other once
        DOUBLE_ROUND_ROBIN   // Each team plays each other home and away (default)
    }
    
    private MatchFormat matchFormat = MatchFormat.DOUBLE_ROUND_ROBIN;
    
    /**
     * Default constructor - creates modern rules (3-1-0, goal difference)
     */
    public CompetitionRules() {
        // Default modern rules (3-1-0 system, goal difference tie-breaker)
        tieBreakingOrder.add(TieBreaker.POINTS);
        tieBreakingOrder.add(TieBreaker.GOAL_DIFFERENCE);
        tieBreakingOrder.add(TieBreaker.GOALS_SCORED);
        tieBreakingOrder.add(TieBreaker.ALPHABETICAL); // Final fallback
    }
    
    /**
     * Create default modern rules (3 points for win, 1 for draw, 0 for loss)
     * Tie-breakers: Points → Goal Difference → Goals Scored → Alphabetical
     */
    public static CompetitionRules createDefaultRules() {
        return new CompetitionRules();
    }
    
    /**
     * Create historical rules (2 points for win, 1 for draw, 0 for loss)
     * Used in early Football League (1888-1981)
     * Tie-breakers: Points → Goal Average → Goals Scored → Alphabetical
     */
    public static CompetitionRules createHistoricalRules() {
        CompetitionRules rules = new CompetitionRules();
        rules.setPointsForWin(2);
        rules.setPointsForDraw(1);
        rules.setPointsForLoss(0);
        rules.getTieBreakingOrder().clear();
        rules.getTieBreakingOrder().add(TieBreaker.POINTS);
        rules.getTieBreakingOrder().add(TieBreaker.GOAL_AVERAGE);
        rules.getTieBreakingOrder().add(TieBreaker.GOALS_SCORED);
        rules.getTieBreakingOrder().add(TieBreaker.ALPHABETICAL);
        return rules;
    }
    
    // Getters and setters
    public Integer getPointsForWin() {
        return pointsForWin;
    }
    
    public void setPointsForWin(Integer pointsForWin) {
        this.pointsForWin = pointsForWin != null ? pointsForWin : 3;
    }
    
    public Integer getPointsForDraw() {
        return pointsForDraw;
    }
    
    public void setPointsForDraw(Integer pointsForDraw) {
        this.pointsForDraw = pointsForDraw != null ? pointsForDraw : 1;
    }
    
    public Integer getPointsForLoss() {
        return pointsForLoss;
    }
    
    public void setPointsForLoss(Integer pointsForLoss) {
        this.pointsForLoss = pointsForLoss != null ? pointsForLoss : 0;
    }
    
    public List<TieBreaker> getTieBreakingOrder() {
        return tieBreakingOrder;
    }
    
    public void setTieBreakingOrder(List<TieBreaker> tieBreakingOrder) {
        this.tieBreakingOrder = tieBreakingOrder != null ? tieBreakingOrder : new ArrayList<>();
    }
    
    public MatchFormat getMatchFormat() {
        return matchFormat;
    }
    
    public void setMatchFormat(MatchFormat matchFormat) {
        this.matchFormat = matchFormat != null ? matchFormat : MatchFormat.DOUBLE_ROUND_ROBIN;
    }
}
