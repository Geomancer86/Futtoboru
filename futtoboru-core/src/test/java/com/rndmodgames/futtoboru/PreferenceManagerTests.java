package com.rndmodgames.futtoboru;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.PreferencesManager;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiScale;

class PreferenceManagerTests {

    private static Futtoboru application;
    
    //
    static FuttoboruGameEngine gameEngine;
    static ScriptsManager scriptsManager;
    static AuthorityManager authorityManager;
    
    @BeforeAll
    static void preload() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;

        // 
        application = new Futtoboru();
        
        // Initialize the Saved Game
        application.setCurrentGame(new SaveGame());

        // Initialize Script Manager
        scriptsManager = new ScriptsManager(application);

        // Initialize the Authority Manager
        authorityManager = new AuthorityManager(application);
        
        // Initialize Game Engine
        gameEngine = new FuttoboruGameEngine(application, scriptsManager, authorityManager);
        
        // Set the Script Manager
        application.setScriptsManager(scriptsManager);
        
        // Set the Game Engine
        application.setGameEngine(gameEngine);

        new HeadlessApplication(application , config);
        com.rndmodgames.futtoboru.BaseTest.awaitHeadlessCreate(); // create() must finish: DatabaseLoader publishes its instance before loading
    }
    
    @Test
    void preferencesManagerTest() {

        // This triggers the PreferenceManager instance
        application.create();
        
        assertNotNull(application.preferences);
        assertEquals(false, application.preferences.getFullscreen());
        assertNotNull(application.preferences.getResolution());

        // FULLSCREEN defaults to false
        Gdx.app.log("PreferenceManagerTests", "Basic Preference Manager working, FULLSCREEN is: " + application.preferences.getFullscreen());
        
        // Set the default resolution
        application.preferences.updateScreenResolution(PreferencesManager.DEFAULT_RESOLUTION);
        
        //
        Gdx.app.log("PreferenceManagerTests", "Basic Preference Manager working, RESOLUTION is: " + application.preferences.getResolution());
        
        // Resolution should be set to the default
        assertEquals(PreferencesManager.DEFAULT_RESOLUTION, application.preferences.getResolution());
    }
    
    @Test
    void oldSmallResolutionBecomes1280x720AndUiScaleRoundTrips() {

        PreferencesManager preferences = application.preferences != null ? application.preferences : new PreferencesManager();
        String savedResolution = preferences.getPreference(PreferencesManager.SETTINGS_RESOLUTION_PREF);
        String savedScale = preferences.getPreference(PreferencesManager.SETTINGS_UI_SCALE_PREF);
        try {
            // D18: an old sub-720p preference is replaced by the 1280x720 default
            preferences.updateScreenResolution("640x480");
            assertEquals("1280x720", preferences.getResolution());
            assertEquals("1280x720", preferences.getPreference(PreferencesManager.SETTINGS_RESOLUTION_PREF));

            // U-03a: Auto by default, then 200% / 100% saved; odd values snap to an integer step
            preferences.updateUserPreference(PreferencesManager.SETTINGS_UI_SCALE_PREF, "");
            assertEquals(UiScale.AUTO, preferences.getUiScale(), 0f);
            preferences.updateUiScale(2f);
            assertEquals(2f, preferences.getUiScale(), 0f);
            preferences.updateUiScale(1.25f);
            assertEquals(1f, preferences.getUiScale(), 0f);
            preferences.updateUserPreference(PreferencesManager.SETTINGS_UI_SCALE_PREF, "big");
            assertEquals(UiScale.AUTO, preferences.getUiScale(), 0f);
        } finally {
            preferences.updateUserPreference(PreferencesManager.SETTINGS_RESOLUTION_PREF, savedResolution == null ? "" : savedResolution);
            preferences.updateUserPreference(PreferencesManager.SETTINGS_UI_SCALE_PREF, savedScale == null ? "" : savedScale);
        }
    }

    @Test
    void savePreferenceTest() {
        
        //
        String settingsResolutionPref = application.preferences.getPreference(PreferencesManager.SETTINGS_RESOLUTION_PREF);
        
        //
        assertNotNull(settingsResolutionPref);
        
        //
        Gdx.app.log("PreferenceManagerTests", "settingsResolutionPref: " + settingsResolutionPref);
    }
}