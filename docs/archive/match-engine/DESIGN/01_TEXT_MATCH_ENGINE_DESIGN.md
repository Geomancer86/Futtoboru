# Text Match Engine Design - Phase 1

**Design Date:** 2025-01-XX  
**Repository:** futtoboru_match_engine  
**Phase:** 1 - Text-by-Text Match Engine  
**Priority:** HIGH - Foundation for 2D Engine

---

## Executive Summary

The text match engine is the **first phase** of the Futtoboru match engine development. It provides event-driven simulation with detailed text commentary, making it easier to develop than a visual engine while establishing the foundation for the 2D engine.

**Key Design Principles:**
- **Event-Driven**: Matches progress through discrete events
- **Probability-Based**: Events generated based on calculated probabilities
- **Factor-Integrated**: All factors affect event probabilities
- **Commentary-Rich**: Detailed text commentary for each event
- **Historically Accurate**: 1888 rules and conditions implemented

---

## 1. Architecture Overview

### 1.1 System Components

```
TextMatchEngine
├── MatchSimulator (Core simulation logic)
├── EventGenerator (Event probability and generation)
├── FactorCalculator (Factor calculation and weighting)
├── CommentaryGenerator (Text commentary generation)
├── StatisticsTracker (Match statistics tracking)
└── MatchReporter (Match report generation)
```

### 1.2 Data Flow

```
Input Data (Teams, Players, Environment, etc.)
    ↓
FactorCalculator (Calculate all factors)
    ↓
EventGenerator (Generate events based on probabilities)
    ↓
MatchSimulator (Simulate match minute-by-minute)
    ↓
CommentaryGenerator (Generate text commentary)
    ↓
StatisticsTracker (Track statistics)
    ↓
MatchReporter (Generate match report)
    ↓
Output (Match Result, Events, Commentary, Statistics)
```

---

## 2. Core Simulation Algorithm

### 2.1 Match Progression

**Time-Based Simulation:**
- Match divided into minutes (0-90+)
- Each minute can have 0-N events
- Events occur at specific times within minute
- Match progresses chronologically

**Algorithm:**
```java
public class TextMatchEngine {
    
    public MatchResult simulateMatch(MatchInput input) {
        // Initialize
        MatchState state = new MatchState(input);
        List<MatchEvent> events = new ArrayList<>();
        StatisticsTracker stats = new StatisticsTracker();
        
        // Pre-match calculations
        FactorCalculator factors = new FactorCalculator();
        MatchModifiers modifiers = factors.calculateAllFactors(input);
        
        // Simulate match minute-by-minute
        for (int minute = 0; minute <= 90; minute++) {
            // Calculate event probabilities for this minute
            EventProbabilities probs = calculateEventProbabilities(
                state, modifiers, minute
            );
            
            // Generate events for this minute
            List<MatchEvent> minuteEvents = generateEvents(probs, minute);
            
            // Process events
            for (MatchEvent event : minuteEvents) {
                processEvent(event, state, stats);
                events.add(event);
            }
            
            // Update match state
            updateMatchState(state, minute);
        }
        
        // Generate final result
        MatchResult result = new MatchResult(state, events, stats);
        return result;
    }
}
```

### 2.2 Event Generation

**Event Types:**
1. **Goal Events**: Goals scored (with scorer, assist, type)
2. **Shot Events**: Shots on target, off target, blocked
3. **Pass Events**: Passes completed, intercepted, failed
4. **Tackle Events**: Tackles won, lost, fouls
5. **Card Events**: Yellow cards, red cards
6. **Foul Events**: Fouls committed, free kicks awarded
7. **Corner Events**: Corners awarded
8. **Throw-In Events**: Throw-ins
9. **Offside Events**: Offside decisions (1888: 2-opponent rule)
10. **Substitution Events**: Substitutions (if rules allow)
11. **Injury Events**: Player injuries
12. **Save Events**: Goalkeeper saves

**Event Generation Algorithm:**
```java
public class EventGenerator {
    
    public List<MatchEvent> generateEvents(
        EventProbabilities probs, 
        int minute
    ) {
        List<MatchEvent> events = new ArrayList<>();
        
        // Generate events based on probabilities
        // Higher probability = more likely to occur
        
        // Goal probability (very low per minute)
        if (random.nextDouble() < probs.getGoalProbability()) {
            events.add(generateGoalEvent(probs, minute));
        }
        
        // Shot probability (moderate)
        if (random.nextDouble() < probs.getShotProbability()) {
            events.add(generateShotEvent(probs, minute));
        }
        
        // Pass probability (high, but not every minute)
        if (random.nextDouble() < probs.getPassProbability()) {
            events.add(generatePassEvent(probs, minute));
        }
        
        // Tackle probability (moderate)
        if (random.nextDouble() < probs.getTackleProbability()) {
            events.add(generateTackleEvent(probs, minute));
        }
        
        // Foul probability (low-moderate)
        if (random.nextDouble() < probs.getFoulProbability()) {
            events.add(generateFoulEvent(probs, minute));
        }
        
        // Card probability (very low)
        if (random.nextDouble() < probs.getCardProbability()) {
            events.add(generateCardEvent(probs, minute));
        }
        
        // Corner probability (low)
        if (random.nextDouble() < probs.getCornerProbability()) {
            events.add(generateCornerEvent(probs, minute));
        }
        
        // Offside probability (moderate, 1888 rules)
        if (random.nextDouble() < probs.getOffsideProbability()) {
            events.add(generateOffsideEvent(probs, minute));
        }
        
        return events;
    }
}
```

---

## 3. Factor System Integration

### 3.1 Factor Calculation

**All Factors Calculated Pre-Match:**
```java
public class FactorCalculator {
    
    public MatchModifiers calculateAllFactors(MatchInput input) {
        MatchModifiers modifiers = new MatchModifiers();
        
        // Player factors
        modifiers.addPlayerModifiers(calculatePlayerFactors(input));
        
        // Team factors
        modifiers.addTeamModifiers(calculateTeamFactors(input));
        
        // Staff factors
        modifiers.addStaffModifiers(calculateStaffFactors(input));
        
        // Environmental factors
        modifiers.addEnvironmentalModifiers(
            calculateEnvironmentalFactors(input)
        );
        
        // Contextual factors
        modifiers.addContextualModifiers(
            calculateContextualFactors(input)
        );
        
        // Historical factors
        modifiers.addHistoricalModifiers(
            calculateHistoricalFactors(input)
        );
        
        return modifiers;
    }
}
```

### 3.2 Factor Application to Events

**Event Probability Calculation:**
```java
public class EventProbabilities {
    
    public double calculateGoalProbability(
        MatchState state, 
        MatchModifiers modifiers
    ) {
        // Base probability
        double baseProb = 0.02; // 2% chance per minute
        
        // Apply modifiers
        baseProb *= modifiers.getAttackingModifier();
        baseProb *= modifiers.getPlayerQualityModifier();
        baseProb *= modifiers.getTacticalModifier();
        baseProb *= modifiers.getEnvironmentalModifier();
        baseProb *= modifiers.getContextualModifier();
        
        // Match state effects
        if (state.isLosing()) {
            baseProb *= 1.2; // More attacking when losing
        }
        if (state.isWinning()) {
            baseProb *= 0.8; // Less attacking when winning
        }
        
        return Math.min(0.10, baseProb); // Cap at 10%
    }
    
    public double calculateShotProbability(
        MatchState state,
        MatchModifiers modifiers
    ) {
        // Base probability
        double baseProb = 0.15; // 15% chance per minute
        
        // Apply modifiers (similar to goals)
        baseProb *= modifiers.getAttackingModifier();
        baseProb *= modifiers.getPlayerQualityModifier();
        // ... other modifiers
        
        return Math.min(0.30, baseProb); // Cap at 30%
    }
    
    // Similar methods for all event types
}
```

---

## 4. Commentary System

### 4.1 Commentary Generation

**Event-to-Commentary Mapping:**
```java
public class CommentaryGenerator {
    
    public String generateCommentary(MatchEvent event) {
        switch (event.getType()) {
            case GOAL:
                return generateGoalCommentary((GoalEvent) event);
            case SHOT:
                return generateShotCommentary((ShotEvent) event);
            case PASS:
                return generatePassCommentary((PassEvent) event);
            case TACKLE:
                return generateTackleCommentary((TackleEvent) event);
            // ... other event types
        }
    }
    
    private String generateGoalCommentary(GoalEvent event) {
        StringBuilder commentary = new StringBuilder();
        
        // Time
        commentary.append(String.format("%d' ", event.getMinute()));
        
        // Scorer
        commentary.append(event.getScorer().getName());
        
        // Goal type
        switch (event.getGoalType()) {
            case OPEN_PLAY:
                commentary.append(" scores from open play!");
                break;
            case FREE_KICK:
                commentary.append(" scores from a free kick!");
                break;
            case CORNER:
                commentary.append(" scores from a corner!");
                break;
            case PENALTY:
                // Not possible in 1888, but handle for future
                commentary.append(" scores from the penalty spot!");
                break;
        }
        
        // Score update
        commentary.append(String.format(
            " %d-%d",
            event.getHomeScore(),
            event.getAwayScore()
        ));
        
        return commentary.toString();
    }
}
```

### 4.2 Commentary Variety

**Multiple Commentary Templates:**
- Different phrases for same event type
- Context-aware commentary
- Historical 1888 language/style
- Engaging and varied

**Example Templates:**
```java
private static final String[] GOAL_COMMENTARIES = {
    "%s finds the back of the net!",
    "%s scores! A fine goal!",
    "%s puts it in! The crowd erupts!",
    "Goal! %s with a brilliant finish!",
    "%s scores! What a strike!"
};
```

---

## 5. Match Statistics Tracking

### 5.1 Statistics Collection

**Real-Time Statistics:**
```java
public class StatisticsTracker {
    
    // Team statistics
    private int homeShots = 0;
    private int homeShotsOnTarget = 0;
    private int homePossession = 0;
    private int homePasses = 0;
    private int homePassesCompleted = 0;
    private int homeTackles = 0;
    private int homeTacklesWon = 0;
    private int homeFouls = 0;
    private int homeCorners = 0;
    private int homeOffsides = 0;
    
    // Away team statistics (similar)
    
    // Player statistics
    private Map<Long, PlayerStatistics> playerStats = new HashMap<>();
    
    public void recordEvent(MatchEvent event) {
        switch (event.getType()) {
            case SHOT:
                recordShot((ShotEvent) event);
                break;
            case PASS:
                recordPass((PassEvent) event);
                break;
            case TACKLE:
                recordTackle((TackleEvent) event);
                break;
            // ... other event types
        }
    }
    
    private void recordShot(ShotEvent event) {
        if (event.getTeam() == Team.HOME) {
            homeShots++;
            if (event.isOnTarget()) {
                homeShotsOnTarget++;
            }
        } else {
            awayShots++;
            if (event.isOnTarget()) {
                awayShotsOnTarget++;
            }
        }
        
        // Update player statistics
        playerStats.get(event.getPlayerId())
            .incrementShots();
    }
}
```

### 5.2 Player Ratings

**Rating Calculation:**
```java
public class PlayerRatingCalculator {
    
    public float calculateRating(PlayerStatistics stats, MatchResult result) {
        float rating = 6.0f; // Base rating
        
        // Goals scored
        rating += stats.getGoals() * 1.5f;
        
        // Assists
        rating += stats.getAssists() * 1.0f;
        
        // Shots on target
        rating += stats.getShotsOnTarget() * 0.3f;
        
        // Passes completed
        rating += (stats.getPassCompletionRate() - 0.5f) * 2.0f;
        
        // Tackles won
        rating += stats.getTacklesWon() * 0.5f;
        
        // Fouls (negative)
        rating -= stats.getFouls() * 0.3f;
        
        // Cards (negative)
        rating -= stats.getYellowCards() * 0.5f;
        rating -= stats.getRedCards() * 2.0f;
        
        // Match result bonus
        if (result.isWin()) {
            rating += 0.5f;
        } else if (result.isDraw()) {
            rating += 0.2f;
        }
        
        return Math.min(10.0f, Math.max(1.0f, rating));
    }
}
```

---

## 6. Historical 1888 Rules Implementation

### 6.1 Offside Rule (2 Opponents)

**Offside Detection:**
```java
public class OffsideDetector {
    
    public boolean isOffside(Player attacker, Ball ball, MatchState state) {
        // 1888 Rule: 2 or more opponents between player and goal
        
        // Get goal line
        Line goalLine = state.getPitch().getGoalLine(attacker.getTeam());
        
        // Count opponents between attacker and goal
        int opponentsBetween = countOpponentsBetween(
            attacker.getPosition(),
            goalLine,
            state.getOpposingTeam(attacker.getTeam())
        );
        
        // Onside if 2 or more opponents
        return opponentsBetween < 2;
    }
    
    private int countOpponentsBetween(
        Point attackerPos,
        Line goalLine,
        List<Player> opponents
    ) {
        int count = 0;
        for (Player opponent : opponents) {
            if (isBetween(attackerPos, opponent.getPosition(), goalLine)) {
                count++;
            }
        }
        return count;
    }
}
```

### 6.2 No Penalty Kicks

**Foul Handling:**
```java
public class FoulHandler {
    
    public MatchEvent handleFoulInArea(FoulEvent foul, MatchState state) {
        // 1888: No penalty kicks, only indirect free kicks
        
        if (foul.isInPenaltyArea()) {
            // Indirect free kick (no direct shot on goal)
            return new IndirectFreeKickEvent(
                foul.getLocation(),
                foul.getTeam(),
                foul.getMinute()
            );
        } else {
            // Direct free kick
            return new DirectFreeKickEvent(
                foul.getLocation(),
                foul.getTeam(),
                foul.getMinute()
            );
        }
    }
}
```

### 6.3 Goalkeeper Handling Rules

**Goalkeeper Behavior:**
```java
public class GoalkeeperHandler {
    
    public boolean canHandleBall(Player goalkeeper, Ball ball, MatchState state) {
        // 1888: Can handle anywhere in own half
        
        Pitch pitch = state.getPitch();
        Point ballPos = ball.getPosition();
        Point midfieldLine = pitch.getMidfieldLine();
        
        // Check if ball is in goalkeeper's half
        if (goalkeeper.getTeam() == Team.HOME) {
            return ballPos.getY() < midfieldLine.getY();
        } else {
            return ballPos.getY() > midfieldLine.getY();
        }
    }
}
```

---

## 7. Match Report Generation

### 7.1 Report Structure

**Report Sections:**
1. **Match Summary**: Score, date, competition, attendance
2. **Match Events**: Chronological list of all events
3. **Statistics**: Team and player statistics
4. **Player Ratings**: All player ratings
5. **Tactical Analysis**: Formation, tactics, key moments
6. **Commentary**: Full match commentary

### 7.2 Report Generation

```java
public class MatchReporter {
    
    public MatchReport generateReport(MatchResult result) {
        MatchReport report = new MatchReport();
        
        // Summary
        report.setSummary(generateSummary(result));
        
        // Events
        report.setEvents(result.getEvents());
        
        // Statistics
        report.setStatistics(result.getStatistics());
        
        // Player ratings
        report.setPlayerRatings(result.getPlayerRatings());
        
        // Tactical analysis
        report.setTacticalAnalysis(generateTacticalAnalysis(result));
        
        // Full commentary
        report.setCommentary(generateFullCommentary(result));
        
        return report;
    }
    
    private String generateSummary(MatchResult result) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format(
            "Match: %s %d - %d %s\n",
            result.getHomeTeam().getName(),
            result.getHomeScore(),
            result.getAwayScore(),
            result.getAwayTeam().getName()
        ));
        
        summary.append(String.format(
            "Competition: %s\n",
            result.getCompetition().getName()
        ));
        
        summary.append(String.format(
            "Date: %s\n",
            result.getMatchDate()
        ));
        
        summary.append(String.format(
            "Attendance: %d\n",
            result.getAttendance()
        ));
        
        return summary.toString();
    }
}
```

---

## 8. Implementation Details

### 8.1 Data Structures

**MatchState:**
```java
public class MatchState {
    private int minute;
    private int homeScore;
    private int awayScore;
    private Ball ball;
    private Map<Long, Player> players;
    private Team homeTeam;
    private Team awayTeam;
    private Pitch pitch;
    private Weather weather;
    // ... other state
}
```

**MatchEvent:**
```java
public abstract class MatchEvent {
    private EventType type;
    private int minute;
    private int second; // Within minute
    private Team team;
    private Long playerId;
    private String description;
    // ... event-specific data
}
```

**MatchResult:**
```java
public class MatchResult {
    private Match match;
    private int homeScore;
    private int awayScore;
    private List<MatchEvent> events;
    private MatchStatistics statistics;
    private Map<Long, Float> playerRatings;
    private String commentary;
    // ... other result data
}
```

### 8.2 Performance Considerations

**Optimization Strategies:**
- Pre-calculate factors (don't recalculate every minute)
- Cache probability calculations
- Efficient event generation
- Stream statistics updates (don't recalculate everything)

**Target Performance:**
- Match simulation: < 1 second
- Event generation: < 100ms per minute
- Commentary generation: < 50ms per event
- Report generation: < 500ms

---

## 9. Testing Strategy

### 9.1 Unit Tests

**Test Areas:**
- Factor calculations
- Event probability calculations
- Event generation
- Commentary generation
- Statistics tracking
- Rule implementations

### 9.2 Integration Tests

**Test Areas:**
- Full match simulation
- Match result validation
- Statistics accuracy
- Historical rule compliance

### 9.3 Validation Tests

**Test Areas:**
- Score distributions match historical data
- Event frequencies match historical data
- Rules correctly implemented
- Realistic match outcomes

---

## 10. Success Criteria

### 10.1 Functional

- ✅ Matches simulate correctly
- ✅ Events generated appropriately
- ✅ Commentary is engaging
- ✅ Statistics are accurate
- ✅ Reports are comprehensive

### 10.2 Historical

- ✅ 1888 rules correctly implemented
- ✅ Historical patterns matched
- ✅ Period authenticity achieved

### 10.3 Performance

- ✅ Simulation < 1 second
- ✅ No performance issues
- ✅ Efficient resource usage

---

**Status:** ✅ Design Complete  
**Next Document:** `02_2D_TOP_DOWN_ENGINE_DESIGN.md`



