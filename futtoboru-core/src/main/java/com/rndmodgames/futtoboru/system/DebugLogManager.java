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
 * Performance: Minimal overhead when logging is disabled (simple boolean check).
 * 
 * @author Geomancer86
 */
public class DebugLogManager {
    
    private static DebugLogManager instance;
    private com.badlogic.gdx.Preferences preferences; // Direct reference to ensure same instance
    private Map<String, Boolean> categoryStates;
    private boolean allEnabled; // Master switch
    
    // ============================================
    // CATEGORY CONSTANTS
    // ============================================
    
    // Engine Categories
    public static final String CATEGORY_ENGINE_GAME = "engine.game";
    public static final String CATEGORY_ENGINE_MATCH = "engine.match";
    public static final String CATEGORY_ENGINE_CUP = "engine.cup";
    public static final String CATEGORY_ENGINE_LEAGUE = "engine.league";
    public static final String CATEGORY_ENGINE_SCHEDULING = "engine.scheduling";
    public static final String CATEGORY_ENGINE_MESSAGES = "engine.messages";
    public static final String CATEGORY_ENGINE_JOBS = "engine.jobs";
    public static final String CATEGORY_ENGINE_SCRIPTS = "engine.scripts";
    public static final String CATEGORY_ENGINE_AUTHORITY = "engine.authority";
    
    // UI Categories
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
    
    // Data Categories
    public static final String CATEGORY_DATA_LOADING = "data.loading";
    public static final String CATEGORY_DATA_SAVE = "data.save";
    public static final String CATEGORY_DATA_GENERATION = "data.generation";
    
    // System Categories
    public static final String CATEGORY_SYSTEM_INIT = "system.init";
    public static final String CATEGORY_SYSTEM_CONFIG = "system.config";
    
    // ============================================
    // PREFERENCES KEYS
    // ============================================
    
    private static final String PREF_ALL_ENABLED = "debug.logging.all_enabled";
    private static final String PREF_PREFIX = "debug.logging.category.";
    
    // ============================================
    // CONSTANTS
    // ============================================
    
    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";
    
    // ============================================
    // CONSTRUCTOR & SINGLETON
    // ============================================
    
    private DebugLogManager() {
        // Get Preferences directly - do NOT create PreferencesManager here to avoid circular dependency
        // PreferencesManager may call DebugLogManager.getInstance() during its construction
        preferences = Gdx.app.getPreferences(PreferencesManager.PREFERENCES_NAME);
        categoryStates = new HashMap<>();
        // Ensure Preferences object is valid
        if (preferences == null) {
            System.err.println("ERROR: Preferences object is null in DebugLogManager constructor!");
        }
        loadSettings();
    }
    
    public static DebugLogManager getInstance() {
        if (instance == null) {
            instance = new DebugLogManager();
        }
        return instance;
    }
    
    // ============================================
    // LOGGING METHODS
    // ============================================
    
    /**
     * Check if logging is enabled for a category
     * 
     * @param category The logging category
     * @return true if logging is enabled for this category
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
     * Get the raw category state (for UI display purposes)
     * This ignores the master switch and returns the actual saved state
     * 
     * @param category The logging category
     * @return true if category is enabled (regardless of master switch)
     */
    public boolean getCategoryState(String category) {
        Boolean categoryEnabled = categoryStates.get(category);
        return categoryEnabled != null ? categoryEnabled : true; // Default: enabled
    }
    
    /**
     * Log a message (INFO level)
     * 
     * @param category The logging category
     * @param message The message to log
     */
    public void log(String category, String message) {
        if (isEnabled(category)) {
            System.out.println("[" + category + "] " + message);
        }
    }
    
    /**
     * Log a message with tag (for Gdx.app.log compatibility)
     * 
     * @param category The logging category
     * @param tag The log tag (e.g., class name)
     * @param message The message to log
     */
    public void log(String category, String tag, String message) {
        if (isEnabled(category)) {
            Gdx.app.log(tag, message);
            System.out.println("[" + category + "][" + tag + "] " + message);
        }
    }
    
    /**
     * Log a debug message
     * 
     * @param category The logging category
     * @param message The message to log
     */
    public void debug(String category, String message) {
        if (isEnabled(category)) {
            Gdx.app.debug("DEBUG", "[" + category + "] " + message);
            System.out.println("[DEBUG][" + category + "] " + message);
        }
    }
    
    /**
     * Log a debug message with tag
     * 
     * @param category The logging category
     * @param tag The log tag
     * @param message The message to log
     */
    public void debug(String category, String tag, String message) {
        if (isEnabled(category)) {
            Gdx.app.debug(tag, "[" + category + "] " + message);
            System.out.println("[DEBUG][" + category + "][" + tag + "] " + message);
        }
    }
    
    /**
     * Log a warning message
     * 
     * @param category The logging category
     * @param message The message to log
     */
    public void warn(String category, String message) {
        if (isEnabled(category)) {
            Gdx.app.log("WARN", "[" + category + "] " + message);
            System.out.println("[WARN][" + category + "] " + message);
        }
    }
    
    /**
     * Log a warning message with tag
     * 
     * @param category The logging category
     * @param tag The log tag
     * @param message The message to log
     */
    public void warn(String category, String tag, String message) {
        if (isEnabled(category)) {
            Gdx.app.log(tag, "[WARN][" + category + "] " + message);
            System.out.println("[WARN][" + category + "][" + tag + "] " + message);
        }
    }
    
    /**
     * Log an error message (always enabled for safety)
     * 
     * @param category The logging category
     * @param message The message to log
     */
    public void error(String category, String message) {
        Gdx.app.error("ERROR", "[" + category + "] " + message);
        System.err.println("[ERROR][" + category + "] " + message);
    }
    
    /**
     * Log an error message with tag (always enabled for safety)
     * 
     * @param category The logging category
     * @param tag The log tag
     * @param message The message to log
     */
    public void error(String category, String tag, String message) {
        Gdx.app.error(tag, "[" + category + "] " + message);
        System.err.println("[ERROR][" + category + "][" + tag + "] " + message);
    }
    
    /**
     * Log an error with exception (always enabled for safety)
     * 
     * @param category The logging category
     * @param message The message to log
     * @param exception The exception to log
     */
    public void error(String category, String message, Throwable exception) {
        Gdx.app.error("ERROR", "[" + category + "] " + message, exception);
        System.err.println("[ERROR][" + category + "] " + message);
        exception.printStackTrace();
    }
    
    /**
     * Log an error with exception and tag (always enabled for safety)
     * 
     * @param category The logging category
     * @param tag The log tag
     * @param message The message to log
     * @param exception The exception to log
     */
    public void error(String category, String tag, String message, Throwable exception) {
        Gdx.app.error(tag, "[" + category + "] " + message, exception);
        System.err.println("[ERROR][" + category + "][" + tag + "] " + message);
        exception.printStackTrace();
    }
    
    // ============================================
    // SETTINGS MANAGEMENT
    // ============================================
    
    /**
     * Enable/disable logging for a category
     * 
     * @param category The logging category
     * @param enabled true to enable, false to disable
     */
    public void setCategoryEnabled(String category, boolean enabled) {
        System.out.println("DEBUG: setCategoryEnabled(" + category + ", " + enabled + ")");
        categoryStates.put(category, enabled);
        saveCategorySetting(category, enabled);
        // Verify it was saved immediately using same Preferences instance
        String key = PREF_PREFIX + category;
        String savedValue = preferences.getString(key, "NOT_FOUND");
        System.out.println("DEBUG: After save, preference key=" + key + ", value=" + savedValue);
    }
    
    /**
     * Enable/disable all logging (master switch)
     * 
     * @param enabled true to enable all, false to disable all
     */
    public void setAllEnabled(boolean enabled) {
        allEnabled = enabled;
        // Write directly to preferences and flush immediately (same pattern as individual categories)
        preferences.putString(PREF_ALL_ENABLED, enabled ? ENABLED : DISABLED);
        preferences.flush();
    }
    
    /**
     * Check if all logging is enabled (master switch)
     * 
     * @return true if master switch is enabled
     */
    public boolean isAllEnabled() {
        return allEnabled;
    }
    
    /**
     * Get all available categories
     * 
     * @return Array of all category strings
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
     * Get user-friendly display name for a category
     * 
     * @param category The category string
     * @return Display name for the category
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
     * Get category group (for UI organization)
     * 
     * @param category The category string
     * @return Group name (Engine, UI, Data, System)
     */
    public String getCategoryGroup(String category) {
        if (category.startsWith("engine.")) {
            return "Engine";
        } else if (category.startsWith("ui.")) {
            return "UI";
        } else if (category.startsWith("data.")) {
            return "Data";
        } else if (category.startsWith("system.")) {
            return "System";
        }
        return "Other";
    }
    
    // ============================================
    // PREFERENCES INTEGRATION
    // ============================================
    
    /**
     * Load settings from preferences
     */
    private void loadSettings() {
        System.out.println("DEBUG: loadSettings() called");
        
        // Ensure we have a valid Preferences instance
        if (preferences == null) {
            preferences = Gdx.app.getPreferences(PreferencesManager.PREFERENCES_NAME);
            System.out.println("DEBUG: Re-obtained Preferences instance");
        }
        
        // Load master switch (default: enabled for backward compatibility)
        String allEnabledStr = preferences.getString(PREF_ALL_ENABLED, null);
        if (allEnabledStr == null || allEnabledStr.trim().isEmpty()) {
            allEnabled = true; // Default: enabled
        } else {
            allEnabled = ENABLED.equals(allEnabledStr.trim());
        }
        System.out.println("DEBUG: Master switch loaded: " + allEnabled);
        
        // Load category settings
        for (String category : getAllCategories()) {
            String key = PREF_PREFIX + category;
            // Read directly from preferences with explicit default
            String value = preferences.getString(key, null);
            
            System.out.println("DEBUG: Loading category " + category + " - key=" + key + ", raw value=" + value);
            
            // Default: enabled (for backward compatibility)
            // Explicitly check for "disabled" to handle null, empty string, and "disabled" cases
            boolean enabled;
            if (value == null || value.trim().isEmpty()) {
                enabled = true; // Default: enabled (key doesn't exist or is empty)
                System.out.println("DEBUG: Category " + category + " - no value found, defaulting to enabled");
            } else {
                String trimmedValue = value.trim();
                if (DISABLED.equals(trimmedValue)) {
                    enabled = false; // Explicitly disabled
                    System.out.println("DEBUG: Category " + category + " - found DISABLED, setting to false");
                } else {
                    enabled = true; // Explicitly enabled or default to enabled
                    System.out.println("DEBUG: Category " + category + " - found " + trimmedValue + ", setting to true");
                }
            }
            
            categoryStates.put(category, enabled);
            System.out.println("DEBUG: Category " + category + " final state: " + enabled);
        }
    }
    
    /**
     * Save category setting to preferences
     * 
     * @param category The category
     * @param enabled Whether it's enabled
     */
    private void saveCategorySetting(String category, boolean enabled) {
        String key = PREF_PREFIX + category;
        String value = enabled ? ENABLED : DISABLED;
        
        System.out.println("DEBUG: saveCategorySetting(" + category + ", " + enabled + ") - key=" + key + ", value=" + value);
        
        // CRITICAL: Save directly to Preferences (same instance) and flush immediately
        // Use the same pattern as setAllEnabled() which works correctly
        preferences.putString(key, value);
        preferences.flush();
        
        // Verify immediately after flush
        String verifyValue = preferences.getString(key, "NOT_FOUND");
        System.out.println("DEBUG: After flush, read back value=" + verifyValue + " (expected=" + value + ")");
        if (!value.equals(verifyValue)) {
            System.err.println("ERROR: Save verification failed! Expected " + value + " but got " + verifyValue);
        }
    }
    
    /**
     * Force reload settings from preferences (useful after settings screen changes)
     */
    public void reloadSettings() {
        System.out.println("DEBUG: reloadSettings() called");
        // Ensure we're using the current Preferences instance (re-obtain to be safe)
        preferences = Gdx.app.getPreferences(PreferencesManager.PREFERENCES_NAME);
        loadSettings();
    }
}
