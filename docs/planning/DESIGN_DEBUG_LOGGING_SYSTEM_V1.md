# Debug Logging System Design v1.0

**Branch:** `feature/debug-logging-settings`  
**Date:** 2025-01-XX  
**Purpose:** Detailed design for configurable debug logging system

---

## Overview

This document provides detailed design specifications for the Debug Logging System, including class structure, API design, settings persistence, and UI implementation.

---

## Class Structure

### DebugLogManager (Singleton)

**Location:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/system/DebugLogManager.java`

**Purpose:** Centralized logging manager with category-based filtering

**Design:**
```java
package com.rndmodgames.futtoboru.system;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.rndmodgames.PreferencesManager;

/**
 * Debug Logging Manager v1.0
 * 
 * Centralized logging system with category-based filtering.
 * Allows users to enable/disable logging per system/screen via Settings.
 * 
 * @author Geomancer86
 */
public class DebugLogManager {
    
    private static DebugLogManager instance;
    private PreferencesManager preferencesManager;
    private Map<String, Boolean> categoryStates;
    private boolean allEnabled; // Master switch
    
    // Category Constants
    public static final String CATEGORY_ENGINE_GAME = "engine.game";
    public static final String CATEGORY_ENGINE_MATCH = "engine.match";
    public static final String CATEGORY_ENGINE_CUP = "engine.cup";
    public static final String CATEGORY_ENGINE_LEAGUE = "engine.league";
    public static final String CATEGORY_ENGINE_SCHEDULING = "engine.scheduling";
    public static final String CATEGORY_ENGINE_MESSAGES = "engine.messages";
    public static final String CATEGORY_ENGINE_JOBS = "engine.jobs";
    public static final String CATEGORY_ENGINE_SCRIPTS = "engine.scripts";
    public static final String CATEGORY_ENGINE_AUTHORITY = "engine.authority";
    
    public static final String CATEGORY_UI_CUP = "ui.cup";
    public static final String CATEGORY_UI_LEAGUE = "ui.league";
    public static final String CATEGORY_UI_CLUB = "ui.club";
    public static final String CATEGORY_UI_MATCH = "ui.match";
    public static final String CATEGORY_UI_JOBS = "ui.jobs";
    public static final String CATEGORY_UI_INBOX = "ui.inbox";
    public static final String CATEGORY_UI_COMPETITIONS = "ui.competitions";
    public static final String CATEGORY_UI_SCHEDULE = "ui.schedule";
    public static final String CATEGORY_UI_DRAW = "ui.draw";
    public static final String CATEGORY_UI_MENU = "ui.menu";
    
    public static final String CATEGORY_DATA_LOADING = "data.loading";
    public static final String CATEGORY_DATA_SAVE = "data.save";
    public static final String CATEGORY_DATA_GENERATION = "data.generation";
    
    public static final String CATEGORY_SYSTEM_INIT = "system.init";
    public static final String CATEGORY_SYSTEM_CONFIG = "system.config";
    
    // Preferences Keys
    private static final String PREF_ALL_ENABLED = "debug.logging.all_enabled";
    private static final String PREF_PREFIX = "debug.logging.category.";
    
    private DebugLogManager() {
        preferencesManager = new PreferencesManager();
        categoryStates = new HashMap<>();
        loadSettings();
    }
    
    public static DebugLogManager getInstance() {
        if (instance == null) {
            instance = new DebugLogManager();
        }
        return instance;
    }
    
    /**
     * Check if logging is enabled for a category
     */
    public boolean isEnabled(String category) {
        // Master switch: if all disabled, return false immediately
        if (!allEnabled) {
            return false;
        }
        
        // Check category-specific setting (default: enabled)
        Boolean categoryEnabled = categoryStates.get(category);
        return categoryEnabled != null ? categoryEnabled : true;
    }
    
    /**
     * Log a message (INFO level)
     */
    public void log(String category, String message) {
        if (isEnabled(category)) {
            System.out.println("[" + category + "] " + message);
        }
    }
    
    /**
     * Log a message with tag (for Gdx.app.log compatibility)
     */
    public void log(String category, String tag, String message) {
        if (isEnabled(category)) {
            Gdx.app.log(tag, message);
            System.out.println("[" + category + "][" + tag + "] " + message);
        }
    }
    
    /**
     * Log a debug message
     */
    public void debug(String category, String message) {
        if (isEnabled(category)) {
            Gdx.app.debug("DEBUG", "[" + category + "] " + message);
            System.out.println("[DEBUG][" + category + "] " + message);
        }
    }
    
    /**
     * Log a warning message
     */
    public void warn(String category, String message) {
        if (isEnabled(category)) {
            Gdx.app.log("WARN", "[" + category + "] " + message);
            System.out.println("[WARN][" + category + "] " + message);
        }
    }
    
    /**
     * Log an error message (always enabled for safety)
     */
    public void error(String category, String message) {
        Gdx.app.error("ERROR", "[" + category + "] " + message);
        System.err.println("[ERROR][" + category + "] " + message);
    }
    
    /**
     * Log an error with exception
     */
    public void error(String category, String message, Throwable exception) {
        Gdx.app.error("ERROR", "[" + category + "] " + message, exception);
        System.err.println("[ERROR][" + category + "] " + message);
        exception.printStackTrace();
    }
    
    /**
     * Enable/disable logging for a category
     */
    public void setCategoryEnabled(String category, boolean enabled) {
        categoryStates.put(category, enabled);
        saveCategorySetting(category, enabled);
    }
    
    /**
     * Enable/disable all logging
     */
    public void setAllEnabled(boolean enabled) {
        allEnabled = enabled;
        preferencesManager.updateUserPreference(PREF_ALL_ENABLED, enabled ? "enabled" : "disabled");
        preferencesManager.saveUserPreferences();
    }
    
    /**
     * Get all categories
     */
    public String[] getAllCategories() {
        return new String[] {
            CATEGORY_ENGINE_GAME, CATEGORY_ENGINE_MATCH, CATEGORY_ENGINE_CUP,
            CATEGORY_ENGINE_LEAGUE, CATEGORY_ENGINE_SCHEDULING, CATEGORY_ENGINE_MESSAGES,
            CATEGORY_ENGINE_JOBS, CATEGORY_ENGINE_SCRIPTS, CATEGORY_ENGINE_AUTHORITY,
            CATEGORY_UI_CUP, CATEGORY_UI_LEAGUE, CATEGORY_UI_CLUB, CATEGORY_UI_MATCH,
            CATEGORY_UI_JOBS, CATEGORY_UI_INBOX, CATEGORY_UI_COMPETITIONS,
            CATEGORY_UI_SCHEDULE, CATEGORY_UI_DRAW, CATEGORY_UI_MENU,
            CATEGORY_DATA_LOADING, CATEGORY_DATA_SAVE, CATEGORY_DATA_GENERATION,
            CATEGORY_SYSTEM_INIT, CATEGORY_SYSTEM_CONFIG
        };
    }
    
    /**
     * Get display name for category
     */
    public String getCategoryDisplayName(String category) {
        // Map category to user-friendly name
        switch (category) {
            case CATEGORY_ENGINE_GAME: return "Game Engine";
            case CATEGORY_ENGINE_MATCH: return "Match Simulation";
            case CATEGORY_ENGINE_CUP: return "Cup System";
            case CATEGORY_ENGINE_LEAGUE: return "League System";
            case CATEGORY_ENGINE_SCHEDULING: return "Scheduling";
            case CATEGORY_ENGINE_MESSAGES: return "Messages";
            case CATEGORY_ENGINE_JOBS: return "Jobs";
            case CATEGORY_ENGINE_SCRIPTS: return "Scripts";
            case CATEGORY_ENGINE_AUTHORITY: return "Authority";
            case CATEGORY_UI_CUP: return "Cup Screens";
            case CATEGORY_UI_LEAGUE: return "League Screens";
            case CATEGORY_UI_CLUB: return "Club Screens";
            case CATEGORY_UI_MATCH: return "Match Screens";
            case CATEGORY_UI_JOBS: return "Job Screens";
            case CATEGORY_UI_INBOX: return "Inbox";
            case CATEGORY_UI_COMPETITIONS: return "Competitions";
            case CATEGORY_UI_SCHEDULE: return "Schedule";
            case CATEGORY_UI_DRAW: return "Draw Screens";
            case CATEGORY_UI_MENU: return "Menu Screens";
            case CATEGORY_DATA_LOADING: return "Data Loading";
            case CATEGORY_DATA_SAVE: return "Save/Load";
            case CATEGORY_DATA_GENERATION: return "Data Generation";
            case CATEGORY_SYSTEM_INIT: return "Initialization";
            case CATEGORY_SYSTEM_CONFIG: return "Configuration";
            default: return category;
        }
    }
    
    /**
     * Load settings from preferences
     */
    private void loadSettings() {
        // Load master switch (default: enabled)
        String allEnabledStr = preferencesManager.getPreference(PREF_ALL_ENABLED);
        allEnabled = (allEnabledStr == null || allEnabledStr.equals("enabled"));
        
        // Load category settings
        for (String category : getAllCategories()) {
            String key = PREF_PREFIX + category;
            String value = preferencesManager.getPreference(key);
            boolean enabled = (value == null || value.equals("enabled")); // Default: enabled
            categoryStates.put(category, enabled);
        }
    }
    
    /**
     * Save category setting to preferences
     */
    private void saveCategorySetting(String category, boolean enabled) {
        String key = PREF_PREFIX + category;
        preferencesManager.updateUserPreference(key, enabled ? "enabled" : "disabled");
    }
}
```

---

## Settings UI Component

### DebugLoggingSettingsTable

**Location:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/settings/DebugLoggingSettingsTable.java`

**Purpose:** UI component for debug logging settings in Settings Screen

**Design:**
```java
package com.rndmodgames.futtoboru.tables.settings;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Debug Logging Settings Table v1.0
 * 
 * UI component for configuring debug logging per category.
 * 
 * @author Geomancer86
 */
public class DebugLoggingSettingsTable extends VisTable {
    
    private Game game;
    private DebugLogManager logManager;
    private VisCheckBox allEnabledCheckBox;
    private VisCheckBox[] categoryCheckBoxes;
    
    public DebugLoggingSettingsTable(Game game) {
        super(true);
        this.game = game;
        this.logManager = DebugLogManager.getInstance();
        
        buildUI();
    }
    
    private void buildUI() {
        // Title
        VisLabel titleLabel = new VisLabel("DEBUG LOGGING SETTINGS");
        titleLabel.setFontScale(1.2f);
        this.add(titleLabel).colspan(2).pad(10).row();
        
        // Master switch
        allEnabledCheckBox = new VisCheckBox("Enable All Debug Logging");
        allEnabledCheckBox.setChecked(logManager.isEnabled("")); // Check if any category enabled
        allEnabledCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = allEnabledCheckBox.isChecked();
                logManager.setAllEnabled(enabled);
                updateCategoryCheckBoxes(enabled);
            }
        });
        this.add(allEnabledCheckBox).colspan(2).pad(5).row();
        
        this.row().pad(10);
        
        // Engine Systems Section
        addSection("Engine Systems", DebugLogManager.CATEGORY_ENGINE_GAME, 
            DebugLogManager.CATEGORY_ENGINE_MATCH, DebugLogManager.CATEGORY_ENGINE_CUP,
            DebugLogManager.CATEGORY_ENGINE_LEAGUE, DebugLogManager.CATEGORY_ENGINE_SCHEDULING,
            DebugLogManager.CATEGORY_ENGINE_MESSAGES, DebugLogManager.CATEGORY_ENGINE_JOBS,
            DebugLogManager.CATEGORY_ENGINE_SCRIPTS, DebugLogManager.CATEGORY_ENGINE_AUTHORITY);
        
        // UI Systems Section
        addSection("UI Systems", DebugLogManager.CATEGORY_UI_CUP, 
            DebugLogManager.CATEGORY_UI_LEAGUE, DebugLogManager.CATEGORY_UI_CLUB,
            DebugLogManager.CATEGORY_UI_MATCH, DebugLogManager.CATEGORY_UI_JOBS,
            DebugLogManager.CATEGORY_UI_INBOX, DebugLogManager.CATEGORY_UI_COMPETITIONS,
            DebugLogManager.CATEGORY_UI_SCHEDULE, DebugLogManager.CATEGORY_UI_DRAW,
            DebugLogManager.CATEGORY_UI_MENU);
        
        // Data Systems Section
        addSection("Data Systems", DebugLogManager.CATEGORY_DATA_LOADING,
            DebugLogManager.CATEGORY_DATA_SAVE, DebugLogManager.CATEGORY_DATA_GENERATION);
        
        // System Section
        addSection("System", DebugLogManager.CATEGORY_SYSTEM_INIT,
            DebugLogManager.CATEGORY_SYSTEM_CONFIG);
        
        // Buttons
        this.row().pad(10);
        VisTextButton enableAllButton = new VisTextButton("Enable All");
        enableAllButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                enableAllCategories();
            }
        });
        
        VisTextButton disableAllButton = new VisTextButton("Disable All");
        disableAllButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                disableAllCategories();
            }
        });
        
        this.add(enableAllButton).pad(5);
        this.add(disableAllButton).pad(5);
    }
    
    private void addSection(String sectionTitle, String... categories) {
        VisLabel sectionLabel = new VisLabel(sectionTitle + ":");
        sectionLabel.setFontScale(1.1f);
        this.add(sectionLabel).colspan(2).pad(5).align(com.badlogic.gdx.utils.Align.left).row();
        
        for (String category : categories) {
            VisCheckBox checkBox = new VisCheckBox(logManager.getCategoryDisplayName(category));
            checkBox.setChecked(logManager.isEnabled(category));
            checkBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    logManager.setCategoryEnabled(category, checkBox.isChecked());
                }
            });
            
            this.add(checkBox).colspan(2).pad(2).align(com.badlogic.gdx.utils.Align.left).row();
        }
        
        this.row().pad(5);
    }
    
    private void updateCategoryCheckBoxes(boolean enabled) {
        // Update all category checkboxes to match master switch
        // Implementation depends on how we store checkboxes
    }
    
    private void enableAllCategories() {
        logManager.setAllEnabled(true);
        allEnabledCheckBox.setChecked(true);
        // Update all checkboxes
    }
    
    private void disableAllCategories() {
        logManager.setAllEnabled(false);
        allEnabledCheckBox.setChecked(false);
        // Update all checkboxes
    }
}
```

---

## Settings Screen Integration

**Modify:** `SettingsScreen.java`

**Changes:**
1. Add `DebugLoggingSettingsTable` to settings panels
2. Add to main container layout

```java
// In SettingsScreen constructor:
VisTable debugLoggingSettings = new DebugLoggingSettingsTable(this.game);

// Add to main container:
mainContainer.add(debugLoggingSettings).pad(10).align(Align.top);
```

---

## PreferencesManager Integration

**Modify:** `PreferencesManager.java`

**Add Constants:**
```java
// DEBUG LOGGING SETTINGS
public static final String DEBUG_LOGGING_ALL_ENABLED = "debug.logging.all_enabled";
public static final String DEBUG_LOGGING_CATEGORY_PREFIX = "debug.logging.category.";
```

**Note:** The actual preference keys are managed by `DebugLogManager`, but we document them here for reference.

---

## Migration Examples

### Example 1: Engine System

**Before (FuttoboruGameEngine.java):**
```java
System.out.println("FuttoboruGameEngine: Simulating matches for date: " + targetDate);
```

**After:**
```java
DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, 
    "FuttoboruGameEngine", "Simulating matches for date: " + targetDate);
```

### Example 2: UI System

**Before (CupDrawScreenTable.java):**
```java
System.out.println("CupDrawScreenTable: Found " + allMatches.size() + " matches");
```

**After:**
```java
DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_CUP, 
    "CupDrawScreenTable", "Found " + allMatches.size() + " matches");
```

### Example 3: With Tag

**Before:**
```java
Gdx.app.log("CupBracketManager", "Winner advanced to next round");
```

**After:**
```java
DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_CUP, 
    "CupBracketManager", "Winner advanced to next round");
```

### Example 4: Error (Always Enabled)

**Before:**
```java
Gdx.app.error("SaveLoadSystem", "Failed to save game: " + e.getMessage());
```

**After:**
```java
DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_SAVE, 
    "SaveLoadSystem", "Failed to save game: " + e.getMessage(), e);
```

---

## Performance Considerations

### Boolean Check Optimization

The `isEnabled()` method is called before every log statement. To minimize overhead:

1. **Fast boolean check** - Simple map lookup, no string operations
2. **Early return** - Master switch check first
3. **No string concatenation** - Message only built if logging enabled

### String Concatenation

**Bad:**
```java
// String concatenation happens even if logging disabled
DebugLogManager.log(category, "Value: " + expensiveOperation());
```

**Good:**
```java
// Check first, then build message
if (DebugLogManager.getInstance().isEnabled(category)) {
    DebugLogManager.getInstance().log(category, "Value: " + expensiveOperation());
}
```

**Better (for frequent calls):**
```java
// Use method that checks internally
DebugLogManager.getInstance().log(category, "Value: " + expensiveOperation());
// Method checks internally, but string still built
```

**Best (for very frequent calls in loops):**
```java
// Cache enabled state outside loop
boolean loggingEnabled = DebugLogManager.getInstance().isEnabled(category);
if (loggingEnabled) {
    for (...) {
        DebugLogManager.getInstance().log(category, "Message");
    }
}
```

---

## Testing Strategy

### Unit Tests

1. **DebugLogManager Tests**
   - Test singleton pattern
   - Test category enable/disable
   - Test master switch
   - Test settings persistence
   - Test all logging methods

2. **Settings UI Tests**
   - Test checkbox state
   - Test enable/disable all
   - Test settings persistence

### Integration Tests

1. **Settings Screen Integration**
   - Test UI displays correctly
   - Test settings save/load
   - Test settings apply immediately

2. **Logging Integration**
   - Test logging works when enabled
   - Test logging disabled when category off
   - Test error logging always works

### Performance Tests

1. **Overhead Measurement**
   - Measure performance with all logging enabled
   - Measure performance with all logging disabled
   - Compare to baseline (current System.out.println)

---

## Future Enhancements

1. **Log Levels**
   - Add DEBUG, INFO, WARN, ERROR levels
   - Filter by level in settings

2. **File Logging**
   - Option to log to file
   - Log rotation
   - Log file size limits

3. **Advanced Filtering**
   - Filter by tag
   - Filter by message content
   - Regex filtering

4. **Performance Metrics**
   - Track logging overhead
   - Log call counts per category
   - Performance impact reporting

---

## Implementation Checklist

### Phase 1: Core Infrastructure
- [ ] Create DebugLogManager class
- [ ] Define category constants
- [ ] Implement logging methods
- [ ] Implement settings persistence
- [ ] Add unit tests
- [ ] Test basic functionality

### Phase 2: Settings UI
- [ ] Create DebugLoggingSettingsTable class
- [ ] Implement checkbox UI
- [ ] Implement enable/disable all
- [ ] Integrate with SettingsScreen
- [ ] Test UI functionality
- [ ] Test settings persistence

### Phase 3: Migration (High Priority)
- [ ] FuttoboruGameEngine
- [ ] AuthorityManager
- [ ] CupBracketManager
- [ ] LeagueFixtureGenerator
- [ ] MatchScheduler

### Phase 4: Migration (UI Systems)
- [ ] CupDrawScreenTable
- [ ] CupDetailScreenTable
- [ ] CupPlayoffsTable
- [ ] ClubDetailScreenTable
- [ ] MainGameMenuTable

### Phase 5: Migration (Other Systems)
- [ ] Remaining engine systems
- [ ] Remaining UI systems
- [ ] Data loading systems
- [ ] Other systems

### Phase 6: Testing & Validation
- [ ] Test all migrated systems
- [ ] Performance validation
- [ ] Settings persistence validation
- [ ] Edge case testing

---

## Notes

- Default behavior: All logging enabled (backward compatibility)
- Error logging always enabled (safety)
- Settings persist across game sessions
- Settings apply immediately (no restart required)
- Consider adding log level filtering in future version
