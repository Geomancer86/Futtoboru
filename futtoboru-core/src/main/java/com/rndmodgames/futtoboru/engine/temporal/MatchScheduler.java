package com.rndmodgames.futtoboru.engine.temporal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    private static BigDecimal FRIENDLY_TICKET_PRICE = new BigDecimal("0.5");
    
    //
    DecimalFormat df = new DecimalFormat("#,###.00");
    
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
         * Iterate all scheduled matches
         * 
         *  - add money to the club balance depending on the tickets sold
         *  - random tickets by day
         *  - randomize by club reputation TODO
         *  - sales should stop if the stadium is full
         */
        for (Match scheduled : club.getScheduledMatches()) {
            
            /**
             * RANDOM TICKETS:
             * 
             *  - number of max random tickets should be less or equal to the number of available tickets for sale
             *  - we need all the stadiums to compare or this wont work
             */
            int maxTickets = club.getStadium().getCapacity() - scheduled.getAttendance();
            
            System.out.println("MATCH: " + DatabaseLoader.getClubById(scheduled.getHomeClubId()).getName() + " vs " + DatabaseLoader.getClubById(scheduled.getAwayClubId()).getName());
            System.out.println("STADIUM " + club.getStadium().getName() + " HAS A FULL CAPACITY OF " + club.getStadium().getCapacity());
            System.out.println("CURRENT AVAILABLE TICKETS (CAPACITY): " + maxTickets);
            
            if (maxTickets > 0) {
                
                /**
                 * TODO: balance match day sales
                 */
                int maxPerDay = maxTickets / 7; // sell out on 7 days
                int minPerDay = 20;
                
                //
                System.out.println("TICKETS TO SELL TODAY: min: " + minPerDay + ", max: " + maxPerDay);
                
                int randomTickets;
                
                // TODO: fix matches not selling out
                if (minPerDay < maxPerDay) {
                    
                    randomTickets = DatabaseLoader.RNG.nextInt(minPerDay, maxPerDay);
                    
                } else {
                    
                    // sell out
                    randomTickets = maxPerDay;
                }
                
                /**
                 * Add to Match Attendance - TODO Stadiums / Capacity
                 */
                scheduled.setAttendance(scheduled.getAttendance() + randomTickets);
                
                BigDecimal dayCash = new BigDecimal(randomTickets).multiply(FRIENDLY_TICKET_PRICE);
                
                System.out.println("SOLD TICKETS: " + randomTickets);
                System.out.println("MATCH DAY CASH IS: $" + df.format(dayCash));
                
                // ADD TO CLUB
                club.setClubBalance(club.getClubBalance().add(dayCash));
                
            } else {
                
                //
                System.out.println("NOT SELLING TICKETS FOR MATCH, SOLD OUT!");
            }
        }
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