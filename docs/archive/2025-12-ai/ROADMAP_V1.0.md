# Futtoboru v1.0 Roadmap

## Overview
This document outlines the plan to complete Futtoboru v1.0 - a playable Football Manager-style game with core features functional.

## Current State Assessment

### ✅ What's Working
- **Project Structure**: Maven build system, modular architecture
- **Database System**: Countries, leagues, seasons, clubs loading
- **UI Framework**: LibGDX + VisUI integration
- **Game Screens**: Menu, New Game, Settings screens functional
- **Game Engine Foundation**: Basic time progression, script system
- **Match Scheduling**: Competition and match scheduling infrastructure
- **Localization**: Multi-language support framework
- **Save Game System**: Basic structure exists

### ⚠️ What's Partially Implemented
- **Match Simulation**: Scheduling works, but actual match results not simulated
- **Game Screens**: Many screens exist but incomplete (Squad, Finances, Competitions, etc.)
- **Player System**: Basic structure exists, but player attributes/generation incomplete
- **Competition System**: FA Cup structure exists, but execution incomplete
- **Time Progression**: Basic day-by-day, needs refinement

### ❌ What's Missing
- **Match Result Simulation**: No actual match engine to generate results
- **Player Attributes**: Skills, positions, ratings not fully implemented
- **Financial System**: Club finances, transfers, contracts
- **Squad Management**: Team selection, tactics, formations
- **Competition Completion**: FA Cup and league competitions not fully playable
- **Save/Load Game**: Save game persistence incomplete
- **Bug Fixes**: Null pointer exceptions on startup need investigation

---

## v1.0 Feature Requirements

### Core Gameplay Loop (CRITICAL)
1. **Start New Game** ✅
   - Select season ✅
   - Select countries/leagues ✅
   - Create manager ✅
   - Start game ✅

2. **Time Progression** ⚠️
   - Continue game button advances time ✅
   - Match day detection ✅
   - Time progression needs refinement ⚠️

3. **Match Simulation** ❌
   - Match preview screen ⚠️
   - Match result generation ❌
   - Match result display ❌
   - Match history ⚠️

4. **Competition System** ⚠️
   - FA Cup scheduling ✅
   - FA Cup execution ❌
   - League scheduling ⚠️
   - League execution ❌
   - Competition completion ❌

### Essential Screens (CRITICAL)
1. **Home Screen** ✅
   - Basic implementation exists
   - Needs dynamic content updates

2. **Squad Screen** ⚠️
   - Basic structure exists
   - Needs: player list, team selection, tactics

3. **Schedule Screen** ⚠️
   - Basic structure exists
   - Needs: match list, match details

4. **Club Info Screen** ⚠️
   - Basic structure exists
   - Needs: club details, facilities, staff

5. **Finances Screen** ❌
   - Structure exists but incomplete
   - Needs: income, expenses, budget

6. **Competitions Screen** ⚠️
   - Basic structure exists
   - Needs: league tables, cup brackets

7. **Match Preview Screen** ⚠️
   - Basic structure exists
   - Needs: team lineups, match details

8. **Match Result Screen** ❌
   - Needs implementation

### Core Systems (CRITICAL)
1. **Player System** ⚠️
   - Player generation ⚠️
   - Player attributes ❌
   - Player positions ⚠️
   - Player ratings ❌

2. **Match Engine** ❌
   - Match simulation algorithm
   - Goal generation
   - Player performance calculation
   - Match statistics

3. **Financial System** ❌
   - Club finances
   - Match revenue
   - Player contracts
   - Transfers (v1.0: basic only)

4. **Save/Load System** ⚠️
   - Save game persistence ❌
   - Load game functionality ❌
   - Auto-save ⚠️

### Polish & Stability (HIGH PRIORITY)
1. **Bug Fixes**
   - Fix null pointer on startup
   - Fix match scheduling issues
   - Fix UI update issues
   - Memory leaks

2. **UI/UX Improvements**
   - Loading screens
   - Error handling
   - User feedback (toasts, dialogs)
   - Settings persistence

3. **Performance**
   - Optimize database loading
   - Optimize UI rendering
   - Memory management

---

## Implementation Phases

### Phase 1: Critical Bug Fixes & Stability (Week 1-2)
**Goal**: Game runs without crashes

- [ ] Fix null pointer exception on startup
- [ ] Fix any initialization issues
- [ ] Add proper error handling
- [ ] Add loading screens
- [ ] Fix basic UI update issues

**Deliverable**: Stable game that can start and navigate menus

### Phase 2: Match Engine Core (Week 3-4)
**Goal**: Matches can be simulated and results displayed

- [ ] Implement basic match simulation algorithm
  - [ ] Simple probability-based result generation
  - [ ] Home/away advantage
  - [ ] Basic goal generation
- [ ] Create MatchResult data structure
- [ ] Implement match result screen
- [ ] Update match history
- [ ] Test with various club combinations

**Deliverable**: Matches can be played and results shown

### Phase 3: Player System (Week 5-6)
**Goal**: Players have attributes and can be managed

- [ ] Complete player attribute system
  - [ ] Basic stats (age, position, skill ratings)
  - [ ] Player generation with attributes
  - [ ] Player display in squad screen
- [ ] Implement basic squad management
  - [ ] View squad
  - [ ] Basic team selection (v1.0: simple only)
- [ ] Player performance in matches

**Deliverable**: Players have stats and can be viewed/managed

### Phase 4: Competition System (Week 7-8)
**Goal**: Competitions can be completed

- [ ] Complete FA Cup execution
  - [ ] Match scheduling for all rounds
  - [ ] Progression through rounds
  - [ ] Winner determination
- [ ] Implement league system
  - [ ] League table
  - [ ] League fixtures
  - [ ] League completion
- [ ] Competition screens updates

**Deliverable**: FA Cup and at least one league fully playable

### Phase 5: Essential Screens Completion (Week 9-10)
**Goal**: All core screens functional

- [ ] Complete Squad Screen
- [ ] Complete Schedule Screen
- [ ] Complete Club Info Screen
- [ ] Complete Finances Screen (basic)
- [ ] Complete Competitions Screen
- [ ] Complete Match Preview Screen
- [ ] Complete Match Result Screen

**Deliverable**: All screens accessible and show relevant data

### Phase 6: Save/Load System (Week 11)
**Goal**: Games can be saved and loaded

- [ ] Implement save game serialization
- [ ] Implement load game functionality
- [ ] Add save/load UI
- [ ] Implement auto-save
- [ ] Test save game compatibility

**Deliverable**: Save and load games work reliably

### Phase 7: Polish & Testing (Week 12)
**Goal**: v1.0 release ready

- [ ] UI/UX polish
- [ ] Performance optimization
- [ ] Bug fixes
- [ ] Testing across different scenarios
- [ ] Documentation updates
- [ ] Release preparation

**Deliverable**: v1.0 release candidate

---

## Technical Debt & Future Considerations

### Known Issues to Address
1. **DatabaseLoader**: Marked as "ugliest part" - consider refactoring for v2.0
2. **Screen ID System**: Current screen ID scheme needs rework (noted in code)
3. **Random Generator**: Need unified random generator with seed support
4. **Error Handling**: Many TODO comments about error handling and file restoration

### Out of Scope for v1.0
- Advanced tactics system
- Transfer market (basic only if time permits)
- Staff management
- Training system
- Youth academy
- Advanced statistics
- Multiplayer
- Advanced graphics/3D scenes (Prologue scene)

---

## Success Criteria for v1.0

A successful v1.0 release should allow a player to:

1. ✅ Start a new game
2. ✅ Navigate all main screens
3. ✅ View their squad
4. ✅ See their schedule
5. ✅ Play through at least one full season
6. ✅ See match results
7. ✅ Complete a competition (FA Cup or League)
8. ✅ Save and load their game
9. ✅ Play without critical bugs or crashes

---

## Risk Assessment

### High Risk Items
- **Match Engine**: Core gameplay depends on this - must be prioritized
- **Save/Load**: Critical for player experience - needs thorough testing
- **Competition System**: Complex logic - may need iteration

### Mitigation Strategies
- Start with simple implementations, iterate
- Test frequently with real game scenarios
- Keep scope focused - defer advanced features
- Regular playtesting to catch issues early

---

## Next Steps

1. **Immediate**: Fix startup null pointer exception
2. **This Week**: Begin Phase 1 (bug fixes)
3. **Next Week**: Start Phase 2 (match engine)
4. **Ongoing**: Regular testing and iteration

---

## Notes

- This roadmap is a living document - adjust as needed
- Prioritize playability over perfection
- Focus on core loop first, polish later
- Regular testing is essential
- Keep scope manageable for v1.0

---

*Last Updated: 2025-12-14*
*Version: 1.0*

