# Text Commentary Engine Design v1.0

**Phase:** 1 (Text Commentary Engine)  
**Status:** DESIGN  
**Priority:** High (Foundation for all phases)

---

## Overview

The Text Commentary Engine generates rich, engaging text-based commentary for football matches. This is Phase 1 of the match engine and provides the foundation for later phases (2D and 3D visualization will use the same event system).

## Goals

- Generate **vivid, engaging commentary** that brings matches to life
- Provide **time-stamped event timeline** for match narrative
- Use **actual player names** in commentary
- Support **variety** (multiple ways to describe similar events)
- Reflect **match context** (rivalry, importance, etc.)
- Use **1888-appropriate language** and terminology

## Architecture

### Commentary Generation Flow

```
Match Event → Event Type Identification → Commentary Template Selection → 
Player Name Lookup → Context Application → Text Generation → Commentary Entry
```

### Components

1. **CommentaryGenerator** - Main entry point
2. **EventCommentary** - Maps events to commentary templates
3. **CommentaryTemplates** - Stores commentary text templates
4. **CommentaryContext** - Provides context (player names, match situation, etc.)

## Commentary Event Types

### Core Events

- **PASS** - Player passes the ball
- **SHOT** - Player attempts shot on goal
- **GOAL** - Goal scored
- **SAVE** - Goalkeeper saves shot
- **TACKLE** - Player makes tackle
- **FOUL** - Foul committed
- **YELLOW_CARD** - Yellow card shown
- **RED_CARD** - Red card shown
- **CORNER** - Corner kick
- **FREE_KICK** - Free kick
- **OFFSIDE** - Offside call
- **THROW_IN** - Throw-in
- **SUBSTITUTION** - Player substitution (if allowed)
- **INJURY** - Player injury
- **HALF_TIME** - Half-time
- **FULL_TIME** - Full-time
- **MISSED_CHANCE** - Clear scoring opportunity missed

### Phase Events (for flow)

- **ATTACK_BUILD_UP** - Team building up attack
- **COUNTER_ATTACK** - Counter-attack
- **DEFENSIVE_CLEARANCE** - Defensive clearance
- **POSSESSION_CHANGE** - Ball possession changes teams

## Commentary Templates

### Template Structure

```java
public class CommentaryTemplate {
    private String template; // Text template with placeholders
    private List<String> variations; // Multiple variations
    private CommentaryStyle style; // NORMAL, EXCITED, DRAMATIC, etc.
    private Float excitementLevel; // 0.0-1.0 (affects template selection)
}
```

### Template Placeholders

- `{player}` - Player name
- `{team}` - Team name
- `{minute}` - Match minute
- `{score}` - Current score
- `{position}` - Field position (left wing, center, penalty area, etc.)

### Example Templates

#### Goal Commentary

**Templates:**
- "GOAL! {player} finds the net! {score}"
- "{player} scores! The crowd erupts! {score}"
- "It's in! {player} puts it away! {score}"
- "What a strike from {player}! {score}"

**Variations by context:**
- **Early goal (0-15 min):** "Early goal! {player} gives {team} the lead!"
- **Equalizer:** "{player} levels the scores! {score}"
- **Winning goal:** "{player} puts {team} ahead! {score}"
- **Late goal (75+ min):** "Late drama! {player} scores! {score}"

#### Shot Commentary

**Templates:**
- "{player} shoots... wide of the goal!"
- "{player} attempts a shot, but it's saved by the goalkeeper!"
- "{player} goes for goal, but the ball flies over the bar!"
- "Powerful strike from {player}, but it's blocked!"
- "{player} with a shot on target, but the goalkeeper makes a fine save!"

#### Pass Commentary

**Templates:**
- "{player} passes to {targetPlayer}"
- "{player} finds {targetPlayer} with a through ball"
- "Good pass from {player} to {targetPlayer}"
- "{player} plays it out wide to {targetPlayer}"

#### Tackle Commentary

**Templates:**
- "{player} makes a strong tackle"
- "{player} wins the ball with a well-timed tackle"
- "Excellent tackle from {player} to break up the attack"
- "{player} dispossesses {opponent} with a clean tackle"

## Commentary Generation Algorithm

```java
public CommentaryEntry generateCommentary(MatchEvent event, MatchContext context) {
    // 1. Identify event type
    EventType type = event.getType();
    
    // 2. Get commentary templates for this event type
    List<CommentaryTemplate> templates = getTemplatesForEvent(type);
    
    // 3. Filter templates by context (match situation, excitement level, etc.)
    templates = filterByContext(templates, context, event);
    
    // 4. Select random template from filtered list
    CommentaryTemplate template = selectRandomTemplate(templates);
    
    // 5. Fill in placeholders
    String commentary = fillTemplate(template, event, context);
    
    // 6. Create commentary entry
    CommentaryEntry entry = new CommentaryEntry();
    entry.setTimestamp(event.getTimestamp());
    entry.setText(commentary);
    entry.setEventType(type);
    entry.setPlayerId(event.getPlayerId());
    entry.setExcitementLevel(template.getExcitementLevel());
    
    return entry;
}
```

## Context-Aware Commentary

### Match Situation Context

Commentary should reflect:
- **Score:** Winning, losing, drawing
- **Time:** Early match, late match, stoppage time
- **Importance:** League position, rivalry, cup match
- **Momentum:** Team on attack, defending, counter-attacking

### Examples

**Early Goal (5th minute, 1-0):**
- "Early breakthrough! {player} gives {team} the lead in the opening minutes!"

**Late Equalizer (87th minute, 1-1):**
- "Drama in the closing minutes! {player} finds the equalizer for {team}!"

**Rivalry Match Goal:**
- "Goal in the derby! {player} scores against their rivals! {score}"

**Important Cup Match:**
- "Crucial goal in the cup! {player} scores! {score}"

## Commentary Density

### Event Frequency

- **High-frequency events** (passes, small movements): Not all commented on
- **Medium-frequency events** (shots, tackles, possession changes): Most commented on
- **Low-frequency events** (goals, cards, injuries): Always commented on

### Commentary Selection

```java
// Determine if event should generate commentary
public boolean shouldCommentate(MatchEvent event, List<CommentaryEntry> recentCommentary) {
    // Always commentate on important events
    if (event.isImportant()) {
        return true;
    }
    
    // Check time since last commentary (avoid spam)
    float timeSinceLastCommentary = getTimeSinceLastCommentary(recentCommentary);
    if (timeSinceLastCommentary < 5.0f) { // Less than 5 seconds
        return false; // Too soon
    }
    
    // Probability-based for regular events
    float commentProbability = getCommentaryProbability(event);
    return random.nextFloat() < commentProbability;
}
```

### Commentary Probabilities

- **GOAL:** 100% (always)
- **SHOT:** 80%
- **SAVE:** 90%
- **FOUL:** 70%
- **TACKLE:** 30%
- **PASS:** 5% (only notable passes)
- **CORNER:** 100%
- **FREE_KICK:** 80%

## 1888 Language and Terminology

### Historical Terms

- Use period-appropriate language
- Avoid modern football terminology
- Use historical position names (half-back, inside forward, etc.)
- Reflect 1888 football culture

### Examples

**Modern:** "The striker runs into the box"
**1888:** "The forward advances into the penalty area"

**Modern:** "The midfielder plays a through ball"
**1888:** "The half-back sends the ball forward"

**Modern:** "The defender clears the ball"
**1888:** "The full-back kicks the ball clear"

## Commentary Timeline Structure

```java
public class MatchCommentary {
    private List<CommentaryEntry> entries; // Chronological list
    private Map<Float, List<CommentaryEntry>> entriesByMinute; // Indexed by minute
    
    // Get commentary for specific time period
    public List<CommentaryEntry> getCommentaryForPeriod(float startTime, float endTime);
    
    // Get commentary summary (key moments)
    public List<CommentaryEntry> getKeyMoments();
    
    // Get commentary by event type
    public List<CommentaryEntry> getCommentaryByEventType(EventType type);
}
```

## Commentary Entry Structure

```java
public class CommentaryEntry implements Serializable {
    private Float timestamp; // Match time in seconds
    private Integer minute; // Match minute
    private Integer second; // Match second
    private String text; // Commentary text
    private EventType eventType; // Type of event
    private Long playerId; // Player involved (if applicable)
    private Long teamId; // Team involved
    private Float excitementLevel; // 0.0-1.0
    private CommentaryStyle style; // NORMAL, EXCITED, DRAMATIC
    private Map<String, Object> metadata; // Additional data
}
```

## Integration with Match Simulation

### Event Generation → Commentary

```java
// In match simulation loop
for (MatchEvent event : matchEvents) {
    if (shouldCommentate(event, recentCommentary)) {
        CommentaryEntry commentary = commentaryGenerator.generateCommentary(event, context);
        matchCommentary.addEntry(commentary);
        recentCommentary.add(commentary);
    }
}
```

### Real-Time Commentary (for 2D/3D phases)

```java
// For real-time visualization
public interface CommentaryCallback {
    void onCommentaryGenerated(CommentaryEntry commentary);
}
```

## Testing

### Unit Tests

- Test template selection
- Test placeholder filling
- Test context filtering
- Test commentary generation for each event type

### Integration Tests

- Test full match commentary generation
- Test commentary timeline correctness
- Test commentary variety (not repetitive)
- Test commentary density (appropriate frequency)

### Validation Tests

- Readability: Commentary should read naturally
- Variety: Similar events should have different commentary
- Accuracy: Commentary should match events accurately
- Context: Commentary should reflect match context

## Implementation Plan

### Step 1: Core Structure

1. Create `CommentaryGenerator` class
2. Create `CommentaryTemplate` class
3. Create `CommentaryEntry` class
4. Create basic template storage

### Step 2: Template System

1. Define all event types
2. Create templates for each event type
3. Implement template selection logic
4. Implement placeholder filling

### Step 3: Context System

1. Create context detection
2. Implement context-aware template filtering
3. Add match situation awareness
4. Add player name lookup

### Step 4: Commentary Timeline

1. Create `MatchCommentary` class
2. Implement timeline building
3. Implement query methods (by time, by type, etc.)
4. Implement key moments extraction

### Step 5: Integration

1. Integrate with match simulation
2. Test with various match scenarios
3. Tune commentary density
4. Add more template variations

### Step 6: Polish

1. Add 1888 language refinements
2. Add more template variations
3. Improve context awareness
4. Performance optimization

## Success Criteria

- ✅ Commentary generated for all important events
- ✅ Commentary is engaging and readable
- ✅ Good variety (not repetitive)
- ✅ Player names used correctly
- ✅ Context reflected (score, time, importance)
- ✅ 1888-appropriate language
- ✅ Time-stamped correctly
- ✅ Performance acceptable (real-time generation)

---

**Next:** Begin implementation of core commentary generation system



