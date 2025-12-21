# Cup Tournament Bracket System - Complete Design v1.0

## 1. REQUIREMENTS ANALYSIS

### Current State:
- Only first round is drawn (16 matches for 32 teams)
- No future rounds exist until first round completes
- No bracket structure linking matches together
- Winners don't automatically advance to next round

### Desired State:
- **Complete bracket drawn upfront** - All rounds created at initial draw
- **Match dependencies** - Future matches reference "Winner of Match X vs Winner of Match Y"
- **Automatic winner advancement** - When match completes, winner populates next round
- **Progressive elimination** - Teams eliminated round by round until final
- **Day-by-day progression** - Game advances, matches play, bracket fills in

### Tournament Structure (32 teams):
- **Round 1 (First Round)**: 32 teams → 16 matches → 16 winners
- **Round 2 (Second Round)**: 16 teams → 8 matches → 8 winners
- **Round 3 (Third Round)**: 8 teams → 4 matches → 4 winners
- **Round 4 (Semi-Finals)**: 4 teams → 2 matches → 2 winners
- **Round 5 (Final)**: 2 teams → 1 match → 1 champion

**Total: 31 matches (16 + 8 + 4 + 2 + 1)**

---

## 2. DATA MODEL DESIGN

### 2.1 Match Dependencies

Each match needs to know:
- **Parent matches** (which matches feed into this match)
- **Position in bracket** (which "slot" this match occupies)
- **Bracket path** (unique identifier for this match in the bracket tree)

```java
// Add to Match.java
private Long parentMatch1Id;      // First parent match (e.g., Match 1 winner)
private Long parentMatch2Id;      // Second parent match (e.g., Match 2 winner)
private Integer bracketPosition;  // Position in round (1, 2, 3, ...)
private String bracketPath;       // Unique path like "R1M1" (Round 1 Match 1)
```

### 2.2 Bracket Structure

The bracket is a binary tree:
```
                    Final (R5M1)
                   /            \
          Semi-Final 1 (R4M1)    Semi-Final 2 (R4M2)
         /            \           /            \
    Quarter 1 (R3M1)  Q2 (R3M2) Q3 (R3M3)  Q4 (R3M4)
    /        \        /    \     /    \     /    \
  R2M1      R2M2    R2M3  R2M4  R2M5  R2M6 R2M7 R2M8
  /  \      /  \    /  \   /  \  /  \  /  \  /  \  /  \
R1M1 R1M2 R1M3 R1M4 ... (16 matches total)
```

### 2.3 Match States

- **Scheduled** - Match exists, teams not yet determined (shows "Winner of X vs Winner of Y")
- **Teams Determined** - Parent matches completed, teams populated
- **Played** - Match completed, winner determined
- **Winner Advanced** - Winner populated in next round match

---

## 3. ALGORITHM DESIGN

### 3.1 Initial Bracket Generation

**Step 1: Generate First Round (32 teams → 16 matches)**
```
FOR each pair of teams:
    CREATE Match
    SET round = 1
    SET bracketPosition = matchNumber (1-16)
    SET bracketPath = "R1M" + matchNumber
    SET parentMatch1Id = null (first round has no parents)
    SET parentMatch2Id = null
    SET homeClubId = team1
    SET awayClubId = team2
    SET matchDateTime = firstRoundDate
```

**Step 2: Generate Subsequent Rounds**
```
FOR round = 2 to 5:
    matchesInRound = previousRoundMatches / 2
    
    FOR matchNumber = 1 to matchesInRound:
        CREATE Match
        SET round = currentRound
        SET bracketPosition = matchNumber
        SET bracketPath = "R" + round + "M" + matchNumber
        
        // Calculate parent matches
        parent1Position = (matchNumber * 2) - 1
        parent2Position = (matchNumber * 2)
        parent1 = findMatch(previousRound, parent1Position)
        parent2 = findMatch(previousRound, parent2Position)
        
        SET parentMatch1Id = parent1.getId()
        SET parentMatch2Id = parent2.getId()
        
        // Teams not yet determined
        SET homeClubId = null (will be populated when parents complete)
        SET awayClubId = null
        
        SET matchDateTime = calculateDate(round, matchNumber)
```

### 3.2 Winner Advancement Algorithm

**When a match completes:**
```
FUNCTION advanceWinner(match):
    IF match.isPlayed == false:
        RETURN
    
    winner = determineWinner(match) // homeClub or awayClub
    
    // Find next round match that depends on this match
    nextRoundMatch = findMatchWithParent(match.getId())
    
    IF nextRoundMatch == null:
        // This is the final - cup is complete
        completeCup(match.competitionEditionId, winner)
        RETURN
    
    // Determine which parent slot this match fills
    IF nextRoundMatch.parentMatch1Id == match.getId():
        nextRoundMatch.setHomeClubId = winner.getId()
    ELSE IF nextRoundMatch.parentMatch2Id == match.getId():
        nextRoundMatch.setAwayClubId = winner.getId()
    
    // Check if both parents are complete
    IF nextRoundMatch.homeClubId != null AND nextRoundMatch.awayClubId != null:
        // Both teams determined - match is ready to be scheduled/played
        markMatchReady(nextRoundMatch)
```

### 3.3 Match Scheduling

**Future round matches:**
- Created with `matchDateTime` calculated based on round
- Teams initially null (shows "Winner of Match X")
- When both parent matches complete, teams are populated
- Match becomes "ready" to be played

**Date calculation:**
```
firstRoundDate = drawDate + 2 weeks
round2Date = round1Date + 2 weeks (after round 1 completes)
round3Date = round2Date + 2 weeks
semiFinalDate = round3Date + 2 weeks
finalDate = semiFinalDate + 2 weeks
```

---

## 4. IMPLEMENTATION PLAN

### Phase 1: Data Model Extensions
1. Add bracket fields to `Match.java`:
   - `parentMatch1Id`, `parentMatch2Id`
   - `bracketPosition`, `bracketPath`
   - Helper methods: `getParentMatches()`, `getNextRoundMatch()`

### Phase 2: Complete Bracket Generation
1. Modify `CompetitionScheduler.competitionDraw()`:
   - Generate all rounds upfront
   - Link matches via parent relationships
   - Set bracket positions and paths

2. Create `CupBracketGenerator.java`:
   - `generateCompleteBracket(Competition cup, CompetitionEdition edition, List<Long> teamIds)`
   - Returns all matches for all rounds
   - Handles bracket tree structure

### Phase 3: Winner Advancement System
1. Modify `AuthorityManager.checkCupRoundProgression()`:
   - After each match completes, call `advanceWinner()`
   - Check if next round match is ready (both teams determined)
   - Schedule next round matches when ready

2. Create `CupBracketManager.java`:
   - `advanceWinner(Match completedMatch)`
   - `findMatchWithParent(Long parentMatchId)`
   - `isMatchReady(Match match)` - both teams determined
   - `getBracketPath(Match match)` - for UI display

### Phase 4: UI Updates
1. Update `CupPlayoffsTable.java`:
   - Show all rounds (even future ones)
   - Display "Winner of Match X" for undetermined teams
   - Highlight completed matches
   - Show bracket tree structure

2. Update `CupDetailScreenTable.java`:
   - Show complete schedule
   - Display match dependencies
   - Show progression status

### Phase 5: Match Simulation Integration
1. Modify `MatchSimulator.simulateMatch()`:
   - After simulation, trigger `advanceWinner()`
   - Update bracket state

2. Modify `FuttoboruGameEngine.getMatchResult()`:
   - For cup matches, call bracket advancement
   - Check for round completion
   - Schedule next round if needed

---

## 5. CODE STRUCTURE

### New Classes:
```
com.rndmodgames.futtoboru.engine.cup/
├── CupBracketGenerator.java      # Generates complete bracket
├── CupBracketManager.java        # Manages bracket state and advancement
└── CupBracketNode.java           # Represents a match in bracket tree (optional)
```

### Modified Classes:
```
com.rndmodgames.futtoboru.data/
└── Match.java                     # Add bracket fields

com.rndmodgames.futtoboru.engine.temporal/
└── CompetitionScheduler.java     # Generate complete bracket

com.rndmodgames.futtoboru.engine/
└── AuthorityManager.java         # Winner advancement logic

com.rndmodgames.futtoboru.tables.competitions/
├── CupPlayoffsTable.java         # Show complete bracket
└── CupDetailScreenTable.java     # Show schedule with dependencies
```

---

## 6. EXAMPLE FLOW

### Initial Draw (May 1, 1888):
```
Round 1 (16 matches):
  Match 1: Team 1 vs Team 2 (scheduled for May 15)
  Match 2: Team 3 vs Team 4 (scheduled for May 15)
  ...
  Match 16: Team 31 vs Team 32 (scheduled for May 15)

Round 2 (8 matches):
  Match 1: Winner of Match 1 vs Winner of Match 2 (scheduled for May 29)
  Match 2: Winner of Match 3 vs Winner of Match 4 (scheduled for May 29)
  ...
  Match 8: Winner of Match 15 vs Winner of Match 16 (scheduled for May 29)

Round 3 (4 matches):
  Match 1: Winner of R2M1 vs Winner of R2M2 (scheduled for June 12)
  ...

Semi-Finals (2 matches):
  Match 1: Winner of R3M1 vs Winner of R3M2 (scheduled for June 26)
  Match 2: Winner of R3M3 vs Winner of R3M4 (scheduled for June 26)

Final (1 match):
  Match 1: Winner of R4M1 vs Winner of R4M2 (scheduled for July 10)
```

### After Round 1 Completes:
```
Round 1 Match 1: Team 1 (3) vs Team 2 (1) → Team 1 wins
Round 1 Match 2: Team 3 (2) vs Team 4 (0) → Team 3 wins

Round 2 Match 1: Team 1 vs Team 3 (teams now determined!)
```

### After Final:
```
Final: Team A (2) vs Team B (1) → Team A wins
→ Cup Complete! Team A is champion
```

---

## 7. EDGE CASES

1. **Replays/Draws**: Handle draws (replay matches)
2. **Byes**: Handle odd number of teams (already implemented)
3. **Match Cancellations**: Handle postponed matches
4. **Team Withdrawals**: Handle teams that can't play
5. **Bracket Visualization**: Ensure UI can display large brackets

---

## 8. TESTING STRATEGY

1. **Unit Tests**:
   - Bracket generation for 32 teams
   - Winner advancement logic
   - Match dependency resolution

2. **Integration Tests**:
   - Complete tournament simulation
   - Round-by-round progression
   - Final determination

3. **UI Tests**:
   - Bracket display with all rounds
   - "Winner of X" labels
   - Progressive filling of bracket

---

## 9. PERFORMANCE CONSIDERATIONS

- **Bracket Size**: 32 teams = 31 matches (manageable)
- **Lookup Efficiency**: Use HashMap for match lookup by bracketPath
- **UI Rendering**: Only render visible rounds, lazy load if needed

---

## 10. FUTURE ENHANCEMENTS

1. **Seeding System**: Rank teams and seed bracket
2. **Two-Leg Ties**: Support home/away legs
3. **Group Stages**: Support group + knockout format
4. **Third Place Playoff**: Add consolation match
5. **Historical Brackets**: Save and display past brackets
