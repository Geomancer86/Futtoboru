package com.rndmodgames.futtoboru;

import org.junit.jupiter.api.BeforeAll;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

public class BaseTest {
    
    //
    protected static Futtoboru application;
    
    //
    protected static FuttoboruGameEngine gameEngine;
    protected static ScriptsManager scriptsManager;
    protected static AuthorityManager authorityManager;
    
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
        
        new HeadlessApplication(application, config);
    }
    
}