package com.rndmodgames.futtoboru.engine.temporal;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Competition Scheduler v1
 * 
 * @author Geomancer86
 */
public class CompetitionScheduler {

    //
    private Futtoboru game;
    
    //
    public CompetitionScheduler(Futtoboru parent) {
        
        //
        this.game = parent;
    }
    
    /**
     * TODO: Competition Rules are Parametrized
     * @return 
     */
    public List<Match> competitionDraw(Competition competition, List<Long> clubIds) {
        
        Gdx.app.log("CompetitionScheduler", "Randomizing Competition Draw for: " + competition.getName() + ", Clubs: " + clubIds.size());
        
        List<Match> competitionMatches = new ArrayList<>();
        
        while (!clubIds.isEmpty()) {
            
            Gdx.app.log("CompetitionScheduler", "Clubs Left: " + clubIds.size() + ", Matches: " + competitionMatches.size());
            
            // pick random clubs and create new match
            Match match = new Match();
            
            //
            match.setHomeClubId(clubIds.get(DatabaseLoader.RNG.nextInt(clubIds.size() - 1)));
            
            // concurrent modification exception?
            clubIds.remove(match.getHomeClubId());
            
            // take care of just 1 club in the list
            if (clubIds.size() == 1) {
                
                match.setAwayClubId(clubIds.get(0));
                
            } else {
                
                match.setAwayClubId(clubIds.get(DatabaseLoader.RNG.nextInt(clubIds.size() - 1)));
            }
            
            
            // concurrent modification exception?
            clubIds.remove(match.getAwayClubId());
            
            //
            competitionMatches.add(match);
        }

        // Should return a list of matches between all participants
        Gdx.app.log("CompetitionScheduler", "Competition Draw Matches: " + competitionMatches.size());
        
        return competitionMatches;
    }
}