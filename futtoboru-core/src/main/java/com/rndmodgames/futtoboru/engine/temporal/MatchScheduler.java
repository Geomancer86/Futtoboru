package com.rndmodgames.futtoboru.engine.temporal;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;

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

    /**
     * Check Club Scheduled Matches
     * 
     *  -
     */
    public void checkClubSheduledMatches(Club club) {
        
        //
        System.out.println("CLUB SCHEDULED MATCHES: " + club.getScheduledMatches());
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