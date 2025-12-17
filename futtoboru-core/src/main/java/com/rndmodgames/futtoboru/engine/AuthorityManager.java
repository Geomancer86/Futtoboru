package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
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
                    edition = createNewCupEdition(cup, currentDate);
                    if (edition == null) {
                        Gdx.app.error("AuthorityManager", "Failed to create cup edition for: " + cup.getName());
                        continue;
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
        
        // Generate draw using CompetitionScheduler
        List<Match> drawMatches = competitionScheduler.competitionDraw(cup, edition.getParticipantClubsIds());
        
        if (drawMatches == null || drawMatches.isEmpty()) {
            Gdx.app.error("AuthorityManager", "Cup draw generated no matches");
            return;
        }
        
        // Schedule matches with dates (first round in 2 weeks)
        LocalDateTime currentDate = currentGame.getGameDate();
        LocalDateTime matchDate = currentDate.plusWeeks(2);
        
        int roundNumber = 1; // First round
        MessageManager messageManager = game.getMessageManager();
        
        for (Match match : drawMatches) {
            // Set match properties
            match.setMatchType(Match.CUP_MATCH);
            match.setCompetitionId(cup.getId());
            match.setCompetitionEditionId(edition.getId());
            match.setRound(roundNumber);
            match.setMatchDateTime(matchDate);
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
            
            // Create cup draw message for participating clubs
            if (homeClub != null && awayClub != null) {
                Message drawMessage = messageManager.createCupDrawResultMessage(
                    cup, homeClub, awayClub, matchDate, "First Round"
                );
                if (drawMessage != null) {
                    currentGame.addMessage(drawMessage);
                }
            }
        }
        
        Gdx.app.log("AuthorityManager", "Generated cup draw: " + drawMatches.size() + 
            " matches for " + cup.getName() + " (Round " + roundNumber + ")");
    }
    
    /**
     * Check if cup rounds need to be advanced (v1.0 - Cup Round Progression)
     */
    private void checkCupRoundProgression() {
        // TODO: Implement cup round progression
        // This will check if a round is complete and advance to next round
    }
}