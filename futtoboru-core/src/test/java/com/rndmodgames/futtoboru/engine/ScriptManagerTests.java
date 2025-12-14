package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTestTools;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

public class ScriptManagerTests {

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
    void leagueCreationTest() {
        
        // Init the DataBase
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // Base country
        Country england = DatabaseLoader.getCountryById(1000L);
        
        List<Club> allClubs = dbLoader.getClubsByCountry(england);
        
        Gdx.app.debug("ScriptManagerTests", "All Clubs: " + allClubs.size());

        // Initialize savegame
        BaseTestTools tools = new BaseTestTools();
        tools.initializeV1SaveGame(application.getCurrentGame(), dbLoader);

        int daysToContinue = 30;
        
        for (int a = 0; a < daysToContinue; a++){
            
            // Fire a League Creation Script
            scriptsManager.checkGameScripts();
            
            // Progress game time
            application.getGameEngine().continueGame();
        }
        
        // Assert a League was Created
        List<League> allLeagues = application.getCurrentGame().getMainAuthority().getLeagues();
        
        assertNotNull(allLeagues);
        
        assertNotEquals(true, allLeagues.isEmpty());
        
        Gdx.app.debug("ScriptManagerTests", "League Creation Script is Correct, now Leagues are: " + allLeagues.size());
    }
}