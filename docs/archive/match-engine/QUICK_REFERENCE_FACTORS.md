# Match Engine - Quick Reference: Factors & Calculations

**Purpose:** Quick reference guide for match engine factors and calculations  
**Use Case:** Implementation reference, testing, validation

---

## Team Strength Calculation

### Base Formula
```
TeamStrength = Average(PlayerStrength for all 11 players)
```

### Player Strength
```
PlayerStrength = 
    (PhysicalAttributes * 0.3) +
    (MentalAttributes * 0.3) +
    (TechnicalAttributes * 0.3) +
    (PositionRating * 0.1)
```

### Condition Modifier
```
PlayerStrength *= (Fitness / 100) * (MatchFitness / 100) * (1 - Fatigue / 200)
```

### Physical Attributes
- Acceleration, Speed, Strength, Stamina, Endurance, Jumping

### Mental Attributes
- Concentration, Determination, Positioning, Teamwork, Perception, Courage

### Technical Attributes
- Position-dependent (Passing, Kicking, Tackling, Marking, Heading, etc.)

---

## Home Advantage

### Base Home Advantage
```
HomeAdvantage = 1.15 (15% boost)
```

### Crowd Influence
```
CrowdBonus = (Attendance / StadiumCapacity) * Atmosphere
HomeAdvantage *= (1 + CrowdBonus * 0.1)
```

### Travel Fatigue (Away Team)
```
TravelFatigue = f(Distance, RestDays)
AwayTeamStrength *= (1 - TravelFatigue * 0.1)
```

### Final Calculation
```
HomeTeamFinalStrength = HomeTeamStrength * HomeAdvantage
AwayTeamFinalStrength = AwayTeamStrength * (1 - TravelFatigue)
```

---

## Staff Bonuses

### Manager Bonus
```
TacticalBonus = ManagerTacticalKnowledge / 100
TeamStrength *= (1 + TacticalBonus * 0.05)  // Up to 5% boost
```

### Coaching Staff Bonus
```
TrainingBonus = Average(CoachingStaffAbility) / 100
PlayerCondition *= (1 + TrainingBonus * 0.03)  // Up to 3% boost
```

### Medical Staff Bonus
```
MedicalBonus = MedicalStaffAbility / 100
InjuryRecoverySpeed *= (1 + MedicalBonus * 0.1)
InjuryPrevention *= (1 + MedicalBonus * 0.05)
```

---

## Environmental Factors

### Weather: Rain
```
TechnicalAttributes *= 0.95  // 5% reduction
BallControl *= 0.90           // 10% reduction
PassingAccuracy *= 0.92       // 8% reduction
```

### Weather: Snow
```
TechnicalAttributes *= 0.85  // 15% reduction
BallControl *= 0.80           // 20% reduction
Speed *= 0.90                // 10% reduction
```

### Weather: Wind
```
LongPassAccuracy *= 0.85     // 15% reduction
ShotAccuracy *= 0.90         // 10% reduction
```

### Pitch Condition: Poor
```
TechnicalAttributes *= 0.90   // 10% reduction
BallControl *= 0.85            // 15% reduction
InjuryRisk *= 1.2              // 20% increase
```

### Pitch Condition: Excellent
```
TechnicalAttributes *= 1.02   // 2% boost
BallControl *= 1.03            // 3% boost
```

### Temperature: Cold (< 5°C)
```
PhysicalAttributes *= 0.95     // 5% reduction
InjuryRisk *= 1.1              // 10% increase
```

### Temperature: Hot (> 30°C)
```
Stamina *= 0.90                // 10% reduction
FatigueAccumulation *= 1.2     // 20% increase
```

---

## Referee Influence

### Referee Strictness
```
RefereeStrictness = 0.5 to 1.5 (0.5 = lenient, 1.5 = strict)

FoulProbability *= RefereeStrictness
CardProbability *= RefereeStrictness
PenaltyProbability *= RefereeStrictness
```

### Home Bias
```
HomeBias = -0.1 to 0.1 (-0.1 = favors away, 0.1 = favors home)

HomeTeamFoulProbability *= (1 - HomeBias)
AwayTeamFoulProbability *= (1 + HomeBias)
```

### Referee Experience
```
RefereeExperience = 0 to 100

DecisionAccuracy = 0.7 + (RefereeExperience / 100) * 0.3
```

---

## Tactical System

### Formation Impact
```
FormationStrength = f(Formation, PlayerPositions, TacticalInstructions)

// Example: 2-3-5 (Pyramid)
DefensiveStrength = (Defenders * 1.0) + (Midfielders * 0.5)
AttackingStrength = (Forwards * 1.0) + (Midfielders * 0.5)
```

### Mentality Impact
```
Defensive:
    DefensiveStrength *= 1.1
    AttackingStrength *= 0.9

Balanced:
    No change

Attacking:
    DefensiveStrength *= 0.9
    AttackingStrength *= 1.1
```

### Tempo Impact
```
Slow:
    FatigueAccumulation *= 0.8
    TechnicalAttributes *= 1.05

Normal:
    No change

Fast:
    FatigueAccumulation *= 1.2
    PhysicalAttributes *= 1.05
```

### Width Impact
```
Narrow:
    CentralPlay *= 1.1
    WidePlay *= 0.9

Normal:
    No change

Wide:
    CentralPlay *= 0.9
    WidePlay *= 1.1
```

---

## League Standings Pressure

### Relegation Battle
```
if (TeamPosition near RelegationZone):
    Pressure = High
    DeterminationBonus = 1.05  // 5% boost
    NervousnessPenalty = 1.02  // 2% penalty
```

### Title Race
```
if (TeamPosition near Top):
    Pressure = High
    DeterminationBonus = 1.03  // 3% boost
    NervousnessPenalty = 1.01  // 1% penalty
```

### Mid-Table
```
Pressure = Low
No significant bonuses/penalties
```

---

## Media Pressure

### High Expectations, Poor Form
```
Pressure = High
NervousnessPenalty = 1.05  // 5% penalty
```

### Low Expectations, Good Form
```
ConfidenceBonus = 1.03     // 3% boost
```

---

## Fatigue and Condition

### Match Fatigue
```
FatigueAccumulation = f(PlayerStamina, MatchIntensity, MinutesPlayed)

// Per minute:
FatigueIncrease = (100 - Stamina) / 100 * MatchIntensity * 0.1

// Fatigue affects performance:
PerformanceModifier = 1 - (Fatigue / 200)  // Max 50% reduction
```

### Travel Fatigue
```
TravelFatigue = f(Distance, RestDays, TravelMethod)

// Example:
TravelFatigue = (Distance / 100) * (1 / RestDays) * TravelMethodFactor

// TravelMethodFactor:
// Train (1888): 1.0
// Modern: 0.5 (faster, more comfortable)
```

### Recovery
```
RecoveryPerDay = f(Rest, MedicalStaff, TrainingIntensity)

// Full rest:
RecoveryPerDay = 20% (5 days to full recovery)

// With medical staff:
RecoveryPerDay *= (1 + MedicalBonus * 0.2)  // Up to 20% faster
```

---

## Historical Rules (1888)

### Offside Rule
```
OffsidePlayersRequired = 3  // (Modern: 2)
```

### Substitutions
```
MaxSubstitutions = 0  // (Modern: 3)
```

### Card System
```
CardSystemActive = false  // (Modern: true)
// But referee can still send off
```

### Penalty Kicks
```
PenaltyKicksAllowed = false  // (1891+: true)
```

### Pitch Dimensions
```
Length: 100-130 yards (91-119 meters)
Width: 50-100 yards (46-91 meters)
// Variable, not standardized
```

---

## Score Generation

### Base Distribution
```
30% chance: 0 goals
30% chance: 1 goal
25% chance: 2 goals
10% chance: 3 goals
5% chance: 4-5 goals
```

### Strength Adjustment
```
if (StrengthRatio > 0.6):
    baseGoals += 1  // Strong team bonus

if (StrengthRatio < 0.4):
    baseGoals = max(0, baseGoals - 1)  // Weak team penalty
```

### Cap
```
MaxGoals = 5  // (v1.0)
```

---

## Event Probabilities

### Goal Probability
```
GoalProbability = f(TeamStrength, OpponentStrength, Position, Tactics)
```

### Shot Probability
```
ShotProbability = f(TeamStrength, Position, Tactics, MatchState)
```

### Card Probability
```
CardProbability = f(RefereeStrictness, PlayerAggression, FoulType)
```

### Injury Probability
```
InjuryProbability = f(PlayerCondition, PitchCondition, MatchIntensity, TackleType)
```

---

## Performance Targets

### Text Engine
```
MatchSimulation: < 1 second
CommentaryGeneration: < 100ms
EventGeneration: < 50ms per event
```

### 2D Engine
```
FrameRate: 30-60 FPS
PhysicsUpdate: 60 updates/second
Rendering: < 16ms per frame (60 FPS)
```

---

## Implementation Notes

### Randomness
- Use seeded random number generator
- Reproducible results with same seed
- Different seed = different match outcome

### Factor Order
1. Calculate base team strengths
2. Apply condition modifiers
3. Apply staff bonuses
4. Apply environmental factors
5. Apply tactical modifiers
6. Apply context factors (pressure, etc.)
7. Apply home advantage
8. Apply travel fatigue
9. Generate events based on final strengths

### Caching
- Cache calculations where possible
- Recalculate only when factors change
- Optimize for performance

---

**Document Version:** 1.0  
**Last Updated:** 2025-01-XX



