package com.rndmodgames.futtoboru.engine.temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * League Fixture Generator v1.0
 * 
 * Generates home-and-away fixtures for leagues using round-robin algorithm.
 * 
 * Algorithm:
 * - For N clubs, generates N-1 rounds (home fixtures)
 * - Each club plays every other club once per round
 * - Return fixtures are scheduled in second half of season
 * - Match days follow the era calendar (SeasonCalendar, P2-11): Saturdays, September to April
 * 
 * @author Geomancer86
 */
public class LeagueFixtureGenerator {
    
    private SaveGame currentGame;
    private final Futtoboru gameInstance;
    
    public LeagueFixtureGenerator(Futtoboru gameInstance) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
    }
    
    /**
     * Match ids come from the game's allocator (P2-4), read fresh in case the game was replaced
     */
    private long nextMatchId() {
        SaveGame game = gameInstance != null && gameInstance.getCurrentGame() != null
                ? gameInstance.getCurrentGame() : currentGame;
        return game.getIdAllocator().nextId();
    }
    
    /**
     * Generate all league fixtures for a league
     * 
     * @param league The league to generate fixtures for
     * @param seasonStartDate Start date of the season
     * @param seasonEndDate End date of the season
     * @return List of generated matches
     */
    public List<Match> generateLeagueFixtures(League league, LocalDateTime seasonStartDate, LocalDateTime seasonEndDate) {
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator.generateLeagueFixtures() CALLED");
        Log.info(Log.ENGINE_SCHEDULING, "League: " + (league != null ? league.getName() : "NULL"));
        Log.info(Log.ENGINE_SCHEDULING, "Season Start: " + seasonStartDate);
        Log.info(Log.ENGINE_SCHEDULING, "Season End: " + seasonEndDate);
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Starting fixture generation for league: " + (league != null ? league.getName() : "NULL"));
        
        if (league == null) {
            Log.error(Log.ENGINE_SCHEDULING, "league is NULL!");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Cannot generate fixtures: league is null");
            return new ArrayList<>();
        }
        
        if (league.getLeagueClubs() == null || league.getLeagueClubs().isEmpty()) {
            Log.error(Log.ENGINE_SCHEDULING, "league.getLeagueClubs() is " + (league.getLeagueClubs() == null ? "NULL" : "EMPTY"));
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Cannot generate fixtures: league has no clubs");
            return new ArrayList<>();
        }
        
        List<Club> clubs = new ArrayList<>(league.getLeagueClubs());
        int numClubs = clubs.size();
        
        Log.info(Log.ENGINE_SCHEDULING, "League has " + numClubs + " clubs");
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "League has " + numClubs + " clubs");
        
        if (numClubs < 2) {
            Log.error(Log.ENGINE_SCHEDULING, "League has less than 2 clubs (" + numClubs + ")");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Cannot generate fixtures: league has less than 2 clubs");
            return new ArrayList<>();
        }
        
        /**
         * Validate club data completeness before generating fixtures
         * Each club needs:
         * - At least 11 players (minimum squad size)
         * - A stadium with capacity > 0
         * - To be in SaveGame (for match references)
         */
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        Log.info(Log.ENGINE_SCHEDULING, "VALIDATING CLUBS FOR FIXTURE GENERATION");
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        Log.info(Log.ENGINE_SCHEDULING, "Clubs received for validation: " + clubs.size());
        for (Club c : clubs) {
            Log.info(Log.ENGINE_SCHEDULING, "  - " + (c != null ? c.getName() : "NULL") + " (ID: " + (c != null ? c.getId() : "NULL") + ")");
        }
        
        List<Club> validClubs = new ArrayList<>();
        for (Club club : clubs) {
            Log.info(Log.ENGINE_SCHEDULING, "Checking club: " + (club != null ? club.getName() : "NULL") + " (ID: " + (club != null ? club.getId() : "NULL") + ")");
            
            // isClubReady will throw RuntimeException if validation fails (crashes game)
            // This ensures we catch data issues immediately
            try {
                boolean isReady = isClubReady(club);
                if (isReady) {
                    if (!validClubs.contains(club)) {
                        validClubs.add(club);
                        Log.info(Log.ENGINE_SCHEDULING, "  -> ADDED to valid clubs list");
                    } else {
                        Log.info(Log.ENGINE_SCHEDULING, "  -> WARNING: Duplicate club " + club.getName() + " found in league list. Skipping.");
                    }
                }
            } catch (RuntimeException e) {
                // Re-throw to crash the game - don't silently skip clubs
                Log.error(Log.ENGINE_SCHEDULING, "GAME CRASHING: Club validation failed for " + (club != null ? club.getName() : "UNKNOWN") + "!");
                Log.error(Log.ENGINE_SCHEDULING, "This prevents silent failures and ensures all clubs are properly configured.");
                throw e;
            }
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        Log.info(Log.ENGINE_SCHEDULING, "CLUB VALIDATION RESULTS:");
        Log.info(Log.ENGINE_SCHEDULING, "Total clubs in league: " + numClubs);
        Log.info(Log.ENGINE_SCHEDULING, "Valid unique clubs: " + validClubs.size());
        Log.info(Log.ENGINE_SCHEDULING, "Invalid/Duplicate clubs: " + (numClubs - validClubs.size()));
        
        // List final clubs
        Log.info(Log.ENGINE_SCHEDULING, "FINAL CLUBS FOR GENERATION (" + validClubs.size() + "):");
        for (Club c : validClubs) {
            Log.info(Log.ENGINE_SCHEDULING, "  - " + c.getName() + " (ID: " + c.getId() + ")");
        }
        
        // List which clubs are invalid
        if (validClubs.size() < numClubs) {
            Log.info(Log.ENGINE_SCHEDULING, "INVALID CLUBS (will be excluded):");
            for (Club club : clubs) {
                if (!validClubs.contains(club)) {
                    Log.info(Log.ENGINE_SCHEDULING, "  - " + (club != null ? club.getName() : "NULL") + " (ID: " + (club != null ? club.getId() : "NULL") + ")");
                }
            }
        }
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        
        if (validClubs.size() < 2) {
            Log.info(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "Cannot generate fixtures!");
            Log.info(Log.ENGINE_SCHEDULING, "Only " + validClubs.size() + " clubs have complete data (need at least 2)");
            Log.info(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Cannot generate fixtures: only " + validClubs.size() + " clubs have complete data (need at least 2)");
            return new ArrayList<>();
        }
        
        // Check for odd number of clubs (will result in bye each round)
        if (validClubs.size() % 2 != 0) {
            Log.warn(Log.ENGINE_SCHEDULING, "Odd number of valid clubs (" + validClubs.size() + ")!");
            Log.info(Log.ENGINE_SCHEDULING, "This will result in " + (validClubs.size() / 2) + " matches per matchday");
            Log.info(Log.ENGINE_SCHEDULING, "One team will have a bye each matchday (bye rotates each round)");
            Log.warn(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Odd number of clubs (" + validClubs.size() + ") - one team will have bye each matchday");
        }
        
        if (validClubs.size() < numClubs) {
            Log.warn(Log.ENGINE_SCHEDULING, "Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
            Log.warn(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
        }
        
        // Use only valid clubs for fixture generation
        clubs = validClubs;
        numClubs = clubs.size();
        
        Log.info(Log.ENGINE_SCHEDULING, "FINAL: Generating fixtures for " + numClubs + " clubs");
        Log.info(Log.ENGINE_SCHEDULING, "CLUBS LIST:");
        for (Club c : clubs) {
            Log.info(Log.ENGINE_SCHEDULING, "  - " + c.getName() + " (ID: " + c.getId() + ")");
        }
        Log.info(Log.ENGINE_SCHEDULING, "Matches per matchday: " + (numClubs / 2));
        
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Generating fixtures for league: " + league.getName() + " with " + numClubs + " clubs");
        
        List<Match> allMatches = new ArrayList<>();
        
        // Generate first half fixtures with proper home/away rotation
        List<Match> firstHalfFixtures = generateRoundRobinFixtures(clubs);
        
        // Generate second half fixtures by reversing home/away for each match
        // This ensures each club plays every other club once at home and once away
        List<Match> secondHalfFixtures = generateReturnFixtures(firstHalfFixtures);
        
        // Combine and schedule matches
        allMatches.addAll(firstHalfFixtures);
        allMatches.addAll(secondHalfFixtures);
        
        Log.info(Log.ENGINE_SCHEDULING, "TOTAL MATCHES GENERATED: " + allMatches.size());
        if (!allMatches.isEmpty()) {
            Log.info(Log.ENGINE_SCHEDULING, "First Match: " + allMatches.get(0).getId());
            Log.info(Log.ENGINE_SCHEDULING, "Last Match: " + allMatches.get(allMatches.size() - 1).getId());
        }
        
        // Schedule matches across season dates (pass actual number of clubs for correct matchday calculation)
        scheduleMatchesAcrossSeason(allMatches, numClubs, seasonStartDate, seasonEndDate);
        
        // Add matches to clubs' scheduledMatches lists
        Log.info(Log.ENGINE_SCHEDULING, "Adding " + allMatches.size() + " matches to clubs' scheduledMatches lists...");
        int matchesAddedToClubs = 0;
        int matchesSkipped = 0;
        
        for (Match match : allMatches) {
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub == null) {
                Log.error(Log.ENGINE_SCHEDULING, "Home club not found for ID: " + match.getHomeClubId());
                Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Home club not found in SaveGame for ID: " + match.getHomeClubId());
                matchesSkipped++;
                continue;
            }
            if (awayClub == null) {
                Log.error(Log.ENGINE_SCHEDULING, "Away club not found for ID: " + match.getAwayClubId());
                Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Away club not found in SaveGame for ID: " + match.getAwayClubId());
                matchesSkipped++;
                continue;
            }
            
            // Initialize scheduledMatches if null
            if (homeClub.getScheduledMatches() == null) {
                homeClub.setScheduledMatches(new ArrayList<>());
                Log.info(Log.ENGINE_SCHEDULING, "Initialized scheduledMatches for home club: " + homeClub.getName());
                Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Initialized scheduledMatches for home club: " + homeClub.getName());
            }
            if (awayClub.getScheduledMatches() == null) {
                awayClub.setScheduledMatches(new ArrayList<>());
                Log.info(Log.ENGINE_SCHEDULING, "Initialized scheduledMatches for away club: " + awayClub.getName());
                Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Initialized scheduledMatches for away club: " + awayClub.getName());
            }
            
            homeClub.getScheduledMatches().add(match);
            awayClub.getScheduledMatches().add(match);
            matchesAddedToClubs += 2;
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        Log.info(Log.ENGINE_SCHEDULING, "MATCHES ADDED TO CLUBS:");
        Log.info(Log.ENGINE_SCHEDULING, "Matches added: " + matchesAddedToClubs + " references (2 per match)");
        Log.info(Log.ENGINE_SCHEDULING, "Matches skipped: " + matchesSkipped);
        Log.info(Log.ENGINE_SCHEDULING, "Expected: " + (allMatches.size() * 2) + " references");
        
        // Verify each club has matches
        Log.info(Log.ENGINE_SCHEDULING, "VERIFICATION: Checking matches per club:");
        for (Club club : clubs) {
            if (club != null && club.getScheduledMatches() != null) {
                int matchCount = club.getScheduledMatches().size();
                Log.info(Log.ENGINE_SCHEDULING, "  - " + club.getName() + ": " + matchCount + " matches");
            } else {
                Log.info(Log.ENGINE_SCHEDULING, "  - " + (club != null ? club.getName() : "NULL") + ": 0 matches (ERROR!)");
            }
        }
        Log.info(Log.ENGINE_SCHEDULING, "========================================");
        
        // Verify matches were actually added
        int totalMatchesInClubs = 0;
        for (Club club : clubs) {
            if (club != null && club.getScheduledMatches() != null) {
                totalMatchesInClubs += club.getScheduledMatches().size();
            }
        }
        Log.info(Log.ENGINE_SCHEDULING, "VERIFICATION: Total matches in all clubs' scheduledMatches: " + totalMatchesInClubs);
        
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Added " + matchesAddedToClubs + " match references to clubs (2 per match)");
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Generated " + allMatches.size() + " league fixtures");
        
        return allMatches;
    }
    
    /**
     * Generate round-robin fixtures for a list of clubs
     * 
     * Round-robin algorithm: Each club plays every other club once.
     * For N clubs, we need N-1 rounds. In each round, we pair clubs.
     * Home/away alternates each round to ensure balanced distribution.
     * 
     * @param clubs List of clubs
     * @return List of matches (first half of season)
     */
    private List<Match> generateRoundRobinFixtures(List<Club> clubs) {
        Log.info(Log.ENGINE_SCHEDULING, "generateRoundRobinFixtures() called with " + clubs.size() + " clubs");
        
        List<Match> matches = new ArrayList<>();
        int numClubs = clubs.size();
        
        if (numClubs < 2) {
            Log.error(Log.ENGINE_SCHEDULING, "Not enough clubs (" + numClubs + ") to generate fixtures");
            return matches;
        }
        
        // Create a working copy
        List<Club> workingClubs = new ArrayList<>(clubs);
        
        // Note: For odd number of clubs, one club gets a bye each round
        // The algorithm handles this automatically by only pairing available clubs
        
        int numRounds = numClubs - 1;
        int matchesPerRound = numClubs / 2;
        
        Log.info(Log.ENGINE_SCHEDULING, "Round-robin parameters: " + numRounds + " rounds, " + matchesPerRound + " matches per round");
        Log.info(Log.ENGINE_SCHEDULING, "Expected total matches: " + (numRounds * matchesPerRound));
        
        int matchesCreated = 0;
        
        // Generate rounds using round-robin algorithm
        for (int round = 0; round < numRounds; round++) {
            Log.info(Log.ENGINE_SCHEDULING, "Generating round " + (round + 1) + " of " + numRounds);
            
            // Determine home/away pattern for this round
            // Alternate pattern: even rounds have first club home, odd rounds have second club home
            // This ensures balanced distribution
            boolean firstClubIsHome = (round % 2 == 0);
            
            // Pair clubs: first vs last, second vs second-last, etc.
            for (int i = 0; i < matchesPerRound; i++) {
                int club1Index = i;
                int club2Index = numClubs - 1 - i;
                
                // Skip if we'd pair a club with itself (shouldn't happen with even number)
                if (club1Index >= club2Index) {
                    continue;
                }
                
                Club club1 = workingClubs.get(club1Index);
                Club club2 = workingClubs.get(club2Index);
                
                if (club1 == null || club2 == null) {
                    Log.error(Log.ENGINE_SCHEDULING, "Null club at index " + club1Index + " or " + club2Index);
                    continue;
                }
                
                if (club1.getId() == null || club2.getId() == null) {
                    Log.error(Log.ENGINE_SCHEDULING, "Club has null ID - club1: " + (club1 != null ? club1.getName() : "NULL") + ", club2: " + (club2 != null ? club2.getName() : "NULL"));
                    continue;
                }
                
                Match match = new Match();
                match.setId(nextMatchId());
                match.setMatchType(Match.LEAGUE_MATCH);
                match.setIsProposed(false);
                match.setIsAccepted(true); // League matches are automatically accepted
                match.setIsPlayed(false);
                
                // Set home/away with balanced rotation
                // Alternate which club in the pair is home each round
                if (firstClubIsHome) {
                    match.setHomeClubId(club1.getId());
                    match.setAwayClubId(club2.getId());
                } else {
                    match.setHomeClubId(club2.getId());
                    match.setAwayClubId(club1.getId());
                }
                
                matches.add(match);
                matchesCreated++;
                
                if (matchesCreated % 10 == 0) {
                    Log.info(Log.ENGINE_SCHEDULING, "  Created " + matchesCreated + " matches so far...");
                }
            }
            
            // Rotate clubs for next round (round-robin rotation)
            // Keep first club fixed, rotate others clockwise
            // Standard round-robin: [1, 2, 3, 4, ..., N] -> [1, N, 2, 3, ..., N-1]
            if (round < numRounds - 1) {
                // Move last club to position 1 (after first club, which stays fixed)
                Club last = workingClubs.remove(workingClubs.size() - 1);
                workingClubs.add(1, last);
            }
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "Generated " + matchesCreated + " matches in first half");
        return matches;
    }
    
    /**
     * Generate return fixtures by reversing home/away for each match
     * 
     * This ensures each club plays every other club once at home and once away.
     * 
     * @param firstHalfFixtures List of matches from first half
     * @return List of return fixtures with reversed home/away
     */
    private List<Match> generateReturnFixtures(List<Match> firstHalfFixtures) {
        Log.info(Log.ENGINE_SCHEDULING, "generateReturnFixtures() called with " + firstHalfFixtures.size() + " matches");
        
        List<Match> returnFixtures = new ArrayList<>();
        
        for (Match originalMatch : firstHalfFixtures) {
            if (originalMatch == null) {
                continue;
            }
            
            // Create return fixture by swapping home and away
            Match returnMatch = new Match();
            returnMatch.setId(nextMatchId());
            returnMatch.setMatchType(Match.LEAGUE_MATCH);
            returnMatch.setIsProposed(false);
            returnMatch.setIsAccepted(true);
            returnMatch.setIsPlayed(false);
            
            // Reverse home/away
            returnMatch.setHomeClubId(originalMatch.getAwayClubId());
            returnMatch.setAwayClubId(originalMatch.getHomeClubId());
            
            returnFixtures.add(returnMatch);
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "Generated " + returnFixtures.size() + " return fixtures");
        return returnFixtures;
    }
    
    /**
     * Schedule matches on the era calendar (P2-11, {@link SeasonCalendar}): one round of the round robin per
     * league match day, the match days spread over the Saturdays of September to April of the season that
     * {@code seasonStart} belongs to, never before {@code seasonStart} and never on a cup date. The season end
     * passed by the caller is only logged: the calendar decides the last match day.
     *
     * @param matches List of matches to schedule, round by round
     * @param numClubs Number of clubs in the league (used to calculate matches per matchday)
     * @param seasonStart Start of the season (any date of it; matches are not scheduled before it)
     * @param seasonEnd The caller's season end (informative)
     */
    private void scheduleMatchesAcrossSeason(List<Match> matches, int numClubs, LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        if (matches.isEmpty()) {
            return;
        }
        int matchesPerMatchday = Math.max(1, numClubs / 2);
        int numMatchdays = (int) Math.ceil((double) matches.size() / matchesPerMatchday);

        SeasonCalendar calendar = SeasonCalendar.forDate(seasonStart);
        List<LocalDateTime> days = calendar.leagueMatchDays(numMatchdays, seasonStart.toLocalDate());
        Log.info(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Season " + calendar.getStartYear() + "-"
                + (calendar.getStartYear() + 1) % 100 + ": " + numMatchdays + " match days from " + days.get(0)
                + " to " + days.get(days.size() - 1) + " (caller's window " + seasonStart + " to " + seasonEnd + ")");

        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            LocalDateTime matchDate = days.get(i / matchesPerMatchday);
            match.setMatchDateTime(matchDate);
            match.setProposeDateTime(matchDate); // propose date same as match date for league matches
        }
    }
    
    /**
     * Check if a club has all required data for fixture generation
     * 
     * @param club The club to validate
     * @return true if club is ready, false otherwise
     * @throws RuntimeException if club validation fails (crashes game with clear error)
     */
    private boolean isClubReady(Club club) {
        String clubName = club != null ? club.getName() : "NULL";
        Long clubId = club != null ? club.getId() : null;
        String errorPrefix = "CRITICAL ERROR: Club validation failed for: " + clubName + " (ID: " + clubId + ")";
        
        if (club == null) {
            String error = errorPrefix + "\n  REASON: Club is null";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "    Checking players...");
        // Check if club has players
        if (club.getPlayers() == null) {
            String error = errorPrefix + "\n  REASON: club.getPlayers() is NULL\n  FIX: Club needs a players list";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        if (club.getPlayers().isEmpty()) {
            String error = errorPrefix + "\n  REASON: club.getPlayers() is EMPTY (0 players)\n  FIX: Club needs at least 11 players";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        int playerCount = club.getPlayers().size();
        Log.info(Log.ENGINE_SCHEDULING, "    Players: " + playerCount);
        
        if (playerCount < 11) {
            String error = errorPrefix + "\n  REASON: Only " + playerCount + " players (need at least 11)\n  FIX: Add more players to the club";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "    Checking stadium...");
        // Check if club has stadium
        if (club.getStadium() == null) {
            String error = errorPrefix + "\n  REASON: club.getStadium() is NULL\n  FIX: Club needs a stadium";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Integer capacity = club.getStadium().getCapacity();
        Log.info(Log.ENGINE_SCHEDULING, "    Stadium capacity: " + capacity);
        
        if (capacity == null || capacity <= 0) {
            String error = errorPrefix + "\n  REASON: Invalid stadium capacity: " + capacity + " (must be > 0)\n  FIX: Set stadium capacity to a positive number";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "    Checking if club is in SaveGame...");
        // Check if club is in SaveGame (for match references)
        Club saveGameClub = currentGame.getClubById(club.getId());
        if (saveGameClub == null) {
            String error = errorPrefix + "\n  REASON: Club not found in SaveGame (ID: " + club.getId() + ")\n  Total clubs in SaveGame: " + (currentGame.getAllClubs() != null ? currentGame.getAllClubs().size() : 0) + "\n  FIX: Ensure club is properly added to SaveGame";
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, error);
            Log.error(Log.ENGINE_SCHEDULING, "========================================");
            Log.error(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Log.info(Log.ENGINE_SCHEDULING, "    SUCCESS: Club is ready!");
        Log.info(Log.ENGINE_SCHEDULING, "      - Players: " + playerCount);
        Log.info(Log.ENGINE_SCHEDULING, "      - Stadium capacity: " + capacity);
        Log.info(Log.ENGINE_SCHEDULING, "      - In SaveGame: YES");
        Log.debug(Log.ENGINE_SCHEDULING, "LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") is ready - " + playerCount + " players, stadium capacity: " + capacity);
        return true;
    }
    
    /**
     * Check if a league already has fixtures scheduled
     * 
     * @param league The league to check
     * @return true if fixtures exist, false otherwise
     */
    public boolean hasFixturesScheduled(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return false;
        }
        
        // Check if any club in the league has league matches scheduled
        for (Club club : league.getLeagueClubs()) {
            if (club != null && club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && match.getMatchType() != null && 
                        match.getMatchType() == Match.LEAGUE_MATCH) {
                        return true; // Found at least one league match
                    }
                }
            }
        }
        
        return false;
    }
}

