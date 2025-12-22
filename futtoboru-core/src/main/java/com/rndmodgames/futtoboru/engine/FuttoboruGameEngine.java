package com.rndmodgames.futtoboru.engine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Game;
import java.math.BigDecimal;

import com.rndmodgames.futtoboru.data.AttributeTrackingConstants;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.engine.finances.ExpenseCalculator;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.engine.temporal.MatchScheduler;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.system.SaveGame;
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
    
    // Expense Calculator (v1.0)
    private ExpenseCalculator expenseCalculator;
    
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
    public static final int DRAW_ACTION = 4;  // Mandatory draw screen (blocks time advancement)

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
        this.expenseCalculator = new ExpenseCalculator();
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
     * Check if the Game can CONTINUE or MATCH PREVIEW or DRAW
     * 
     * 1: CONTINUE GAME
     * 2: MATCH PREVIEW
     * 4: DRAW (mandatory - blocks time advancement)
     */
    public int getNextGameAction() {
        
        SaveGame currentGame = gameInstance.getCurrentGame();
        
        /**
         * Check for mandatory messages that block time advancement
         * (e.g., draw screens that must be viewed)
         */
        if (currentGame != null && currentGame.getAllMessages() != null) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Checking for mandatory messages. Total messages: " + currentGame.getAllMessages().size());
            
            for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
                if (message != null) {
                    boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                    boolean isUnread = message.getIsRead() == null || !message.getIsRead();
                    boolean isNotDeleted = message.getIsDeleted() == null || !message.getIsDeleted();
                    String messageType = message.getMessageType();
                    
                    if (isMandatory && isUnread && isNotDeleted) {
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Found mandatory message - Type: " + messageType + 
                                         ", Title: " + message.getTitle() + ", IsMandatory: " + isMandatory + 
                                         ", IsRead: " + message.getIsRead());
                        
                        // Check if it's a draw-related message
                        if (messageType != null && 
                            (messageType.equals("LEAGUE_DRAW") || 
                             messageType.equals("CUP_DRAW") ||
                             messageType.equals("FIXTURE_DRAW"))) {
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Mandatory draw message found, blocking time advancement");
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: BLOCKING TIME ADVANCEMENT - Mandatory draw message: " + message.getTitle());
                            return DRAW_ACTION;
                        }
                    }
                }
            }
            
            // Also check scheduled messages for mandatory draws
            if (currentGame.getScheduledMessages() != null) {
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Checking scheduled messages. Total scheduled: " + currentGame.getScheduledMessages().size());
                for (com.rndmodgames.futtoboru.data.Message message : currentGame.getScheduledMessages()) {
                    if (message != null && 
                        message.getIsMandatory() != null && message.getIsMandatory() &&
                        message.getMessageType() != null && 
                        (message.getMessageType().equals("LEAGUE_DRAW") || 
                         message.getMessageType().equals("CUP_DRAW") ||
                         message.getMessageType().equals("FIXTURE_DRAW"))) {
                        
                        // Check if scheduled date is today or past
                        if (message.getScheduledDate() != null && 
                            !message.getScheduledDate().isAfter(currentGame.getGameDate())) {
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Found scheduled mandatory draw message that should be delivered: " + message.getTitle());
                            // Message should be delivered, but if it hasn't been, we should still block
                            // This is a safety check
                        }
                    }
                }
            }
        }

        // Current Club (can be null for unemployed players)
        Club currentClub = currentGame.getCurrentClub();
        
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
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "MATCH RESULT - SIMULATING MATCH");
        
        // Get Current Club
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();
        
        // Null check: unemployed players don't have a club
        if (currentClub == null || currentClub.getScheduledMatches() == null) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine", "Cannot process match result: player is unemployed (no club)");
            return;
        }
        
        // Sort matches chronologically
        Comparator<Match> comparatorChronological = (match1, match2) -> match1.getMatchDateTime()
                                                             .compareTo(match2.getMatchDateTime());
        Collections.sort(currentClub.getScheduledMatches(), comparatorChronological);
        
        // Check we have at least one match
        if (currentClub.getScheduledMatches().isEmpty()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine", "No scheduled matches to simulate");
            return;
        }
        
        // Find the next playable match (both teams determined, not played, date is today or past)
        Match nextMatch = null;
        LocalDateTime currentDate = gameInstance.getCurrentGame().getGameDate();
        
        for (Match match : currentClub.getScheduledMatches()) {
            // Skip if teams not determined
            if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
                continue;
            }
            
            // Skip if already played
            if (match.getIsPlayed() != null && match.getIsPlayed()) {
                continue;
            }
            
            // Skip if match date is in the future
            if (match.getMatchDateTime() != null && match.getMatchDateTime().isAfter(currentDate)) {
                continue;
            }
            
            // This match is playable
            nextMatch = match;
            break;
        }
        
        if (nextMatch == null) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine", "No playable matches found for club: " + currentClub.getName());
            return;
        }
        
        // Create match simulator
        MatchSimulator simulator = new MatchSimulator(gameInstance);
        
        // Simulate the match
        boolean simulated = simulator.simulateMatch(nextMatch);
        
        if (!simulated) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine", "Failed to simulate match");
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
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "Match simulated: " + (homeClub != null ? homeClub.getName() : "Unknown") + 
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
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine", "Created match result message for " + 
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
        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "ADVANCING THE SIMULATION");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        /**
         * STEP 4: Deliver scheduled messages BEFORE advancing date
         * This ensures messages scheduled for the current date are delivered
         */
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "STEP 4: Delivering scheduled messages for date: " + current);
        int scheduledCountBefore = (gameInstance.getCurrentGame().getScheduledMessages() != null) ? 
            gameInstance.getCurrentGame().getScheduledMessages().size() : 0;
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "Scheduled messages before delivery: " + scheduledCountBefore);
        
        messageManager.deliverScheduledMessages(current);
        
        int scheduledCountAfter = (gameInstance.getCurrentGame().getScheduledMessages() != null) ? 
            gameInstance.getCurrentGame().getScheduledMessages().size() : 0;
        int allMessagesCount = (gameInstance.getCurrentGame().getAllMessages() != null) ? 
            gameInstance.getCurrentGame().getAllMessages().size() : 0;
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "Scheduled messages after delivery: " + scheduledCountAfter);
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "Total messages in inbox: " + allMessagesCount);
        
        /**
         * STEP 3.5: Simulate all matches scheduled for the current date
         * This must happen BEFORE advancing the date, so matches scheduled for "today" are simulated on "today"
         */
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "STEP 3.5: Simulating matches for current date: " + current);
        simulateMatchesForDate(current);
        
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
         * STEP 5: Deliver scheduled messages AFTER advancing date
         * This catches messages scheduled for the new date (same day events)
         */
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "STEP 5: Delivering scheduled messages for new date: " + newDate);
        messageManager.deliverScheduledMessages(newDate);
        
        int finalScheduledCount = (gameInstance.getCurrentGame().getScheduledMessages() != null) ? 
            gameInstance.getCurrentGame().getScheduledMessages().size() : 0;
        int finalAllMessagesCount = (gameInstance.getCurrentGame().getAllMessages() != null) ? 
            gameInstance.getCurrentGame().getAllMessages().size() : 0;
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "Final scheduled messages: " + finalScheduledCount);
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "Final total messages in inbox: " + finalAllMessagesCount);
        
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
         * Calculate and Deduct Weekly Expenses (v1.0)
         * Calculates expenses every 7 days and deducts from club balance
         */
        calculateAndDeductWeeklyExpenses();
        
        /**
         * Current Club
         * 
         * NOTE: player might not have a CURRENT_CLUB
         *       
         *       - simulate all clubs, not only player controlled
         */
        for (Club currentClub : gameInstance.getCurrentGame().getAllClubs()) {

            //
            DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "PROCESSING CLUB: " + currentClub.getName());
            
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
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "UI updated after continueGame()");
        } else {
            DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "WARNING: mainMenuManager is null, UI not updated");
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "continueGame() completed. New date: " + gameInstance.getCurrentGame().getGameDate());
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
        
        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Updating player attributes daily...");
        
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
        
        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Updated attributes for " + playersUpdated + " players");
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
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Created " + snapshotsCreated + " weekly attribute snapshots");
        }
    }
    
    /**
     * Calculate and deduct weekly expenses for all clubs (v1.0)
     * Expenses are calculated every 7 days and deducted from club balance
     */
    private void calculateAndDeductWeeklyExpenses() {
        if (gameInstance == null || gameInstance.getCurrentGame() == null || expenseCalculator == null) {
            return;
        }
        
        LocalDateTime currentDate = gameInstance.getCurrentGame().getGameDate();
        
        // Check if we need to calculate expenses (every 7 days)
        // Get the last expense date
        LocalDateTime lastExpenseDate = getLastExpenseDate();
        
        if (lastExpenseDate != null) {
            long daysSinceLastExpense = java.time.temporal.ChronoUnit.DAYS.between(lastExpenseDate, currentDate);
            if (daysSinceLastExpense < 7) {
                // Not time for weekly expenses yet
                return;
            }
        }
        
        // Calculate and deduct expenses for all clubs
        int expensesCalculated = 0;
        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {
            if (club == null) {
                continue;
            }
            
            // Calculate weekly expenses
            ClubExpenses expenses = expenseCalculator.calculateWeeklyExpenses(club, currentDate);
            if (expenses == null) {
                continue;
            }
            
            // Add to expense history
            club.addExpense(expenses);
            
            // Deduct from club balance
            BigDecimal totalExpenses = expenses.getTotalExpenses();
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentBalance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
                club.setClubBalance(currentBalance.subtract(totalExpenses));
                
                // Update period expenditure tracking
                club.setSeasonExpenditure(club.getSeasonExpenditure().add(totalExpenses));
                club.setMonthExpenditure(club.getMonthExpenditure().add(totalExpenses));
                
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Deducted weekly expenses: $" + totalExpenses + 
                           " from " + club.getName() + " (New balance: $" + club.getClubBalance() + ")");
                expensesCalculated++;
            }
        }
        
        if (expensesCalculated > 0) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Calculated and deducted weekly expenses for " + expensesCalculated + " clubs");
        }
    }
    
    /**
     * Get the date of the most recent expense calculation
     */
    private LocalDateTime getLastExpenseDate() {
        if (gameInstance == null || gameInstance.getCurrentGame() == null) {
            return null;
        }
        
        LocalDateTime lastDate = null;
        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {
            if (club == null || club.getExpensesHistory() == null) {
                continue;
            }
            
            for (ClubExpenses expense : club.getExpensesHistory()) {
                if (expense != null && expense.getPeriodStart() != null) {
                    if (lastDate == null || expense.getPeriodStart().isAfter(lastDate)) {
                        lastDate = expense.getPeriodStart();
                    }
                }
            }
        }
        
        return lastDate;
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
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Cleaned up " + removed + " old attribute snapshots");
        }
    }
    
    /**
     * Simulate all matches scheduled for a specific date
     * 
     * v1.0: Automatically simulates all matches scheduled for the given date
     * 
     * @param date The date to simulate matches for
     */
    private void simulateMatchesForDate(LocalDateTime date) {
        if (date == null || gameInstance == null || gameInstance.getCurrentGame() == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Cannot simulate matches: date or game instance is null");
            return;
        }
        
        SaveGame game = gameInstance.getCurrentGame();
        MatchSimulator simulator = new MatchSimulator(gameInstance);
        Set<Match> simulatedToday = new HashSet<>(); // Prevent duplicate simulation
        
        LocalDate targetDate = date.toLocalDate();
        int matchesFound = 0;
        int matchesSimulated = 0;
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Simulating matches for date: " + targetDate);
        
        // First pass: Collect all matches to simulate (avoid ConcurrentModificationException)
        // Also reschedule matches with passed dates that haven't been played
        List<Match> matchesToSimulate = new ArrayList<>();
        java.util.Set<Long> matchIdsAdded = new java.util.HashSet<>(); // Use ID-based deduplication
        java.util.List<Match> matchesToReschedule = new java.util.ArrayList<>(); // Matches with passed dates
        
        // Iterate through all clubs to find matches scheduled for this date
        for (Club club : game.getAllClubs()) {
            if (club == null || club.getScheduledMatches() == null) {
                continue;
            }
            
            // Find matches scheduled for this date
            for (Match match : club.getScheduledMatches()) {
                if (match == null || match.getId() == null) {
                    continue;
                }
                
                // Skip if already added (use ID to avoid duplicates)
                if (matchIdsAdded.contains(match.getId())) {
                    continue;
                }
                
                // Skip if already simulated or already played
                if (simulatedToday.contains(match) || (match.getIsPlayed() != null && match.getIsPlayed())) {
                    continue;
                }
                
                // CRITICAL: Skip matches where teams are not yet determined (future round matches)
                if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
                    // Log for debugging cup matches
                    if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH) {
                        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_ENGINE_CUP, "FuttoboruGameEngine: SKIPPING cup match " + 
                            (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                            " (Round " + match.getRound() + ", ID: " + match.getId() + 
                            ") - teams not determined. Home=" + match.getHomeClubId() + ", Away=" + match.getAwayClubId());
                    }
                    continue; // Match not ready to play yet
                }
                
                // Check if match is scheduled for target date OR has passed (critical for replays)
                if (match.getMatchDateTime() != null) {
                    LocalDate matchDate = match.getMatchDateTime().toLocalDate();
                    
                    // CRITICAL: Simulate matches scheduled for today OR matches that have passed
                    // This ensures replays and rescheduled matches are always picked up
                    boolean shouldSimulate = false;
                    if (matchDate.equals(targetDate)) {
                        // Match scheduled for today
                        shouldSimulate = true;
                    } else if (matchDate.isBefore(targetDate)) {
                        // Match date has passed - simulate it now (prevents matches from being lost)
                        // This is critical for replays and matches that were rescheduled
                        DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: *** MATCH DATE PASSED *** " + 
                            (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                            " scheduled for " + matchDate + " but today is " + targetDate + 
                            " - will simulate now");
                        shouldSimulate = true;
                    }
                    
                    if (shouldSimulate) {
                        // Add to list if not already there (prevent duplicates by ID)
                        if (!matchIdsAdded.contains(match.getId())) {
                            matchesToSimulate.add(match);
                            matchIdsAdded.add(match.getId());
                            matchesFound++;
                            
                            // Log cup matches for debugging
                            if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH) {
                                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_CUP, "FuttoboruGameEngine: *** FOUND CUP MATCH TO SIMULATE *** " + 
                                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                                    " (Round " + match.getRound() + ", ID: " + match.getId() + 
                                    ", Home: " + match.getHomeClubId() + ", Away: " + match.getAwayClubId() + 
                                    ") scheduled for " + matchDate + ", simulating on " + targetDate);
                            }
                        }
                    } else if (matchDate.isBefore(targetDate)) {
                        // Match date has passed but teams not determined - reschedule for tomorrow
                        // This handles cases where replays or future round matches weren't ready
                        if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH &&
                            (match.getHomeClubId() == null || match.getAwayClubId() == null)) {
                            if (!matchIdsAdded.contains(match.getId())) {
                                matchesToReschedule.add(match);
                                matchIdsAdded.add(match.getId());
                                DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_ENGINE_CUP, "FuttoboruGameEngine: *** RESCHEDULING CUP MATCH *** " + 
                                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                                    " from " + matchDate + " to " + targetDate.plusDays(1) + 
                                    " (teams not determined)");
                            }
                        }
                    }
                }
            }
        }
        
        // Second pass: Simulate matches and update lists
        for (Match match : matchesToSimulate) {
            // Simulate the match
            boolean success = simulator.simulateMatch(match);
            
            if (success) {
                simulatedToday.add(match);
                matchesSimulated++;
                
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Simulated match " + match.getId() + " on " + targetDate);
                
                // Update match lists for both clubs (now safe to modify)
                Club homeClub = game.getClubById(match.getHomeClubId());
                Club awayClub = game.getClubById(match.getAwayClubId());
                
                if (homeClub != null) {
                    homeClub.getScheduledMatches().remove(match);
                    if (!homeClub.getPlayedMatches().contains(match)) {
                        homeClub.getPlayedMatches().add(match);
                    }
                }
                
                if (awayClub != null) {
                    awayClub.getScheduledMatches().remove(match);
                    if (!awayClub.getPlayedMatches().contains(match)) {
                        awayClub.getPlayedMatches().add(match);
                    }
                }
                
                // Create match result message
                if (messageManager != null && homeClub != null && awayClub != null) {
                    com.rndmodgames.futtoboru.data.Message matchResultMessage = 
                        messageManager.createMatchResultMessage(match, homeClub, awayClub);
                    if (matchResultMessage != null) {
                        messageManager.deliverMessage(matchResultMessage);
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "FuttoboruGameEngine: Created match result message for " + 
                                         homeClub.getName() + " vs " + awayClub.getName());
                    }
                }
            } else {
                DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Failed to simulate match " + match.getId());
            }
        }
        
        // Reschedule matches with passed dates that aren't ready (teams not determined)
        for (Match match : matchesToReschedule) {
            if (match != null && match.getMatchDateTime() != null) {
                // Reschedule for tomorrow (or next available date)
                java.time.LocalDateTime newDate = targetDate.plusDays(1).atStartOfDay();
                match.setMatchDateTime(newDate);
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Rescheduled match " + 
                    (match.getBracketPath() != null ? match.getBracketPath() : "ID: " + match.getId()) + 
                    " to " + newDate);
            }
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: *** MATCH SIMULATION SUMMARY FOR " + targetDate + " ***");
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Found " + matchesFound + " matches, simulated " + matchesSimulated + " matches");
        if (!matchesToReschedule.isEmpty()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine: Rescheduled " + matchesToReschedule.size() + " matches with passed dates");
        }
        
        // Log breakdown by match type
        int cupMatchesFound = 0;
        int cupMatchesSimulated = 0;
        for (Match m : matchesToSimulate) {
            if (m.getMatchType() != null && m.getMatchType() == Match.CUP_MATCH) {
                cupMatchesFound++;
                if (simulatedToday.contains(m)) {
                    cupMatchesSimulated++;
                }
            }
        }
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_CUP, "FuttoboruGameEngine: Cup matches - Found: " + cupMatchesFound + ", Simulated: " + cupMatchesSimulated);
        
        if (matchesSimulated > 0) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_GAME, "FuttoboruGameEngine", "Simulated " + matchesSimulated + " matches for " + targetDate);
        }
    }
}