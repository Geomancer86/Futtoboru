package com.rndmodgames.futtoboru.engine.temporal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTestTools;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

public class CompetitionSchedulerTests {

    private static Futtoboru application;
    
    //
    static ScriptsManager scriptsManager;
    static FuttoboruGameEngine gameEngine;
    
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

        // Initialize Game Engine
        gameEngine = new FuttoboruGameEngine(application, scriptsManager);
        
        // Set the Script Manager
        application.setScriptsManager(scriptsManager);
        
        // Set the Game Engine
        application.setGameEngine(gameEngine);
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void initializeCompetitionsTest() {
        
        /**
         * This makes sure a new SaveGame has competitions for the selected clubs/countries during NEW GAME
         */
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // Base Country
        Country england = DatabaseLoader.getCountryById(1000L);
        
        // Initialize savegame
        BaseTestTools tools = new BaseTestTools();
        tools.initializeV1SaveGame(application.getCurrentGame());
        
        //
        application.getCurrentGame().getSelectedCountries().add(england);
        application.getCurrentGame().setAllClubs(DatabaseLoader.getInstance().getClubsByCountry(england));

        
        // Without advancing the date, the clubs should be attached to a competition
        // BUT NO A SEASON
        Gdx.app.debug("CompetitionSchedulerTests", "All Clubs: " + application.getCurrentGame().getAllClubs().size());
        
        Competition faCup = DatabaseLoader.getCompetitions().get(0);
        
        //
        assertNotNull(faCup);
        
        //
//        assertNotNull(saveGame.getAllCups());
//        assertNotEquals(true, saveGame.getAllCups().isEmpty());
    }
    
    @Test
    void basicCompetitionDrawTest() {
        
        // Init the DataBase
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        /**
         * This should start the game, progress time as needed and the 18th Edition for the FA Cup
         *  will have their 32 teams picked and the playoff/rounds drawn.
         *  
         *  The test must make sure days go by, matches get played, rematches get played, and the rounds progress
         *      until the finals and a winner is chosen
         */
        
        // Base Country
        Country england = DatabaseLoader.getCountryById(1000L);
        
        assertNotNull(england);
        
        // Base Club
        Club preston = DatabaseLoader.getClubById(1L);
        
        assertNotNull(preston);
        
        // Base Competition
        Competition faCup = DatabaseLoader.getCompetitions().get(0);
        
        assertNotNull(faCup);

        // Competition Season
        CompetitionEdition faCupSeason = faCup.getEditions().get(0);
        
        assertNotNull(faCupSeason);
        
        // Initialize savegame
        BaseTestTools tools = new BaseTestTools();
        tools.initializeV1SaveGame(application.getCurrentGame());


        int daysToContinue = 7;
        
        for (int a = 0; a < daysToContinue; a++){
            
            // Progress game time
            // NOTE: scripts are checked there
            application.getGameEngine().continueGame();
        }
        
        // Preston should have scheduled matches in the FA Cup
//        assertNotEquals(true, preston.getScheduledMatches().isEmpty());
        
        // 
        Gdx.app.debug("CompetitionSchedulerTests", "Club               : " + preston.getName());
        Gdx.app.debug("CompetitionSchedulerTests", "Competition        : " + faCup.getName());
        Gdx.app.debug("CompetitionSchedulerTests", "Competition Edition: " + faCupSeason.getName());
        Gdx.app.debug("CompetitionSchedulerTests", "Club Matches       : " + preston.getScheduledMatches().size());
    }
}