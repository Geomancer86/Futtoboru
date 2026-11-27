# Match Factors System Design

**Design Date:** 2025-01-XX  
**Repository:** futtoboru_match_engine  
**Purpose:** Comprehensive design for all factors affecting match outcomes

---

## Executive Summary

The match factors system is the **foundation** of realistic match simulation. It calculates and applies all factors that affect match outcomes, from player attributes to weather conditions. This system ensures that matches are influenced by all relevant factors in a realistic and historically accurate manner.

**Key Design Principles:**
- **Comprehensive**: All factors considered
- **Weighted**: Factors have appropriate weights
- **Interactive**: Factors interact with each other
- **Historical**: 1888-specific factors included
- **Modular**: Easy to add new factors

---

## 1. Factor Categories

### 1.1 Player Factors

**Physical Attributes:**
- Speed, Acceleration, Stamina, Strength, Endurance, Jumping, Dexterity

**Mental Attributes:**
- Concentration, Courage, Determination, Leadership, Perception, Positioning, Teamwork

**Technical Attributes:**
- Passing, Kicking, Long Shots, Trick Shots, Heading, One Twos, Free Kicks, Corner Kicks, Penalty Kicks, Throw Ins, Marking, Tackling

**Goalkeeper Attributes:**
- Shot Stopping, Area Control, Punching, Hand to Hand, Rushing Out, Area Positioning

**Condition Factors:**
- Current Fitness (0-100%)
- Match Fitness/Sharpness (0-100%)
- Fatigue (0-100%)
- Injury Status

**Form Factors:**
- Recent Match Ratings
- Average Form
- Confidence Level

### 1.2 Team Factors

**Tactical Factors:**
- Formation (2-3-5, 2-2-6, etc. - 1888 formations)
- Tactical Instructions
- Style of Play
- Player Roles

**Cohesion Factors:**
- Team Cohesion (0-100)
- Player Relationships
- Time Together
- Recent Results Impact

**Morale Factors:**
- Team Morale (0-100)
- Recent Results
- League Position
- Media Pressure
- Fan Expectations

### 1.3 Staff Factors

**Manager Factors:**
- Tactical Knowledge
- Motivation Ability
- Experience
- Reputation

**Coach Factors:**
- Coaching Ability
- Specialization
- Experience
- Motivation

**Bonuses:**
- Tactical Bonus
- Training Bonus
- Motivation Bonus
- Experience Bonus

### 1.4 Environmental Factors

**Weather:**
- Rain (affects passing, ball control)
- Snow (affects all technical attributes)
- Wind (affects long passes, shots)
- Temperature (affects fatigue)

**Pitch:**
- Condition (POOR, FAIR, GOOD, EXCELLENT)
- Dimensions (length, width)
- Surface Type (grass, mud, hard)
- Quality

**Stadium:**
- Atmosphere (0-100)
- Crowd Size
- Home Advantage
- Stadium Type

### 1.5 Contextual Factors

**Travel:**
- Distance Traveled
- Travel Time
- Fatigue from Travel
- Preparation Time Lost

**Context:**
- Home/Away
- League Position
- Media Standings
- Match Importance
- Rivalry Level

### 1.6 Historical Factors

**1888 Rules:**
- Offside Rule (2 opponents)
- No Penalty Kicks
- Goalkeeper Handling Rules
- Goal Kick Rules
- Throw-In Rules

**Historical Conditions:**
- Period-Appropriate Tactics
- Historical Pitch Dimensions
- Historical Match Conditions
- Period-Appropriate Equipment

---

## 2. Factor Calculation System

### 2.1 Factor Calculator Architecture

```java
public class FactorCalculator {
    
    public MatchModifiers calculateAllFactors(MatchInput input) {
        MatchModifiers modifiers = new MatchModifiers();
        
        // Calculate each category
        modifiers.addPlayerModifiers(
            calculatePlayerFactors(input)
        );
        modifiers.addTeamModifiers(
            calculateTeamFactors(input)
        );
        modifiers.addStaffModifiers(
            calculateStaffFactors(input)
        );
        modifiers.addEnvironmentalModifiers(
            calculateEnvironmentalFactors(input)
        );
        modifiers.addContextualModifiers(
            calculateContextualFactors(input)
        );
        modifiers.addHistoricalModifiers(
            calculateHistoricalFactors(input)
        );
        
        // Combine and normalize
        return normalizeModifiers(modifiers);
    }
}
```

### 2.2 Player Factor Calculation

```java
public class PlayerFactorCalculator {
    
    public PlayerModifiers calculatePlayerFactors(MatchInput input) {
        PlayerModifiers modifiers = new PlayerModifiers();
        
        // Calculate for each team
        modifiers.setHomeTeamModifiers(
            calculateTeamPlayerFactors(input.getHomeTeam())
        );
        modifiers.setAwayTeamModifiers(
            calculateTeamPlayerFactors(input.getAwayTeam())
        );
        
        return modifiers;
    }
    
    private TeamPlayerModifiers calculateTeamPlayerFactors(Team team) {
        TeamPlayerModifiers modifiers = new TeamPlayerModifiers();
        
        // Calculate average attributes
        modifiers.setAverageSpeed(
            calculateAverageAttribute(team, PlayerAttribute.SPEED)
        );
        modifiers.setAveragePassing(
            calculateAverageAttribute(team, PlayerAttribute.PASSING)
        );
        // ... other attributes
        
        // Calculate condition effects
        modifiers.setAverageFitness(
            calculateAverageFitness(team)
        );
        modifiers.setAverageFatigue(
            calculateAverageFatigue(team)
        );
        
        // Calculate form effects
        modifiers.setAverageForm(
            calculateAverageForm(team)
        );
        
        return modifiers;
    }
}
```

### 2.3 Team Factor Calculation

```java
public class TeamFactorCalculator {
    
    public TeamModifiers calculateTeamFactors(MatchInput input) {
        TeamModifiers modifiers = new TeamModifiers();
        
        // Formation effects
        modifiers.setFormationModifier(
            calculateFormationModifier(input.getHomeFormation())
        );
        
        // Tactical effects
        modifiers.setTacticalModifier(
            calculateTacticalModifier(input.getHomeTactics())
        );
        
        // Cohesion effects
        modifiers.setCohesionModifier(
            calculateCohesionModifier(input.getHomeTeam())
        );
        
        // Morale effects
        modifiers.setMoraleModifier(
            calculateMoraleModifier(input.getHomeTeam())
        );
        
        // Repeat for away team
        // ...
        
        return modifiers;
    }
}
```

### 2.4 Environmental Factor Calculation

```java
public class EnvironmentalFactorCalculator {
    
    public EnvironmentalModifiers calculateEnvironmentalFactors(
        MatchInput input
    ) {
        EnvironmentalModifiers modifiers = new EnvironmentalModifiers();
        
        // Weather effects
        Weather weather = input.getWeather();
        switch (weather) {
            case RAIN:
                modifiers.addPassingAccuracy(-0.20);
                modifiers.addBallControl(-0.25);
                modifiers.addPlayerMovement(-0.15);
                break;
            case SNOW:
                modifiers.addPassingAccuracy(-0.40);
                modifiers.addBallControl(-0.50);
                modifiers.addPlayerMovement(-0.30);
                modifiers.addAllTechnicalAttributes(-0.30);
                break;
            case WIND:
                modifiers.addLongPassAccuracy(-0.25);
                modifiers.addShotAccuracy(-0.15);
                break;
        }
        
        // Temperature effects
        int temperature = input.getTemperature();
        if (temperature < 5) {
            modifiers.addPlayerFatigue(0.10); // Cold = more fatigue
        } else if (temperature > 25) {
            modifiers.addPlayerFatigue(0.15); // Hot = more fatigue
        }
        
        // Pitch effects
        Pitch pitch = input.getPitch();
        modifiers.addPitchModifiers(
            calculatePitchModifiers(pitch)
        );
        
        // Stadium effects
        Stadium stadium = input.getStadium();
        modifiers.addStadiumModifiers(
            calculateStadiumModifiers(stadium, input)
        );
        
        return modifiers;
    }
}
```

---

## 3. Modifier System

### 3.1 Modifier Types

**Additive Modifiers:**
- Add/subtract from base value
- Example: +10% passing accuracy

**Multiplicative Modifiers:**
- Multiply base value
- Example: ×1.15 for home advantage

**Set Modifiers:**
- Set absolute value
- Example: Set fatigue to 50%

### 3.2 Modifier Application

```java
public class MatchModifiers {
    private Map<ModifierType, Double> modifiers = new HashMap<>();
    
    public void addModifier(ModifierType type, double value) {
        modifiers.merge(type, value, Double::sum);
    }
    
    public void multiplyModifier(ModifierType type, double multiplier) {
        modifiers.merge(type, multiplier, (a, b) -> a * b);
    }
    
    public double getModifier(ModifierType type) {
        return modifiers.getOrDefault(type, 1.0);
    }
    
    public double applyModifier(double baseValue, ModifierType type) {
        return baseValue * getModifier(type);
    }
}
```

---

## 4. Factor Weights

### 4.1 Weight Configuration

**Default Weights:**
```java
public class FactorWeights {
    // Player factors (40% total)
    public static final double PLAYER_ATTRIBUTES_WEIGHT = 0.30;
    public static final double PLAYER_CONDITION_WEIGHT = 0.10;
    
    // Team factors (25% total)
    public static final double FORMATION_WEIGHT = 0.10;
    public static final double TACTICS_WEIGHT = 0.08;
    public static final double COHESION_WEIGHT = 0.05;
    public static final double MORALE_WEIGHT = 0.02;
    
    // Staff factors (10% total)
    public static final double MANAGER_WEIGHT = 0.06;
    public static final double COACH_WEIGHT = 0.04;
    
    // Environmental factors (15% total)
    public static final double WEATHER_WEIGHT = 0.08;
    public static final double PITCH_WEIGHT = 0.05;
    public static final double STADIUM_WEIGHT = 0.02;
    
    // Contextual factors (8% total)
    public static final double HOME_ADVANTAGE_WEIGHT = 0.05;
    public static final double TRAVEL_WEIGHT = 0.02;
    public static final double CONTEXT_WEIGHT = 0.01;
    
    // Historical factors (2% total)
    public static final double RULES_WEIGHT = 0.02;
}
```

### 4.2 Weight Application

```java
public class WeightedFactorCalculator {
    
    public double calculateWeightedEffect(
        List<Factor> factors,
        FactorWeights weights
    ) {
        double totalEffect = 0.0;
        
        for (Factor factor : factors) {
            double weight = weights.getWeight(factor.getType());
            double effect = factor.calculateEffect();
            totalEffect += effect * weight;
        }
        
        return totalEffect;
    }
}
```

---

## 5. Factor Interactions

### 5.1 Synergistic Effects

**Example: Good Weather + Good Pitch:**
- Both factors positive
- Combined effect > sum of individual effects
- Multiplier: 1.1x

**Example: Rain + Poor Pitch:**
- Both factors negative
- Combined effect > sum of individual effects
- Multiplier: 1.2x (worse)

### 5.2 Antagonistic Effects

**Example: High Morale + Poor Form:**
- Morale positive, form negative
- Effects partially cancel
- Net effect = (morale - form) × 0.8

### 5.3 Implementation

```java
public class FactorInteractionCalculator {
    
    public double calculateInteractionEffect(
        Factor factor1,
        Factor factor2
    ) {
        // Check if factors interact
        if (areSynergistic(factor1, factor2)) {
            return calculateSynergisticEffect(factor1, factor2);
        } else if (areAntagonistic(factor1, factor2)) {
            return calculateAntagonisticEffect(factor1, factor2);
        }
        
        // No interaction, return sum
        return factor1.getEffect() + factor2.getEffect();
    }
}
```

---

## 6. Historical 1888 Factors

### 6.1 Rule-Based Factors

**Offside Rule (2 Opponents):**
- Through ball success: +15-20%
- Offside calls: -30-40%
- Attacking opportunities: +10-15%

**No Penalty Kicks:**
- Fouls in area: Indirect free kicks only
- Goal probability from area fouls: -80%
- Different tactical considerations

**Goalkeeper Handling:**
- Goalkeeper involvement: +20-30%
- Different defensive tactics
- More varied goalkeeper play

### 6.2 Historical Conditions

**Pitch Conditions (1888):**
- More variation in pitch dimensions
- More likely poor conditions (mud, uneven)
- Less standardized surfaces

**Equipment:**
- Heavier balls
- Different boots
- Less protective equipment

---

## 7. Implementation Details

### 7.1 Data Structures

**MatchModifiers:**
```java
public class MatchModifiers {
    private PlayerModifiers playerModifiers;
    private TeamModifiers teamModifiers;
    private StaffModifiers staffModifiers;
    private EnvironmentalModifiers environmentalModifiers;
    private ContextualModifiers contextualModifiers;
    private HistoricalModifiers historicalModifiers;
    
    // Combined effects
    private Map<ModifierType, Double> combinedModifiers;
}
```

### 7.2 Performance Considerations

**Optimization:**
- Pre-calculate factors (don't recalculate every event)
- Cache calculations
- Efficient data structures
- Lazy evaluation where possible

---

## 8. Testing and Validation

### 8.1 Factor Validation

**Test Cases:**
- Each factor individually
- Factor combinations
- Factor interactions
- Weight accuracy

### 8.2 Historical Validation

**Validation:**
- Compare to historical match data
- Verify factor effects match historical patterns
- Check 1888 rule implementations

---

## 9. Success Criteria

### 9.1 Comprehensiveness

- ✅ All factors considered
- ✅ All factors weighted appropriately
- ✅ Factor interactions handled

### 9.2 Accuracy

- ✅ Factor effects realistic
- ✅ Historical accuracy maintained
- ✅ Match outcomes realistic

### 9.3 Performance

- ✅ Factor calculation efficient
- ✅ No performance impact
- ✅ Scalable system

---

**Status:** ✅ Design Complete  
**Next Document:** `04_PLAYER_PERFORMANCE_SYSTEM.md`



