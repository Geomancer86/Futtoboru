package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker;

/**
 * League Standings Manager v1.0
 * 
 * Manages league standings calculation and sorting based on competition rules.
 * Handles tie-breaking criteria including points, goal difference, goal average, etc.
 * 
 * @author Geomancer86
 */
public class LeagueStandingsManager {
    
    /**
     * Recalculate and sort league standings based on competition rules
     * 
     * @param league The league to calculate standings for
     * @return Sorted list of clubs (first place to last place)
     */
    public List<Club> calculateStandings(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return new ArrayList<>();
        }
        
        CompetitionRules rules = league.getRulesOrDefault();
        List<Club> clubs = new ArrayList<>(league.getLeagueClubs());
        
        // Remove null clubs
        clubs.removeIf(club -> club == null);
        
        // Create comparator based on rules
        Comparator<Club> comparator = createStandingsComparator(clubs, rules);
        
        // Sort clubs
        clubs.sort(comparator);
        
        return clubs;
    }
    
    /**
     * Create comparator for standings based on competition rules
     * 
     * @param allClubs All clubs in the league (needed for head-to-head calculations)
     * @param rules Competition rules to use
     * @return Comparator for sorting clubs
     */
    private Comparator<Club> createStandingsComparator(List<Club> allClubs, CompetitionRules rules) {
        Comparator<Club> comparator = null;
        
        for (TieBreaker breaker : rules.getTieBreakingOrder()) {
            Comparator<Club> nextComparator = getTieBreakerComparator(breaker, allClubs);
            
            if (comparator == null) {
                comparator = nextComparator;
            } else {
                comparator = comparator.thenComparing(nextComparator);
            }
        }
        
        // If no tie-breakers defined, use default
        if (comparator == null) {
            comparator = Comparator.comparing((Club c) -> c.getPoints() != null ? c.getPoints() : 0).reversed();
        }
        
        return comparator;
    }
    
    /**
     * Get comparator for a specific tie-breaking criterion
     * 
     * @param breaker The tie-breaking criterion
     * @param allClubs All clubs (for head-to-head calculations)
     * @return Comparator for that criterion
     */
    private Comparator<Club> getTieBreakerComparator(TieBreaker breaker, List<Club> allClubs) {
        switch (breaker) {
            case POINTS:
                return Comparator.comparing((Club c) -> c.getPoints() != null ? c.getPoints() : 0).reversed();
                
            case GOAL_DIFFERENCE:
                return Comparator.comparing((Club c) -> c.getGoalDifference() != null ? c.getGoalDifference() : 0).reversed();
                
            case GOAL_AVERAGE:
                return Comparator.comparing((Club c) -> calculateGoalAverage(c)).reversed();
                
            case GOALS_SCORED:
                return Comparator.comparing((Club c) -> c.getGoalsScored() != null ? c.getGoalsScored() : 0).reversed();
                
            case GOALS_CONCEDED:
                // Ascending order (fewer goals conceded is better)
                return Comparator.comparing((Club c) -> c.getGoalsConceded() != null ? c.getGoalsConceded() : 0);
                
            case HEAD_TO_HEAD:
                // Complex: would need to compare head-to-head records between tied teams
                // For v1.0, fall back to alphabetical as head-to-head requires match history lookup
                // TODO: Implement proper head-to-head comparison in v1.1
                return Comparator.comparing((Club c) -> c.getName() != null ? c.getName() : "");
                
            case ALPHABETICAL:
            default:
                return Comparator.comparing((Club c) -> c.getName() != null ? c.getName() : "");
        }
    }
    
    /**
     * Calculate goal average (goals for / goals against)
     * Returns 0.0 if no goals conceded (perfect defense = very high average)
     * 
     * @param club The club to calculate goal average for
     * @return Goal average (goals for divided by goals against)
     */
    private double calculateGoalAverage(Club club) {
        int goalsFor = club.getGoalsScored() != null ? club.getGoalsScored() : 0;
        int goalsAgainst = club.getGoalsConceded() != null ? club.getGoalsConceded() : 0;
        
        if (goalsAgainst == 0) {
            // Perfect defense: return a very high number if goals scored, 0 if none
            return goalsFor > 0 ? Double.MAX_VALUE : 0.0;
        }
        
        return (double) goalsFor / (double) goalsAgainst;
    }
    
    /**
     * Check if league season is complete (all matches played)
     * 
     * @param league The league to check
     * @return true if all matches in the league have been played
     */
    public boolean isLeagueComplete(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return false;
        }
        
        CompetitionRules rules = league.getRulesOrDefault();
        int numberOfClubs = league.getLeagueClubs().size();
        
        if (numberOfClubs < 2) {
            return false; // Need at least 2 clubs for a league
        }
        
        // Calculate expected matches per club based on format
        int expectedMatchesPerClub;
        if (rules.getMatchFormat() == CompetitionRules.MatchFormat.DOUBLE_ROUND_ROBIN) {
            // Each club plays each other club twice (home and away)
            expectedMatchesPerClub = (numberOfClubs - 1) * 2;
        } else {
            // Single round-robin: each club plays each other club once
            expectedMatchesPerClub = numberOfClubs - 1;
        }
        
        // Check if all clubs have played expected number of matches
        for (Club club : league.getLeagueClubs()) {
            if (club == null) continue;
            
            int matchesPlayed = club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0;
            if (matchesPlayed < expectedMatchesPerClub) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get the champion of the league (first place club)
     * 
     * @param league The league
     * @return The champion club, or null if league is incomplete or empty
     */
    public Club getChampion(League league) {
        if (league == null || !isLeagueComplete(league)) {
            return null;
        }
        
        List<Club> standings = calculateStandings(league);
        if (standings.isEmpty()) {
            return null;
        }
        
        return standings.get(0);
    }
}




