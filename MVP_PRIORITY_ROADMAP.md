# MVP Priority Roadmap - Next Steps

**Date:** 2025-01-XX  
**Current Version:** v0.4.0-SNAPSHOT  
**Goal:** Identify top 5 MVP features with calculated priority percentages

---

## Priority Calculation Methodology

**Factors Considered:**
1. **Playability Impact** (40% weight) - Does it make the game playable?
2. **Dependency Blocking** (30% weight) - Does it block other features?
3. **Current Completion** (20% weight) - How close is it to done?
4. **User Experience** (10% weight) - Does it improve player experience?

**Total:** 100% distributed across top 5 priorities

---

## Top 5 MVP Priorities

### 1. **Match Simulation Engine** - 40% Priority

**Why Highest Priority:**
- **Core Gameplay Blocker:** Without match simulation, the game is not playable
- **Unlocks Everything:** Enables competitions, leagues, cups, seasons
- **Foundation Dependency:** All other gameplay features depend on this
- **Current State:** Designed but 0% implemented

**What's Needed:**
- Basic match result generation (scores, winner/draw)
- Integration with game engine (simulate matches on match day)
- MatchResult data structure
- Update club statistics after matches
- Support for friendlies, leagues, and cups

**Estimated Time:** 2-3 weeks  
**Dependencies:** Player attributes (✅ done), club data (✅ done)  
**Blocks:** Competition completion, league tables, season progression

**MVP Value:** ⭐⭐⭐⭐⭐ (5/5) - Game is unplayable without this

---

### 2. **Competition Completion & League Tables** - 25% Priority

**Why Second Priority:**
- **Enables Season Completion:** Players need to see progress and finish seasons
- **Depends on Match Simulation:** Can't complete without match results
- **Core Gameplay Loop:** Essential for Football Manager experience
- **Current State:** Structure exists, execution incomplete

**What's Needed:**
- League table calculation and updates
- Competition progression (cup rounds, league matches)
- Season completion logic
- League table UI screen
- Competition standings display

**Estimated Time:** 1-2 weeks  
**Dependencies:** Match simulation (❌ must be done first)  
**Blocks:** Multi-season gameplay, competition rewards

**MVP Value:** ⭐⭐⭐⭐ (4/5) - Essential for completing seasons

---

### 3. **Save/Load System Polish & UI** - 15% Priority

**Why Third Priority:**
- **Essential Feature:** Players must be able to save progress
- **Current State:** Basic save/load exists but needs UI and testing
- **Low Risk:** Data model ready, serialization works
- **User Experience:** Critical for player retention

**What's Needed:**
- Save game UI (dialog, file naming)
- Load game UI (list of saves, metadata display)
- Auto-save functionality
- Save file validation
- Error handling for corrupted saves
- Testing and bug fixes

**Estimated Time:** 1 week  
**Dependencies:** SaveGame data model (✅ ready), SaveLoadSystem (⚠️ basic exists)  
**Blocks:** Nothing (can be done in parallel)

**MVP Value:** ⭐⭐⭐⭐ (4/5) - Essential for any game

---

### 4. **Match Result Screen & Match History** - 12% Priority

**Why Fourth Priority:**
- **User Experience:** Players need to see match results
- **Feedback Loop:** Essential for understanding game state
- **Current State:** UI placeholder exists, needs implementation
- **Depends on Match Simulation:** Needs match results to display

**What's Needed:**
- Match result screen implementation
- Display scores, teams, date
- Match history storage and display
- Navigation from schedule to results
- Basic match statistics (optional for MVP)

**Estimated Time:** 3-5 days  
**Dependencies:** Match simulation (❌ must be done first)  
**Blocks:** Nothing (nice to have)

**MVP Value:** ⭐⭐⭐ (3/5) - Important for UX but not blocker

---

### 5. **Training System (Basic)** - 8% Priority

**Why Fifth Priority:**
- **Player Development:** Affects long-term gameplay
- **Current State:** Fully designed, not implemented
- **Not Critical for MVP:** Game is playable without it
- **Can Be Added Later:** Doesn't block core gameplay

**What's Needed:**
- Basic training schedule system
- Training effects on attributes
- Training UI screen
- Staff-managed training (optional for MVP)
- Training intensity levels

**Estimated Time:** 2-3 weeks  
**Dependencies:** Player attributes (✅ done), staff system (⚠️ partial)  
**Blocks:** Nothing (can be added post-MVP)

**MVP Value:** ⭐⭐ (2/5) - Nice to have, not essential for MVP

---

## Priority Breakdown Summary

| Priority | Feature | % | MVP Value | Time | Dependencies |
|----------|----------|---|-----------|------|--------------|
| 1 | Match Simulation Engine | 40% | ⭐⭐⭐⭐⭐ | 2-3 weeks | Attributes ✅ |
| 2 | Competition Completion | 25% | ⭐⭐⭐⭐ | 1-2 weeks | Match Sim ❌ |
| 3 | Save/Load Polish | 15% | ⭐⭐⭐⭐ | 1 week | Data model ✅ |
| 4 | Match Result Screen | 12% | ⭐⭐⭐ | 3-5 days | Match Sim ❌ |
| 5 | Training System | 8% | ⭐⭐ | 2-3 weeks | Attributes ✅ |

**Total:** 100%

---

## Implementation Order Recommendation

### Phase 1: Core Gameplay (Weeks 1-3)
1. **Match Simulation Engine** (40% - 2-3 weeks)
   - Implement basic match result generation
   - Integrate with game engine
   - Test with friendlies, leagues, cups

### Phase 2: Competition System (Weeks 4-5)
2. **Competition Completion** (25% - 1-2 weeks)
   - League table calculation
   - Competition progression
   - Season completion

### Phase 3: Essential Features (Week 6)
3. **Save/Load Polish** (15% - 1 week)
   - UI implementation
   - Auto-save
   - Testing

### Phase 4: UX Enhancement (Week 6-7)
4. **Match Result Screen** (12% - 3-5 days)
   - Result display
   - Match history

### Phase 5: Enhancement (Post-MVP)
5. **Training System** (8% - 2-3 weeks)
   - Can be added after MVP is playable

---

## Critical Path Analysis

**Critical Path:** Match Simulation → Competition Completion → MVP Playable

**Parallel Work:**
- Save/Load can be done in parallel with match simulation
- Match Result Screen can be done after match simulation
- Training System can be done post-MVP

**Blockers:**
- Match Simulation blocks: Competition Completion, Match Result Screen
- Competition Completion blocks: Season progression, Multi-season gameplay

---

## Success Criteria for MVP

**MVP is complete when:**
1. ✅ Players can simulate matches and see results
2. ✅ Players can complete a full season (league or cup)
3. ✅ Players can see league tables and standings
4. ✅ Players can save and load their game
5. ✅ Players can view match results and history

**Estimated Total Time to MVP:** 5-6 weeks

---

## Notes

- **Match Simulation is the #1 blocker** - Everything else depends on it
- **Save/Load can be done in parallel** - Doesn't block anything
- **Training System is post-MVP** - Nice to have but not essential
- **Focus on core gameplay first** - Polish can come later

---

**Document Version:** 1.0  
**Last Updated:** 2025-01-XX

