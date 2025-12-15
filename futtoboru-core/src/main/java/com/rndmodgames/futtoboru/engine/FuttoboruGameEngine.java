package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.AttributeTrackingConstants;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.engine.temporal.MatchScheduler;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;

/**
 * Game Engine v1
 *  
 *  - TODO WIP:
 *  
 *      - Get an instance of this running with the Game
 *      - Move the CONTINUE GAME Button functionality to this class
 *      - Implement Script Execution by LocalDateTime
 *      -
 *      -
 *  
 * @author Geomancer86
 */
public class FuttoboruGameEngine {

    // 
    private Futtoboru gameInstance;
    private AuthorityManager authorityManager;
    private ScriptsManager scriptsManager;
    
    //
    private MainMenuManager mainMenuManager;
    
    //
    CompetitionScheduler competitionScheduler;
    MatchScheduler scheduler;
    
    // Player Attribute Generator (v1.0)
    private PlayerAttributeGenerator attributeGenerator;
    
    // Message Manager (v2.0)
    private MessageManager messageManager;
    
    //
    public static final int CONTINUE_GAME_ACTION = 1;
    public static final int MATCH_PREVIEW_ACTION = 2;
    public static final int MATCH_RESULT_ACTION = 3;

    public FuttoboruGameEngine(Game parent,
                               ScriptsManager scriptsManager,
                               AuthorityManager authorityManager) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
        this.scriptsManager = scriptsManager;
        this.authorityManager = authorityManager;
        
        //
        this.scheduler = new MatchScheduler(gameInstance);
        this.competitionScheduler = new CompetitionScheduler(gameInstance);
        this.attributeGenerator = new PlayerAttributeGenerator();
        this.messageManager = new MessageManager(gameInstance);
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
    
    /**
     * Check if the Game can CONTINUE or MATCH PREVIEW
     * 
     * 1: CONTINUE GAME
     * 2: MATCH PREVIEW
     */
    public int getNextGameAction() {

        // Current Club (can be null for unemployed players)
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();
        
        /**
         * Check if Current Club has a MATCH TODAY
         * NOTE: If player is unemployed (no club), skip match check
         */
        if (currentClub != null) {
            boolean matchDay = scheduler.checkClubMatchDay(currentClub);
            
            if (matchDay) {
                return MATCH_PREVIEW_ACTION;
            }
        }

        return CONTINUE_GAME_ACTION;
    }
    
    /**
     * Process match result - simulate match and update game state
     * 
     * v1.0: Uses MatchSimulator to generate actual match results
     */
    public void getMatchResult() {
        
        System.out.println("MATCH RESULT - SIMULATING MATCH");
        
        // Get Current Club
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();
        
        // Null check: unemployed players don't have a club
        if (currentClub == null || currentClub.getScheduledMatches() == null) {
            Gdx.app.log("FuttoboruGameEngine", "Cannot process match result: player is unemployed (no club)");
            return;
        }
        
        // Sort matches chronologically
        Comparator<Match> comparatorChronological = (match1, match2) -> match1.getMatchDateTime()
                                                             .compareTo(match2.getMatchDateTime());
        Collections.sort(currentClub.getScheduledMatches(), comparatorChronological);
        
        // Check we have at least one match
        if (currentClub.getScheduledMatches().isEmpty()) {
            Gdx.app.log("FuttoboruGameEngine", "No scheduled matches to simulate");
            return;
        }
        
        // Get the next match (first in chronological order)
        Match nextMatch = currentClub.getScheduledMatches().get(0);
        
        // Create match simulator
        MatchSimulator simulator = new MatchSimulator(gameInstance);
        
        // Simulate the match
        boolean simulated = simulator.simulateMatch(nextMatch);
        
        if (!simulated) {
            Gdx.app.error("FuttoboruGameEngine", "Failed to simulate match");
            return;
        }
        
        // Get both clubs (home and away) to update their match lists
        Club homeClub = gameInstance.getCurrentGame().getClubById(nextMatch.getHomeClubId());
        Club awayClub = gameInstance.getCurrentGame().getClubById(nextMatch.getAwayClubId());
        
        // Update home club's match lists
        if (homeClub != null) {
            homeClub.getScheduledMatches().remove(nextMatch);
            if (!homeClub.getPlayedMatches().contains(nextMatch)) {
                homeClub.getPlayedMatches().add(nextMatch);
            }
        }
        
        // Update away club's match lists
        if (awayClub != null) {
            awayClub.getScheduledMatches().remove(nextMatch);
            if (!awayClub.getPlayedMatches().contains(nextMatch)) {
                awayClub.getPlayedMatches().add(nextMatch);
            }
        }
        
        System.out.println("Match simulated: " + (homeClub != null ? homeClub.getName() : "Unknown") + 
                          " " + nextMatch.getHomeGoals() + " - " + nextMatch.getAwayGoals() + 
                          " " + (awayClub != null ? awayClub.getName() : "Unknown"));
        
        // Create match result message for both clubs
        if (messageManager != null && homeClub != null && awayClub != null) {
            com.rndmodgames.futtoboru.data.Message matchResultMessage = 
                messageManager.createMatchResultMessage(nextMatch, homeClub, awayClub);
            
            if (matchResultMessage != null) {
                // Deliver immediately to both clubs' inboxes
                // For now, we'll send to current club if they're involved
                // TODO: In future, each club should have their own inbox or we filter by club
                messageManager.deliverMessage(matchResultMessage);
                Gdx.app.log("FuttoboruGameEngine", "Created match result message for " + 
                           homeClub.getName() + " vs " + awayClub.getName());
            }
        }
        
        // Update UI
        if (mainMenuManager != null) {
            mainMenuManager.updateDynamicComponents();
        }
    }
    
    /**
     * TODO: Continue Game should be fully automatic, the Engine will take care of how long the game has to move forward
     *  
     *  - Day
     *  - Hours
     *  - Etc
     *  
     *  TODO: add a loading screen or processing indicator
     */
    public void continueGame() {
        
        //
        Gdx.app.debug("FuttoboruGameEngine", "ADVANCING THE SIMULATION");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        /**
         * Deliver scheduled messages BEFORE advancing date
         * This ensures messages scheduled for the current date are delivered
         */
        messageManager.deliverScheduledMessages(current);
        
        /**
         * Increment By Required Unit
         * 
         * TODO: make it AM/PM or advance in smaller amount of time depending on time of season/etc as in FM
         */
        LocalDateTime newDate = current.plusDays(1);
        gameInstance.getCurrentGame().setGameDate(newDate);
        
        // Check Game Scripts (may create new scheduled messages)
        scriptsManager.checkGameScripts();

        // Check Competition Schedules
        authorityManager.checkCompetitionsSchedule();
        
        // Update Job Openings (v1.0)
        if (gameInstance.getJobManager() != null) {
            gameInstance.getJobManager().updateJobOpenings();
        }
        
        /**
         * Deliver scheduled messages AFTER advancing date
         * This catches messages scheduled for the new date (same day events)
         */
        messageManager.deliverScheduledMessages(newDate);
        
        /**
         * Update Player Attributes (v1.0 - Testing)
         * TODO: Replace with proper training system in Phase 3
         */
        updatePlayerAttributesDaily();
        
        /**
         * Create Weekly Attribute Snapshots (v1.0)
         * Creates snapshots every 7 days for attribute change tracking
         */
        createWeeklyAttributeSnapshots();
        
        /**
         * Current Club
         * 
         * NOTE: player might not have a CURRENT_CLUB
         *       
         *       - simulate all clubs, not only player controlled
         */
        for (Club currentClub : gameInstance.getCurrentGame().getAllClubs()) {

            //
            Gdx.app.debug("FuttoboruGameEngine", "PROCESSING CLUB: " + currentClub.getName());
            
            /**
             * Check Proposed Friendlies
             */
            scheduler.checkClubProposedMatches(currentClub);
            
            /**
             * Check Scheduled Matches
             */
            scheduler.checkClubSheduledMatches(currentClub);
            
        }
        
        /**
         * Update UI
         * NOTE: this will be null on Unit Tests
         */
        if (mainMenuManager != null) {
            mainMenuManager.updateDynamicComponents();
            Gdx.app.log("FuttoboruGameEngine", "UI updated after continueGame()");
        } else {
            Gdx.app.log("FuttoboruGameEngine", "WARNING: mainMenuManager is null, UI not updated");
        }
        
        Gdx.app.log("FuttoboruGameEngine", "continueGame() completed. New date: " + gameInstance.getCurrentGame().getGameDate());
    }

    public CompetitionScheduler getCompetitionScheduler() {
        return competitionScheduler;
    }

    public void setCompetitionScheduler(CompetitionScheduler competitionScheduler) {
        this.competitionScheduler = competitionScheduler;
    }
    
    /**
     * Update player attributes daily (v1.0 - Testing)
     * TODO: Replace with proper training system in Phase 3
     */
    private void updatePlayerAttributesDaily() {
        if (gameInstance == null || gameInstance.getCurrentGame() == null) {
            return;
        }
        
        Gdx.app.debug("FuttoboruGameEngine", "Updating player attributes daily...");
        
        int playersUpdated = 0;
        LocalDateTime currentDate = gameInstance.getCurrentGame().getGameDate();
        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {
            for (Player player : club.getPlayers()) {
                if (player != null && player.getPerson() != null) {
                    attributeGenerator.applyDailyAttributeChanges(player, currentDate);
                    playersUpdated++;
                }
            }
        }
        
        Gdx.app.debug("FuttoboruGameEngine", "Updated attributes for " + playersUpdated + " players");
    }
    
    /**
     * Create weekly attribute snapshots for all players (v1.0)
     * Snapshots are created every 7 days to track attribute changes over time
     */
    private void createWeeklyAttributeSnapshots() {
        if (gameInstance == null || gameInstance.getCurrentGame() == null) {
            return;
        }
        
        LocalDateTime currentDate = gameInstance.getCurrentGame().getGameDate();
        
        // Check if we need to create snapshots (every 7 days)
        // Get the last snapshot date
        LocalDateTime lastSnapshotDate = getLastSnapshotDate();
        
        if (lastSnapshotDate != null) {
            long daysSinceLastSnapshot = java.time.temporal.ChronoUnit.DAYS.between(lastSnapshotDate, currentDate);
            if (daysSinceLastSnapshot < AttributeTrackingConstants.SNAPSHOT_INTERVAL_DAYS) {
                // Not time for a new snapshot yet
                return;
            }
        }
        
        // Create snapshots for all players
        int snapshotsCreated = 0;
        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {
            for (Player player : club.getPlayers()) {
                if (player != null && player.getPerson() != null && player.getPerson().getId() != null) {
                    PlayerAttributeSnapshot snapshot = PlayerAttributeSnapshot.fromPlayer(
                        player, 
                        currentDate, 
                        "WEEKLY"
                    );
                    if (snapshot != null) {
                        gameInstance.getCurrentGame().getPlayerAttributeSnapshots().add(snapshot);
                        snapshotsCreated++;
                    }
                }
            }
        }
        
        // Cleanup old snapshots (keep only last 365 days)
        cleanupOldSnapshots(currentDate);
        
        if (snapshotsCreated > 0) {
            Gdx.app.log("FuttoboruGameEngine", "Created " + snapshotsCreated + " weekly attribute snapshots");
        }
    }
    
    /**
     * Get the date of the most recent snapshot
     */
    private LocalDateTime getLastSnapshotDate() {
        if (gameInstance == null || gameInstance.getCurrentGame() == null) {
            return null;
        }
        
        java.util.List<PlayerAttributeSnapshot> snapshots = gameInstance.getCurrentGame().getPlayerAttributeSnapshots();
        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }
        
        LocalDateTime lastDate = null;
        for (PlayerAttributeSnapshot snapshot : snapshots) {
            if (snapshot != null && snapshot.getSnapshotDate() != null) {
                if (lastDate == null || snapshot.getSnapshotDate().isAfter(lastDate)) {
                    lastDate = snapshot.getSnapshotDate();
                }
            }
        }
        
        return lastDate;
    }
    
    /**
     * Remove snapshots older than MAX_SNAPSHOT_AGE_DAYS
     */
    private void cleanupOldSnapshots(LocalDateTime currentDate) {
        if (gameInstance == null || gameInstance.getCurrentGame() == null) {
            return;
        }
        
        java.util.List<PlayerAttributeSnapshot> snapshots = gameInstance.getCurrentGame().getPlayerAttributeSnapshots();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }
        
        LocalDateTime cutoffDate = currentDate.minusDays(AttributeTrackingConstants.MAX_SNAPSHOT_AGE_DAYS);
        int removed = 0;
        
        java.util.Iterator<PlayerAttributeSnapshot> iterator = snapshots.iterator();
        while (iterator.hasNext()) {
            PlayerAttributeSnapshot snapshot = iterator.next();
            if (snapshot != null && snapshot.getSnapshotDate() != null) {
                if (snapshot.getSnapshotDate().isBefore(cutoffDate)) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        
        if (removed > 0) {
            Gdx.app.log("FuttoboruGameEngine", "Cleaned up " + removed + " old attribute snapshots");
        }
    }
}