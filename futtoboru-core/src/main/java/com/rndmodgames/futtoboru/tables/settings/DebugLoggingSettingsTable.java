package com.rndmodgames.futtoboru.tables.settings;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Debug Logging Settings Table v1.0
 * 
 * UI component for configuring debug logging per category.
 * Displays checkboxes for each logging category organized by group.
 * 
 * @author Geomancer86
 */
public class DebugLoggingSettingsTable extends VisTable {
    
    private DebugLogManager logManager;
    private Map<String, VisCheckBox> categoryCheckBoxes;
    
    public DebugLoggingSettingsTable(Game game) {
        super(true);
        this.logManager = DebugLogManager.getInstance();
        this.categoryCheckBoxes = new HashMap<>();
        
        buildUI();
    }
    
    private void buildUI() {
        // Title
        VisLabel titleLabel = new VisLabel("DEBUG LOGGING SETTINGS");
        titleLabel.setFontScale(1.2f);
        this.add(titleLabel).colspan(2).pad(10).row();
        
        // Remove redundant "Enable All Debug Logging" checkbox - use buttons instead
        // Master switch functionality is handled by "Enable All" / "Disable All" buttons
        
        this.row().pad(5);
        
        // Engine Systems Section
        addSection("Engine Systems", 
            DebugLogManager.CATEGORY_ENGINE_GAME, 
            DebugLogManager.CATEGORY_ENGINE_MATCH, 
            DebugLogManager.CATEGORY_ENGINE_CUP,
            DebugLogManager.CATEGORY_ENGINE_LEAGUE, 
            DebugLogManager.CATEGORY_ENGINE_SCHEDULING,
            DebugLogManager.CATEGORY_ENGINE_MESSAGES, 
            DebugLogManager.CATEGORY_ENGINE_JOBS,
            DebugLogManager.CATEGORY_ENGINE_SCRIPTS, 
            DebugLogManager.CATEGORY_ENGINE_AUTHORITY);
        
        // UI Systems Section
        addSection("UI Systems", 
            DebugLogManager.CATEGORY_UI_CUP, 
            DebugLogManager.CATEGORY_UI_LEAGUE, 
            DebugLogManager.CATEGORY_UI_CLUB,
            DebugLogManager.CATEGORY_UI_MATCH, 
            DebugLogManager.CATEGORY_UI_JOBS,
            DebugLogManager.CATEGORY_UI_INBOX, 
            DebugLogManager.CATEGORY_UI_COMPETITIONS,
            DebugLogManager.CATEGORY_UI_SCHEDULE, 
            DebugLogManager.CATEGORY_UI_DRAW,
            DebugLogManager.CATEGORY_UI_MENU);
        
        // Data Systems Section
        addSection("Data Systems", 
            DebugLogManager.CATEGORY_DATA_LOADING,
            DebugLogManager.CATEGORY_DATA_SAVE, 
            DebugLogManager.CATEGORY_DATA_GENERATION);
        
        // System Section
        addSection("System", 
            DebugLogManager.CATEGORY_SYSTEM_INIT,
            DebugLogManager.CATEGORY_SYSTEM_CONFIG);
        
        // Buttons row
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
    
    /**
     * Add a section with category checkboxes
     */
    private void addSection(String sectionTitle, String... categories) {
        VisLabel sectionLabel = new VisLabel(sectionTitle + ":");
        sectionLabel.setFontScale(1.1f);
        this.add(sectionLabel).colspan(2).pad(5).align(Align.left).row();
        
        for (String category : categories) {
            VisCheckBox checkBox = new VisCheckBox(logManager.getCategoryDisplayName(category));
            // Use getCategoryState() for initial UI state (shows actual saved state, not affected by master switch)
            checkBox.setChecked(logManager.getCategoryState(category));
            checkBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    boolean enabled = checkBox.isChecked();
                    // Save individual category state - this works independently of master switch
                    // Master switch only acts as an override (if OFF, all disabled; if ON, individual states apply)
                    logManager.setCategoryEnabled(category, enabled);
                    
                    // Don't automatically update master switch - let user control it via buttons
                    // Individual category changes should work independently
                }
            });
            
            categoryCheckBoxes.put(category, checkBox);
            
            this.add(checkBox).colspan(2).pad(2).align(Align.left).row();
        }
        
        this.row().pad(5);
    }
    
    /**
     * Update all category checkboxes to match master switch
     * NOTE: Master switch checkbox removed, this method kept for potential future use
     */
    private void updateAllCategoryCheckBoxes(boolean enabled) {
        for (VisCheckBox checkBox : categoryCheckBoxes.values()) {
            checkBox.setChecked(enabled);
        }
    }
    
    /**
     * Update master switch state based on category checkboxes
     * NOTE: Master switch checkbox removed, this method updates internal state only
     */
    private void updateMasterSwitchState() {
        boolean allChecked = true;
        boolean allUnchecked = true;
        
        for (VisCheckBox checkBox : categoryCheckBoxes.values()) {
            if (checkBox.isChecked()) {
                allUnchecked = false;
            } else {
                allChecked = false;
            }
        }
        
        // Update internal master switch state based on individual checkboxes
        // This ensures consistency between UI and internal state
        if (allChecked) {
            logManager.setAllEnabled(true);
        } else if (allUnchecked) {
            logManager.setAllEnabled(false);
        }
        // If mixed, leave master switch as is (indeterminate state)
    }
    
    /**
     * Enable all categories
     */
    private void enableAllCategories() {
        // First enable master switch
        logManager.setAllEnabled(true);
        
        // Enable all individual categories
        for (String category : logManager.getAllCategories()) {
            logManager.setCategoryEnabled(category, true);
        }
        
        // Update all checkboxes
        for (VisCheckBox checkBox : categoryCheckBoxes.values()) {
            checkBox.setChecked(true);
        }
    }
    
    /**
     * Disable all categories
     */
    private void disableAllCategories() {
        // First disable master switch
        logManager.setAllEnabled(false);
        
        // Disable all individual categories
        for (String category : logManager.getAllCategories()) {
            logManager.setCategoryEnabled(category, false);
        }
        
        // Update all checkboxes
        for (VisCheckBox checkBox : categoryCheckBoxes.values()) {
            checkBox.setChecked(false);
        }
    }
    
    /**
     * Refresh checkbox states from DebugLogManager
     * Call this when settings screen is shown to ensure UI matches current settings
     */
    public void refreshSettings() {
        System.out.println("DEBUG: refreshSettings() called");
        logManager.reloadSettings();
        
        // Master switch checkbox removed - no need to update it
        // Individual checkboxes show actual category state (getCategoryState ignores master switch for display)
        
        for (Map.Entry<String, VisCheckBox> entry : categoryCheckBoxes.entrySet()) {
            String category = entry.getKey();
            // Use getCategoryState() to show actual saved state for UI display
            // This shows the true state regardless of master switch, so users can see what they've configured
            boolean enabled = logManager.getCategoryState(category);
            System.out.println("DEBUG: Refreshing checkbox for " + category + " - setting to " + enabled + " (raw state)");
            entry.getValue().setChecked(enabled);
        }
    }
}
