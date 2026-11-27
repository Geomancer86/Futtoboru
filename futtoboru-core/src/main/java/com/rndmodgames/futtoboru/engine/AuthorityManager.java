package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
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
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

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
        Gdx.app.debug("AuthorityManager", "checkCompetitionsSchedule()");
        
        if (currentGame == null || currentGame.getMainAuthority() == null) {
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
            Gdx.app.error("AuthorityManager", "Error in checkLeagueCompletion()", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Check if league season is already marked as completed
     */
    private boolean isLeagueAlreadyCompleted(League league) {
        // Find existing messages for this league's completion
        if (currentGame.getAllMessages() == null) return false;
        
        for (Message m : currentGame.getAllMessages()) {
            if (m != null && "LEAGUE_COMPLETE".equals(m.getMessageType())) {
                // If message contains league name, assume it's this one
                if (m.getTitle() != null && m.getTitle().contains(league.getName())) {
                    return true;
                }
            }
        }
        return false;
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
        Gdx.app.log("AuthorityManager", "Season complete for league: " + league.getName());
        
        List<Club> standings = standingsManager.calculateStandings(league);
        if (standings.isEmpty()) return;
        
        Club champion = standings.get(0);
        Club runnerUp = standings.size() > 1 ? standings.get(1) : null;
        
        // Calculate Top Scorer
        com.rndmodgames.futtoboru.data.Person topScorer = calculateTopScorer(league);
        int topScorerGoals = topScorer != null ? getPlayerSeasonGoals(topScorer.getId(), league) : 0;
        
        // Calculate Best Defense
        Club bestDefense = calculateBestDefense(standings);
        
        Gdx.app.log("AuthorityManager", "CHAMPION: " + champion.getName());
        if (topScorer != null) {
            Gdx.app.log("AuthorityManager", "TOP SCORER: " + topScorer.getName() + " " + topScorer.getLastname() + " (" + topScorerGoals + " goals)");
        }
        
        // Update league edition with champions
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = getCurrentLeagueEdition(league);
        if (edition != null) {
            edition.setChampionsId(champion.getId());
            if (runnerUp != null) {
                edition.setRunnersUpId(runnerUp.getId());
            }
            Gdx.app.log("AuthorityManager", "Updated league edition " + edition.getName() + " with champions: " + champion.getName());
        }
        
        // Give prize money (historical estimation)
        java.math.BigDecimal championPrize = new java.math.BigDecimal("500.00");
        java.math.BigDecimal runnerUpPrize = new java.math.BigDecimal("250.00");
        
        if (champion.getClubBalance() != null) {
            champion.setClubBalance(champion.getClubBalance().add(championPrize));
        }
        
        if (runnerUp != null && runnerUp.getClubBalance() != null) {
            runnerUp.setClubBalance(runnerUp.getClubBalance().add(runnerUpPrize));
        }
        
        // Create completion and awards messages
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        
        // 1. Standings Summary
        Message completionMessage = createLeagueCompletionMessage(league, standings);
        if (completionMessage != null) {
            messageManager.deliverMessage(completionMessage);
        }
        
        // 2. Champion Announcement
        Message championMessage = createChampionMessage(league, champion);
        if (championMessage != null) {
            messageManager.deliverMessage(championMessage);
        }
        
        // 3. Top Scorer Announcement
        if (topScorer != null) {
            Message scorerMessage = createTopScorerMessage(league, topScorer, topScorerGoals);
            if (scorerMessage != null) {
                messageManager.deliverMessage(scorerMessage);
            }
        }
        
        // 4. Best Defense Announcement
        if (bestDefense != null) {
            Message defenseMessage = createBestDefenseMessage(league, bestDefense);
            if (defenseMessage != null) {
                messageManager.deliverMessage(defenseMessage);
            }
        }
    }
    
    /**
     * Calculate top scorer for the league season
     */
    private com.rndmodgames.futtoboru.data.Person calculateTopScorer(League league) {
        java.util.Map<Long, Integer> playerGoals = new java.util.HashMap<>();
        
        // Iterate all clubs in league
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) continue;
            
            // Iterate played matches
            for (Match match : club.getPlayedMatches()) {
                if (match == null || match.getMatchType() == null || match.getMatchType() != Match.LEAGUE_MATCH) continue;
                
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
        
        // Find person by ID
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
    private int getPlayerSeasonGoals(Long playerId, League league) {
        int goals = 0;
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) continue;
            for (Match match : club.getPlayedMatches()) {
                if (match == null || match.getMatchType() == null || match.getMatchType() != Match.LEAGUE_MATCH) continue;
                
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
    private Message createTopScorerMessage(League league, Person scorer, int goals) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_TOP_SCORER");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Golden Boot: " + scorer.getName() + " " + scorer.getLastname());
        
        StringBuilder content = new StringBuilder();
        content.append(scorer.getName()).append(" ").append(scorer.getLastname())
               .append(" has finished as the top goalscorer in the ").append(league.getName())
               .append(" with ").append(goals).append(" goals!\n\n");
        
        Club club = currentGame.getClubById(scorer.getCurrentClubId());
        if (club != null) {
            content.append("His goals were vital for ").append(club.getName()).append(" this season.");
        }
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create best defense announcement message
     */
    private Message createBestDefenseMessage(League league, Club club) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_BEST_DEFENSE");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Solid Rock: " + club.getName() + "'s Defense");
        
        StringBuilder content = new StringBuilder();
        content.append(club.getName()).append(" has finished the season with the best defensive record in the ")
               .append(league.getName()).append(".\n\n");
        content.append("They conceded only ").append(club.getGoalsConceded()).append(" goals in ")
               .append(club.getMatchesPlayed()).append(" matches.");
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create league completion summary message
     */
    private Message createLeagueCompletionMessage(League league, List<Club> standings) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_COMPLETE");
        message.setPriority(MessagePriority.URGENT);
        message.setTitle(league.getName() + " - Season Complete");
        
        StringBuilder content = new StringBuilder();
        content.append("The season for the ").append(league.getName()).append(" has come to an end.\n\n");
        content.append("FINAL STANDINGS:\n");
        
        for (int i = 0; i < Math.min(5, standings.size()); i++) {
            Club c = standings.get(i);
            content.append(i + 1).append(". ").append(c.getName())
                   .append(" (").append(c.getPoints()).append(" pts)\n");
        }
        
        if (standings.size() > 5) {
            content.append("... and ").append(standings.size() - 5).append(" more clubs.\n");
        }
        
        content.append("\nCongratulations to the Champions: ").append(standings.get(0).getName()).append("!");
        
        message.setPlainTextMessage(content.toString());
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create specific champion announcement message
     */
    private Message createChampionMessage(League league, Club champion) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_CHAMPION");
        message.setPriority(MessagePriority.URGENT);
        message.setTitle("CHAMPIONS! " + champion.getName() + " win the " + league.getName());
        
        StringBuilder content = new StringBuilder();
        content.append(champion.getName()).append(" have been declared champions of the ").append(league.getName()).append("!\n\n");
        content.append("After a grueling season, they have emerged at the top of the table.\n\n");
        content.append("The club has been awarded a prize of $500.00 for their achievement.");
        
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
            Gdx.app.log("AuthorityManager", "checkAndScheduleLeagueFixtures() called");
            
            if (mainAuthority == null) {
                Gdx.app.debug("AuthorityManager", "mainAuthority is null - no leagues to check");
                return;
            }
            
            if (mainAuthority.getLeagues() == null) {
                Gdx.app.debug("AuthorityManager", "mainAuthority.getLeagues() is null - no leagues to check");
                return;
            }
            
            List<League> leagues = mainAuthority.getLeagues();
            
            Gdx.app.log("AuthorityManager", "Found " + leagues.size() + " leagues in main authority");
            
            if (leagues.isEmpty()) {
                Gdx.app.debug("AuthorityManager", "No leagues found in main authority");
                return;
            }
            
            // Get season start date (use game start date or season start date)
            LocalDateTime seasonStart = getSeasonStartDate();
            LocalDateTime seasonEnd = getSeasonEndDate(seasonStart);
            
            Gdx.app.log("AuthorityManager", "Season dates: " + seasonStart + " to " + seasonEnd);
            
            for (League league : leagues) {
                if (league == null) {
                    Gdx.app.error("AuthorityManager", "Found null league in list");
                    continue;
                }
                
                Gdx.app.log("AuthorityManager", "Checking league: " + league.getName());
                
                if (league.getLeagueClubs() == null) {
                    Gdx.app.error("AuthorityManager", "League " + league.getName() + " has null leagueClubs list");
                    continue;
                }
                
                if (league.getLeagueClubs().isEmpty()) {
                    Gdx.app.error("AuthorityManager", "League " + league.getName() + " has no clubs");
                    continue;
                }
                
                Gdx.app.log("AuthorityManager", "League " + league.getName() + " has " + league.getLeagueClubs().size() + " clubs");
                
                // Check if fixtures are already scheduled
                boolean hasFixtures = fixtureGenerator.hasFixturesScheduled(league);
                Gdx.app.log("AuthorityManager", "League " + league.getName() + " has fixtures scheduled: " + hasFixtures);
                
                if (!hasFixtures) {
                    Gdx.app.log("AuthorityManager", "Generating fixtures for league: " + league.getName());
                    
                    // Generate fixtures
                    List<com.rndmodgames.futtoboru.data.Match> fixtures = 
                        fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
                    
                    Gdx.app.log("AuthorityManager", "Generated " + fixtures.size() + " fixtures for " + league.getName());
                } else {
                    Gdx.app.debug("AuthorityManager", "League " + league.getName() + " already has fixtures scheduled");
                }
            }
        } catch (Exception e) {
            Gdx.app.error("AuthorityManager", "Error in checkAndScheduleLeagueFixtures()", e);
            e.printStackTrace();
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
        // Typical season: September to May (9 months)
        return seasonStart.plusMonths(9);
    }
    
    /**
     * Check all cups and generate draws if needed (v1.0 - Cup Draw Integration)
     */
    private void checkAndScheduleCupDraws() {
        try {
            Gdx.app.log("AuthorityManager", "checkAndScheduleCupDraws() called");
            
            if (currentGame == null) {
                Gdx.app.debug("AuthorityManager", "currentGame is null - no cups to check");
                return;
            }
            
            List<Competition> cups = currentGame.getAllCups();
            if (cups == null || cups.isEmpty()) {
                Gdx.app.debug("AuthorityManager", "No cups found in current game");
                return;
            }
            
            Gdx.app.log("AuthorityManager", "Found " + cups.size() + " cups to check");
            LocalDateTime currentDate = currentGame.getGameDate();
            
            for (Competition cup : cups) {
                if (cup == null) {
                    Gdx.app.error("AuthorityManager", "Found null cup in list");
                    continue;
                }
                
                // Only process CUP type competitions
                if (!Competition.COMPETITION_CUP.equals(cup.getCompetitionType())) {
                    continue;
                }
                
                Gdx.app.log("AuthorityManager", "Checking cup: " + cup.getName());
                
                // Get or create current edition
                CompetitionEdition edition = getCurrentCupEdition(cup, currentDate);
                if (edition == null) {
                    // Check if there's an edition from script that hasn't started yet
                    edition = findUpcomingEdition(cup, currentDate);
                    
                    if (edition == null) {
                        edition = createNewCupEdition(cup, currentDate);
                    } else {
                        // Activate the upcoming edition if it's time
                        // FA Cup creation script runs on May 1st
                        if (currentDate.getMonthValue() >= 5) {
                            edition.setStartDate(currentDate);
                            edition.setEndDate(currentDate.plusMonths(12)); // Allow a full year
                            Gdx.app.log("AuthorityManager", "Activated upcoming cup edition: " + edition.getName());
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
                    Gdx.app.log("AuthorityManager", "Generating cup draw for: " + cup.getName());
                    generateCupDraw(cup, edition);
                } else {
                    Gdx.app.debug("AuthorityManager", "Cup " + cup.getName() + " already has matches scheduled");
                }
            }
        } catch (Exception e) {
            Gdx.app.error("AuthorityManager", "Error in checkAndScheduleCupDraws()", e);
            e.printStackTrace();
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
        edition.setId(System.currentTimeMillis()); // TODO: Better ID generation
        edition.setName(cup.getName() + " " + currentDate.getYear());
        edition.setStartDate(currentDate);
        edition.setEndDate(currentDate.plusMonths(6)); // Cup typically runs 6 months
        
        // Get participant clubs (all clubs from leagues)
        List<Long> participantIds = getCupParticipantClubs();
        edition.setParticipantClubsIds(participantIds);
        edition.setParticipantClubs(participantIds.size());
        
        if (cup.getEditions() == null) {
            cup.setEditions(new ArrayList<>());
        }
        cup.getEditions().add(edition);
        
        Gdx.app.log("AuthorityManager", "Created new cup edition: " + edition.getName() + 
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
     * Generate cup draw and schedule matches
     */
    private void generateCupDraw(Competition cup, CompetitionEdition edition) {
        if (edition.getParticipantClubsIds() == null || edition.getParticipantClubsIds().isEmpty()) {
            Gdx.app.error("AuthorityManager", "Cannot generate cup draw: no participants");
            return;
        }
        
        // Generate COMPLETE bracket (all rounds) using CupBracketGenerator
        LocalDateTime currentDate = currentGame.getGameDate();
        LocalDateTime firstRoundDate = currentDate.plusWeeks(2);
        int weeksBetweenRounds = 2;
        
        com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator bracketGenerator = 
            new com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator();
        
        List<Match> allMatches = bracketGenerator.generateCompleteBracket(
            edition.getParticipantClubsIds(),
            cup.getId(),
            edition.getId(),
            firstRoundDate,
            weeksBetweenRounds
        );
        
        if (allMatches == null || allMatches.isEmpty()) {
            Gdx.app.error("AuthorityManager", "Cup bracket generation produced no matches");
            System.out.println("AuthorityManager: ERROR - Bracket generation returned null or empty!");
            return;
        }
        
        System.out.println("AuthorityManager: *** BRACKET GENERATION COMPLETE ***");
        System.out.println("AuthorityManager: Total matches generated: " + allMatches.size());
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match m : allMatches) {
            if (m != null && m.getRound() != null) {
                matchesByRound.put(m.getRound(), matchesByRound.getOrDefault(m.getRound(), 0) + 1);
            }
        }
        System.out.println("AuthorityManager: Matches by round BEFORE adding to clubs: " + matchesByRound);
        
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        
        // Process all matches and add to clubs
        // Strategy: Add ALL matches to ALL participating clubs so they're accessible
        // Round 1 matches: Add to specific clubs (home/away)
        // Future rounds: Add to all participating clubs (will be filtered when teams determined)
        
        int round1Count = 0;
        int round2PlusCount = 0;
        int nullRoundCount = 0;
        
        System.out.println("AuthorityManager: Processing " + allMatches.size() + " matches from bracket generator");
        
        for (Match match : allMatches) {
            if (match == null) {
                System.out.println("AuthorityManager: WARNING - Null match in allMatches list!");
                continue;
            }
            
            // Debug: Log match details
            if (match.getRound() == null) {
                nullRoundCount++;
                System.out.println("AuthorityManager: WARNING - Match has null round! ID: " + match.getId() + 
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
                            cup, homeClub, awayClub, match.getMatchDateTime(), "First Round"
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
                System.out.println("AuthorityManager: *** ADDING FUTURE ROUND MATCH *** " + match.getBracketPath() + 
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
                System.out.println("AuthorityManager: Added match " + match.getBracketPath() + 
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
                                System.out.println("AuthorityManager: VERIFIED - Match " + match.getBracketPath() + 
                                    " is in club " + testClub.getName() + "'s scheduled matches");
                                break;
                            }
                        }
                        if (!found) {
                            System.out.println("AuthorityManager: ERROR - Match " + match.getBracketPath() + 
                                " was NOT found in club " + testClub.getName() + " after adding!");
                        }
                    }
                }
            } else {
                System.out.println("AuthorityManager: WARNING - Match with null or invalid round: " + 
                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                    ", Round: " + match.getRound());
            }
        }
        
        System.out.println("AuthorityManager: Processed " + round1Count + " Round 1 matches, " + 
            round2PlusCount + " Round 2+ matches, " + nullRoundCount + " matches with null round");
        
        if (round2PlusCount == 0 && allMatches.size() > round1Count) {
            System.out.println("AuthorityManager: CRITICAL ERROR - Expected Round 2+ matches but found none!");
            System.out.println("AuthorityManager: Total matches: " + allMatches.size() + ", Round 1: " + round1Count);
            // Debug: List all matches to see what's wrong
            for (Match m : allMatches) {
                if (m != null) {
                    System.out.println("  - Match: " + m.getBracketPath() + ", Round: " + m.getRound() + 
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
        System.out.println("AuthorityManager: FINAL VERIFICATION - Matches by round in clubs: " + finalMatchesByRoundInClubs);
        
        // Create MANDATORY draw message for the user (to reveal the draw)
        Message mandatoryDrawMessage = messageManager.createCupDrawMessage(cup, edition, 1, "First Round");
        if (mandatoryDrawMessage != null) {
            mandatoryDrawMessage.setScheduledDate(currentDate.plusDays(1));
            messageManager.scheduleMessage(mandatoryDrawMessage);
            Gdx.app.log("AuthorityManager", "Scheduled mandatory cup draw message for: " + cup.getName());
        }
        
        // Log summary (already logged above with matchesByRound)
        Gdx.app.log("AuthorityManager", "Generated COMPLETE cup bracket: " + allMatches.size() + " total matches");
        // Use the matchesByRound already calculated above
        for (Map.Entry<Integer, Integer> entry : matchesByRound.entrySet()) {
            Gdx.app.log("AuthorityManager", "  Round " + entry.getKey() + ": " + entry.getValue() + " matches");
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
                
                // Check if current round is complete
                if (isCupRoundComplete(edition)) {
                    // Get winners from current round
                    List<Club> winners = getRoundWinners(edition);
                    
                    if (winners.isEmpty()) {
                        Gdx.app.error("AuthorityManager", "Round complete but no winners found for: " + cup.getName());
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
            Gdx.app.error("AuthorityManager", "Error in checkCupRoundProgression()", e);
            e.printStackTrace();
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
            Gdx.app.error("AuthorityManager", "Cannot get matches for edition: SaveGame is null");
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
            Gdx.app.error("AuthorityManager", "Cannot advance round: no winners");
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
            Gdx.app.error("AuthorityManager", "Cannot advance round: no valid winner IDs");
            return;
        }
        
        // Generate next round draw
        List<Match> nextRoundMatches = competitionScheduler.competitionDraw(cup, winnerIds);
        
        if (nextRoundMatches == null || nextRoundMatches.isEmpty()) {
            Gdx.app.error("AuthorityManager", "Next round draw generated no matches");
            return;
        }
        
        // Schedule next round matches (2 weeks from now)
        LocalDateTime currentDate = currentGame.getGameDate();
        LocalDateTime nextRoundDate = currentDate.plusWeeks(2);
        
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
        
        Gdx.app.log("AuthorityManager", "Advanced cup to " + roundName + ": " + 
            nextRoundMatches.size() + " matches for " + cup.getName());
    }
    
    /**
     * Get round name (e.g., "First Round", "Second Round", "Quarter-Final", etc.)
     */
    private String getRoundName(Integer roundNumber, int participants) {
        if (roundNumber == null) {
            return "Round " + roundNumber;
        }
        
        // Calculate round name based on number of participants
        if (participants == 2) {
            return "Final";
        } else if (participants == 4) {
            return "Semi-Final";
        } else if (participants == 8) {
            return "Quarter-Final";
        } else {
            // Use ordinal for other rounds
            String[] ordinals = {"", "First", "Second", "Third", "Fourth", "Fifth", "Sixth"};
            if (roundNumber > 0 && roundNumber < ordinals.length) {
                return ordinals[roundNumber] + " Round";
            }
            return "Round " + roundNumber;
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
            Gdx.app.error("AuthorityManager", "Cannot complete cup: winner is null");
            return;
        }
        
        // Use provided SaveGame or fall back to currentGame
        SaveGame gameToUse = saveGame != null ? saveGame : currentGame;
        if (gameToUse == null) {
            Gdx.app.error("AuthorityManager", "Cannot complete cup: SaveGame is null");
            return;
        }
        
        // Store winner
        edition.setChampionsId(winner.getId());
        
        // Find runner-up (loser of final)
        List<Match> finalMatches = getMatchesForEdition(edition, gameToUse);
        Integer finalRound = getCurrentRound(finalMatches);
        if (finalRound != null) {
            for (Match match : finalMatches) {
                if (match.getRound() != null && match.getRound().equals(finalRound) &&
                    match.getIsPlayed() != null && match.getIsPlayed()) {
                    Club runnerUp = getMatchLoser(match, winner, gameToUse);
                    if (runnerUp != null) {
                        edition.setRunnersUpId(runnerUp.getId());
                        Gdx.app.log("AuthorityManager", "Cup runner-up saved: " + runnerUp.getName() + " (ID: " + runnerUp.getId() + ")");
                        break;
                    }
                }
            }
        }
        
        // Log completion details
        Gdx.app.log("AuthorityManager", "Cup completion saved - Champion ID: " + edition.getChampionsId() + 
            ", Runner-up ID: " + edition.getRunnersUpId());
        
        // Create cup completion message
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        Message completionMessage = createCupCompletionMessage(cup, winner);
        if (completionMessage != null) {
            messageManager.deliverMessage(completionMessage);
        }
        
        Gdx.app.log("AuthorityManager", "Cup complete: " + cup.getName() + " won by " + winner.getName());
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
    private Message createCupCompletionMessage(Competition cup, Club winner) {
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_COMPLETE");
        message.setPriority(MessagePriority.URGENT);
        message.setTitle(cup.getName() + " Complete");
        
        StringBuilder content = new StringBuilder();
        content.append("The ").append(cup.getName()).append(" has concluded.\n\n");
        content.append("Winners: ").append(winner.getName()).append("\n\n");
        content.append("Congratulations to ").append(winner.getName()).append(" on winning the ").append(cup.getName()).append("!");
        
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
            Gdx.app.error("AuthorityManager", "Error in checkCupReplays()", e);
            e.printStackTrace();
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
        replay.setMatchDateTime(originalMatch.getMatchDateTime().plusDays(7)); // 7 days later
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
        
        Gdx.app.log("AuthorityManager", "Scheduled cup replay: " + 
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
        message.setTitle(cup.getName() + " Replay");
        
        StringBuilder content = new StringBuilder();
        content.append("The ").append(cup.getName()).append(" match between ");
        content.append(homeClub.getName()).append(" and ").append(awayClub.getName());
        content.append(" ended in a draw.\n\n");
        content.append("A replay has been scheduled for ");
        content.append(formatDate(replayDate));
        content.append(".\n\n");
        content.append("The replay will be played at ").append(homeClub.getName()).append("'s ground.");
        
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
            return "TBA";
        }
        return date.getDayOfMonth() + " " + date.getMonth().toString() + " " + date.getYear();
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
        
        if (needsNextSeasonGeneration(league, currentDate)) {
            Gdx.app.log("AuthorityManager", "Generating next season for " + league.getName() + " after completion");
            generateNextSeason(league, currentDate);
        }
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
        Gdx.app.log("AuthorityManager", "Generating next season for league: " + league.getName());
        
        CompetitionEdition nextEdition = new CompetitionEdition();
        nextEdition.setId(System.currentTimeMillis());
        int startYear = currentDate.getYear();
        nextEdition.setName(league.getName() + " " + startYear + "-" + ((startYear + 1) % 100));
        
        // Start around September 1st
        nextEdition.setStartDate(LocalDateTime.of(startYear, 9, 1, 0, 0));
        nextEdition.setEndDate(nextEdition.getStartDate().plusMonths(9));
        
        // Copy participants from previous edition if applicable
        if (!league.getEditions().isEmpty()) {
            CompetitionEdition lastEdition = league.getEditions().get(league.getEditions().size() - 1);
            nextEdition.getParticipantClubsIds().addAll(lastEdition.getParticipantClubsIds());
            nextEdition.setParticipantClubs(lastEdition.getParticipantClubs());
        }
        
        league.getEditions().add(nextEdition);
        
        // CRITICAL: Generate fixtures for the new season
        // This ensures the league can be played immediately
        List<com.rndmodgames.futtoboru.data.Match> fixtures = 
            fixtureGenerator.generateLeagueFixtures(league, nextEdition.getStartDate(), nextEdition.getEndDate());
        Gdx.app.log("AuthorityManager", "Generated " + fixtures.size() + " fixtures for new season: " + nextEdition.getName());
        
        // Reset league stats for all participating clubs
        for (Long clubId : nextEdition.getParticipantClubsIds()) {
            Club club = currentGame.getClubById(clubId);
            if (club != null) {
                // Clear any season-specific stats if we implement them later
            }
        }
        
        // Inform user via Inbox
        MessageManager messageManager = game.getGameEngine().getMessageManager();
        Message msg = new Message();
        msg.setCategory(MessageCategory.SYSTEM);
        msg.setPriority(MessagePriority.NORMAL);
        msg.setTitle("New Season: " + nextEdition.getName());
        msg.setPlainTextMessage("The fixture list for the upcoming " + nextEdition.getName() + " season is being prepared.");
        msg.setIsRead(false);
        msg.setIsDeleted(false);
        messageManager.deliverMessage(msg);
    }

    private boolean needsNextCupStaging(Competition cup, LocalDateTime currentDate) {
        // Similar logic for cups
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
        Gdx.app.log("AuthorityManager", "Generating next season staging for cup: " + cup.getName());
        
        CompetitionEdition nextEdition = new CompetitionEdition();
        nextEdition.setId(System.currentTimeMillis());
        int startYear = currentDate.getYear();
        nextEdition.setName(cup.getName() + " " + startYear + "-" + ((startYear + 1) % 100));
        
        // Set dates for cup staging
        nextEdition.setStartDate(currentDate);
        nextEdition.setEndDate(currentDate.plusMonths(6)); // Cup typically runs 6 months
        
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
        Gdx.app.log("AuthorityManager", "Generated cup draw for new staging: " + nextEdition.getName());
    }
}