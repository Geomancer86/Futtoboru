package com.rndmodgames.futtoboru.engine.temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

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
        if (league == null || league.getLeagueClubs() == null || league.getLeagueClubs().isEmpty()) {
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: league is null or has no clubs");
            return new ArrayList<>();
        }
        
        List<Club> clubs = new ArrayList<>(league.getLeagueClubs());
        int numClubs = clubs.size();
        
        if (numClubs < 2) {
            Gdx.app.error("LeagueFixtureGenerator", "Cannot generate fixtures: league has less than 2 clubs");
            return new ArrayList<>();
        }
        
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
        int matchesAddedToClubs = 0;
        for (Match match : allMatches) {
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub == null) {
                Gdx.app.error("LeagueFixtureGenerator", "Home club not found in SaveGame for ID: " + match.getHomeClubId());
                continue;
            }
            if (awayClub == null) {
                Gdx.app.error("LeagueFixtureGenerator", "Away club not found in SaveGame for ID: " + match.getAwayClubId());
                continue;
            }
            
            // Initialize scheduledMatches if null
            if (homeClub.getScheduledMatches() == null) {
                homeClub.setScheduledMatches(new ArrayList<>());
                Gdx.app.log("LeagueFixtureGenerator", "Initialized scheduledMatches for home club: " + homeClub.getName());
            }
            if (awayClub.getScheduledMatches() == null) {
                awayClub.setScheduledMatches(new ArrayList<>());
                Gdx.app.log("LeagueFixtureGenerator", "Initialized scheduledMatches for away club: " + awayClub.getName());
            }
            
            homeClub.getScheduledMatches().add(match);
            awayClub.getScheduledMatches().add(match);
            matchesAddedToClubs += 2;
        }
        
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
        List<Match> matches = new ArrayList<>();
        int numClubs = clubs.size();
        
        if (numClubs < 2) {
            return matches;
        }
        
        // Create a working copy
        List<Club> workingClubs = new ArrayList<>(clubs);
        
        // Note: For odd number of clubs, one club gets a bye each round
        // The algorithm handles this automatically by only pairing available clubs
        
        int numRounds = numClubs - 1;
        int matchesPerRound = numClubs / 2;
        
        // Generate rounds using round-robin algorithm
        for (int round = 0; round < numRounds; round++) {
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
                
                // Skip if either club is null
                if (club1 == null || club2 == null || club1.getId() == null || club2.getId() == null) {
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

