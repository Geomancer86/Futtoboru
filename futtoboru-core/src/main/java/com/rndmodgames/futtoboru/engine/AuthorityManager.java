package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Authority Manager v1
 * 
 *  - handle the decisions taken by the Game Authorities (ie: football associations, IFAB, FIFA, etc)
 *  
 *  - TODO:
 *      - iterate all authorities, this should be done in a hierarchical way as we have parent/children authorities
 *          
 *          - authority checks if existing competitions are scheduled/playing/finished
 *              
 *              - if a competition dont have a competition edition for the current season, the authority creates one
 *                  - this needs to take care of (as needed):
 *                      - inviting clubs
 *                      - automatically promoting clubs in case of cups/first round propers
 *                      - scheduling playoffs and rematches
 *                      - scheduling league matches
 *                      - changing the game rules (position table sorting rules, ie: 2 points for win changed to 3 points for win, etc. /offsides/redcards/etc / scripted)
 *                      - awarding prize money to champions/runnersups/participants/etc
 *                      - splitting money for games (setting the ruling)
 *                      - bans/fines
 *                      - 
 * 
 * @author Geomancer86
 */
public class AuthorityManager {

    // League prize money, historical estimate (P2-7: one place, the messages format it with Money)
    public static final java.math.BigDecimal LEAGUE_CHAMPION_PRIZE = new java.math.BigDecimal("500.00");
    public static final java.math.BigDecimal LEAGUE_RUNNER_UP_PRIZE = new java.math.BigDecimal("250.00");

    /** The month a cup's season opens (its edition starts and is drawn): May, as for the 1888-89 FA Cup */
    static final int CUP_SEASON_OPENS_MONTH = 5;

    // for quick reference
    Futtoboru game;
    SaveGame currentGame;
    Authority mainAuthority;
    LeagueFixtureGenerator fixtureGenerator;
    CompetitionScheduler competitionScheduler;
    
    //
    public AuthorityManager(Futtoboru parent) {
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        this.fixtureGenerator = new LeagueFixtureGenerator(parent);
        this.reElection = new com.rndmodgames.futtoboru.engine.season.ReElectionManager(parent);
    }

    /** G-08: the re-election vote after each League season the data's rule covers. */
    private final com.rndmodgames.futtoboru.engine.season.ReElectionManager reElection;

    /** @return the re-election manager (G-08) */
    public com.rndmodgames.futtoboru.engine.season.ReElectionManager getReElectionManager() {
        return reElection;
    }
    
    /**
     * Runtime ids for objects made during play (P2-4), from the current game
     */
    private com.rndmodgames.futtoboru.system.data.IdAllocator ids() {
        return currentGame().getIdAllocator();
    }

    /**
     * The game being played (P2-8). This manager is built in {@code Futtoboru.create()} before any game exists, so
     * the game is read from {@code Futtoboru} on every entry instead of once in the constructor; that copy stayed
     * null and the daily competition check never ran. Falls back to the stored game when none is set (tests that
     * build their own manager).
     */
    SaveGame currentGame() {
        if (game != null && game.getCurrentGame() != null) {
            currentGame = game.getCurrentGame();
        }
        return currentGame;
    }
    
    /**
     * Authorities AI
     * 
     *  - iterates all over the game authorities
     *      - check if cups are without scheduled draws and/or matches
     *          - schedule/draw accordingly
     *          
     *      - check if rounds are finished
     *          - advance rounds
     *          
     *      - check if rematches need to be scheduled
     *          - schedule rematches
     *          
     *      - give match prizes/entry to clubs (cup splits 50/50 and league is 70/30) TODO make it parametrizable
     *      
     *      - give competition prizes for winners/runners up at the end of competition
     *      
     *      - handle re-elections and relegation
     *      
     *      - make sure the competitions are perpetual (ie: they should be played once a year forever as long as no scripts change something).
     *      
     *  TODO: iterate hierarchy of authorities
     *  TODO: draw FA Cup
     *  TODO: play FA Cup
     *  TODO: finish/restart FA Cup
     */
    public void checkCompetitionsSchedule() {

        //
        Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "checkCompetitionsSchedule()");

        if (currentGame() == null || currentGame.getMainAuthority() == null) {
            return;
        }
        
        mainAuthority = currentGame.getMainAuthority();
        
        /**
         * Iterate all over the game current/existing competitions (hierarchical)
         *  - if the season doesnt exist, create one
         *  - if the season exist:
         *      - check if matches are scheduled
         *      - draw matches
         */
        
        // Check leagues and generate fixtures if needed
        checkAndScheduleLeagueFixtures();
        
        // Check cups and generate draws if needed
        checkAndScheduleCupDraws();
        
        // Check cup round progression
        checkCupRoundProgression();
        
        // Check for cup replays (tied matches)
        checkCupReplays();
        
        // Check league completion and awards
        checkLeagueCompletion();
        
        // G-08: annual meetings due today vote on League membership; then the next season can be generated
        List<League> voted = reElection.holdDueMeetings(mainAuthority.getLeagues(), currentGame.getGameDate());
        for (League league : voted) {
            checkAndGenerateNextSeasonForLeague(league);
        }

        // Check for new season generation
        checkNewSeasonGeneration();
    }
    
    /**
     * Check all leagues for completion and trigger awards
     */
    private void checkLeagueCompletion() {
        try {
            if (currentGame == null || currentGame.getMainAuthority() == null) {
                return;
            }
            
            List<League> leagues = currentGame.getMainAuthority().getLeagues();
            if (leagues == null || leagues.isEmpty()) {
                return;
            }
            
            com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager standingsManager = 
                new com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager();
            
            for (League league : leagues) {
                if (league == null) continue;
                
                // Check if league is complete
                if (standingsManager.isLeagueComplete(league)) {
                    // Check if already completed (champions declared)
                    // We need a way to check if this specific season is already awarded
                    // For now, let's check if the champion message was already sent
                    if (!isLeagueAlreadyCompleted(league)) {
                        completeLeagueSeason(league, standingsManager);
                        
                        // CRITICAL FIX: After completing a league season, check if next season needs to be generated
                        // This ensures new seasons are created immediately after completion, not just in July
                        checkAndGenerateNextSeasonForLeague(league);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Error in checkLeagueCompletion()", e);
            Log.error(Log.ENGINE_AUTHORITY, e);
        }
    }
    
    /**
     * Check if league season is already marked as completed
     */
    private boolean isLeagueAlreadyCompleted(League league) {
        // P2-6: by edition, not by message title (a "<league> - Season Complete" message from the first
        // season used to block the completion of every later season)
        return league.getEditions() != null && !league.getEditions().isEmpty()
            && findEditionToComplete(league) == null;
    }

    /**
     * The season being completed: the first edition that has started and has no final table yet
     * (null if there is none). The next season's edition can already exist (it is generated early).
     */
    private com.rndmodgames.futtoboru.data.CompetitionEdition findEditionToComplete(League league) {
        if (league.getEditions() == null) {
            return null;
        }
        LocalDateTime now = currentGame.getGameDate();
        for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : league.getEditions()) {
            if (edition == null || !edition.getFinalStandings().isEmpty()) {
                continue;
            }
            if (edition.getStartDate() == null || now == null || !edition.getStartDate().isAfter(now)) {
                return edition;
            }
        }
        return null;
    }

    /**
     * Get the current active edition for a league
     */
    private com.rndmodgames.futtoboru.data.CompetitionEdition getCurrentLeagueEdition(League league) {
        if (league.getEditions() == null || league.getEditions().isEmpty()) {
            return null;
        }
        
        LocalDateTime currentDate = currentGame.getGameDate();
        for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : league.getEditions()) {
            if (edition.getStartDate() != null && edition.getEndDate() != null) {
                if (!currentDate.isBefore(edition.getStartDate()) && !currentDate.isAfter(edition.getEndDate())) {
                    return edition;
                }
            }
        }
        
        // Fallback: return the last edition if none match current date
        return league.getEditions().get(league.getEditions().size() - 1);
    }
    
    /**
     * Complete the league season and trigger awards
     */
    private void completeLeagueSeason(League league, com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager standingsManager) {
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Season complete for league: " + league.getName());
        
        List<Club> standings = standingsManager.calculateStandings(league);
        if (standings.isEmpty()) return;
        
        Club champion = standings.get(0);
        Club runnerUp = standings.size() > 1 ? standings.get(1) : null;
        
        // The season being completed
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = findEditionToComplete(league);
        if (edition == null && league.getEditions() != null && !league.getEditions().isEmpty()) {
            edition = getCurrentLeagueEdition(league);
        }
        
        // Calculate Top Scorer (this season's league goals only)
        com.rndmodgames.futtoboru.data.Person topScorer = calculateTopScorer(league, edition);
        int topScorerGoals = topScorer != null ? getPlayerSeasonGoals(topScorer.getId(), league, edition) : 0;
        
        // Calculate Best Defense
        Club bestDefense = calculateBestDefense(standings);
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "CHAMPION: " + champion.getName());
        if (topScorer != null) {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "TOP SCORER: " + topScorer.getName() + " " + topScorer.getLastname() + " (" + topScorerGoals + " goals)");
        }
        
        // Update league edition with champions and keep its final table (P2-6)
        if (edition != null) {
            edition.setChampionsId(champion.getId());
            if (runnerUp != null) {
                edition.setRunnersUpId(runnerUp.getId());
            }
            List<com.rndmodgames.futtoboru.data.LeagueStandingRow> table = new ArrayList<>();
            for (int i = 0; i < standings.size(); i++) {
                table.add(com.rndmodgames.futtoboru.data.LeagueStandingRow.of(i + 1, standings.get(i)));
            }
            edition.setFinalStandings(table);
Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Updated league edition " + edition.getName() + " with champions: " + champion.getName());
        }
        
        // Season name in the message titles: MessageManager drops a message whose type and title are already in
        // the inbox, so titles without the season hid every later season's messages
        String seasonName = edition != null && edition.getName() != null ? edition.getName() : league.getName();
        
        // Give prize money (historical estimation)
        java.math.BigDecimal championPrize = LEAGUE_CHAMPION_PRIZE;
        java.math.BigDecimal runnerUpPrize = LEAGUE_RUNNER_UP_PRIZE;
        
        if (champion.getClubBalance() != null) {
            champion.setClubBalance(champion.getClubBalance().add(championPrize));
        }
        
        if (runnerUp != null && runnerUp.getClubBalance() != null) {
            runnerUp.setClubBalance(runnerUp.getClubBalance().add(runnerUpPrize));
        }
        
        // Create completion and awards messages
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        
        // 1. Standings Summary
        Message completionMessage = createLeagueCompletionMessage(league, standings, seasonName);
        if (completionMessage != null) {
            messageManager.deliverMessage(completionMessage);
        }
        
        // 2. Champion Announcement
        Message championMessage = createChampionMessage(league, champion, seasonName);
        if (championMessage != null) {
            messageManager.deliverMessage(championMessage);
        }
        
        // 3. Top Scorer Announcement
        if (topScorer != null) {
            Message scorerMessage = createTopScorerMessage(league, topScorer, topScorerGoals, seasonName);
            if (scorerMessage != null) {
                messageManager.deliverMessage(scorerMessage);
            }
        }
        
        // 4. Best Defense Announcement
        if (bestDefense != null) {
            Message defenseMessage = createBestDefenseMessage(league, bestDefense, seasonName);
            if (defenseMessage != null) {
                messageManager.deliverMessage(defenseMessage);
            }
        }

        // 5. G-08: the committee's season letter to the player's club, then the re-election (if the rule covers
        // this season: the next season waits for the annual meeting)
        Club ownClub = currentGame.getCurrentClub();
        if (ownClub != null && league.getLeagueClubs() != null && league.getLeagueClubs().contains(ownClub)) {
            Message summary = com.rndmodgames.futtoboru.engine.season.SeasonSummaryLetter.build(ownClub, league,
                    edition, standings, currentGame.getGameDate());
            if (summary != null) {
                messageManager.deliverMessage(summary);
            }
        }
        reElection.open(league, edition, standings, currentGame.getGameDate());

        // 6. Season rollover (P2-6): the final table is kept on the edition, the new season starts from zero
        resetLeagueTable(league);
        // E-01b: whoever joined a squad without a position gets the slot his squad is shortest of
        if (game.getCurrentGame() != null) {
            com.rndmodgames.futtoboru.system.generators.PositionAssigner.fillMissing(
                    game.getCurrentGame().getAllClubs(), "season rollover");
        }
        seasonRollovers++; // U-06: the engine autosaves on a rollover day
    }

    /** U-06: league seasons completed and rolled over by this manager (the autosave watches it change). */
    private int seasonRollovers;

    /** @return how many league seasons this manager has rolled over */
    public int getSeasonRollovers() {
        return seasonRollovers;
    }

    /**
     * Season rollover: reset the league statistics (the league table) of every club in the league
     */
    private void resetLeagueTable(League league) {
        if (league.getLeagueClubs() == null) {
            return;
        }
        for (Club club : league.getLeagueClubs()) {
            if (club == null) {
                continue;
            }
            club.setMatchesPlayed(0);
            club.setMatchesWon(0);
            club.setMatchesDrawn(0);
            club.setMatchesLost(0);
            club.setGoalsScored(0);
            club.setGoalsConceded(0);
            club.setPoints(0);
        }
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "League table reset for the new season: " + league.getName());
    }

    /**
     * A played league match of the given season (P2-3: the played lists keep every season's matches, so the
     * top scorer counted all of them): on or after the season's start and before the next season's start.
     * A null season (or start) means no date limit.
     */
    private static boolean isSeasonLeagueMatch(Match match, League league,
            com.rndmodgames.futtoboru.data.CompetitionEdition season) {
        if (match == null || match.getMatchType() == null || match.getMatchType() != Match.LEAGUE_MATCH) {
            return false;
        }
        if (season == null || season.getStartDate() == null || match.getMatchDateTime() == null) {
            return true;
        }
        if (match.getMatchDateTime().isBefore(season.getStartDate())) {
            return false;
        }
        for (com.rndmodgames.futtoboru.data.CompetitionEdition other : league.getEditions()) {
            if (other != null && other.getStartDate() != null && other.getStartDate().isAfter(season.getStartDate())
                    && !match.getMatchDateTime().isBefore(other.getStartDate())) {
                return false; // a later season's match
            }
        }
        return true;
    }

    /**
     * Calculate top scorer for the league season
     */
    com.rndmodgames.futtoboru.data.Person calculateTopScorer(League league,
            com.rndmodgames.futtoboru.data.CompetitionEdition season) {
        java.util.Map<Long, Integer> playerGoals = new java.util.LinkedHashMap<>();
        
        // Iterate all clubs in league
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) continue;
            
            // Iterate played matches
            for (Match match : club.getPlayedMatches()) {
                if (!isSeasonLeagueMatch(match, league, season)) continue;
                
                // Count home scorers
                if (match.getHomeClubId() != null && match.getHomeClubId().equals(club.getId())) {
                    for (Long scorerId : match.getHomeScorerIds()) {
                        playerGoals.put(scorerId, playerGoals.getOrDefault(scorerId, 0) + 1);
                    }
                }
                
                // Count away scorers
                if (match.getAwayClubId() != null && match.getAwayClubId().equals(club.getId())) {
                    for (Long scorerId : match.getAwayScorerIds()) {
                        playerGoals.put(scorerId, playerGoals.getOrDefault(scorerId, 0) + 1);
                    }
                }
            }
        }
        
        // Find player with most goals
        Long topScorerId = null;
        int maxGoals = -1;
        
        for (java.util.Map.Entry<Long, Integer> entry : playerGoals.entrySet()) {
            if (entry.getValue() > maxGoals) {
                maxGoals = entry.getValue();
                topScorerId = entry.getKey();
            }
        }
        
        if (topScorerId == null) return null;
        
        // Find person by ID: scorers are the league clubs' players (SaveGame.allPersons holds only a few people,
        // so the top scorer was never found there)
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayers() == null) continue;
            for (com.rndmodgames.futtoboru.data.Player player : club.getPlayers()) {
                if (player != null && player.getPerson() != null && topScorerId.equals(player.getPerson().getId())) {
                    return player.getPerson();
                }
            }
        }
        for (Person p : currentGame.getAllPersons()) {
            if (p != null && p.getId() != null && p.getId().equals(topScorerId)) {
                return p;
            }
        }
        
        return null;
    }
    
    /**
     * Get total goals scored by a player in a league season
     */
    int getPlayerSeasonGoals(Long playerId, League league,
            com.rndmodgames.futtoboru.data.CompetitionEdition season) {
        int goals = 0;
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) continue;
            for (Match match : club.getPlayedMatches()) {
                if (!isSeasonLeagueMatch(match, league, season)) continue;
                
                if (match.getHomeClubId() != null && match.getHomeClubId().equals(club.getId())) {
                    for (Long id : match.getHomeScorerIds()) {
                        if (id.equals(playerId)) goals++;
                    }
                }
                if (match.getAwayClubId() != null && match.getAwayClubId().equals(club.getId())) {
                    for (Long id : match.getAwayScorerIds()) {
                        if (id.equals(playerId)) goals++;
                    }
                }
            }
        }
        return goals;
    }
    
    /**
     * Calculate club with best defense (fewest goals conceded)
     */
    private Club calculateBestDefense(List<Club> standings) {
        Club best = null;
        int minGoals = Integer.MAX_VALUE;
        
        for (Club c : standings) {
            int conceded = c.getGoalsConceded() != null ? c.getGoalsConceded() : 0;
            if (conceded < minGoals) {
                minGoals = conceded;
                best = c;
            }
        }
        return best;
    }
    
    /**
     * Create top scorer announcement message
     */
    private Message createTopScorerMessage(League league, Person scorer, int goals, String seasonName) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_TOP_SCORER");
        message.setPriority(MessagePriority.NORMAL);
        String scorerName = scorer.getName() + " " + scorer.getLastname();
        message.setTitle(LanguageModLoader.format("msg_golden_boot_title", seasonName, scorerName));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_golden_boot_text", scorerName, league.getName(), goals))
               .append("\n\n");

        Club club = currentGame.getClubById(scorer.getCurrentClubId());
        if (club != null) {
            content.append(LanguageModLoader.format("msg_golden_boot_club", club.getName()));
        }
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create best defense announcement message
     */
    private Message createBestDefenseMessage(League league, Club club, String seasonName) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_BEST_DEFENSE");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(LanguageModLoader.format("msg_best_defence_title", seasonName, club.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_best_defence_text", club.getName(), league.getName()))
               .append("\n\n");
        content.append(LanguageModLoader.format("msg_best_defence_conceded", club.getGoalsConceded(),
               club.getMatchesPlayed()));
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create league completion summary message
     */
    private Message createLeagueCompletionMessage(League league, List<Club> standings, String seasonName) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_COMPLETE");
        message.setPriority(MessagePriority.URGENT);
        message.setTitle(LanguageModLoader.format("msg_league_complete_title", seasonName));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_league_complete_text", league.getName())).append("\n\n");
        content.append(LanguageModLoader.getValue("msg_final_standings")).append("\n");

        for (int i = 0; i < Math.min(5, standings.size()); i++) {
            Club c = standings.get(i);
            content.append(LanguageModLoader.format("msg_standings_row", i + 1, c.getName(), c.getPoints()))
                   .append("\n");
        }

        if (standings.size() > 5) {
            content.append(LanguageModLoader.format("msg_standings_more", standings.size() - 5)).append("\n");
        }

        content.append("\n").append(LanguageModLoader.format("msg_league_complete_champions",
                standings.get(0).getName()));
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create specific champion announcement message
     */
    private Message createChampionMessage(League league, Club champion, String seasonName) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_CHAMPION");
        message.setPriority(MessagePriority.URGENT);
        message.setTitle(LanguageModLoader.format("msg_champion_title", champion.getName(), seasonName));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_champion_text", champion.getName(), league.getName(),
               Money.format(LEAGUE_CHAMPION_PRIZE)));
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Check all leagues and generate fixtures if they don't have any scheduled
     */
    private void checkAndScheduleLeagueFixtures() {
        try {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "checkAndScheduleLeagueFixtures() called");
            
            if (mainAuthority == null) {
                Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "mainAuthority is null - no leagues to check");
                return;
            }
            
            if (mainAuthority.getLeagues() == null) {
                Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "mainAuthority.getLeagues() is null - no leagues to check");
                return;
            }
            
            List<League> leagues = mainAuthority.getLeagues();
            
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Found " + leagues.size() + " leagues in main authority");
            
            if (leagues.isEmpty()) {
                Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "No leagues found in main authority");
                return;
            }
            
            // Get season start date (use game start date or season start date)
            LocalDateTime seasonStart = getSeasonStartDate();
            LocalDateTime seasonEnd = getSeasonEndDate(seasonStart);
            
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Season dates: " + seasonStart + " to " + seasonEnd);
            
            for (League league : leagues) {
                if (league == null) {
                    Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Found null league in list");
                    continue;
                }
                
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Checking league: " + league.getName());
                
                if (league.getLeagueClubs() == null) {
                    Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "League " + league.getName() + " has null leagueClubs list");
                    continue;
                }
                
                if (league.getLeagueClubs().isEmpty()) {
                    Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "League " + league.getName() + " has no clubs");
                    continue;
                }
                
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "League " + league.getName() + " has " + league.getLeagueClubs().size() + " clubs");
                
                // G-08: between the last League day and the annual meeting there are no fixtures, by design; and a
                // league that has played is scheduled season by season (generateNextSeason), never from the game's
                // start date again (once the 1889-90 season was no longer made in January, the day the last
                // 1888-89 match was played got a second 1888-89 fixture list)
                if (reElection.isPending(league) || hasPlayedLeagueMatches(league)) {
                    continue;
                }

                // Check if fixtures are already scheduled
                boolean hasFixtures = fixtureGenerator.hasFixturesScheduled(league);
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "League " + league.getName() + " has fixtures scheduled: " + hasFixtures);
                
                if (!hasFixtures) {
                    Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generating fixtures for league: " + league.getName());
                    
                    // Generate fixtures
                    List<com.rndmodgames.futtoboru.data.Match> fixtures = 
                        fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
                    
                    Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generated " + fixtures.size() + " fixtures for " + league.getName());
                } else {
                    Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "League " + league.getName() + " already has fixtures scheduled");
                }
            }
        } catch (Exception e) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Error in checkAndScheduleLeagueFixtures()", e);
            Log.error(Log.ENGINE_AUTHORITY, e);
        }
    }
    
    /**
     * Get season start date
     * Uses game start date or season start date if available
     */
    private LocalDateTime getSeasonStartDate() {
        // Try to get from current season if available
        // For now, use game start date or current game date
        if (currentGame.getGameStartDate() != null) {
            return currentGame.getGameStartDate();
        }
        if (currentGame.getGameDate() != null) {
            return currentGame.getGameDate();
        }
        // Default: September 1st of current year (typical season start)
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), 9, 1, 15, 0); // September 1st, 3 PM
    }
    
    /**
     * Get season end date (typically 9 months after start, around May/June)
     */
    private LocalDateTime getSeasonEndDate(LocalDateTime seasonStart) {
        // P2-11: the league's last match day of the era calendar (mid-April)
        return SeasonCalendar.forDate(seasonStart).leagueEnd();
    }
    
    /**
     * Check all cups and generate draws if needed (v1.0 - Cup Draw Integration)
     */
    private void checkAndScheduleCupDraws() {
        try {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "checkAndScheduleCupDraws() called");
            
            if (currentGame == null) {
                Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "currentGame is null - no cups to check");
                return;
            }
            
            List<Competition> cups = currentGame.getAllCups();
            if (cups == null || cups.isEmpty()) {
                Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "No cups found in current game");
                return;
            }
            
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Found " + cups.size() + " cups to check");
            LocalDateTime currentDate = currentGame.getGameDate();
            
            for (Competition cup : cups) {
                if (cup == null) {
                    Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Found null cup in list");
                    continue;
                }
                
                // Only process CUP type competitions
                if (!Competition.COMPETITION_CUP.equals(cup.getCompetitionType())) {
                    continue;
                }
                
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Checking cup: " + cup.getName());
                
                // Get or create current edition
                CompetitionEdition edition = getCurrentCupEdition(cup, currentDate);
                if (edition == null) {
                    // Check if there's an edition from script that hasn't started yet
                    edition = findUpcomingEdition(cup, currentDate);
                    
                    if (edition == null) {
                        if (cup.getEditions() != null && !cup.getEditions().isEmpty()) {
                            // P2-3: between editions; the next one is staged in May (checkNewSeasonGeneration).
                            // Making one here gave an "FA Cup 1889" of the 12 league clubs in February 1889.
                            continue;
                        }
                        edition = createNewCupEdition(cup, currentDate);
                    } else {
                        // Activate the upcoming edition if it's time
                        // FA Cup creation script runs on May 1st
                        if (currentDate.getMonthValue() >= CUP_SEASON_OPENS_MONTH) {
                            edition.setStartDate(currentDate);
                            edition.setEndDate(SeasonCalendar.forDate(currentDate).cupEnd()); // P2-11: past the final
                            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Activated upcoming cup edition: " + edition.getName());
                        } else {
                            continue; // Not yet time to start this cup
                        }
                    }
                }
                
                // Add league to allLeagues list if it's missing (ensures it shows up in UI)
                if (currentGame.getAllLeagues() != null) {
                    boolean found = false;
                    for (Competition comp : currentGame.getAllLeagues()) {
                        if (comp.getName() != null && comp.getName().equals(cup.getName())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        currentGame.getAllLeagues().add(cup);
                    }
                }
                
                // Check if cup needs initial draw
                if (needsCupDraw(edition)) {
                    Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generating cup draw for: " + cup.getName());
                    generateCupDraw(cup, edition);
                } else {
                    Log.debug(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup " + cup.getName() + " already has matches scheduled");
                }
            }
        } catch (Exception e) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Error in checkAndScheduleCupDraws()", e);
            Log.error(Log.ENGINE_AUTHORITY, e);
        }
    }
    
    /**
     * Find an edition that hasn't started yet
     */
    private CompetitionEdition findUpcomingEdition(Competition cup, LocalDateTime currentDate) {
        if (cup.getEditions() == null) return null;
        
        for (CompetitionEdition edition : cup.getEditions()) {
            if (edition.getStartDate() == null) {
                // If it's the 1888-89 edition and we are in 1888, this is the one
                if (edition.getName() != null && edition.getName().contains("1888-89")) {
                    return edition;
                }
            }
        }
        return null;
    }
    
    /**
     * Get current cup edition that covers the current date
     */
    private CompetitionEdition getCurrentCupEdition(Competition cup, LocalDateTime currentDate) {
        if (cup.getEditions() == null || cup.getEditions().isEmpty()) {
            return null;
        }
        
        // Find edition that covers current date
        for (CompetitionEdition edition : cup.getEditions()) {
            if (edition.getStartDate() != null && edition.getEndDate() != null) {
                if (!currentDate.isBefore(edition.getStartDate()) && 
                    !currentDate.isAfter(edition.getEndDate())) {
                    return edition;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Create a new cup edition for the current season
     */
    private CompetitionEdition createNewCupEdition(Competition cup, LocalDateTime currentDate) {
        CompetitionEdition edition = new CompetitionEdition();
        edition.setId(ids().nextId());
        edition.setName(cup.getName() + " " + currentDate.getYear());
        edition.setStartDate(currentDate);
        edition.setEndDate(SeasonCalendar.forDate(currentDate).cupEnd()); // P2-11: past the final
        
        // Get participant clubs (all clubs from leagues)
        List<Long> participantIds = getCupParticipantClubs();
        edition.setParticipantClubsIds(participantIds);
        edition.setParticipantClubs(participantIds.size());
        
        if (cup.getEditions() == null) {
            cup.setEditions(new ArrayList<>());
        }
        cup.getEditions().add(edition);
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Created new cup edition: " + edition.getName() + 
            " with " + participantIds.size() + " participants");
        
        return edition;
    }
    
    /**
     * Get list of club IDs that should participate in cups
     * For now, uses all clubs from all leagues
     */
    private List<Long> getCupParticipantClubs() {
        List<Long> clubIds = new ArrayList<>();
        
        if (mainAuthority != null && mainAuthority.getLeagues() != null) {
            for (League league : mainAuthority.getLeagues()) {
                if (league.getLeagueClubs() != null) {
                    for (Club club : league.getLeagueClubs()) {
                        if (club != null && club.getId() != null) {
                            clubIds.add(club.getId());
                        }
                    }
                }
            }
        }
        
        return clubIds;
    }
    
    /**
     * Check if cup needs an initial draw (no matches scheduled yet)
     */
    private boolean needsCupDraw(CompetitionEdition edition) {
        if (edition.getParticipantClubsIds() == null || edition.getParticipantClubsIds().isEmpty()) {
            return false;
        }
        
        // Check if any matches exist for this edition
        for (Club club : currentGame.getAllClubs()) {
            if (club == null || club.getScheduledMatches() == null) {
                continue;
            }
            for (Match match : club.getScheduledMatches()) {
                if (match != null && match.getCompetitionEditionId() != null &&
                    match.getCompetitionEditionId().equals(edition.getId())) {
                    return false; // Matches already scheduled
                }
            }
        }
        
        return true; // No matches found, need draw
    }
    
    /**
     * P2-11: the round dates of a cup drawn today, on the era calendar (proper rounds in February and March, the
     * final on the last Saturday of March). A draw made after the season's first round date plays on the next
     * season's calendar when its final has passed, else from two weeks on, two weeks apart (never in the past).
     */
    static List<LocalDateTime> cupRoundDates(int rounds, LocalDateTime today) {
        SeasonCalendar calendar = SeasonCalendar.forDate(today);
        List<LocalDateTime> dates = calendar.cupRoundDates(rounds);
        if (dates.get(0).toLocalDate().isAfter(today.toLocalDate())) {
            return dates;
        }
        if (!calendar.cupFinal().isAfter(today.toLocalDate())) {
            return SeasonCalendar.forSeason(calendar.getStartYear() + 1).cupRoundDates(rounds);
        }
        Log.warn(Log.ENGINE_SCHEDULING, "AuthorityManager", "Cup drawn on " + today
                + " after the season's first round date; rounds two weeks apart from two weeks on");
        List<LocalDateTime> late = new ArrayList<>();
        LocalDateTime first = today.toLocalDate().plusWeeks(2)
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY))
                .atTime(SeasonCalendar.KICK_OFF);
        for (int r = 0; r < rounds; r++) {
            late.add(first.plusWeeks(2L * r));
        }
        return late;
    }

    /**
     * Generate cup draw and schedule matches
     */
    private void generateCupDraw(Competition cup, CompetitionEdition edition) {
        if (edition.getParticipantClubsIds() == null || edition.getParticipantClubsIds().isEmpty()) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot generate cup draw: no participants");
            return;
        }
        
        // Generate COMPLETE bracket (all rounds) using CupBracketGenerator
        LocalDateTime currentDate = currentGame.getGameDate();
        
        com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator bracketGenerator = 
            new com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator(ids(),
                Futtoboru.randomOf(game).fork("cup-draw:" + cup.getId() + ":" + edition.getId()));
        
        List<Match> allMatches = bracketGenerator.generateCompleteBracket(
            edition.getParticipantClubsIds(),
            cup.getId(),
            edition.getId(),
            rounds -> cupRoundDates(rounds, currentDate)
        );
        
        if (allMatches == null || allMatches.isEmpty()) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup bracket generation produced no matches");
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: ERROR - Bracket generation returned null or empty!");
            return;
        }
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: *** BRACKET GENERATION COMPLETE ***");
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Total matches generated: " + allMatches.size());
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match m : allMatches) {
            if (m != null && m.getRound() != null) {
                matchesByRound.put(m.getRound(), matchesByRound.getOrDefault(m.getRound(), 0) + 1);
            }
        }
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Matches by round BEFORE adding to clubs: " + matchesByRound);
        
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        
        // Process all matches and add to clubs
        // Strategy: Add ALL matches to ALL participating clubs so they're accessible
        // Round 1 matches: Add to specific clubs (home/away)
        // Future rounds: Add to all participating clubs (will be filtered when teams determined)
        
        int round1Count = 0;
        int round2PlusCount = 0;
        int nullRoundCount = 0;
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Processing " + allMatches.size() + " matches from bracket generator");
        
        for (Match match : allMatches) {
            if (match == null) {
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: WARNING - Null match in allMatches list!");
                continue;
            }
            
            // Debug: Log match details
            if (match.getRound() == null) {
                nullRoundCount++;
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: WARNING - Match has null round! ID: " + match.getId() + 
                    ", BracketPath: " + match.getBracketPath() + ", Edition: " + match.getCompetitionEditionId());
            }
            
            // Matches already have competition info set by generator
            
            if (match.getRound() != null && match.getRound() == 1) {
                round1Count++;
                // Round 1: Add to specific clubs
                if (match.getHomeClubId() != null && match.getAwayClubId() != null) {
                    Club homeClub = currentGame.getClubById(match.getHomeClubId());
                    Club awayClub = currentGame.getClubById(match.getAwayClubId());
                    
                    if (homeClub != null) {
                        if (homeClub.getScheduledMatches() == null) {
                            homeClub.setScheduledMatches(new ArrayList<>());
                        }
                        if (!homeClub.getScheduledMatches().contains(match)) {
                            homeClub.getScheduledMatches().add(match);
                        }
                    }
                    if (awayClub != null) {
                        if (awayClub.getScheduledMatches() == null) {
                            awayClub.setScheduledMatches(new ArrayList<>());
                        }
                        if (!awayClub.getScheduledMatches().contains(match)) {
                            awayClub.getScheduledMatches().add(match);
                        }
                    }
                    
                    // Create cup draw result message for Round 1 matches
                    if (homeClub != null && awayClub != null) {
                        Message drawResultMessage = messageManager.createCupDrawResultMessage(
                            cup, homeClub, awayClub, match.getMatchDateTime(), LanguageModLoader.getValue("msg_round_first")
                        );
                        if (drawResultMessage != null) {
                            messageManager.deliverMessage(drawResultMessage);
                        }
                    }
                }
            } else if (match.getRound() != null && match.getRound() > 1) {
                // Future rounds: Add to ALL participating clubs so matches are accessible
                // When teams are determined, they'll already be in the clubs' lists
                round2PlusCount++;
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: *** ADDING FUTURE ROUND MATCH *** " + match.getBracketPath() + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + 
                    ", Date: " + match.getMatchDateTime() + ") to all " + edition.getParticipantClubsIds().size() + " participating clubs");
                
                int addedCount = 0;
                for (Long clubId : edition.getParticipantClubsIds()) {
                    Club club = currentGame.getClubById(clubId);
                    if (club != null) {
                        if (club.getScheduledMatches() == null) {
                            club.setScheduledMatches(new ArrayList<>());
                        }
                        // Use ID-based check instead of contains() for reliability
                        boolean alreadyAdded = false;
                        for (Match m : club.getScheduledMatches()) {
                            if (m != null && m.getId() != null && m.getId().equals(match.getId())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            club.getScheduledMatches().add(match);
                            addedCount++;
                        }
                    }
                }
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Added match " + match.getBracketPath() + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + 
                    ", Edition: " + match.getCompetitionEditionId() + 
                    ") to " + addedCount + " clubs");
                
                // CRITICAL VERIFICATION: Check if match is actually in clubs after adding
                if (addedCount > 0) {
                    Club testClub = currentGame.getClubById(edition.getParticipantClubsIds().get(0));
                    if (testClub != null && testClub.getScheduledMatches() != null) {
                        boolean found = false;
                        for (Match m : testClub.getScheduledMatches()) {
                            if (m != null && m.getId() != null && m.getId().equals(match.getId())) {
                                found = true;
                                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: VERIFIED - Match " + match.getBracketPath() + 
                                    " is in club " + testClub.getName() + "'s scheduled matches");
                                break;
                            }
                        }
                        if (!found) {
                            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: ERROR - Match " + match.getBracketPath() + 
                                " was NOT found in club " + testClub.getName() + " after adding!");
                        }
                    }
                }
            } else {
                Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: WARNING - Match with null or invalid round: " + 
                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                    ", Round: " + match.getRound());
            }
        }
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Processed " + round1Count + " Round 1 matches, " + 
            round2PlusCount + " Round 2+ matches, " + nullRoundCount + " matches with null round");
        
        if (round2PlusCount == 0 && allMatches.size() > round1Count) {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: CRITICAL ERROR - Expected Round 2+ matches but found none!");
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: Total matches: " + allMatches.size() + ", Round 1: " + round1Count);
            // Debug: List all matches to see what's wrong
            for (Match m : allMatches) {
                if (m != null) {
                    Log.info(Log.ENGINE_AUTHORITY, "  - Match: " + m.getBracketPath() + ", Round: " + m.getRound() + 
                        ", Edition: " + m.getCompetitionEditionId());
                }
            }
        }
        
        // FINAL VERIFICATION: Count matches by round in all clubs after adding
        Map<Integer, Integer> finalMatchesByRoundInClubs = new HashMap<>();
        for (Long clubId : edition.getParticipantClubsIds()) {
            Club club = currentGame.getClubById(clubId);
            if (club != null && club.getScheduledMatches() != null) {
                for (Match m : club.getScheduledMatches()) {
                    if (m != null && m.getCompetitionEditionId() != null && 
                        m.getCompetitionEditionId().equals(edition.getId()) &&
                        m.getRound() != null) {
                        finalMatchesByRoundInClubs.put(m.getRound(), 
                            finalMatchesByRoundInClubs.getOrDefault(m.getRound(), 0) + 1);
                    }
                }
            }
        }
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager: FINAL VERIFICATION - Matches by round in clubs: " + finalMatchesByRoundInClubs);
        
        // Create MANDATORY draw message for the user (to reveal the draw)
        Message mandatoryDrawMessage = messageManager.createCupDrawMessage(cup, edition, 1, LanguageModLoader.getValue("msg_round_first"));
        if (mandatoryDrawMessage != null) {
            mandatoryDrawMessage.setScheduledDate(currentDate.plusDays(1));
            messageManager.scheduleMessage(mandatoryDrawMessage);
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Scheduled mandatory cup draw message for: " + cup.getName());
        }
        
        // Log summary (already logged above with matchesByRound)
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generated COMPLETE cup bracket: " + allMatches.size() + " total matches");
        // Use the matchesByRound already calculated above
        for (Map.Entry<Integer, Integer> entry : matchesByRound.entrySet()) {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "  Round " + entry.getKey() + ": " + entry.getValue() + " matches");
        }
    }
    
    /**
     * Check if cup rounds need to be advanced (v1.0 - Cup Round Progression)
     */
    private void checkCupRoundProgression() {
        try {
            if (currentGame == null) {
                return;
            }
            
            List<Competition> cups = currentGame.getAllCups();
            if (cups == null || cups.isEmpty()) {
                return;
            }
            
            LocalDateTime currentDate = currentGame.getGameDate();
            
            for (Competition cup : cups) {
                if (cup == null || !Competition.COMPETITION_CUP.equals(cup.getCompetitionType())) {
                    continue;
                }
                
                CompetitionEdition edition = getCurrentCupEdition(cup, currentDate);
                if (edition == null) {
                    continue; // No active edition
                }
                if (edition.getChampionsId() != null) {
                    continue; // already won (the bracket path completes it when the final is recorded)
                }
                
                // Check if current round is complete
                if (isCupRoundComplete(edition)) {
                    // Get winners from current round
                    List<Club> winners = getRoundWinners(edition);
                    
                    if (winners.isEmpty()) {
                        Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Round complete but no winners found for: " + cup.getName());
                        continue;
                    }
                    
                    if (winners.size() == 1) {
                        // Cup complete!
                        completeCupEdition(cup, edition, winners.get(0));
                    } else {
                        // Advance to next round
                        advanceCupRound(cup, edition, winners);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Error in checkCupRoundProgression()", e);
            Log.error(Log.ENGINE_AUTHORITY, e);
        }
    }
    
    /**
     * Check if a cup round is complete (all matches played)
     */
    private boolean isCupRoundComplete(CompetitionEdition edition) {
        // Get all matches for this edition
        List<Match> editionMatches = getMatchesForEdition(edition);
        
        if (editionMatches.isEmpty()) {
            return false; // No matches yet
        }
        
        // Get current round number
        Integer currentRound = getCurrentRound(editionMatches);
        if (currentRound == null) {
            return false; // No round started yet
        }
        
        // Get all matches for current round
        List<Match> roundMatches = new ArrayList<>();
        for (Match match : editionMatches) {
            if (match.getRound() != null && match.getRound().equals(currentRound)) {
                roundMatches.add(match);
            }
        }
        
        if (roundMatches.isEmpty()) {
            return false; // No matches for this round
        }
        
        // Check if all round matches are played
        for (Match match : roundMatches) {
            if (match.getIsPlayed() == null || !match.getIsPlayed()) {
                return false; // Round not complete
            }
        }
        
        return true; // All matches played
    }
    
    /**
     * Get all matches for a competition edition
     */
    private List<Match> getMatchesForEdition(CompetitionEdition edition) {
        return getMatchesForEdition(edition, currentGame);
    }
    
    /**
     * Get all matches for a competition edition (with SaveGame parameter)
     */
    private List<Match> getMatchesForEdition(CompetitionEdition edition, SaveGame saveGame) {
        List<Match> matches = new ArrayList<>();
        
        if (edition == null || edition.getId() == null) {
            return matches;
        }
        
        if (saveGame == null || saveGame.getAllClubs() == null) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot get matches for edition: SaveGame is null");
            return matches;
        }
        
        // Search through all clubs' scheduled and played matches
        for (Club club : saveGame.getAllClubs()) {
            if (club == null) {
                continue;
            }
            
            // Check scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && match.getCompetitionEditionId() != null &&
                        match.getCompetitionEditionId().equals(edition.getId())) {
                        if (!matches.contains(match)) {
                            matches.add(match);
                        }
                    }
                }
            }
            
            // Check played matches
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match != null && match.getCompetitionEditionId() != null &&
                        match.getCompetitionEditionId().equals(edition.getId())) {
                        if (!matches.contains(match)) {
                            matches.add(match);
                        }
                    }
                }
            }
        }
        
        return matches;
    }
    
    /**
     * Get current round number from matches
     */
    private Integer getCurrentRound(List<Match> editionMatches) {
        Integer maxRound = null;
        
        for (Match match : editionMatches) {
            if (match.getRound() != null) {
                if (maxRound == null || match.getRound() > maxRound) {
                    maxRound = match.getRound();
                }
            }
        }
        
        return maxRound;
    }
    
    /**
     * Get winners from current round
     */
    private List<Club> getRoundWinners(CompetitionEdition edition) {
        List<Club> winners = new ArrayList<>();
        
        // Get current round
        List<Match> editionMatches = getMatchesForEdition(edition);
        Integer currentRound = getCurrentRound(editionMatches);
        if (currentRound == null) {
            return winners;
        }
        
        // Get all matches for current round
        for (Match match : editionMatches) {
            if (match.getRound() != null && match.getRound().equals(currentRound) &&
                match.getIsPlayed() != null && match.getIsPlayed()) {
                
                // Determine winner
                Club winner = getMatchWinner(match);
                if (winner != null) {
                    winners.add(winner);
                }
            }
        }
        
        return winners;
    }
    
    /**
     * Get winner of a match (or null if draw)
     */
    private Club getMatchWinner(Match match) {
        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            return null;
        }
        
        if (match.getHomeGoals() > match.getAwayGoals()) {
            return currentGame.getClubById(match.getHomeClubId());
        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            return currentGame.getClubById(match.getAwayClubId());
        }
        
        // Draw - need replay (handled separately)
        return null;
    }
    
    /**
     * Advance cup to next round
     */
    private void advanceCupRound(Competition cup, CompetitionEdition edition, List<Club> winners) {
        if (winners.isEmpty()) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot advance round: no winners");
            return;
        }
        
        // Get next round number
        List<Match> editionMatches = getMatchesForEdition(edition);
        Integer currentRound = getCurrentRound(editionMatches);
        Integer nextRound = (currentRound != null) ? currentRound + 1 : 1;
        
        // Get winner IDs
        List<Long> winnerIds = new ArrayList<>();
        for (Club winner : winners) {
            if (winner != null && winner.getId() != null) {
                winnerIds.add(winner.getId());
            }
        }
        
        if (winnerIds.isEmpty()) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot advance round: no valid winner IDs");
            return;
        }
        
        // Generate next round draw
        List<Match> nextRoundMatches = competitionScheduler.competitionDraw(cup, winnerIds);
        
        if (nextRoundMatches == null || nextRoundMatches.isEmpty()) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Next round draw generated no matches");
            return;
        }
        
        // Schedule next round matches on the next cup date of the era calendar (P2-11), else 2 weeks from now
        LocalDateTime currentDate = currentGame.getGameDate();
        LocalDateTime nextRoundDate = SeasonCalendar.forDate(currentDate).nextCupDateAfter(currentDate.toLocalDate());
        if (nextRoundDate == null) {
            nextRoundDate = currentDate.toLocalDate().plusWeeks(2)
                    .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY))
                    .atTime(SeasonCalendar.KICK_OFF);
        }
        
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        String roundName = getRoundName(nextRound, winnerIds.size());
        
        for (Match match : nextRoundMatches) {
            match.setMatchType(Match.CUP_MATCH);
            match.setCompetitionId(cup.getId());
            match.setCompetitionEditionId(edition.getId());
            match.setRound(nextRound);
            match.setMatchDateTime(nextRoundDate);
            match.setIsProposed(false);
            match.setIsAccepted(true);
            match.setIsPlayed(false);
            
            // Add to clubs' scheduled matches
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub != null) {
                if (homeClub.getScheduledMatches() == null) {
                    homeClub.setScheduledMatches(new ArrayList<>());
                }
                homeClub.getScheduledMatches().add(match);
            }
            if (awayClub != null) {
                if (awayClub.getScheduledMatches() == null) {
                    awayClub.setScheduledMatches(new ArrayList<>());
                }
                awayClub.getScheduledMatches().add(match);
            }
            
            // Create cup draw message
            if (homeClub != null && awayClub != null) {
                Message drawMessage = messageManager.createCupDrawResultMessage(
                    cup, homeClub, awayClub, nextRoundDate, roundName
                );
                if (drawMessage != null) {
                    messageManager.deliverMessage(drawMessage);
                }
            }
        }
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Advanced cup to " + roundName + ": " + 
            nextRoundMatches.size() + " matches for " + cup.getName());
    }
    
    /**
     * Get round name (e.g., "First Round", "Second Round", "Quarter-Final", etc.)
     */
    private String getRoundName(Integer roundNumber, int participants) {
        if (roundNumber == null) {
            return LanguageModLoader.format("msg_round_number", roundNumber);
        }

        // Calculate round name based on number of participants
        if (participants == 2) {
            return LanguageModLoader.getValue("msg_round_final");
        } else if (participants == 4) {
            return LanguageModLoader.getValue("msg_round_semi_final");
        } else if (participants == 8) {
            return LanguageModLoader.getValue("msg_round_quarter_final");
        } else {
            // Use ordinal for other rounds
            switch (roundNumber) {
            case 1: return LanguageModLoader.getValue("msg_round_first");
            case 2: return LanguageModLoader.getValue("msg_round_second");
            case 3: return LanguageModLoader.getValue("msg_round_third");
            case 4: return LanguageModLoader.getValue("msg_round_fourth");
            case 5: return LanguageModLoader.getValue("msg_round_fifth");
            case 6: return LanguageModLoader.getValue("msg_round_sixth");
            default: return LanguageModLoader.format("msg_round_number", roundNumber);
            }
        }
    }
    
    /**
     * Complete cup and declare winner
     * Public method to allow CupBracketManager to trigger completion
     */
    public void completeCupEdition(Competition cup, CompetitionEdition edition, Club winner) {
        completeCupEdition(cup, edition, winner, null);
    }
    
    /**
     * Complete cup and declare winner (with optional SaveGame parameter)
     * Public method to allow CupBracketManager to trigger completion
     * 
     * @param cup The cup competition
     * @param edition The competition edition
     * @param winner The winning club
     * @param saveGame Optional SaveGame instance (if null, uses this.currentGame)
     */
    public void completeCupEdition(Competition cup, CompetitionEdition edition, Club winner, SaveGame saveGame) {
        if (winner == null) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot complete cup: winner is null");
            return;
        }
        
        // Use provided SaveGame or fall back to currentGame
        SaveGame gameToUse = saveGame != null ? saveGame : currentGame();
        if (gameToUse == null) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cannot complete cup: SaveGame is null");
            return;
        }
        
        // Once only: the bracket path (CupBracketManager) and checkCupRoundProgression can both get here
        if (edition.getChampionsId() != null) {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup already complete: " + edition.getName());
            return;
        }

        // Store winner
        edition.setChampionsId(winner.getId());

        // Runner-up: the other finalist of the bracket final (P2-7)
        Match finalMatch = findPlayedFinal(edition, winner, gameToUse);
        Club runnerUp = finalMatch != null ? getMatchLoser(finalMatch, winner, gameToUse) : null;
        if (runnerUp != null) {
            edition.setRunnersUpId(runnerUp.getId());
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup runner-up saved: " + runnerUp.getName() + " (ID: " + runnerUp.getId() + ")");
        }
        
        // Log completion details
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup completion saved - Champion ID: " + edition.getChampionsId() + 
            ", Runner-up ID: " + edition.getRunnersUpId());
        
        // Create cup completion message
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        Message completionMessage = createCupCompletionMessage(cup, edition, winner);
        if (completionMessage != null) {
            messageManager.deliverMessage(completionMessage);
        }
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Cup complete: " + cup.getName() + " won by " + winner.getName());
    }
    
    /**
     * The played final of a cup edition: the last round of the bracket, the latest played match of that round
     * that the winner took part in (a replay of the final if there was one). Null if not found.
     */
    private Match findPlayedFinal(CompetitionEdition edition, Club winner, SaveGame saveGame) {
        List<Match> matches = getMatchesForEdition(edition, saveGame);
        Integer finalRound = getCurrentRound(matches);
        Match found = null;
        if (finalRound == null) {
            return null;
        }
        for (Match match : matches) {
            if (finalRound.equals(match.getRound()) && match.getIsPlayed() != null && match.getIsPlayed()
                    && (winner.getId().equals(match.getHomeClubId()) || winner.getId().equals(match.getAwayClubId()))) {
                if (found == null || (match.getMatchDateTime() != null && found.getMatchDateTime() != null
                        && match.getMatchDateTime().isAfter(found.getMatchDateTime()))) {
                    found = match;
                }
            }
        }
        return found;
    }

    /**
     * Get loser of a match (opposite of winner)
     */
    private Club getMatchLoser(Match match, Club winner) {
        return getMatchLoser(match, winner, currentGame);
    }
    
    /**
     * Get loser of a match (opposite of winner) with explicit SaveGame
     */
    private Club getMatchLoser(Match match, Club winner, SaveGame saveGame) {
        if (match == null || winner == null || saveGame == null) {
            return null;
        }
        
        Club homeClub = saveGame.getClubById(match.getHomeClubId());
        Club awayClub = saveGame.getClubById(match.getAwayClubId());
        
        if (homeClub != null && homeClub.getId().equals(winner.getId())) {
            return awayClub;
        } else if (awayClub != null && awayClub.getId().equals(winner.getId())) {
            return homeClub;
        }
        
        return null;
    }
    
    /**
     * Create cup completion message
     */
    private Message createCupCompletionMessage(Competition cup, CompetitionEdition edition, Club winner) {
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_COMPLETE");
        message.setPriority(MessagePriority.URGENT);
        // edition name in the title: MessageManager drops a second message with the same type and title
        message.setTitle(LanguageModLoader.format("msg_cup_complete_title",
                edition != null && edition.getName() != null ? edition.getName() : cup.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_cup_complete_text", cup.getName(), winner.getName()));
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Check for cup matches that ended in draws and schedule replays (v1.0 - Cup Replay Handling)
     */
    private void checkCupReplays() {
        try {
            if (currentGame == null) {
                return;
            }
            
            List<Competition> cups = currentGame.getAllCups();
            if (cups == null || cups.isEmpty()) {
                return;
            }
            
            LocalDateTime currentDate = currentGame.getGameDate();
            
            for (Competition cup : cups) {
                if (cup == null || !Competition.COMPETITION_CUP.equals(cup.getCompetitionType())) {
                    continue;
                }
                
                CompetitionEdition edition = getCurrentCupEdition(cup, currentDate);
                if (edition == null) {
                    continue;
                }
                
                // Check all matches for this edition
                List<Match> editionMatches = getMatchesForEdition(edition);
                
                for (Match match : editionMatches) {
                    if (match.getBracketPath() != null) {
                        continue; // bracket ties: replays come from the result path (CupBracketManager), not from here
                    }
                    // Check if match is played and ended in a draw
                    if (match.getIsPlayed() != null && match.getIsPlayed() &&
                        match.getHomeGoals() != null && match.getAwayGoals() != null &&
                        match.getHomeGoals().equals(match.getAwayGoals())) {
                        
                        // Check if replay already scheduled
                        if (!isReplayScheduled(match, edition)) {
                            scheduleCupReplay(match, cup, edition);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(Log.ENGINE_AUTHORITY, "AuthorityManager", "Error in checkCupReplays()", e);
            Log.error(Log.ENGINE_AUTHORITY, e);
        }
    }
    
    /**
     * Check if a replay is already scheduled for a tied match
     */
    private boolean isReplayScheduled(Match originalMatch, CompetitionEdition edition) {
        if (originalMatch == null || edition == null) {
            return false;
        }
        
        // Check if a replay match exists (same clubs, same round, later date)
        List<Match> editionMatches = getMatchesForEdition(edition);
        LocalDateTime originalDate = originalMatch.getMatchDateTime();
        
        for (Match match : editionMatches) {
            if (match == null || match.equals(originalMatch)) {
                continue; // Skip the original match
            }
            
            // Check if this is a replay (same clubs, same round, later date)
            if (match.getHomeClubId() != null && match.getAwayClubId() != null &&
                originalMatch.getHomeClubId() != null && originalMatch.getAwayClubId() != null &&
                match.getRound() != null && originalMatch.getRound() != null &&
                match.getRound().equals(originalMatch.getRound())) {
                
                // Check if clubs match (could be swapped home/away)
                boolean clubsMatch = 
                    (match.getHomeClubId().equals(originalMatch.getHomeClubId()) && 
                     match.getAwayClubId().equals(originalMatch.getAwayClubId())) ||
                    (match.getHomeClubId().equals(originalMatch.getAwayClubId()) && 
                     match.getAwayClubId().equals(originalMatch.getHomeClubId()));
                
                if (clubsMatch && match.getMatchDateTime() != null && originalDate != null &&
                    match.getMatchDateTime().isAfter(originalDate)) {
                    return true; // Replay already scheduled
                }
            }
        }
        
        return false;
    }
    
    /**
     * Schedule a cup replay for a tied match (7 days later)
     */
    private void scheduleCupReplay(Match originalMatch, Competition cup, CompetitionEdition edition) {
        if (originalMatch == null || cup == null || edition == null) {
            return;
        }
        
        // Create replay match (swap home/away)
        Match replay = new Match();
        replay.setHomeClubId(originalMatch.getAwayClubId()); // Swap home/away
        replay.setAwayClubId(originalMatch.getHomeClubId());
        replay.setMatchType(Match.CUP_MATCH);
        replay.setCompetitionId(cup.getId());
        replay.setCompetitionEditionId(edition.getId());
        replay.setRound(originalMatch.getRound());
        // P2-11: the following Saturday, else midweek, when both clubs are free and before the next round
        java.time.LocalDate today = currentGame.getGameDate().toLocalDate();
        LocalDateTime nextRound = SeasonCalendar.forDate(originalMatch.getMatchDateTime())
                .nextCupDateAfter(originalMatch.getMatchDateTime().toLocalDate());
        replay.setMatchDateTime(SeasonCalendar.replayDate(currentGame, originalMatch, today,
                nextRound != null ? nextRound.toLocalDate() : null));
        replay.setIsProposed(false);
        replay.setIsAccepted(true);
        replay.setIsPlayed(false);
        
        // Add to clubs' scheduled matches
        Club homeClub = currentGame.getClubById(replay.getHomeClubId());
        Club awayClub = currentGame.getClubById(replay.getAwayClubId());
        
        if (homeClub != null) {
            if (homeClub.getScheduledMatches() == null) {
                homeClub.setScheduledMatches(new ArrayList<>());
            }
            homeClub.getScheduledMatches().add(replay);
        }
        if (awayClub != null) {
            if (awayClub.getScheduledMatches() == null) {
                awayClub.setScheduledMatches(new ArrayList<>());
            }
            awayClub.getScheduledMatches().add(replay);
        }
        
        // Create replay message
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        if (homeClub != null && awayClub != null) {
            Message replayMessage = createCupReplayMessage(cup, homeClub, awayClub, replay.getMatchDateTime());
            if (replayMessage != null) {
                messageManager.deliverMessage(replayMessage);
            }
        }
        
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Scheduled cup replay: " + 
            (homeClub != null ? homeClub.getName() : "Unknown") + " v " + 
            (awayClub != null ? awayClub.getName() : "Unknown") + 
            " on " + replay.getMatchDateTime());
    }
    
    /**
     * Create cup replay message
     */
    private Message createCupReplayMessage(Competition cup, Club homeClub, Club awayClub, LocalDateTime replayDate) {
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_REPLAY");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(LanguageModLoader.format("msg_cup_replay_title", cup.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_cup_replay_text", cup.getName(), homeClub.getName(),
                awayClub.getName(), formatDate(replayDate)));
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Format date for messages (helper method)
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return LanguageModLoader.getValue("msg_tba");
        }
        return MessageManager.formatDay(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    /**
     * Check if any competition needs a new season edition generated
     * CRITICAL FIX: Now checks more frequently (not just July) and also after league completion
     */
    private void checkNewSeasonGeneration() {
        LocalDateTime currentDate = currentGame.getGameDate();
        if (currentDate == null) return;
        
        // Check leagues - now checks every month, not just July
        // This ensures seasons are generated even if league completes outside of July
        if (currentGame.getMainAuthority() != null && currentGame.getMainAuthority().getLeagues() != null) {
            for (League league : currentGame.getMainAuthority().getLeagues()) {
                // G-08: never while a season is still being played (the 1889-90 season was generated on 1 January
                // 1889 with the old clubs) or while its re-election waits for the annual meeting
                if (league == null || isSeasonRunning(league) || reElection.isPending(league)) {
                    continue;
                }
                if (needsNextSeasonGeneration(league, currentDate)) {
                    generateNextSeason(league, currentDate);
                }
            }
        }
        
        // Check cups
        if (currentGame.getAllCups() != null) {
            for (Competition cup : currentGame.getAllCups()) {
                if (needsNextCupStaging(cup, currentDate)) {
                    generateNextCupStaging(cup, currentDate);
                }
            }
        }
    }
    
    /**
     * Check and generate next season for a specific league
     * Called after league completion to ensure new season is created immediately
     */
    private void checkAndGenerateNextSeasonForLeague(League league) {
        if (league == null) return;
        
        LocalDateTime currentDate = currentGame.getGameDate();
        if (currentDate == null) return;
        
        if (!reElection.isPending(league) && needsNextSeasonGeneration(league, currentDate)) {
            Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generating next season for " + league.getName() + " after completion");
            generateNextSeason(league, currentDate);
        }
    }

    /** @return true when a club of the league has a played league match */
    private static boolean hasPlayedLeagueMatches(League league) {
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) {
                continue;
            }
            for (Match m : club.getPlayedMatches()) {
                if (m != null && m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return true when the league's latest season has no final table yet (it is being played or about to be) */
    private static boolean isSeasonRunning(League league) {
        if (league.getEditions() == null || league.getEditions().isEmpty()) {
            return false;
        }
        CompetitionEdition last = league.getEditions().get(league.getEditions().size() - 1);
        return last != null && last.getFinalStandings().isEmpty();
    }

    private boolean needsNextSeasonGeneration(League league, LocalDateTime currentDate) {
        // Find if an edition for the upcoming year already exists
        int nextYear = currentDate.getYear();
        String nextSeasonNameFragment = nextYear + "-" + ((nextYear + 1) % 100);
        
        for (CompetitionEdition edition : league.getEditions()) {
            if (edition.getName() != null && edition.getName().contains(nextSeasonNameFragment)) {
                return false; // Already exists
            }
        }
        return true;
    }

    private void generateNextSeason(League league, LocalDateTime currentDate) {
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generating next season for league: " + league.getName());
        
        CompetitionEdition nextEdition = new CompetitionEdition();
        nextEdition.setId(ids().nextId());
        int startYear = currentDate.getYear();
        nextEdition.setName(league.getName() + " " + startYear + "-" + ((startYear + 1) % 100));
        
        // Start around September 1st
        nextEdition.setStartDate(LocalDateTime.of(startYear, 9, 1, 0, 0));
        nextEdition.setEndDate(SeasonCalendar.forSeason(startYear).leagueEnd()); // P2-11: mid-April
        
        // The League's clubs now (G-08: after the re-election vote, if there was one), not a copy of the last season
        if (league.getLeagueClubs() != null) {
            for (Club club : league.getLeagueClubs()) {
                if (club != null && !nextEdition.getParticipantClubsIds().contains(club.getId())) {
                    nextEdition.getParticipantClubsIds().add(club.getId());
                }
            }
            nextEdition.setParticipantClubs(nextEdition.getParticipantClubsIds().size());
        }

        league.getEditions().add(nextEdition);
        
        // CRITICAL: Generate fixtures for the new season
        // This ensures the league can be played immediately
        List<com.rndmodgames.futtoboru.data.Match> fixtures = 
            fixtureGenerator.generateLeagueFixtures(league, nextEdition.getStartDate(), nextEdition.getEndDate());
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generated " + fixtures.size() + " fixtures for new season: " + nextEdition.getName());
        
        // The league table is reset when the previous season completes (completeLeagueSeason, P2-6), not here:
        // this edition can be generated while the previous season is still being played.

// Inform user via Inbox
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        Message msg = new Message();
        msg.setCategory(MessageCategory.SYSTEM);
        msg.setPriority(MessagePriority.NORMAL);
        msg.setTitle(LanguageModLoader.format("msg_new_season_title", nextEdition.getName()));
        msg.setPlainTextMessage(LanguageModLoader.format("msg_new_season_text", nextEdition.getName()));
        msg.setIsRead(false);
        msg.setIsDeleted(false);
        messageManager.deliverMessage(msg);
    }

    private boolean needsNextCupStaging(Competition cup, LocalDateTime currentDate) {
        // P2-3: not on 1 January (a 1889-90 cup was drawn and played in January 1889, alongside the 1888-89
        // season): the next edition opens in May, like the first one (the cup creation script and the 1888-89
        // activation in checkAndScheduleCupDraws), and not while the edition being played has no winner yet
        if (currentDate.getMonthValue() < CUP_SEASON_OPENS_MONTH) {
            return false;
        }
        CompetitionEdition current = getCurrentCupEdition(cup, currentDate);
        if (current != null && current.getChampionsId() == null) {
            return false;
        }
        int nextYear = currentDate.getYear();
        String nextSeasonNameFragment = nextYear + "-" + ((nextYear + 1) % 100);
        
        for (CompetitionEdition edition : cup.getEditions()) {
            if (edition.getName() != null && edition.getName().contains(nextSeasonNameFragment)) {
                return false; // Already exists
            }
        }
        return true;
    }

    private void generateNextCupStaging(Competition cup, LocalDateTime currentDate) {
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generating next season staging for cup: " + cup.getName());
        
        CompetitionEdition nextEdition = new CompetitionEdition();
        nextEdition.setId(ids().nextId());
        int startYear = currentDate.getYear();
        nextEdition.setName(cup.getName() + " " + startYear + "-" + ((startYear + 1) % 100));
        
        // Set dates for cup staging
        nextEdition.setStartDate(currentDate);
        nextEdition.setEndDate(SeasonCalendar.forDate(currentDate).cupEnd()); // P2-11: past the final
        
        // Copy participants from previous edition if applicable
        if (!cup.getEditions().isEmpty()) {
            CompetitionEdition lastEdition = cup.getEditions().get(cup.getEditions().size() - 1);
            nextEdition.getParticipantClubsIds().addAll(lastEdition.getParticipantClubsIds());
            nextEdition.setParticipantClubs(lastEdition.getParticipantClubs());
        } else {
            // Get participants from leagues (if first edition)
            List<Long> participantIds = getCupParticipantClubs();
            nextEdition.setParticipantClubsIds(participantIds);
            nextEdition.setParticipantClubs(participantIds.size());
        }
        
        cup.getEditions().add(nextEdition);
        
        // CRITICAL: Generate cup draw for the new staging
        // This ensures the cup can be played immediately
        generateCupDraw(cup, nextEdition);
        Log.info(Log.ENGINE_AUTHORITY, "AuthorityManager", "Generated cup draw for new staging: " + nextEdition.getName());
    }
}