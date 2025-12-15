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
        
        List<Club> validClubs = new ArrayList<>();
        for (Club club : clubs) {
            System.out.println("Checking club: " + (club != null ? club.getName() : "NULL") + " (ID: " + (club != null ? club.getId() : "NULL") + ")");
            boolean isReady = isClubReady(club);
            System.out.println("  -> Club is ready: " + isReady);
            
            if (isReady) {
                validClubs.add(club);
                System.out.println("  -> ADDED to valid clubs list");
            } else {
                System.out.println("  -> SKIPPED (missing required data)");
                Gdx.app.error("LeagueFixtureGenerator", "Skipping club " + club.getName() + " (ID: " + club.getId() + ") - missing required data");
            }
        }
        
        System.out.println("========================================");
        System.out.println("CLUB VALIDATION RESULTS:");
        System.out.println("Total clubs in league: " + numClubs);
        System.out.println("Valid clubs: " + validClubs.size());
        System.out.println("Invalid clubs: " + (numClubs - validClubs.size()));
        System.out.println("========================================");
        
        if (validClubs.size() < 2) {
            System.out.println("========================================");
            System.out.println("ERROR: Cannot generate fixtures!");
            System.out.println("Only " + validClubs.size() + " clubs have complete data (need at least 2)");
            System.out.println("========================================");
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: only " + validClubs.size() + " clubs have complete data (need at least 2)");
            return new ArrayList<>();
        }
        
        if (validClubs.size() < numClubs) {
            System.out.println("WARNING: Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
            Gdx.app.log("LeagueFixtureGenerator", "WARNING: Only " + validClubs.size() + " out of " + numClubs + " clubs have complete data. Generating fixtures for valid clubs only.");
        }
        
        // Use only valid clubs for fixture generation
        clubs = validClubs;
        numClubs = clubs.size();
        
        Gdx.app.log("LeagueFixtureGenerator", "Generating fixtures for league: " + league.getName() + " with " + numClubs + " clubs");
        
        List<Match> allMatches = new ArrayList<>();
        
        // Generate home fixtures (first half of season)
        List<Match> homeFixtures = generateRoundRobinFixtures(clubs, true);
        
        // Generate away fixtures (second half of season) - reverse home/away
        List<Match> awayFixtures = generateRoundRobinFixtures(clubs, false);
        
        // Combine and schedule matches
        allMatches.addAll(homeFixtures);
        allMatches.addAll(awayFixtures);
        
        // Schedule matches across season dates
        scheduleMatchesAcrossSeason(allMatches, seasonStartDate, seasonEndDate);
        
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
     * Round-robin algorithm: Each club plays every other club once per round.
     * For N clubs, we need N-1 rounds. In each round, we pair clubs.
     * 
     * @param clubs List of clubs
     * @param isHomeRound If true, first club in pair is home; if false, second club is home
     * @return List of matches
     */
    private List<Match> generateRoundRobinFixtures(List<Club> clubs, boolean isHomeRound) {
        System.out.println("generateRoundRobinFixtures() called with " + clubs.size() + " clubs, isHomeRound: " + isHomeRound);
        
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
                
                // Set home/away based on isHomeRound flag
                if (isHomeRound) {
                    match.setHomeClubId(club1.getId());
                    match.setAwayClubId(club2.getId());
                } else {
                    // Reverse for return fixtures
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
            if (round < numRounds - 1) {
                Club first = workingClubs.remove(0);
                Club second = workingClubs.remove(0);
                workingClubs.add(second);
                workingClubs.add(0, first);
            }
        }
        
        return matches;
    }
    
    /**
     * Schedule matches across the season dates
     * 
     * Distributes matches evenly across the season, typically one matchday per week.
     * 
     * @param matches List of matches to schedule
     * @param seasonStart Start date of season
     * @param seasonEnd End date of season
     */
    private void scheduleMatchesAcrossSeason(List<Match> matches, LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        if (matches.isEmpty()) {
            return;
        }
        
        int totalMatches = matches.size();
        
        // Calculate total days in season
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(seasonStart, seasonEnd);
        
        // For a league with N clubs, we have N*(N-1) matches (home and away)
        // Typically, all clubs play on the same matchday
        // Calculate matches per matchday: if we have 12 clubs, we have 6 matches per matchday
        int numClubs = (int) Math.sqrt(totalMatches / 2.0) + 1; // Rough estimate
        int matchesPerMatchday = Math.max(1, numClubs / 2);
        
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
     */
    private boolean isClubReady(Club club) {
        if (club == null) {
            System.out.println("    ERROR: Club is null");
            Gdx.app.error("LeagueFixtureGenerator", "Club is null");
            return false;
        }
        
        System.out.println("    Checking players...");
        // Check if club has players
        if (club.getPlayers() == null) {
            System.out.println("    ERROR: club.getPlayers() is NULL");
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") has null players list");
            return false;
        }
        
        if (club.getPlayers().isEmpty()) {
            System.out.println("    ERROR: club.getPlayers() is EMPTY");
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") has no players");
            return false;
        }
        
        int playerCount = club.getPlayers().size();
        System.out.println("    Players: " + playerCount);
        
        if (playerCount < 11) {
            System.out.println("    ERROR: Only " + playerCount + " players (need at least 11)");
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") has only " + playerCount + " players (need at least 11)");
            return false;
        }
        
        System.out.println("    Checking stadium...");
        // Check if club has stadium
        if (club.getStadium() == null) {
            System.out.println("    ERROR: club.getStadium() is NULL");
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") has no stadium");
            return false;
        }
        
        Integer capacity = club.getStadium().getCapacity();
        System.out.println("    Stadium capacity: " + capacity);
        
        if (capacity == null || capacity <= 0) {
            System.out.println("    ERROR: Invalid stadium capacity: " + capacity);
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") has invalid stadium capacity: " + capacity);
            return false;
        }
        
        System.out.println("    Checking if club is in SaveGame...");
        // Check if club is in SaveGame (for match references)
        Club saveGameClub = currentGame.getClubById(club.getId());
        if (saveGameClub == null) {
            System.out.println("    ERROR: Club not found in SaveGame (ID: " + club.getId() + ")");
            System.out.println("    Total clubs in SaveGame: " + (currentGame.getAllClubs() != null ? currentGame.getAllClubs().size() : 0));
            Gdx.app.error("LeagueFixtureGenerator", "Club " + club.getName() + " (ID: " + club.getId() + ") is not in SaveGame");
            return false;
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

