# Fixture Draw Bug Analysis v1.0

**Issue:** Player's team is always playing at home (or always away)
**Date:** 2025-01-XX

---

## Problem Description

The fixture generation algorithm doesn't properly distribute home and away matches. Some clubs end up playing all matches at home, while others play all matches away.

---

## Root Cause Analysis

### Current Algorithm (LeagueFixtureGenerator.java)

1. **First Half Generation (line 164):**
   ```java
   List<Match> homeFixtures = generateRoundRobinFixtures(clubs, true);
   ```
   - Generates matches where `club1` is always home, `club2` is always away

2. **Second Half Generation (line 167):**
   ```java
   List<Match> awayFixtures = generateRoundRobinFixtures(clubs, false);
   ```
   - Generates matches where `club2` is always home, `club1` is always away

3. **The Problem:**
   - The `isHomeRound` flag determines which club in the pair is home
   - But the club list order doesn't change between calls
   - If a club is always at position 0, they'll be `club1` in first half and `club1` in second half
   - In first half: `club1` is home (when `isHomeRound=true`)
   - In second half: `club1` is away (when `isHomeRound=false`, so `club2` is home)
   - **BUT**: The rotation within each round means the club at position 0 might not always be paired the same way

### The Real Issue

The algorithm generates two separate fixture sets:
- **First half**: All matches where the "first club in pair" is home
- **Second half**: All matches where the "second club in pair" is home

But this doesn't guarantee balanced home/away distribution because:
1. The club list order is fixed
2. The rotation happens within each call, but doesn't account for which specific clubs should be home/away
3. A club might be `club1` in some rounds and `club2` in others, but the home/away assignment is based on position, not on ensuring balance

---

## Solution

### Proper Round-Robin Algorithm

For a proper home-and-away round-robin tournament:

1. **Generate N-1 rounds** (where N = number of clubs)
2. **In each round**, pair clubs and assign home/away
3. **For return fixtures**, reverse the home/away for each specific match pair

### Implementation Strategy

**Option 1: Generate all matches, then reverse half**
- Generate N-1 rounds with proper home/away rotation
- For return fixtures, take each match and swap home/away
- This ensures each club plays every other club twice (once home, once away)

**Option 2: Proper round-robin with home/away rotation**
- Use standard round-robin algorithm
- Rotate home/away assignment each round
- Ensure each club gets roughly equal home/away matches

**Option 3: Generate complete fixture list with balanced home/away**
- Generate all N-1 rounds
- For each round, alternate which club in the pair is home
- Generate return fixtures by reversing each match

---

## Recommended Fix

**Use Option 1** - Simplest and most reliable:

1. Generate first half (N-1 rounds) with proper home/away rotation
2. For second half, take each match from first half and swap home/away
3. This guarantees:
   - Each club plays every other club twice
   - Each club gets equal home and away matches (if even number of clubs)
   - Fair distribution

---

## Implementation Plan

1. Modify `generateRoundRobinFixtures()` to properly rotate home/away
2. Instead of using `isHomeRound` flag, generate balanced fixtures
3. For return fixtures, reverse each match's home/away assignment
4. Test with 12 clubs to verify each club gets 11 home and 11 away matches

---

## Testing

After fix, verify:
- Each club plays every other club exactly twice (once home, once away)
- Each club has equal (or nearly equal) home and away matches
- No club plays all matches at home or all matches away
- Fixtures are properly distributed across the season
