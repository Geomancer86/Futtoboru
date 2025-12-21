package com.rndmodgames.futtoboru.engine.temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * League Fixture Generator v1.0
 * 
 * Generates home-and-away fixtures for leagues using round-robin algorithm.
 * 
 * Algorithm:
 * - For N clubs, generates N-1 rounds (home fixtures)
 * - Each club plays every other club once per round
 * - Return fixtures are scheduled in second half of season
 * - Matches are distributed evenly across season dates
 * 
 * @author Geomancer86
 */
public class LeagueFixtureGenerator {
    
    private static final int DAYS_BETWEEN_MATCHDAYS = 7; // One match per week
    
    private SaveGame currentGame;
    private long nextMatchId;
    
    public LeagueFixtureGenerator(Futtoboru gameInstance) {
        this.currentGame = gameInstance.getCurrentGame();
        // Initialize match ID counter (use negative range to avoid conflicts)
        this.nextMatchId = -1000000L;
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
        System.out.println("========================================");
        System.out.println("LeagueFixtureGenerator.generateLeagueFixtures() CALLED");
        System.out.println("League: " + (league != null ? league.getName() : "NULL"));
        System.out.println("Season Start: " + seasonStartDate);
        System.out.println("Season End: " + seasonEndDate);
        System.out.println("========================================");
        
        Gdx.app.log("LeagueFixtureGenerator", "Starting fixture generation for league: " + (league != null ? league.getName() : "NULL"));
        
        if (league == null) {
            System.out.println("ERROR: league is NULL!");
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: league is null");
            return new ArrayList<>();
        }
        
        if (league.getLeagueClubs() == null || league.getLeagueClubs().isEmpty()) {
            System.out.println("ERROR: league.getLeagueClubs() is " + (league.getLeagueClubs() == null ? "NULL" : "EMPTY"));
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: league has no clubs");
            return new ArrayList<>();
        }
        
        List<Club> clubs = new ArrayList<>(league.getLeagueClubs());
        int numClubs = clubs.size();
        
        System.out.println("League has " + numClubs + " clubs");
        Gdx.app.log("LeagueFixtureGenerator", "League has " + numClubs + " clubs");
        
        if (numClubs < 2) {
            System.out.println("ERROR: League has less than 2 clubs (" + numClubs + ")");
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: league has less than 2 clubs");
            return new ArrayList<>();
        }
        
        /**
         * Validate club data completeness before generating fixtures
         * Each club needs:
         * - At least 11 players (minimum squad size)
         * - A stadium with capacity > 0
         * - To be in SaveGame (for match references)
         */
        System.out.println("========================================");
        System.out.println("VALIDATING CLUBS FOR FIXTURE GENERATION");
        System.out.println("========================================");
        System.out.println("Clubs received for validation: " + clubs.size());
        for (Club c : clubs) {
            System.out.println("  - " + (c != null ? c.getName() : "NULL") + " (ID: " + (c != null ? c.getId() : "NULL") + ")");
        }
        
        List<Club> validClubs = new ArrayList<>();
        for (Club club : clubs) {
            System.out.println("Checking club: " + (club != null ? club.getName() : "NULL") + " (ID: " + (club != null ? club.getId() : "NULL") + ")");
            
            // isClubReady will throw RuntimeException if validation fails (crashes game)
            // This ensures we catch data issues immediately
            try {
                boolean isReady = isClubReady(club);
                if (isReady) {
                    if (!validClubs.contains(club)) {
                        validClubs.add(club);
                        System.out.println("  -> ADDED to valid clubs list");
                    } else {
                        System.out.println("  -> WARNING: Duplicate club " + club.getName() + " found in league list. Skipping.");
                    }
                }
            } catch (RuntimeException e) {
                // Re-throw to crash the game - don't silently skip clubs
                System.err.println("GAME CRASHING: Club validation failed for " + (club != null ? club.getName() : "UNKNOWN") + "!");
                System.err.println("This prevents silent failures and ensures all clubs are properly configured.");
                throw e;
            }
        }
        
        System.out.println("========================================");
        System.out.println("CLUB VALIDATION RESULTS:");
        System.out.println("Total clubs in league: " + numClubs);
        System.out.println("Valid unique clubs: " + validClubs.size());
        System.out.println("Invalid/Duplicate clubs: " + (numClubs - validClubs.size()));
        
        // List final clubs
        System.out.println("FINAL CLUBS FOR GENERATION (" + validClubs.size() + "):");
        for (Club c : validClubs) {
            System.out.println("  - " + c.getName() + " (ID: " + c.getId() + ")");
        }
        
        // List which clubs are invalid
        if (validClubs.size() < numClubs) {
            System.out.println("INVALID CLUBS (will be excluded):");
            for (Club club : clubs) {
                if (!validClubs.contains(club)) {
                    System.out.println("  - " + (club != null ? club.getName() : "NULL") + " (ID: " + (club != null ? club.getId() : "NULL") + ")");
                }
            }
        }
        System.out.println("========================================");
        
        if (validClubs.size() < 2) {
            System.out.println("========================================");
            System.out.println("ERROR: Cannot generate fixtures!");
            System.out.println("Only " + validClubs.size() + " clubs have complete data (need at least 2)");
            System.out.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: only " + validClubs.size() + " clubs have complete data (need at least 2)");
            return new ArrayList<>();
        }
        
        // Check for odd number of clubs (will result in bye each round)
        if (validClubs.size() % 2 != 0) {
            System.out.println("WARNING: Odd number of valid clubs (" + validClubs.size() + ")!");
            System.out.println("This will result in " + (validClubs.size() / 2) + " matches per matchday");
            System.out.println("One team will have a bye each matchday (bye rotates each round)");
            Gdx.app.log("LeagueFixtureGenerator", "WARNING: Odd number of clubs (" + validClubs.size() + ") - one team will have bye each matchday");
        }
        
        if (validClubs.size() < numClubs) {
            System.out.println("WARNING: Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
            Gdx.app.log("LeagueFixtureGenerator", "WARNING: Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
        }
        
        // Use only valid clubs for fixture generation
        clubs = validClubs;
        numClubs = clubs.size();
        
        System.out.println("FINAL: Generating fixtures for " + numClubs + " clubs");
        System.out.println("CLUBS LIST:");
        for (Club c : clubs) {
            System.out.println("  - " + c.getName() + " (ID: " + c.getId() + ")");
        }
        System.out.println("Matches per matchday: " + (numClubs / 2));
        
        Gdx.app.log("LeagueFixtureGenerator", "Generating fixtures for league: " + league.getName() + " with " + numClubs + " clubs");
        
        List<Match> allMatches = new ArrayList<>();
        
        // Generate first half fixtures with proper home/away rotation
        List<Match> firstHalfFixtures = generateRoundRobinFixtures(clubs);
        
        // Generate second half fixtures by reversing home/away for each match
        // This ensures each club plays every other club once at home and once away
        List<Match> secondHalfFixtures = generateReturnFixtures(firstHalfFixtures);
        
        // Combine and schedule matches
        allMatches.addAll(firstHalfFixtures);
        allMatches.addAll(secondHalfFixtures);
        
        System.out.println("TOTAL MATCHES GENERATED: " + allMatches.size());
        if (!allMatches.isEmpty()) {
            System.out.println("First Match: " + allMatches.get(0).getId());
            System.out.println("Last Match: " + allMatches.get(allMatches.size() - 1).getId());
        }
        
        // Schedule matches across season dates (pass actual number of clubs for correct matchday calculation)
        scheduleMatchesAcrossSeason(allMatches, numClubs, seasonStartDate, seasonEndDate);
        
        // Add matches to clubs' scheduledMatches lists
        System.out.println("Adding " + allMatches.size() + " matches to clubs' scheduledMatches lists...");
        int matchesAddedToClubs = 0;
        int matchesSkipped = 0;
        
        for (Match match : allMatches) {
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub == null) {
                System.out.println("ERROR: Home club not found for ID: " + match.getHomeClubId());
                Gdx.app.error("LeagueFixtureGenerator", "Home club not found in SaveGame for ID: " + match.getHomeClubId());
                matchesSkipped++;
                continue;
            }
            if (awayClub == null) {
                System.out.println("ERROR: Away club not found for ID: " + match.getAwayClubId());
                Gdx.app.error("LeagueFixtureGenerator", "Away club not found in SaveGame for ID: " + match.getAwayClubId());
                matchesSkipped++;
                continue;
            }
            
            // Initialize scheduledMatches if null
            if (homeClub.getScheduledMatches() == null) {
                homeClub.setScheduledMatches(new ArrayList<>());
                System.out.println("Initialized scheduledMatches for home club: " + homeClub.getName());
                Gdx.app.log("LeagueFixtureGenerator", "Initialized scheduledMatches for home club: " + homeClub.getName());
            }
            if (awayClub.getScheduledMatches() == null) {
                awayClub.setScheduledMatches(new ArrayList<>());
                System.out.println("Initialized scheduledMatches for away club: " + awayClub.getName());
                Gdx.app.log("LeagueFixtureGenerator", "Initialized scheduledMatches for away club: " + awayClub.getName());
            }
            
            homeClub.getScheduledMatches().add(match);
            awayClub.getScheduledMatches().add(match);
            matchesAddedToClubs += 2;
        }
        
        System.out.println("========================================");
        System.out.println("MATCHES ADDED TO CLUBS:");
        System.out.println("Matches added: " + matchesAddedToClubs + " references (2 per match)");
        System.out.println("Matches skipped: " + matchesSkipped);
        System.out.println("Expected: " + (allMatches.size() * 2) + " references");
        
        // Verify each club has matches
        System.out.println("VERIFICATION: Checking matches per club:");
        for (Club club : clubs) {
            if (club != null && club.getScheduledMatches() != null) {
                int matchCount = club.getScheduledMatches().size();
                System.out.println("  - " + club.getName() + ": " + matchCount + " matches");
            } else {
                System.out.println("  - " + (club != null ? club.getName() : "NULL") + ": 0 matches (ERROR!)");
            }
        }
        System.out.println("========================================");
        
        // Verify matches were actually added
        int totalMatchesInClubs = 0;
        for (Club club : clubs) {
            if (club != null && club.getScheduledMatches() != null) {
                totalMatchesInClubs += club.getScheduledMatches().size();
            }
        }
        System.out.println("VERIFICATION: Total matches in all clubs' scheduledMatches: " + totalMatchesInClubs);
        
        Gdx.app.log("LeagueFixtureGenerator", "Added " + matchesAddedToClubs + " match references to clubs (2 per match)");
        Gdx.app.log("LeagueFixtureGenerator", "Generated " + allMatches.size() + " league fixtures");
        
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
        System.out.println("generateRoundRobinFixtures() called with " + clubs.size() + " clubs");
        
        List<Match> matches = new ArrayList<>();
        int numClubs = clubs.size();
        
        if (numClubs < 2) {
            System.out.println("ERROR: Not enough clubs (" + numClubs + ") to generate fixtures");
            return matches;
        }
        
        // Create a working copy
        List<Club> workingClubs = new ArrayList<>(clubs);
        
        // Note: For odd number of clubs, one club gets a bye each round
        // The algorithm handles this automatically by only pairing available clubs
        
        int numRounds = numClubs - 1;
        int matchesPerRound = numClubs / 2;
        
        System.out.println("Round-robin parameters: " + numRounds + " rounds, " + matchesPerRound + " matches per round");
        System.out.println("Expected total matches: " + (numRounds * matchesPerRound));
        
        int matchesCreated = 0;
        
        // Generate rounds using round-robin algorithm
        for (int round = 0; round < numRounds; round++) {
            System.out.println("Generating round " + (round + 1) + " of " + numRounds);
            
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
                    System.out.println("ERROR: Null club at index " + club1Index + " or " + club2Index);
                    continue;
                }
                
                if (club1.getId() == null || club2.getId() == null) {
                    System.out.println("ERROR: Club has null ID - club1: " + (club1 != null ? club1.getName() : "NULL") + ", club2: " + (club2 != null ? club2.getName() : "NULL"));
                    continue;
                }
                
                Match match = new Match();
                match.setId(nextMatchId++);
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
                    System.out.println("  Created " + matchesCreated + " matches so far...");
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
        
        System.out.println("Generated " + matchesCreated + " matches in first half");
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
        System.out.println("generateReturnFixtures() called with " + firstHalfFixtures.size() + " matches");
        
        List<Match> returnFixtures = new ArrayList<>();
        
        for (Match originalMatch : firstHalfFixtures) {
            if (originalMatch == null) {
                continue;
            }
            
            // Create return fixture by swapping home and away
            Match returnMatch = new Match();
            returnMatch.setId(nextMatchId++);
            returnMatch.setMatchType(Match.LEAGUE_MATCH);
            returnMatch.setIsProposed(false);
            returnMatch.setIsAccepted(true);
            returnMatch.setIsPlayed(false);
            
            // Reverse home/away
            returnMatch.setHomeClubId(originalMatch.getAwayClubId());
            returnMatch.setAwayClubId(originalMatch.getHomeClubId());
            
            returnFixtures.add(returnMatch);
        }
        
        System.out.println("Generated " + returnFixtures.size() + " return fixtures");
        return returnFixtures;
    }
    
    /**
     * Schedule matches across the season dates
     * 
     * Distributes matches evenly across the season, typically one matchday per week.
     * 
     * @param matches List of matches to schedule
     * @param numClubs Number of clubs in the league (used to calculate matches per matchday)
     * @param seasonStart Start date of season
     * @param seasonEnd End date of season
     */
    private void scheduleMatchesAcrossSeason(List<Match> matches, int numClubs, LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        if (matches.isEmpty()) {
            return;
        }
        
        int totalMatches = matches.size();
        
        // Calculate total days in season
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(seasonStart, seasonEnd);
        
        // For a league with N clubs, all clubs should play on each matchday (if even number)
        // Matches per matchday = numClubs / 2 (each match has 2 teams)
        // Example: 12 clubs = 6 matches per matchday (all teams play)
        // Example: 11 clubs = 5 matches + 1 bye (we'll schedule 5 matches, one team sits out)
        int matchesPerMatchday = numClubs / 2;
        
        // Ensure at least 1 match per matchday
        if (matchesPerMatchday < 1) {
            matchesPerMatchday = 1;
        }
        
        System.out.println("LeagueFixtureGenerator: Scheduling " + totalMatches + " matches for " + numClubs + " clubs");
        System.out.println("LeagueFixtureGenerator: Matches per matchday: " + matchesPerMatchday);
        
        // Calculate number of matchdays needed
        int numMatchdays = (int) Math.ceil((double) totalMatches / matchesPerMatchday);
        
        // Calculate days between matchdays (typically weekly)
        long daysBetweenMatchdays = Math.max(DAYS_BETWEEN_MATCHDAYS, totalDays / Math.max(1, numMatchdays));
        
        // Schedule matches
        LocalDateTime currentDate = seasonStart;
        int matchIndex = 0;
        
        for (int matchday = 0; matchday < numMatchdays && matchIndex < totalMatches; matchday++) {
            // Schedule matches for this matchday
            for (int i = 0; i < matchesPerMatchday && matchIndex < totalMatches; i++) {
                Match match = matches.get(matchIndex);
                
                // Set match date (typically Saturday at 3 PM for historical accuracy)
                // Adjust to nearest Saturday if not already Saturday
                LocalDateTime matchDate = currentDate;
                int dayOfWeek = matchDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
                int daysToSaturday = (6 - dayOfWeek + 7) % 7; // Days until next Saturday
                if (daysToSaturday > 0) {
                    matchDate = matchDate.plusDays(daysToSaturday);
                }
                
                matchDate = matchDate.withHour(15).withMinute(0).withSecond(0);
                match.setMatchDateTime(matchDate);
                match.setProposeDateTime(matchDate); // Set propose date same as match date for league matches
                
                matchIndex++;
            }
            
            // Move to next matchday (weekly)
            currentDate = currentDate.plusDays(daysBetweenMatchdays);
            
            // Don't exceed season end
            if (currentDate.isAfter(seasonEnd) || matchIndex >= totalMatches) {
                // Schedule any remaining matches before season end
                for (; matchIndex < totalMatches; matchIndex++) {
                    Match match = matches.get(matchIndex);
                    LocalDateTime matchDate = seasonEnd.minusDays(totalMatches - matchIndex);
                    if (matchDate.isBefore(seasonStart)) {
                        matchDate = seasonStart;
                    }
                    match.setMatchDateTime(matchDate.withHour(15).withMinute(0).withSecond(0));
                    match.setProposeDateTime(match.getMatchDateTime());
                }
                break;
            }
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
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        System.out.println("    Checking players...");
        // Check if club has players
        if (club.getPlayers() == null) {
            String error = errorPrefix + "\n  REASON: club.getPlayers() is NULL\n  FIX: Club needs a players list";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        if (club.getPlayers().isEmpty()) {
            String error = errorPrefix + "\n  REASON: club.getPlayers() is EMPTY (0 players)\n  FIX: Club needs at least 11 players";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        int playerCount = club.getPlayers().size();
        System.out.println("    Players: " + playerCount);
        
        if (playerCount < 11) {
            String error = errorPrefix + "\n  REASON: Only " + playerCount + " players (need at least 11)\n  FIX: Add more players to the club";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        System.out.println("    Checking stadium...");
        // Check if club has stadium
        if (club.getStadium() == null) {
            String error = errorPrefix + "\n  REASON: club.getStadium() is NULL\n  FIX: Club needs a stadium";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        Integer capacity = club.getStadium().getCapacity();
        System.out.println("    Stadium capacity: " + capacity);
        
        if (capacity == null || capacity <= 0) {
            String error = errorPrefix + "\n  REASON: Invalid stadium capacity: " + capacity + " (must be > 0)\n  FIX: Set stadium capacity to a positive number";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        System.out.println("    Checking if club is in SaveGame...");
        // Check if club is in SaveGame (for match references)
        Club saveGameClub = currentGame.getClubById(club.getId());
        if (saveGameClub == null) {
            String error = errorPrefix + "\n  REASON: Club not found in SaveGame (ID: " + club.getId() + ")\n  Total clubs in SaveGame: " + (currentGame.getAllClubs() != null ? currentGame.getAllClubs().size() : 0) + "\n  FIX: Ensure club is properly added to SaveGame";
            System.err.println("========================================");
            System.err.println(error);
            System.err.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", error);
            throw new RuntimeException(error);
        }
        
        System.out.println("    SUCCESS: Club is ready!");
        System.out.println("      - Players: " + playerCount);
        System.out.println("      - Stadium capacity: " + capacity);
        System.out.println("      - In SaveGame: YES");
        Gdx.app.debug("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") is ready - " + playerCount + " players, stadium capacity: " + capacity);
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

