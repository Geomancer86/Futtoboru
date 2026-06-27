# Debug Logging System Analysis v1.0

**Branch:** `feature/debug-logging-settings`  
**Date:** 2025-01-XX  
**Purpose:** Analyze current logging system and design configurable debug logging

---

## Executive Summary

The current logging system uses a mix of `System.out.println()` and `Gdx.app.log()` calls throughout the codebase. With **1,914 logging calls across 97 files**, performance is significantly impacted, especially in loops and frequent operations. We need a centralized, configurable logging system that allows users to enable/disable logging per system/screen via the Settings Screen.

---

## Current State Analysis

### Logging Methods Used

1. **`System.out.println()`** - Most common (~1,500+ calls)
   - Direct console output
   - No filtering or categorization
   - Always executes (performance impact)

2. **`Gdx.app.log(tag, message)`** - Medium usage (~300+ calls)
   - LibGDX logging with tags
   - Can be filtered by log level
   - Better than System.out but still always executes

3. **`Gdx.app.debug(tag, message)`** - Low usage (~50+ calls)
   - Debug-level logging
   - Can be disabled via log level

4. **`Gdx.app.error(tag, message)`** - Low usage (~30+ calls)
   - Error-level logging
   - Should always be enabled

5. **`Gdx.app.warn(tag, message)`** - Very low usage (~10+ calls)
   - Warning-level logging

### Logging Distribution by System

Based on file analysis, logging is distributed across:

#### Engine Systems (High Volume)
- **FuttoboruGameEngine** - Daily simulation, match processing (~200+ calls)
- **AuthorityManager** - Competition management, scheduling (~150+ calls)
- **CupBracketManager** - Cup bracket logic, winner advancement (~45+ calls)
- **CupBracketGenerator** - Bracket generation (~13+ calls)
- **MatchSimulator** - Match simulation (~7+ calls)
- **CompetitionScheduler** - Competition scheduling (~5+ calls)
- **MatchScheduler** - Match scheduling (~22+ calls)
- **LeagueFixtureGenerator** - League fixture generation (~103+ calls)
- **MessageManager** - Message delivery (~27+ calls)
- **ScriptsManager** - Game scripts (~121+ calls)
- **JobManager** - Job system (~123+ calls)

#### UI/Screen Systems (Medium Volume)
- **CupDrawScreenTable** - Cup draw UI (~18+ calls)
- **CupDetailScreenTable** - Cup detail UI (~10+ calls)
- **CupPlayoffsTable** - Playoff bracket UI (~10+ calls)
- **ClubDetailScreenTable** - Club detail UI (~78+ calls)
- **MainGameMenuTable** - Main menu (~22+ calls)
- **MatchPreviewScreenTable** - Match preview (~1+ calls)
- **InboxScreenTable** - Inbox UI (~32+ calls)
- **JobBoardScreenTable** - Job board UI (~76+ calls)
- **CompetitionsScreenTable** - Competitions UI (~2+ calls)
- **FixturesTable** - Fixtures UI (~7+ calls)
- **LeagueDrawScreenTable** - League draw UI (~17+ calls)
- **MatchEngineDebugScreen** - Match engine debug (~9+ calls)

#### Data Loading Systems (Low-Medium Volume)
- **PlayersLoader** - Player data loading (~12+ calls)
- **StadiumsLoader** - Stadium data loading (~8+ calls)
- **ClubsLoader** - Club data loading (~8+ calls)
- **CompetitionsLoader** - Competition data loading (~6+ calls)
- **SeasonsLoader** - Season data loading (~3+ calls)
- **DatabaseLoader** - Database operations (~14+ calls)

#### Other Systems
- **SaveLoadSystem** - Save/load operations (~14+ calls)
- **PersonGenerator** - Person generation (~5+ calls)
- Various other systems with scattered logging

### Performance Impact Areas

**Critical Performance Issues:**
1. **Loops with logging** - Cup bracket generation, fixture generation, daily simulation
2. **Frequent operations** - Match scheduling checks, daily game progression
3. **UI updates** - Screen refresh cycles, dynamic component updates
4. **Data loading** - Large loops through clubs, players, matches

**Example Problem Areas:**
- `LeagueFixtureGenerator` - Logs in loops generating hundreds of fixtures
- `FuttoboruGameEngine.continueGame()` - Logs every day progression
- `CupBracketManager` - Logs every match completion, winner advancement
- `AuthorityManager` - Logs in competition scheduling loops
- UI tables - Logs on every `updateDynamicComponents()` call

---

## Requirements

### Functional Requirements

1. **Categorization**
   - Logging must be categorized by system/screen
   - Categories should be intuitive and match game structure
   - Support for hierarchical categories (e.g., "Engine > Match Simulation")

2. **Settings Screen Integration**
   - New "Debug Logging" section in Settings Screen
   - Checkboxes to enable/disable logging per category
   - "Enable All" / "Disable All" buttons
   - Settings persist across game sessions

3. **Performance**
   - Disabled logging should have minimal overhead (boolean check only)
   - No string concatenation when logging is disabled
   - Efficient category lookup

4. **Backward Compatibility**
   - Existing code should continue to work
   - Gradual migration path (screen by screen, system by system)
   - Default behavior: all logging enabled (for now)

### Non-Functional Requirements

1. **Ease of Use**
   - Simple API for developers
   - Clear category names
   - Easy to add new categories

2. **Maintainability**
   - Centralized logging logic
   - Easy to find and update logging calls
   - Consistent logging format

3. **Extensibility**
   - Easy to add new categories
   - Support for log levels (DEBUG, INFO, WARN, ERROR)
   - Future: file logging, log rotation

---

## Proposed Solution

### Architecture

```
DebugLogManager (Singleton)
├── Category Configuration (Map<String, Boolean>)
├── PreferencesManager Integration
└── Logging Methods
    ├── log(String category, String message)
    ├── log(String category, String tag, String message)
    ├── debug(String category, String message)
    ├── warn(String category, String message)
    └── error(String category, String message)
```

### Category Structure

**Engine Categories:**
- `engine.game` - General game engine
- `engine.match` - Match simulation
- `engine.cup` - Cup system
- `engine.league` - League system
- `engine.scheduling` - Match/competition scheduling
- `engine.messages` - Message system
- `engine.jobs` - Job system
- `engine.scripts` - Game scripts
- `engine.authority` - Authority/competition management

**UI Categories:**
- `ui.cup` - Cup-related screens
- `ui.league` - League-related screens
- `ui.club` - Club screens
- `ui.match` - Match screens
- `ui.jobs` - Job-related screens
- `ui.inbox` - Inbox screen
- `ui.competitions` - Competitions screen
- `ui.schedule` - Schedule/fixtures screens
- `ui.draw` - Draw screens
- `ui.menu` - Menu screens

**Data Categories:**
- `data.loading` - Data loading operations
- `data.save` - Save/load operations
- `data.generation` - Data generation

**System Categories:**
- `system.init` - System initialization
- `system.config` - Configuration

### API Design

```java
// Simple logging
DebugLogManager.log("engine.match", "Match simulated: " + matchId);

// With tag (for Gdx.app.log compatibility)
DebugLogManager.log("engine.cup", "CupBracketManager", "Winner advanced to next round");

// Debug level
DebugLogManager.debug("ui.cup", "Cup draw screen updated");

// Warning level
DebugLogManager.warn("engine.scheduling", "Match date in past: " + date);

// Error level (always enabled)
DebugLogManager.error("data.save", "Failed to save game: " + error);
```

### Settings Screen UI

**New Section: "Debug Logging Settings"**

Layout:
```
┌─────────────────────────────────────┐
│ DEBUG LOGGING SETTINGS              │
├─────────────────────────────────────┤
│ [✓] Enable All Debug Logging         │
│                                     │
│ Engine Systems:                     │
│   [✓] Game Engine                   │
│   [✓] Match Simulation              │
│   [✓] Cup System                    │
│   [✓] League System                 │
│   [✓] Scheduling                    │
│   [✓] Messages                      │
│   [✓] Jobs                          │
│   [✓] Scripts                        │
│   [✓] Authority                     │
│                                     │
│ UI Systems:                         │
│   [✓] Cup Screens                   │
│   [✓] League Screens                │
│   [✓] Club Screens                  │
│   [✓] Match Screens                 │
│   [✓] Job Screens                   │
│   [✓] Inbox                         │
│   [✓] Competitions                  │
│   [✓] Schedule                      │
│   [✓] Draw Screens                  │
│   [✓] Menu Screens                  │
│                                     │
│ Data Systems:                        │
│   [✓] Data Loading                  │
│   [✓] Save/Load                     │
│   [✓] Data Generation               │
│                                     │
│ System:                              │
│   [✓] Initialization                │
│   [✓] Configuration                 │
│                                     │
│ [Disable All] [Enable All]          │
└─────────────────────────────────────┘
```

---

## Implementation Plan

### Phase 1: Core Infrastructure
1. Create `DebugLogManager` class
2. Define category constants
3. Integrate with `PreferencesManager`
4. Implement basic logging methods
5. Add unit tests

### Phase 2: Settings Screen UI
1. Create `DebugLoggingSettingsTable` class
2. Add to `SettingsScreen`
3. Implement checkbox handlers
4. Test settings persistence

### Phase 3: Migration (Screen by Screen, System by System)

**Priority Order:**
1. **High-Volume Engine Systems** (biggest performance impact)
   - FuttoboruGameEngine
   - AuthorityManager
   - CupBracketManager
   - LeagueFixtureGenerator
   - MatchScheduler

2. **UI Systems** (user-visible, frequent updates)
   - CupDrawScreenTable
   - CupDetailScreenTable
   - CupPlayoffsTable
   - ClubDetailScreenTable
   - MainGameMenuTable

3. **Other Engine Systems**
   - MatchSimulator
   - CompetitionScheduler
   - MessageManager
   - ScriptsManager
   - JobManager

4. **Data Loading Systems**
   - PlayersLoader
   - StadiumsLoader
   - ClubsLoader
   - DatabaseLoader

5. **Remaining Systems**
   - Other UI tables
   - Save/Load system
   - Other generators

### Phase 4: Testing & Validation
1. Test each migrated system
2. Verify performance improvements
3. Verify settings persistence
4. Test edge cases (all enabled, all disabled, mixed)

---

## Migration Strategy

### For Each System/Screen:

1. **Identify all logging calls**
   ```java
   // Find all System.out.println, Gdx.app.log, etc.
   ```

2. **Determine appropriate category**
   ```java
   // Map to category (e.g., "engine.match", "ui.cup")
   ```

3. **Replace logging calls**
   ```java
   // OLD:
   System.out.println("Match simulated: " + matchId);
   
   // NEW:
   DebugLogManager.log("engine.match", "Match simulated: " + matchId);
   ```

4. **Test the system**
   - Verify logging works when enabled
   - Verify no logging when disabled
   - Verify performance improvement

5. **Commit and move to next system**

### Example Migration

**Before:**
```java
public void simulateMatch(Match match) {
    System.out.println("MatchSimulator: Simulating match " + match.getId());
    // ... simulation logic ...
    System.out.println("MatchSimulator: Match result: " + homeGoals + "-" + awayGoals);
}
```

**After:**
```java
public void simulateMatch(Match match) {
    DebugLogManager.log("engine.match", "MatchSimulator", "Simulating match " + match.getId());
    // ... simulation logic ...
    DebugLogManager.log("engine.match", "MatchSimulator", "Match result: " + homeGoals + "-" + awayGoals);
}
```

---

## Category Definitions

### Engine Categories

| Category | Description | Files |
|----------|-------------|-------|
| `engine.game` | General game engine operations | FuttoboruGameEngine |
| `engine.match` | Match simulation | MatchSimulator |
| `engine.cup` | Cup competition system | CupBracketManager, CupBracketGenerator |
| `engine.league` | League competition system | LeagueFixtureGenerator |
| `engine.scheduling` | Match/competition scheduling | CompetitionScheduler, MatchScheduler |
| `engine.messages` | Message delivery system | MessageManager |
| `engine.jobs` | Job system | JobManager |
| `engine.scripts` | Game scripts | ScriptsManager |
| `engine.authority` | Authority/competition management | AuthorityManager |

### UI Categories

| Category | Description | Files |
|----------|-------------|-------|
| `ui.cup` | Cup-related screens | CupDrawScreenTable, CupDetailScreenTable, CupPlayoffsTable |
| `ui.league` | League-related screens | LeagueDrawScreenTable |
| `ui.club` | Club screens | ClubDetailScreenTable |
| `ui.match` | Match screens | MatchPreviewScreenTable, MatchResultScreenTable, MatchHistoryScreenTable |
| `ui.jobs` | Job-related screens | JobBoardScreenTable, JobOfferScreenTable, etc. |
| `ui.inbox` | Inbox screen | InboxScreenTable |
| `ui.competitions` | Competitions screen | CompetitionsScreenTable |
| `ui.schedule` | Schedule/fixtures screens | FixturesTable |
| `ui.draw` | Draw screens | CupDrawScreenTable, LeagueDrawScreenTable |
| `ui.menu` | Menu screens | MainGameMenuTable |

### Data Categories

| Category | Description | Files |
|----------|-------------|-------|
| `data.loading` | Data loading operations | PlayersLoader, StadiumsLoader, ClubsLoader, etc. |
| `data.save` | Save/load operations | SaveLoadSystem |
| `data.generation` | Data generation | PersonGenerator, PlayerAttributeGenerator, etc. |

### System Categories

| Category | Description | Files |
|----------|-------------|-------|
| `system.init` | System initialization | Futtoboru.create(), various loaders |
| `system.config` | Configuration | PreferencesManager, SettingsScreen |

---

## Estimated Impact

### Performance Improvement

**Current State:**
- ~1,914 logging calls across 97 files
- Many in loops (fixture generation, daily simulation)
- String concatenation always executed
- Console I/O always performed

**Expected Improvement:**
- 50-90% reduction in logging overhead when disabled
- Significant improvement in:
  - Daily game progression (FuttoboruGameEngine)
  - Cup bracket operations (CupBracketManager)
  - League fixture generation (LeagueFixtureGenerator)
  - UI screen updates (various tables)

### Code Changes

- **New Files:** 2-3 (DebugLogManager, DebugLoggingSettingsTable)
- **Modified Files:** ~97 (gradual migration)
- **Lines Changed:** ~2,000-3,000 (replacing logging calls)

---

## Success Criteria

1. ✅ DebugLogManager created and functional
2. ✅ Settings Screen UI implemented
3. ✅ Settings persist across sessions
4. ✅ All high-volume systems migrated
5. ✅ Performance improvement measurable
6. ✅ No regressions in functionality
7. ✅ All logging can be enabled/disabled independently

---

## Next Steps

1. **Create Design Document** - Detailed design for DebugLogManager and Settings UI
2. **Implement Core Infrastructure** - DebugLogManager class
3. **Implement Settings UI** - DebugLoggingSettingsTable
4. **Begin Migration** - Start with highest-impact systems
5. **Test & Iterate** - Test each system as it's migrated

---

## Notes

- Default: All logging enabled (backward compatibility)
- Error-level logging should always be enabled (safety)
- Consider adding log levels (DEBUG, INFO, WARN, ERROR) in future
- Future: File logging, log rotation, log filtering by level
