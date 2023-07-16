package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;

class CompetitionsLoaderTests {

    private static Futtoboru application;
    
    @BeforeAll
    static void beforeAll() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        
        // Initialize the Saved Game
        application.setCurrentGame(new SaveGame());
        
        // Set the Person Generator
        application.setPersonGenerator(new PersonGenerator(application));
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void loadCompetitionsTest() {
        
        /**
         * Load the Competitions from File, should have 17 historic seasons
         */
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // FA Cup should be loaded
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        
        assertNotNull(allCompetitions);
        
        Competition faCup = allCompetitions.get(0);
        
        assertNotNull(faCup);

        System.out.println("Total Competitions Loaded are: " + allCompetitions.size());
    }
    
    @Test
    void loadCompetitionHistoryTest() {
        
        /**
         * The Competition FA Cup should Exist,
         * 
         * 17 years of history should be preloaded
         */
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        
        Competition faCup = allCompetitions.get(0);
        
        //
        assertNotNull(faCup.getEditions());
        
        System.out.println(faCup.getName() + " has " + faCup.getEditions().size() + " saved Editions");
    }
}