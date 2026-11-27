package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;

class PlayersLoaderTests {

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
        com.rndmodgames.futtoboru.BaseTest.awaitHeadlessCreate(); // create() must finish: DatabaseLoader publishes its instance before loading
    }
    
    @Test
    void loadPlayersTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        /**
         * Iterate all clubs and make sure all players are there
         */
        Country england = DatabaseLoader.getCountryByDataId("england");
        
        List<Club> allClubs = dbLoader.getClubsByCountry(england);
        
        for (Club club : allClubs) {
            
            //
            System.out.println("Club: " + club.getName() + ", Players: " + club.getPlayers().size());
            
            // players come from squads.json, each with a contract
            assertTrue(club.getPlayers().size() >= 10, club.getName() + " has " + club.getPlayers().size() + " players");
            assertEquals(club.getPlayers().size(), club.getPlayerContracts().size());
        }
        
        System.out.println("All " + allClubs.size() + " Clubs Have a Players File.");
    }
}