package com.rndmodgames.futtoboru.engine.temporal;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Competition Scheduler v1
 * 
 * @author Geomancer86
 */
public class CompetitionScheduler {

    private final Futtoboru game;

    public CompetitionScheduler(Futtoboru parent) {
        this.game = parent;
    }
    
    /**
     * This will run once on NEW GAME
     * 
     * Iterates all the competitions and creates instances on Save Game depending on the players selections on new game settings screen
     * 
     *  - Existing Continent Cups
     *  - Existing Country Cups and Leagues
     */
    public void initializeCompetitions() {
        
    }
    
    /**
     * TODO: Competition Rules are Parametrized
     * @return 
     */
    public List<Match> competitionDraw(Competition competition, List<Long> clubIds) {
        
        Log.info(Log.ENGINE_SCHEDULING, "CompetitionScheduler", "Randomizing Competition Draw for: " + competition.getName() + ", Clubs: " + clubIds.size());
        
        List<Long> auxiliarClubsIds = new ArrayList<>(clubIds);
        
        // the same competition and entrants draw the same way for a given game seed (P2-0)
        RandomSource rng = Futtoboru.randomOf(game).fork("draw:" + competition.getId() + ":" + clubIds);
        List<Match> competitionMatches = new ArrayList<>();
        
        // Handle bye if odd number of teams
        if (auxiliarClubsIds.size() % 2 != 0) {
            // Pick a random team to get a bye
            int byeIndex = rng.nextInt(auxiliarClubsIds.size());
            Long byeClubId = auxiliarClubsIds.remove(byeIndex);
            Log.info(Log.ENGINE_SCHEDULING, "CompetitionScheduler", "Club " + byeClubId + " receives a BYE");
        }
        
        while (!auxiliarClubsIds.isEmpty() && auxiliarClubsIds.size() >= 2) {
            
            Log.info(Log.ENGINE_SCHEDULING, "CompetitionScheduler", "Clubs Left: " + auxiliarClubsIds.size() + ", Matches: " + competitionMatches.size());
            
            // pick random clubs and create new match
            Match match = new Match();
            
            // Home Club
            int homeIndex = rng.nextInt(auxiliarClubsIds.size());
            match.setHomeClubId(auxiliarClubsIds.remove(homeIndex));
            
            // Away Club
            int awayIndex = rng.nextInt(auxiliarClubsIds.size());
            match.setAwayClubId(auxiliarClubsIds.remove(awayIndex));
            
            competitionMatches.add(match);
        }

        // Should return a list of matches between all participants
        // For 32 teams: 16 matches (no byes)
        // For 35 teams: 17 matches + 1 bye
        Log.info(Log.ENGINE_SCHEDULING, "CompetitionScheduler", "Competition Draw Complete: " + competitionMatches.size() + 
            " matches for " + clubIds.size() + " teams");
        
        // Verify: matches should equal half the number of teams (or half minus 0.5 if odd)
        int expectedMatches = clubIds.size() / 2;
        if (competitionMatches.size() != expectedMatches) {
            Log.error(Log.ENGINE_SCHEDULING, "CompetitionScheduler", "MISMATCH: Expected " + expectedMatches + 
                " matches but generated " + competitionMatches.size() + " for " + clubIds.size() + " teams!");
        }
        
        return competitionMatches;
    }
}