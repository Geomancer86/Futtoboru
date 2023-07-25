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
    void savePreferenceTest() {
        
        //
        String settingsResolutionPref = application.preferences.getPreference(PreferencesManager.SETTINGS_RESOLUTION_PREF);
        
        //
        assertNotNull(settingsResolutionPref);
        
        //
        Gdx.app.log("PreferenceManagerTests", "settingsResolutionPref: " + settingsResolutionPref);
    }
}