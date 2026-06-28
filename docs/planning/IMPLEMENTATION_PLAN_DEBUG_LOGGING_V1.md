# Debug Logging System Implementation Plan v1.0

**Branch:** `feature/debug-logging-settings`  
**Date:** 2025-01-XX  
**Purpose:** Step-by-step implementation plan with testing checkpoints

---

## Overview

This document provides a detailed, phased implementation plan for the Debug Logging System. Each phase includes specific tasks, testing checkpoints, and success criteria.

---

## Implementation Phases

### Phase 1: Core Infrastructure ✅ (Foundation)

**Goal:** Create the core DebugLogManager and basic infrastructure

**Tasks:**
1. Create `DebugLogManager.java` class
   - Singleton pattern
   - Category constants
   - Basic logging methods (log, debug, warn, error)
   - Settings persistence integration
   - PreferencesManager integration

2. Add preferences constants to `PreferencesManager.java`
   - Document preference keys (actual keys managed by DebugLogManager)

3. Create unit tests for DebugLogManager
   - Test singleton
   - Test category enable/disable
   - Test master switch
   - Test settings persistence

**Files to Create:**
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/system/DebugLogManager.java`
- `futtoboru-core/src/test/java/com/rndmodgames/futtoboru/system/DebugLogManagerTest.java` (optional)

**Files to Modify:**
- `PreferencesManager.java` (add constants/documentation)

**Testing Checkpoint:**
- ✅ DebugLogManager can be instantiated
- ✅ Categories can be enabled/disabled
- ✅ Settings persist across game restarts
- ✅ Logging methods work correctly
- ✅ Master switch works

**Estimated Time:** 2-3 hours

---

### Phase 2: Settings Screen UI ✅ (User Interface)

**Goal:** Add debug logging settings to Settings Screen

**Tasks:**
1. Create `DebugLoggingSettingsTable.java`
   - Checkbox UI for each category
   - Master "Enable All" checkbox
   - "Enable All" / "Disable All" buttons
   - Section grouping (Engine, UI, Data, System)

2. Integrate with `SettingsScreen.java`
   - Add DebugLoggingSettingsTable to settings panels
   - Update layout to accommodate new section

3. Test UI functionality
   - Checkboxes update correctly
   - Settings save when changed
   - Settings load on screen open

**Files to Create:**
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/settings/DebugLoggingSettingsTable.java`

**Files to Modify:**
- `SettingsScreen.java`

**Testing Checkpoint:**
- ✅ Settings screen displays debug logging section
- ✅ All categories visible with checkboxes
- ✅ Checkboxes reflect current settings
- ✅ Changing checkboxes updates settings immediately
- ✅ Settings persist after closing/reopening settings screen
- ✅ "Enable All" / "Disable All" buttons work

**Estimated Time:** 3-4 hours

---

### Phase 3: High-Impact Engine Systems Migration 🔥 (Priority)

**Goal:** Migrate highest-volume logging systems for maximum performance impact

**Systems to Migrate (in order):**

#### 3.1 FuttoboruGameEngine
- **Impact:** Very High (~200+ logging calls)
- **Category:** `engine.game`
- **Files:** `FuttoboruGameEngine.java`
- **Focus Areas:**
  - `continueGame()` - Daily progression
  - `simulateMatchesForDate()` - Match simulation
  - `getMatchResult()` - Match result processing

**Testing Checkpoint:**
- ✅ Game progression works correctly
- ✅ Match simulation works correctly
- ✅ Logging can be enabled/disabled
- ✅ Performance improvement noticeable

#### 3.2 AuthorityManager
- **Impact:** Very High (~150+ logging calls)
- **Category:** `engine.authority`
- **Files:** `AuthorityManager.java`
- **Focus Areas:**
  - Competition scheduling
  - Cup draw generation
  - League fixture generation

**Testing Checkpoint:**
- ✅ Competition scheduling works
- ✅ Cup draws work
- ✅ League fixtures work
- ✅ Logging can be enabled/disabled

#### 3.3 CupBracketManager
- **Impact:** High (~45+ logging calls)
- **Category:** `engine.cup`
- **Files:** `CupBracketManager.java`
- **Focus Areas:**
  - Winner advancement
  - Replay scheduling
  - Match completion

**Testing Checkpoint:**
- ✅ Cup progression works
- ✅ Replays work
- ✅ Winner advancement works
- ✅ Logging can be enabled/disabled

#### 3.4 LeagueFixtureGenerator
- **Impact:** High (~103+ logging calls, many in loops)
- **Category:** `engine.league`
- **Files:** `LeagueFixtureGenerator.java`
- **Focus Areas:**
  - Fixture generation loops
  - Round generation

**Testing Checkpoint:**
- ✅ League fixtures generate correctly
- ✅ All rounds created
- ✅ Performance improvement significant
- ✅ Logging can be enabled/disabled

#### 3.5 MatchScheduler
- **Impact:** Medium (~22+ logging calls)
- **Category:** `engine.scheduling`
- **Files:** `MatchScheduler.java`
- **Focus Areas:**
  - Match day checks
  - Ticket sales

**Testing Checkpoint:**
- ✅ Match day detection works
- ✅ Ticket sales work
- ✅ Logging can be enabled/disabled

**Total Estimated Time:** 6-8 hours (1-2 hours per system)

---

### Phase 4: UI Systems Migration 🖥️ (User-Facing)

**Goal:** Migrate UI systems for better user experience

**Systems to Migrate (in order):**

#### 4.1 Cup UI Screens
- **Impact:** Medium (~38+ logging calls total)
- **Category:** `ui.cup`
- **Files:**
  - `CupDrawScreenTable.java` (~18 calls)
  - `CupDetailScreenTable.java` (~10 calls)
  - `CupPlayoffsTable.java` (~10 calls)

**Testing Checkpoint:**
- ✅ Cup draw screen works
- ✅ Cup detail screen works
- ✅ Playoffs screen works
- ✅ Logging can be enabled/disabled

#### 4.2 ClubDetailScreenTable
- **Impact:** Medium (~78+ logging calls)
- **Category:** `ui.club`
- **Files:** `ClubDetailScreenTable.java`

**Testing Checkpoint:**
- ✅ Club detail screen works
- ✅ All club information displays
- ✅ Logging can be enabled/disabled

#### 4.3 MainGameMenuTable
- **Impact:** Medium (~22+ logging calls)
- **Category:** `ui.menu`
- **Files:** `MainGameMenuTable.java`

**Testing Checkpoint:**
- ✅ Main menu works
- ✅ Navigation works
- ✅ Logging can be enabled/disabled

#### 4.4 Other UI Systems
- **Impact:** Low-Medium
- **Files:**
  - `InboxScreenTable.java` (~32 calls) - `ui.inbox`
  - `JobBoardScreenTable.java` (~76 calls) - `ui.jobs`
  - `CompetitionsScreenTable.java` (~2 calls) - `ui.competitions`
  - `FixturesTable.java` (~7 calls) - `ui.schedule`
  - `LeagueDrawScreenTable.java` (~17 calls) - `ui.draw`
  - `MatchPreviewScreenTable.java` (~1 call) - `ui.match`

**Testing Checkpoint:**
- ✅ Each screen works correctly
- ✅ Logging can be enabled/disabled per screen

**Total Estimated Time:** 4-6 hours

---

### Phase 5: Remaining Engine Systems ⚙️ (Complete Engine)

**Goal:** Migrate remaining engine systems

**Systems to Migrate:**

#### 5.1 MatchSimulator
- **Category:** `engine.match`
- **Files:** `MatchSimulator.java`

#### 5.2 CompetitionScheduler
- **Category:** `engine.scheduling`
- **Files:** `CompetitionScheduler.java`

#### 5.3 MessageManager
- **Category:** `engine.messages`
- **Files:** `MessageManager.java`

#### 5.4 ScriptsManager
- **Category:** `engine.scripts`
- **Files:** `ScriptsManager.java`

#### 5.5 JobManager
- **Category:** `engine.jobs`
- **Files:** `JobManager.java`

#### 5.6 CupBracketGenerator
- **Category:** `engine.cup`
- **Files:** `CupBracketGenerator.java`

**Testing Checkpoint:**
- ✅ Each system works correctly
- ✅ Logging can be enabled/disabled

**Total Estimated Time:** 3-4 hours

---

### Phase 6: Data Loading Systems 📂 (Data Operations)

**Goal:** Migrate data loading and generation systems

**Systems to Migrate:**

#### 6.1 Data Loaders
- **Category:** `data.loading`
- **Files:**
  - `PlayersLoader.java` (~12 calls)
  - `StadiumsLoader.java` (~8 calls)
  - `ClubsLoader.java` (~8 calls)
  - `CompetitionsLoader.java` (~6 calls)
  - `SeasonsLoader.java` (~3 calls)
  - `DatabaseLoader.java` (~14 calls)

#### 6.2 Save/Load System
- **Category:** `data.save`
- **Files:** `SaveLoadSystem.java` (~14 calls)

#### 6.3 Data Generators
- **Category:** `data.generation`
- **Files:**
  - `PersonGenerator.java` (~5 calls)
  - `PlayerAttributeGenerator.java` (~2 calls)
  - `PlayerContractGenerator.java` (~5 calls)

**Testing Checkpoint:**
- ✅ Data loads correctly
- ✅ Save/load works
- ✅ Data generation works
- ✅ Logging can be enabled/disabled

**Total Estimated Time:** 2-3 hours

---

### Phase 7: Remaining Systems 🧹 (Cleanup)

**Goal:** Migrate any remaining systems

**Systems to Migrate:**
- Any remaining files with logging calls
- System initialization logging (`system.init`)
- Configuration logging (`system.config`)

**Testing Checkpoint:**
- ✅ All systems work correctly
- ✅ All logging migrated
- ✅ No System.out.println or Gdx.app.log calls remain (except in DebugLogManager)

**Total Estimated Time:** 2-3 hours

---

### Phase 8: Testing & Validation ✅ (Final)

**Goal:** Comprehensive testing and validation

**Tasks:**
1. **Functional Testing**
   - Test each system with logging enabled
   - Test each system with logging disabled
   - Test mixed settings (some enabled, some disabled)
   - Test edge cases (all enabled, all disabled)

2. **Performance Testing**
   - Measure performance with all logging enabled
   - Measure performance with all logging disabled
   - Compare to baseline
   - Document performance improvements

3. **Settings Persistence Testing**
   - Test settings persist across game restarts
   - Test settings load correctly
   - Test default settings

4. **Regression Testing**
   - Ensure no functionality broken
   - Ensure no crashes
   - Ensure game progression works

**Testing Checkpoint:**
- ✅ All systems functional
- ✅ Performance improved
- ✅ Settings persist correctly
- ✅ No regressions

**Total Estimated Time:** 4-6 hours

---

## Migration Strategy Per System

### Step-by-Step Process

For each system/screen:

1. **Identify Logging Calls**
   ```bash
   # Use grep to find all logging calls in the file
   grep -n "System.out.println\|Gdx.app.log\|Gdx.app.debug" FileName.java
   ```

2. **Determine Category**
   - Match file to category (see category definitions)
   - Use appropriate category constant

3. **Replace Logging Calls**
   ```java
   // OLD:
   System.out.println("Message");
   
   // NEW:
   DebugLogManager.getInstance().log(CATEGORY, "Message");
   ```

4. **Test the System**
   - Run the game
   - Test the specific system/screen
   - Verify logging works when enabled
   - Verify no logging when disabled
   - Verify functionality still works

5. **Commit Changes**
   ```bash
   git add FileName.java
   git commit -m "Migrate FileName logging to DebugLogManager (category)"
   ```

6. **Move to Next System**

---

## Testing Strategy

### Per-System Testing

After migrating each system:

1. **Enable Logging for That Category**
   - Go to Settings > Debug Logging
   - Enable the category
   - Test the system
   - Verify logs appear

2. **Disable Logging for That Category**
   - Disable the category
   - Test the system
   - Verify no logs appear
   - Verify functionality still works

3. **Test Performance**
   - Compare performance with logging on/off
   - Note any improvements

### Integration Testing

After each phase:

1. **Test All Systems in Phase**
   - Enable all categories in phase
   - Test all systems
   - Disable all categories in phase
   - Test all systems

2. **Test Mixed Settings**
   - Enable some categories, disable others
   - Test all systems
   - Verify correct behavior

### Final Testing

After all phases:

1. **Comprehensive Test**
   - Test entire game flow
   - Test all major features
   - Test with all logging enabled
   - Test with all logging disabled
   - Test with mixed settings

2. **Performance Validation**
   - Measure overall performance improvement
   - Document results

---

## Success Criteria

### Phase 1 (Core Infrastructure)
- ✅ DebugLogManager created and functional
- ✅ Settings persistence works
- ✅ Unit tests pass

### Phase 2 (Settings UI)
- ✅ Settings screen shows debug logging section
- ✅ All categories configurable
- ✅ Settings persist correctly

### Phase 3-7 (Migration)
- ✅ All high-impact systems migrated
- ✅ All systems functional
- ✅ Logging can be enabled/disabled per category
- ✅ Performance improvement measurable

### Phase 8 (Final Testing)
- ✅ All systems tested
- ✅ No regressions
- ✅ Performance improved
- ✅ Settings work correctly

---

## Estimated Total Time

- **Phase 1:** 2-3 hours
- **Phase 2:** 3-4 hours
- **Phase 3:** 6-8 hours
- **Phase 4:** 4-6 hours
- **Phase 5:** 3-4 hours
- **Phase 6:** 2-3 hours
- **Phase 7:** 2-3 hours
- **Phase 8:** 4-6 hours

**Total:** 26-37 hours

**With Testing Between Phases:** ~30-40 hours

---

## Risk Mitigation

### Risks

1. **Breaking Existing Functionality**
   - **Mitigation:** Test each system after migration
   - **Mitigation:** Keep old logging as fallback initially (optional)

2. **Performance Not Improved**
   - **Mitigation:** Measure before/after
   - **Mitigation:** Optimize DebugLogManager if needed

3. **Settings Not Persisting**
   - **Mitigation:** Test settings persistence early
   - **Mitigation:** Use PreferencesManager (already tested)

4. **Migration Takes Too Long**
   - **Mitigation:** Prioritize high-impact systems first
   - **Mitigation:** Migrate in phases with testing

---

## Notes

- **Default Behavior:** All logging enabled (backward compatibility)
- **Error Logging:** Always enabled (safety)
- **Testing:** Test after each system migration
- **Commits:** Commit after each system (or small group of related systems)
- **Documentation:** Update as we go if categories change

---

## Next Steps

1. **Review Plans** - Review analysis, design, and implementation plan
2. **Start Phase 1** - Create DebugLogManager
3. **Test Phase 1** - Verify core infrastructure works
4. **Start Phase 2** - Create Settings UI
5. **Test Phase 2** - Verify UI works
6. **Begin Migration** - Start with Phase 3 (high-impact systems)
7. **Test & Iterate** - Test each system as it's migrated
