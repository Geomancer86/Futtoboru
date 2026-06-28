# Cup System Design Review & Architecture

## Current Architecture

### 1. Bracket Generation (`CupBracketGenerator`)
- **Purpose**: Generate complete tournament bracket upfront (all rounds)
- **Output**: List of all matches (31 for 32 teams: 16+8+4+2+1)
- **Key Features**:
  - Creates all matches with parent-child relationships
  - Sets `bracketPath` (e.g., "R1M1", "R2M4")
  - Sets `parentMatch1Id` and `parentMatch2Id` for Round 2+
  - Round 1 matches have both teams determined
  - Round 2+ matches have `null` teams initially

### 2. Match Storage (`AuthorityManager.generateCupDraw()`)
- **Round 1 Matches**: Added to specific clubs (home/away)
- **Round 2+ Matches**: Added to ALL participating clubs (for accessibility)
- **Critical**: Uses ID-based deduplication to avoid duplicates

### 3. Match Lifecycle
1. **Scheduled**: Match in `club.getScheduledMatches()`
2. **Played**: Match moved to `club.getPlayedMatches()` after simulation
3. **Winner Advancement**: `CupBracketManager.advanceWinner()` populates next round match

### 4. Winner Advancement (`CupBracketManager`)
- **Trigger**: After match simulation completes
- **Process**:
  1. Determine winner (or schedule replay if draw)
  2. Find next round match using `parentMatch1Id` or `parentMatch2Id`
  3. Set winner's team ID in next round match (home or away position)
  4. If both teams determined, ensure match is in clubs' lists

### 5. Draw/Replay Handling (`CupBracketManager`)
- **Draw Detection**: `determineWinner()` returns `null` if scores equal
- **Replay Scheduling**: `scheduleReplay()` creates new match 7 days later
- **Venue Swap**: Replay at away team's venue
- **Replay Completion**: Uses original match ID to find next round match

### 6. UI Display (`CupDetailScreenTable`, `CupPlayoffsTable`)
- **Critical Fix**: Search BOTH `scheduledMatches` AND `playedMatches`
- **Reason**: Matches move from scheduled to played after simulation
- **Display**: Shows all rounds, all matches (played and unplayed)

## Design Strengths ✅

1. **Complete Bracket Upfront**: All matches generated at draw time
2. **Parent-Child Linking**: Clear dependency tracking via `parentMatch1Id`/`parentMatch2Id`
3. **ID-Based Deduplication**: Prevents duplicate matches in lists
4. **Match Accessibility**: Round 2+ matches in all clubs ensures they're findable
5. **Replay System**: Handles draws with venue swap

## Design Issues & Fixes 🔧

### Issue 1: UI Only Shows Scheduled Matches
**Problem**: `CupDetailScreenTable` and `CupPlayoffsTable` only searched `scheduledMatches`
**Impact**: Round 1 matches disappeared after being played
**Fix**: ✅ Updated both to search `scheduledMatches` AND `playedMatches`

### Issue 2: Missing Parent Matches
**Problem**: Some Round 1 matches can't find their next round match
**Possible Causes**:
- Bracket generation bug (R2M1 had both parents same ID)
- Edition ID mismatch
- Match not in all clubs' lists
**Status**: ⚠️ Needs investigation with better logging

### Issue 3: Draw Handling
**Problem**: Draws weren't handled, causing matches to stall
**Fix**: ✅ Implemented replay scheduling system

## Validation Checklist

- [x] All matches generated upfront (31 for 32 teams)
- [x] Round 1 matches have both teams determined
- [x] Round 2+ matches have parent links
- [x] Winner advancement works
- [x] Replay scheduling works
- [x] UI shows all matches (scheduled + played)
- [ ] All Round 1 matches have valid next round matches
- [ ] Tournament progresses to champion
- [ ] Team counts correct per round (16→8→4→2→1)

## Next Steps

1. **Fix Missing Parent Matches**: Investigate why R1M2 and R1M14 can't find next round
2. **Bracket Validation**: Add checks to ensure all rounds have correct team counts
3. **Complete Tournament Test**: Verify progression from Round 1 to Final to Champion
