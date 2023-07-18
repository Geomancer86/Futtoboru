package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.ScriptsManager;
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
        
        // Scripts Manager
        ScriptsManager scriptManager = new ScriptsManager(application);
        
        // Set Game Engine
        application.setGameEngine(new FuttoboruGameEngine(application, scriptManager));
        
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
    
    /**
     * This test iterates all the participant Clubs IDs and makes sure DatabaseLoader.getClubById returns an existing/loaded Club
     * 
     * TODO: 12 League Clubs + 20 Playoff Qualifiers
     */
    @Test
    void loadCompetitionParticipantClubsTest() {
        
        // 
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        //
        Competition faCup = DatabaseLoader.getCompetitions().get(0);
        
        assertNotNull(faCup.getEditions());
        
        //
        Gdx.app.debug("CompetitionsLoaderTests", "Testing if " + faCup.getEditions().get(0).getParticipantClubsIds().size() + " Participant Clubs exist on the Database");
        
        //
        for (Long clubId : faCup.getEditions().get(0).getParticipantClubsIds()) {
            
            Club club = DatabaseLoader.getClubById(clubId);
            
            //
            assertNotNull(club, "Club ID: " + clubId + " doesn't exist on Database");
            
            Gdx.app.debug("CompetitionsLoaderTests", "Club ID: " + clubId + ", Club: " + club.getName());
        }
    }
    
    /**
     * This calls the Authority/AI manager for the Competition and do a randomized cup draw
     * 
     *  - NOTE: the clubs in the competition are saved by historic match order, so the draw can be recreated or fully randomized for the player to choose
     */
    @Test
    void competitionCupDrawTest() {
        
        /**
         * TODO:
         *  - basic cup draw with exact number of teams (no byes)
         *      - 1888-89 FA Cup had playoffs that were played with byes/etc until enough clubs were qualified to enter (league teams + qualified = 32 clubs).
         *      
         *  WIP:
         *      - 1: authority does the club draw
         *      - 2: authority generates matches between clubs, with order but no scheduled date besides (start - end) for the cup and makes sure all matches are played
         *      - 3: if a match is tied, a rematch is played 7 days later
         *      
         *      - 4: once no rematchs are left, the competition advances to the next round
         *          - round numbers calculated automatically depending on participant clubs number
         */
        
        // 
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        //
        Competition faCup = DatabaseLoader.getCompetitions().get(0);
        
        assertNotNull(faCup.getEditions());
        
        CompetitionEdition faCupEdition = faCup.getEditions().get(0);
        
        assertNotNull(faCupEdition);
        
        /**
         * Cup Draw
         */
        List<Match> competitionMatches = application.getGameEngine().getCompetitionScheduler().competitionDraw(faCup, faCupEdition.getParticipantClubsIds());
        
        assertNotNull(competitionMatches);
        
        for (Match match : competitionMatches) {
            
            Club homeClub = DatabaseLoader.getClubById(match.getHomeClubId());
            Club awayClub = DatabaseLoader.getClubById(match.getAwayClubId());
            
            Gdx.app.debug("CompetitionsLoaderTests", "Competition Match: " + homeClub.getName() + " v " + awayClub.getName());
        }
    }
    
    /**
     * This calls the Authority/AI manager for the Competition and moves time forward one day by one and calls the Match Scheduler
     * making sure no errors on scheduling are happening (this should end with a complete cup, winners, etc).
     */
    @Test
    void competitionCupScheduleTest() {
        
    }
}