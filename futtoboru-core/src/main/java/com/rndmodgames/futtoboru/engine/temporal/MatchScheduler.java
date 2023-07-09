package com.rndmodgames.futtoboru.engine.temporal;

import java.util.Collections;
import java.util.Comparator;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Match Scheduler v1
 * 
 *  - Solve the friendly match requests
 *  - Put a time to the scheduled matches
 *  - Make sure the league matches are scheduled on free stadiums and timeslots to maximize attendance/etc.
 *  
 * NOTE:
 *  - as the matches are played by two teams we need to make sure we only simulate once, for example only take care of the HOME teams,
 *      and after the match is played, attach the match result to the AWAY team and call it done
 * 
 * 
 * @author Geomancer86
 */
public class MatchScheduler {

    //
    private Futtoboru game;
    
    //
    public MatchScheduler(Futtoboru parent) {
        
        //
        this.game = parent;
    }
    
    /**
     * Returns true if the Club has a Match TODAY (save game current game date)
     */
    public boolean checkClubMatchDay(Club club) {
        
        System.out.println("CHECKING IF TODAY IS A MATCH DAY!");
        
        // TODO: do not recreate the comparator every time
        Comparator<Match> comparatorChronological = (match1, match2) -> match1.getMatchDateTime()
                                                             .compareTo(match2.getMatchDateTime());
        
        // TODO: not required to do on every turn, only on insert new scheduled match
        // Sort
        Collections.sort(club.getScheduledMatches(), comparatorChronological);
        
        // Check we have at least one match
        if (!club.getScheduledMatches().isEmpty()) {
            
            // First scheduled match on list will be the next
            Match nextMatch = club.getScheduledMatches().get(0);
            
            System.out.println("NEXT MATCH DATE: " + nextMatch.getMatchDateTime());
            
            if (nextMatch.getMatchDateTime().isEqual(game.getCurrentGame().getGameDate())) {
                
                System.out.println("MATCH DAY!");
                
                Club homeClub = DatabaseLoader.getClubById(nextMatch.getHomeClubId());
                Club awayClub = DatabaseLoader.getClubById(nextMatch.getAwayClubId());

                System.out.println(homeClub.getName() + " vs " + awayClub.getName());
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check Club Scheduled Matches
     * 
     *  -
     */
    public void checkClubSheduledMatches(Club club) {
        
        //
        System.out.println("CLUB SCHEDULED MATCHES: " + club.getScheduledMatches().size());
        
        /**
         * SOLVE MATCHES:
         * 
         *  - the next match will always be the first on the scheduled matches list
         *  - if current date equals match date
         *      - change the CONTINUE button to MATCH PREVIEW
         *          - requires logic on main menu manager / easy
         *          
         *  - the basic match preview screen will show 
         *      - the MATCH PREVIEW button will change to MATCH RESULT
         *      
         *  - the basic match result screen will show
         *      - the MATCH RESULT button will change back to CONTINUE
         *  - 
         */

        // TODO: do not recreate the comparator every time 
//        Comparator<Match> comparatorChronological = (match1, match2) -> match1.getMatchDateTime()
//                                                            .compareTo(match2.getMatchDateTime());
// 
//        // TODO: not required to do on every turn, only on insert new scheduled match
//        // Sort
//        Collections.sort(club.getScheduledMatches(), comparatorChronological);
//        
//        // Check we have at least one match
//        if (!club.getScheduledMatches().isEmpty()) {
//            
//            /**
//             * TODO: this require the list of scheduled matches sorted BY DATETIME
//             * 
//             *  First scheduled match on list will be the next
//             */
//            Match nextMatch = club.getScheduledMatches().get(0);
//            
//            System.out.println("NEXT MATCH DATE: " + nextMatch.getMatchDateTime());
//            
//            // Compare the next match date with Current Date
//            if (nextMatch.getMatchDateTime().isEqual(game.getCurrentGame().getGameDate())) {
//                
//                System.out.println("MATCH DAY!");
//                
//                Club homeClub = DatabaseLoader.getClubById(nextMatch.getHomeClubId());
//                Club awayClub = DatabaseLoader.getClubById(nextMatch.getAwayClubId());
//                
//                System.out.println(homeClub.getName() + " vs " + awayClub.getName());
//                
//                // Set match as played
////                nextMatch.setIsPlayed(true);
////                
////                // Add to Played Matches
////                club.getPlayedMatches().add(nextMatch);
////                
////                // Remove from Scheduled Matches
////                club.getScheduledMatches().remove(nextMatch);
//            }
//        }
    }
    
    /**
     * Check Club Proposed Matches
     * 
     *  - TODO: to test propose many friendlies in a row to the same Club, 10 for example
     *      
     *      - after continuing the game the other Club should have accepted 1 match, wait a couple of days, accept other and succesively until all done and no scheduling conflicts
     *      
     *  - TODO: validate and avoid more than 1 friendly per day (to avoid too much scheduling)
     */
    public void checkClubProposedMatches(Club club) {
        
        //
        System.out.println("CLUB PROPOSED MATCHES: " + club.getProposedMatches());
        
        //
        for (Match match : club.getProposedMatches()) {
            
            boolean freeSchedule = true;
            
            // 
            System.out.println("CHECKING FRIENDY PROPOSAL AGAINST " + club.getScheduledMatches().size() + " SCHEDULED MATCHES");
            
            // TODO compare scheduled dates and proposed date
            // TODO set freeschedule as needed
            for (Match scheduled : club.getScheduledMatches()) {
                
            }
            
            // TODO: note they accept 100%
            if (freeSchedule) {
                
                //
                System.out.println("Schedule is free, accepting friendly request!");
                
                match.setIsAccepted(true);
                
                // add to accepted matches
                club.getScheduledMatches().add(match);
                
            } else {
                
                System.out.println("Friendly request cannot be accepted, scheduling conflicts!");
            }
            
            //LocalDateTime beforeDate = match.getProposeDateTime().minusDays(3).at;
            //LocalDateTime afterDate = match.getProposeDateTime().plusDays(3).at;
            
        }
        
        // we clear the list and everything that is not accepted will be gone
        club.getProposedMatches().clear();
    }
}