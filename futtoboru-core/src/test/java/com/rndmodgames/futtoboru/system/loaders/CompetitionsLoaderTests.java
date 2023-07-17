package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
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
         * Load the Competitions from File
         */
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // FA Cup should be loaded
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        
        assertNotNull(allCompetitions);
        
        Competition faCup = allCompetitions.get(0);
        
        assertNotNull(faCup);

        //
        Gdx.app.debug("CompetitionsLoaderTests", "Total Competitions Loaded are: " + allCompetitions.size());
    }
    
    @Test
    void loadCompetitionHistoryTest() {
        
        // 
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        /**
         * The Competition FA Cup should Exist
         */
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        
        Competition faCup = allCompetitions.get(0);
        
        /**
         * TODO: we need the current edition of the cup with:
         *          - edition number
         *          - name
         *          - description
         *          - start date
         *          - end date
         *          - invited teams
         *          - participant teams
         *          
         *  32 participants, 5 rounds through the finals
         */
        assertNotNull(faCup.getEditions());
        
        //
        Gdx.app.debug("CompetitionsLoaderTests", faCup.getName() + " has: " + faCup.getEditions().size() + " saved Editions");

        // Participant Clubs exist
        assertNotNull(faCup.getEditions().get(0).getParticipantClubsIds());
        
        // 32 Participant Clubs
        assertEquals(32, faCup.getEditions().get(0).getParticipantClubsIds().size());
        
        //
        Gdx.app.debug("CompetitionsLoaderTests", faCup.getEditions().get(0).getName() + " has: " + faCup.getEditions().get(0).getParticipantClubsIds().size() + " Participant Clubs");
    }
}