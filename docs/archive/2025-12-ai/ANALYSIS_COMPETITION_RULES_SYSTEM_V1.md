# Competition Rules System Analysis & Design v1.0

**Analysis Date:** 2025-01-XX  
**Branch:** feature/match-simulation-v1  
**Priority:** HIGH - Enables flexible, realistic competition rules

---

## Executive Summary

**Current State:** Competition rules are hardcoded throughout the codebase. Points system (3 for win, 1 for draw), tie-breakers (goal difference, goals scored), and match formats are fixed in code rather than configurable per competition.

**Goal:** Design and implement a flexible competition rules system that allows different competitions to have different point systems, tie-breaking rules, and match formats, similar to real-world football competitions that have evolved over time.

**Historical Context:** The original Football League (1888) used 2 points for a win, 1 for a draw. Modern systems use 3/1/0. Different competitions may use different tie-breaking criteria (goal difference vs goal average).

---

## 1. CURRENT STATE ANALYSIS

### 1.1 Hardcoded Rules ❌

#### Points System
- **Location:** `MatchSimulator.updateClubStatistics()`
- **Current:** Hardcoded 3 points for win, 1 for draw, 0 for loss
- **Problem:** Cannot vary by competition or historical period

```java
// Current hardcoded implementation
if (isLeagueMatch) {
    homeClub.setPoints(homeClub.getPoints() + 3); // Win
    // or
    homeClub.setPoints(homeClub.getPoints() + 1); // Draw
}
```

#### Tie-Breaking Rules
- **Location:** `LeagueDetailScreenTable.updateDynamicComponents()`
- **Current:** Hardcoded sorting: Points → Goal Difference → Goals Scored
- **Problem:** Cannot vary by competition (some use goal average instead)

```java
// Current hardcoded sorting
sortedClubs.sort(Comparator
    .comparing((Club c) -> c.getPoints()).reversed()
    .thenComparing((Club c) -> c.getGoalDifference()).reversed()
    .thenComparing((Club c) -> c.getGoalsScored()).reversed()
);
```

#### Match Format
- **Current:** Home and away fixtures (round-robin)
- **Problem:** No configuration for different formats (single round-robin, double round-robin, etc.)

### 1.2 Missing Competition Rules Structure ❌

- ❌ No `CompetitionRules` or `LeagueRules` class
- ❌ `League` class has no rules reference
- ❌ `Competition` class has no rules reference
- ❌ No way to configure rules per competition
- ❌ No historical rules support (2-point system for early years)

---

## 2. REQUIREMENTS

### 2.1 Core Requirements

#### Competition Rules Configuration
1. **Points System**
   - Points for win (typically 3, historically 2)
   - Points for draw (typically 1)
   - Points for loss (typically 0)
   - Configurable per competition

2. **Tie-Breaking Criteria**
   - Primary: Points
   - Secondary: Goal difference OR Goal average (goals for / goals against)
   - Tertiary: Goals scored
   - Quaternary: Head-to-head record
   - Quinary: Alphabetical/club ID (rare, but needed for complete ordering)
   - Configurable order per competition

3. **Match Format**
   - Single round-robin (each team plays each other once)
   - Double round-robin (each team plays each other home and away) - **CURRENT**
   - Custom formats (future)

4. **Season Structure**
   - Start date
   - End date
   - Match scheduling rules

### 2.2 Real-World Examples

#### Modern Premier League (3-1-0 System)
- Points: 3 for win, 1 for draw, 0 for loss
- Tie-breakers: Points → Goal Difference → Goals Scored → Head-to-Head → Playoff (if needed)

#### Original Football League (1888, 2-1-0 System)
- Points: 2 for win, 1 for draw, 0 for loss
- Tie-breakers: Points → Goal Average (Goals For / Goals Against) → Goals Scored

#### Some Continental Leagues
- Goal average instead of goal difference
- Head-to-head takes precedence over goal difference

---

## 3. DESIGN PROPOSAL

### 3.1 CompetitionRules Class

```java
package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Competition Rules v1.0
 * 
 * Defines the rules for a competition, including:
 * - Points system
 * - Tie-breaking criteria
 * - Match format
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
        ALPHABETICAL         // Club name alphabetical order (rare)
    }
    
    private List<TieBreaker> tieBreakingOrder = new ArrayList<>();
    
    // Match Format
    public enum MatchFormat {
        SINGLE_ROUND_ROBIN,  // Each team plays each other once
        DOUBLE_ROUND_ROBIN   // Each team plays each other home and away (current default)
    }
    
    private MatchFormat matchFormat = MatchFormat.DOUBLE_ROUND_ROBIN;
    
    // Constructor
    public CompetitionRules() {
        // Default modern rules (3-1-0, goal difference)
        tieBreakingOrder.add(TieBreaker.POINTS);
        tieBreakingOrder.add(TieBreaker.GOAL_DIFFERENCE);
        tieBreakingOrder.add(TieBreaker.GOALS_SCORED);
        tieBreakingOrder.add(TieBreaker.ALPHABETICAL); // Final fallback
    }
    
    // Historical rules constructor (2-1-0, goal average)
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
    public Integer getPointsForWin() { return pointsForWin; }
    public void setPointsForWin(Integer pointsForWin) { this.pointsForWin = pointsForWin; }
    
    public Integer getPointsForDraw() { return pointsForDraw; }
    public void setPointsForDraw(Integer pointsForDraw) { this.pointsForDraw = pointsForDraw; }
    
    public Integer getPointsForLoss() { return pointsForLoss; }
    public void setPointsForLoss(Integer pointsForLoss) { this.pointsForLoss = pointsForLoss; }
    
    public List<TieBreaker> getTieBreakingOrder() { return tieBreakingOrder; }
    public void setTieBreakingOrder(List<TieBreaker> tieBreakingOrder) { 
        this.tieBreakingOrder = tieBreakingOrder; 
    }
    
    public MatchFormat getMatchFormat() { return matchFormat; }
    public void setMatchFormat(MatchFormat matchFormat) { this.matchFormat = matchFormat; }
}
```

### 3.2 Integration Points

#### League Class
```java
public class League implements Serializable {
    // ... existing fields ...
    
    private CompetitionRules rules; // Competition rules for this league
    
    public CompetitionRules getRules() { return rules; }
    public void setRules(CompetitionRules rules) { this.rules = rules; }
    
    // Convenience method to get rules with defaults
    public CompetitionRules getRulesOrDefault() {
        if (rules == null) {
            return CompetitionRules.createDefaultRules();
        }
        return rules;
    }
}
```

#### Competition Class (for future cup rules)
```java
public class Competition implements Serializable {
    // ... existing fields ...
    
    private CompetitionRules rules; // Competition rules
    
    public CompetitionRules getRules() { return rules; }
    public void setRules(CompetitionRules rules) { this.rules = rules; }
}
```

### 3.3 LeagueStandingsManager Class

```java
package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker;

/**
 * League Standings Manager v1.0
 * 
 * Manages league standings calculation and sorting based on competition rules
 */
public class LeagueStandingsManager {
    
    /**
     * Recalculate and sort league standings based on competition rules
     */
    public List<Club> calculateStandings(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return new ArrayList<>();
        }
        
        CompetitionRules rules = league.getRulesOrDefault();
        List<Club> clubs = new ArrayList<>(league.getLeagueClubs());
        
        // Create comparator based on rules
        Comparator<Club> comparator = createStandingsComparator(clubs, rules);
        
        // Sort clubs
        clubs.sort(comparator);
        
        return clubs;
    }
    
    /**
     * Create comparator for standings based on competition rules
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
        
        return comparator;
    }
    
    /**
     * Get comparator for a specific tie-breaking criterion
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
                return Comparator.comparing((Club c) -> c.getGoalsConceded() != null ? c.getGoalsConceded() : 0); // Ascending
                
            case HEAD_TO_HEAD:
                // Complex: would need to compare head-to-head records between tied teams
                // For v1.0, fall back to alphabetical
                return Comparator.comparing((Club c) -> c.getName() != null ? c.getName() : "");
                
            case ALPHABETICAL:
            default:
                return Comparator.comparing((Club c) -> c.getName() != null ? c.getName() : "");
        }
    }
    
    /**
     * Calculate goal average (goals for / goals against)
     * Returns 0.0 if no goals conceded (perfect defense)
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
     */
    public boolean isLeagueComplete(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return false;
        }
        
        CompetitionRules rules = league.getRulesOrDefault();
        int numberOfClubs = league.getLeagueClubs().size();
        
        // Calculate expected matches per club
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
            int matchesPlayed = club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0;
            if (matchesPlayed < expectedMatchesPerClub) {
                return false;
            }
        }
        
        return true;
    }
}
```

### 3.4 MatchSimulator Integration

```java
// In MatchSimulator.updateClubStatistics()

// Get competition rules (if league match)
CompetitionRules rules = null;
if (isLeagueMatch) {
    // Find league for this match
    League league = findLeagueForMatch(match);
    if (league != null) {
        rules = league.getRulesOrDefault();
    }
}

// Award points based on rules
if (rules != null) {
    if (homeScore > awayScore) {
        homeClub.setPoints(homeClub.getPoints() + rules.getPointsForWin());
        awayClub.setPoints(awayClub.getPoints() + rules.getPointsForLoss());
    } else if (awayScore > homeScore) {
        awayClub.setPoints(awayClub.getPoints() + rules.getPointsForWin());
        homeClub.setPoints(homeClub.getPoints() + rules.getPointsForLoss());
    } else {
        homeClub.setPoints(homeClub.getPoints() + rules.getPointsForDraw());
        awayClub.setPoints(awayClub.getPoints() + rules.getPointsForDraw());
    }
}
```

---

## 4. IMPLEMENTATION PLAN

### Phase 1: Create Competition Rules Data Structure
1. Create `CompetitionRules` class
2. Add rules field to `League` class
3. Add convenience methods and default rules

**Time:** 1-2 hours

### Phase 2: Create League Standings Manager
1. Create `LeagueStandingsManager` class
2. Implement standings calculation with rules
3. Implement league completion detection

**Time:** 2-3 hours

### Phase 3: Integrate Rules into Match Simulation
1. Update `MatchSimulator` to use rules for points
2. Add league lookup helper method
3. Test with different rule sets

**Time:** 1-2 hours

### Phase 4: Update League Detail Screen
1. Use `LeagueStandingsManager` for standings display
2. Show competition rules in league detail view
3. Update UI to use rules-based sorting

**Time:** 1-2 hours

### Phase 5: Default Rules Assignment
1. Assign default modern rules to existing leagues
2. Add historical rules option for early years
3. Test with both rule sets

**Time:** 1 hour

**Total Estimated Time:** 6-10 hours (1-1.5 days)

---

## 5. DEFAULT RULES CONFIGURATION

### Modern Rules (Default)
- Points: 3 for win, 1 for draw, 0 for loss
- Tie-breakers: Points → Goal Difference → Goals Scored → Alphabetical
- Format: Double round-robin

### Historical Rules (1888 Football League)
- Points: 2 for win, 1 for draw, 0 for loss
- Tie-breakers: Points → Goal Average → Goals Scored → Alphabetical
- Format: Double round-robin

---

## 6. FUTURE ENHANCEMENTS

### v1.1+
- Head-to-head tie-breaker implementation (complex, requires match history lookup)
- Custom tie-breaking rules per competition
- Competition-specific point systems (e.g., bonus points)
- Playoff systems for tied positions
- Relegation/promotion rules configuration

---

## 7. TESTING STRATEGY

### Unit Tests
- Test points calculation with different rule sets
- Test tie-breaking comparator creation
- Test goal average calculation
- Test league completion detection

### Integration Tests
- Test full standings calculation with modern rules
- Test full standings calculation with historical rules
- Test league completion detection
- Test match simulation with rules

### Manual Tests
- Create league with modern rules, simulate matches, verify standings
- Create league with historical rules, simulate matches, verify standings
- Verify league detail screen shows correct standings
- Verify rules are displayed correctly

---

## 8. FILES TO CREATE/MODIFY

### New Files
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/data/CompetitionRules.java`
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/simulation/LeagueStandingsManager.java`

### Modified Files
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/data/League.java` - Add rules field
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/simulation/MatchSimulator.java` - Use rules for points
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/competitions/LeagueDetailScreenTable.java` - Use LeagueStandingsManager

---

**Priority:** 🟡 HIGH  
**Status:** 📋 DESIGNED - Ready for implementation  
**Dependencies:** Match simulation system (in progress)


