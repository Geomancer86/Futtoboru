package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

public class ScriptManagerTests {

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
    void leagueCreationTest() {
        
        // Init the DataBase
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        /**
         * Iterate all clubs and make sure all stadiums are there
         */
        Country england = DatabaseLoader.getCountryById(1000L);
        
        List<Club> allClubs = dbLoader.getClubsByCountry(england);
        
        Gdx.app.debug("ScriptManagerTests", "All Clubs: " + allClubs.size());
        
        // Initialize the Save Game
        SaveGame saveGame = new SaveGame();
        saveGame.setGameDate(LocalDateTime.of(1888, Month.APRIL, 1, 4, 00, 00));
        saveGame.setOwner(new Person());
        saveGame.getOwner().setCurrentClubId(1L);

        // Main Authority
        saveGame.setMainAuthority(new Authority());
        
        // Clubs
        saveGame.setAllClubs(new ArrayList<>());
        
        // Hardcoded England ID
        saveGame.getAllClubs().addAll(DatabaseLoader.getClubsByCountry().get(1000L));
        
        // Starting Season
        saveGame.getGameScripts().addAll(dbLoader.getSeasons().get(0).getSeasonScripts());
        
        // Set a New Game as Current Game
        application.setCurrentGame(saveGame);
        
        // Current Club
        Club currentClub = application.getCurrentGame().getCurrentClub();
        
        Gdx.app.debug("ScriptManagerTests", "currentClub: " + currentClub.getName());
        
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