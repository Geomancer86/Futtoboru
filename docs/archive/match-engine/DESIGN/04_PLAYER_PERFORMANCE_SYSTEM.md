# Player Performance System Design

**Design Date:** 2025-01-XX  
**Repository:** futtoboru_match_engine  
**Purpose:** Design for tracking and calculating individual player performance during matches

---

## Executive Summary

The player performance system tracks and calculates how individual players perform during matches. This system is critical for:
- Realistic match simulation (better players perform better)
- Player development tracking
- Match statistics and analysis
- Player ratings and awards

**Key Design Principles:**
- **Attribute-Based**: Performance based on player attributes
- **Context-Aware**: Performance affected by match context
- **Realistic**: Performance matches real-world patterns
- **Trackable**: All performance data tracked and stored

---

## 1. Performance Calculation

### 1.1 Base Performance

**Attribute-Based Performance:**
```java
public class PlayerPerformanceCalculator {
    
    public float calculateBasePerformance(
        Player player,
        PlayerRole role,
        MatchContext context
    ) {
        // Get relevant attributes for role
        Map<PlayerAttribute, Float> roleAttributes = 
            getRelevantAttributes(player, role);
        
        // Calculate weighted average
        float totalWeight = 0.0f;
        float weightedSum = 0.0f;
        
        for (Map.Entry<PlayerAttribute, Float> entry : roleAttributes.entrySet()) {
            float attributeValue = player.getAttribute(entry.getKey());
            float weight = entry.getValue();
            
            weightedSum += attributeValue * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 50.0f;
    }
    
    private Map<PlayerAttribute, Float> getRelevantAttributes(
        Player player,
        PlayerRole role
    ) {
        Map<PlayerAttribute, Float> attributes = new HashMap<>();
        
        switch (role) {
            case GOALKEEPER:
                attributes.put(PlayerAttribute.SHOT_STOPPING, 0.25f);
                attributes.put(PlayerAttribute.AREA_CONTROL, 0.20f);
                attributes.put(PlayerAttribute.HANDLING, 0.15f);
                attributes.put(PlayerAttribute.POSITIONING, 0.15f);
                attributes.put(PlayerAttribute.REFLEXES, 0.15f);
                attributes.put(PlayerAttribute.COMMUNICATION, 0.10f);
                break;
            case DEFENDER:
                attributes.put(PlayerAttribute.TACKLING, 0.25f);
                attributes.put(PlayerAttribute.MARKING, 0.20f);
                attributes.put(PlayerAttribute.POSITIONING, 0.15f);
                attributes.put(PlayerAttribute.STRENGTH, 0.15f);
                attributes.put(PlayerAttribute.HEADING, 0.15f);
                attributes.put(PlayerAttribute.PASSING, 0.10f);
                break;
            case MIDFIELDER:
                attributes.put(PlayerAttribute.PASSING, 0.25f);
                attributes.put(PlayerAttribute.VISION, 0.20f);
                attributes.put(PlayerAttribute.STAMINA, 0.15f);
                attributes.put(PlayerAttribute.TACKLING, 0.15f);
                attributes.put(PlayerAttribute.POSITIONING, 0.15f);
                attributes.put(PlayerAttribute.SHOT, 0.10f);
                break;
            case ATTACKER:
                attributes.put(PlayerAttribute.FINISHING, 0.25f);
                attributes.put(PlayerAttribute.POSITIONING, 0.20f);
                attributes.put(PlayerAttribute.SPEED, 0.15f);
                attributes.put(PlayerAttribute.ACCELERATION, 0.15f);
                attributes.put(PlayerAttribute.COMPOSURE, 0.15f);
                attributes.put(PlayerAttribute.DRIBBLING, 0.10f);
                break;
        }
        
        return attributes;
    }
}
```

### 1.2 Condition Effects

**Fitness and Fatigue:**
```java
public class ConditionEffects {
    
    public float calculateConditionModifier(Player player) {
        float fitness = player.getCurrentFitness() / 100.0f;
        float fatigue = player.getFatigue() / 100.0f;
        float sharpness = player.getMatchFitness() / 100.0f;
        
        // Combined effect
        float conditionModifier = 
            fitness * 0.4f + 
            (1.0f - fatigue) * 0.3f + 
            sharpness * 0.3f;
        
        return conditionModifier;
    }
    
    public float applyConditionToPerformance(
        float basePerformance,
        float conditionModifier
    ) {
        // Condition affects performance linearly
        // 100% condition = 100% performance
        // 50% condition = 50% performance (minimum)
        return basePerformance * Math.max(0.5f, conditionModifier);
    }
}
```

### 1.3 Form Effects

**Recent Performance:**
```java
public class FormEffects {
    
    public float calculateFormModifier(Player player) {
        List<Float> recentRatings = player.getRecentMatchRatings();
        
        if (recentRatings.isEmpty()) {
            return 1.0f; // No form data, neutral
        }
        
        // Calculate average form
        float averageForm = recentRatings.stream()
            .mapToFloat(Float::floatValue)
            .sum() / recentRatings.size();
        
        // Normalize to 0.8-1.2 range
        // 6.0 rating = 1.0 modifier (neutral)
        // 8.0 rating = 1.2 modifier (+20%)
        // 4.0 rating = 0.8 modifier (-20%)
        float formModifier = 0.8f + (averageForm - 4.0f) / 10.0f;
        
        return Math.max(0.7f, Math.min(1.3f, formModifier));
    }
}
```

---

## 2. Match Performance Tracking

### 2.1 Event-Based Performance

**Performance from Events:**
```java
public class MatchPerformanceTracker {
    
    private Map<Long, PlayerMatchPerformance> performances = new HashMap<>();
    
    public void recordEvent(MatchEvent event) {
        Long playerId = event.getPlayerId();
        if (playerId == null) return;
        
        PlayerMatchPerformance perf = performances.getOrDefault(
            playerId,
            new PlayerMatchPerformance(playerId)
        );
        
        switch (event.getType()) {
            case GOAL:
                perf.incrementGoals();
                perf.addPerformancePoints(50); // Major positive
                break;
            case ASSIST:
                perf.incrementAssists();
                perf.addPerformancePoints(30); // Positive
                break;
            case SHOT_ON_TARGET:
                perf.incrementShotsOnTarget();
                perf.addPerformancePoints(5); // Minor positive
                break;
            case SHOT_OFF_TARGET:
                perf.incrementShotsOffTarget();
                perf.addPerformancePoints(-2); // Minor negative
                break;
            case PASS_COMPLETED:
                perf.incrementPassesCompleted();
                perf.addPerformancePoints(0.5f); // Very minor positive
                break;
            case PASS_FAILED:
                perf.incrementPassesFailed();
                perf.addPerformancePoints(-1); // Minor negative
                break;
            case TACKLE_WON:
                perf.incrementTacklesWon();
                perf.addPerformancePoints(3); // Minor positive
                break;
            case TACKLE_LOST:
                perf.incrementTacklesLost();
                perf.addPerformancePoints(-2); // Minor negative
                break;
            case FOUL:
                perf.incrementFouls();
                perf.addPerformancePoints(-5); // Negative
                break;
            case YELLOW_CARD:
                perf.incrementYellowCards();
                perf.addPerformancePoints(-10); // Major negative
                break;
            case RED_CARD:
                perf.incrementRedCards();
                perf.addPerformancePoints(-30); // Very major negative
                break;
        }
        
        performances.put(playerId, perf);
    }
}
```

### 2.2 Positional Performance

**Role-Specific Performance:**
```java
public class PositionalPerformance {
    
    public float calculatePositionalPerformance(
        Player player,
        PlayerRole role,
        PlayerMatchPerformance matchPerf
    ) {
        float baseRating = 6.0f; // Neutral rating
        
        // Role-specific calculations
        switch (role) {
            case GOALKEEPER:
                baseRating = calculateGoalkeeperRating(
                    player, matchPerf
                );
                break;
            case DEFENDER:
                baseRating = calculateDefenderRating(
                    player, matchPerf
                );
                break;
            case MIDFIELDER:
                baseRating = calculateMidfielderRating(
                    player, matchPerf
                );
                break;
            case ATTACKER:
                baseRating = calculateAttackerRating(
                    player, matchPerf
                );
                break;
        }
        
        return baseRating;
    }
    
    private float calculateGoalkeeperRating(
        Player player,
        PlayerMatchPerformance perf
    ) {
        float rating = 6.0f;
        
        // Saves
        rating += perf.getSaves() * 0.5f;
        
        // Goals conceded (negative)
        rating -= perf.getGoalsConceded() * 0.8f;
        
        // Clean sheet bonus
        if (perf.getGoalsConceded() == 0) {
            rating += 0.5f;
        }
        
        // Errors (negative)
        rating -= perf.getErrors() * 1.5f;
        
        return Math.max(1.0f, Math.min(10.0f, rating));
    }
    
    private float calculateAttackerRating(
        Player player,
        PlayerMatchPerformance perf
    ) {
        float rating = 6.0f;
        
        // Goals
        rating += perf.getGoals() * 1.5f;
        
        // Assists
        rating += perf.getAssists() * 1.0f;
        
        // Shots on target
        rating += perf.getShotsOnTarget() * 0.3f;
        
        // Shots off target (negative)
        rating -= perf.getShotsOffTarget() * 0.2f;
        
        // Pass completion
        float passRate = perf.getPassCompletionRate();
        if (passRate > 0.7f) {
            rating += (passRate - 0.7f) * 2.0f;
        } else {
            rating -= (0.7f - passRate) * 1.0f;
        }
        
        return Math.max(1.0f, Math.min(10.0f, rating));
    }
}
```

---

## 3. Player Rating System

### 3.1 Rating Calculation

**Final Match Rating:**
```java
public class PlayerRatingCalculator {
    
    public float calculateMatchRating(
        Player player,
        PlayerMatchPerformance matchPerf,
        MatchResult matchResult
    ) {
        // Base rating from performance
        float baseRating = calculateBaseRating(player, matchPerf);
        
        // Apply condition modifier
        float conditionMod = calculateConditionModifier(player);
        baseRating *= conditionMod;
        
        // Apply form modifier
        float formMod = calculateFormModifier(player);
        baseRating *= formMod;
        
        // Match result bonus/penalty
        float resultMod = calculateResultModifier(
            player, matchResult
        );
        baseRating *= resultMod;
        
        // Normalize to 1.0-10.0 range
        return Math.max(1.0f, Math.min(10.0f, baseRating));
    }
    
    private float calculateResultModifier(
        Player player,
        MatchResult result
    ) {
        boolean isWinner = result.isWinner(player.getTeam());
        boolean isDraw = result.isDraw();
        
        if (isWinner) {
            return 1.1f; // +10% for winners
        } else if (isDraw) {
            return 1.0f; // Neutral for draws
        } else {
            return 0.95f; // -5% for losers
        }
    }
}
```

### 3.2 Rating Distribution

**Expected Distribution:**
- Average rating: ~6.5
- Most players: 5.5-7.5
- Excellent performance: 8.0+
- Poor performance: 4.0-

---

## 4. Performance Statistics

### 4.1 Match Statistics

**Per-Match Statistics:**
```java
public class PlayerMatchPerformance {
    private Long playerId;
    
    // Goals and assists
    private int goals = 0;
    private int assists = 0;
    
    // Shots
    private int shotsOnTarget = 0;
    private int shotsOffTarget = 0;
    private int shotsBlocked = 0;
    
    // Passing
    private int passesCompleted = 0;
    private int passesFailed = 0;
    
    // Defensive
    private int tacklesWon = 0;
    private int tacklesLost = 0;
    private int interceptions = 0;
    private int clearances = 0;
    
    // Goalkeeper
    private int saves = 0;
    private int goalsConceded = 0;
    private int cleanSheets = 0;
    
    // Discipline
    private int fouls = 0;
    private int yellowCards = 0;
    private int redCards = 0;
    
    // Performance
    private float matchRating = 6.0f;
    private int performancePoints = 0;
}
```

### 4.2 Career Statistics

**Career Tracking:**
```java
public class PlayerCareerStatistics {
    private Long playerId;
    
    // Matches
    private int matchesPlayed = 0;
    private int matchesStarted = 0;
    private int matchesSubstituted = 0;
    
    // Goals and assists
    private int totalGoals = 0;
    private int totalAssists = 0;
    
    // Average ratings
    private float averageRating = 0.0f;
    private List<Float> recentRatings = new ArrayList<>();
    
    // Discipline
    private int totalYellowCards = 0;
    private int totalRedCards = 0;
    
    // Awards
    private int manOfTheMatch = 0;
    private int goalsOfTheMonth = 0;
}
```

---

## 5. Performance Factors

### 5.1 Attribute Impact

**How Attributes Affect Performance:**
- Higher attributes = better performance
- Attributes weighted by position/role
- Attribute interactions (e.g., Speed + Finishing)

### 5.2 Context Impact

**Match Context:**
- Important matches: Players perform better/worse (pressure)
- Rivalry matches: Increased intensity
- Home/Away: Home advantage affects performance
- Weather: Affects technical performance

### 5.3 Team Impact

**Team Performance:**
- Better team = better individual performance (support)
- Team tactics affect individual roles
- Team morale affects individual performance

---

## 6. Implementation Details

### 6.1 Data Structures

**PlayerMatchPerformance:**
```java
public class PlayerMatchPerformance implements Serializable {
    // All statistics fields
    // Performance tracking
    // Rating calculation
}
```

### 6.2 Performance Tracking

**Real-Time Tracking:**
- Track events as they occur
- Update statistics immediately
- Calculate ratings periodically
- Store for match report

---

## 7. Success Criteria

### 7.1 Accuracy

- ✅ Performance matches attributes
- ✅ Ratings realistic
- ✅ Statistics accurate

### 7.2 Realism

- ✅ Performance patterns match real football
- ✅ Rating distributions realistic
- ✅ Context affects performance

---

**Status:** ✅ Design Complete  
**Next Document:** `05_PHYSICS_AND_SIMULATION.md`



