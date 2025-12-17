package com.rndmodgames.futtoboru.engine.temporal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
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
    
    /**
     * Historical Ticket Prices (1888-89)
     * 
     * 1888 Currency:
     *  - 1 pound = 20 shillings = 240 pence
     *  - 1 penny = 1/240 pounds = £0.00416666666
     *  - 2 pence = 2/240 pounds = £0.00833333333
     *  - 3 pence = 3/240 pounds = £0.0125
     *  
     * Historical Research:
     *  - Friendly matches: 1 penny (modest, accessible to working-class)
     *  - League matches: 2-3 pence (higher interest, competitive)
     *  - Cup matches: 2-3 pence (similar to league)
     */
    private static BigDecimal FRIENDLY_TICKET_PRICE = new BigDecimal("0.00416666666"); // 1 penny
    private static BigDecimal LEAGUE_TICKET_PRICE = new BigDecimal("0.00833333333"); // 2 pence
    private static BigDecimal CUP_TICKET_PRICE = new BigDecimal("0.01041666666"); // 2.5 pence (average of 2-3)
    
    /**
     * Get ticket price based on match type (v1.0)
     */
    private static BigDecimal getTicketPrice(Integer matchType) {
        if (matchType == null) {
            return FRIENDLY_TICKET_PRICE; // Default
        }
        
        switch (matchType) {
            case Match.FRIENDLY_MATCH:
                return FRIENDLY_TICKET_PRICE; // 1 penny
            case Match.LEAGUE_MATCH:
                return LEAGUE_TICKET_PRICE; // 2 pence
            case Match.CUP_MATCH:
                return CUP_TICKET_PRICE; // 2.5 pence
            default:
                return FRIENDLY_TICKET_PRICE; // Default to friendly price
        }
    }
    
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
        
        // Null check: unemployed players don't have a club
        if (club == null) {
            return false;
        }
        
        System.out.println("CHECKING IF TODAY IS A MATCH DAY!");
        
        // Null check: club might not have scheduled matches initialized
        if (club.getScheduledMatches() == null) {
            return false;
        }
        
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
     *  - Sell tickets for upcoming matches
     *  - Only sell tickets for matches that haven't been played
     *  - Only sell tickets within a reasonable time window before the match (7 days)
     */
    public void checkClubSheduledMatches(Club club) {
        
        //
        System.out.println("CLUB SCHEDULED MATCHES: " + club.getScheduledMatches().size());
        
        // Get current game date
        java.time.LocalDateTime currentDate = game.getCurrentGame().getGameDate();
        
        /**
         * Iterate all scheduled matches
         * 
         *  - add money to the club balance depending on the tickets sold
         *  - random tickets by day
         *  - randomize by club reputation TODO
         *  - sales should stop if the stadium is full
         *  
         *  TODO: all club stadiums
         *  TODO: split money between clubs in friendlies
         *  TODO: research about real world ticket splitting (or they just pay the fee)
         *  TODO: friendly match fees
         *  TODO: differentiate between match and league matches so the ticket values are different
         */
        for (Match scheduled : club.getScheduledMatches()) {
            
            // BUG FIX: Skip matches that have already been played
            if (scheduled.getIsPlayed() != null && scheduled.getIsPlayed()) {
                System.out.println("MatchScheduler: Skipping already played match");
                continue;
            }
            
            // BUG FIX: Only sell tickets for matches scheduled in the future
            if (scheduled.getMatchDateTime() == null) {
                System.out.println("MatchScheduler: Skipping match with no date");
                continue;
            }
            
            // BUG FIX: Only sell tickets starting 7 days before the match
            // This prevents selling tickets for matches scheduled months in advance
            java.time.temporal.ChronoUnit daysUnit = java.time.temporal.ChronoUnit.DAYS;
            long daysUntilMatch = daysUnit.between(currentDate, scheduled.getMatchDateTime());
            
            if (daysUntilMatch > 7) {
                // Match is more than 7 days away, don't sell tickets yet
                System.out.println("MatchScheduler: Match is " + daysUntilMatch + " days away, not selling tickets yet");
                continue;
            }
            
            if (daysUntilMatch < 0) {
                // Match date has passed but hasn't been marked as played yet
                // This shouldn't happen, but skip it to be safe
                System.out.println("MatchScheduler: Match date has passed but not marked as played, skipping");
                continue;
            }
            
            /**
             * RANDOM TICKETS:
             * 
             *  - number of max random tickets should be less or equal to the number of available tickets for sale
             *  - we need all the stadiums to compare or this wont work
             */
            int maxTickets = club.getStadium().getCapacity() - scheduled.getAttendance();
            
            System.out.println("MATCH            : " + DatabaseLoader.getClubById(scheduled.getHomeClubId()).getName() + " vs " + DatabaseLoader.getClubById(scheduled.getAwayClubId()).getName());
            System.out.println("STADIUM NAME     : " + club.getStadium().getName());
            System.out.println("STADIUM CAPACITY : " + club.getStadium().getCapacity());
            System.out.println("TICKETS SOLD     : " + scheduled.getAttendance());
            System.out.println("TICKETS AVAILABLE: " + maxTickets);
            
            if (maxTickets > 0) {
                
                /**
                 * Calculate base ticket sales per day
                 * Friendly matches have drastically reduced attendance (15% of league matches)
                 * Based on historical research: 1888-89 friendlies had 500-700 vs much higher league attendance
                 */
                int baseMaxPerDay = club.getStadium().getCapacity() / 7; // sell out on 7 days
                
                // Apply match type modifier
                double attendanceMultiplier = 1.0;
                int baseMinPerDay = 20;
                
                if (scheduled.getMatchType() != null && scheduled.getMatchType() == Match.FRIENDLY_MATCH) {
                    // Friendly matches: 15% of league match attendance (historical accuracy)
                    attendanceMultiplier = 0.15;
                    baseMinPerDay = 5; // Lower minimum for friendlies
                }
                // League and Cup matches use full multiplier (1.0)
                
                int maxPerDay = (int)(baseMaxPerDay * attendanceMultiplier);
                
                // Cap it at max capacity just in case
                if (maxPerDay > maxTickets) {
                    maxPerDay = maxTickets;
                }
                
                int minPerDay = Math.max((int)(baseMinPerDay * attendanceMultiplier), 1); // At least 1 ticket
                
                //
                System.out.println("MATCH TYPE: " + (scheduled.getMatchType() == Match.FRIENDLY_MATCH ? "FRIENDLY" : 
                                                   scheduled.getMatchType() == Match.LEAGUE_MATCH ? "LEAGUE" : "CUP"));
                System.out.println("ATTENDANCE MULTIPLIER: " + attendanceMultiplier);
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
                 * Add to Match Attendance
                 * 
                 * TODO: we can make the attendance go slightly over stadium capacity depending on the stadium type, original stadiums
                 *          were open and the limits aren set in stone (standing people)
                 */
                scheduled.setAttendance(scheduled.getAttendance() + randomTickets);
                
                // Get ticket price based on match type (v1.0)
                BigDecimal ticketPrice = getTicketPrice(scheduled.getMatchType());
                BigDecimal dayCash = new BigDecimal(randomTickets).multiply(ticketPrice);
                
                System.out.println("SOLD TICKETS: " + randomTickets);
                System.out.println("MATCH DAY CASH IS: $" + df.format(dayCash));
                
                // Record match revenue on Match object (v1.0)
                BigDecimal currentRevenue = scheduled.getMatchRevenue();
                scheduled.setMatchRevenue(currentRevenue.add(dayCash));
                
                // Record match income for financial tracking (v1.0)
                recordMatchIncome(scheduled, club, randomTickets, dayCash);
                
                // ADD TO CLUB BALANCE
                club.setClubBalance(club.getClubBalance().add(dayCash));
                
                // Update period income tracking (v1.0)
                club.setSeasonIncome(club.getSeasonIncome().add(dayCash));
                club.setMonthIncome(club.getMonthIncome().add(dayCash));
                
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
    
    /**
     * Record match income for financial tracking (v1.0)
     * 
     * Creates or updates a MatchIncome object for the match.
     * Since tickets are sold over multiple days, we need to aggregate the data
     * into a single MatchIncome record per match.
     */
    private void recordMatchIncome(Match match, Club homeClub, int ticketsSold, BigDecimal revenue) {
        if (match == null || homeClub == null || revenue == null || match.getId() == null) {
            return;
        }
        
        // Find existing MatchIncome for this match, or create new one
        MatchIncome income = findOrCreateMatchIncome(match, homeClub);
        
        // Accumulate ticket sales and revenue (tickets sold over multiple days)
        income.setTicketRevenue(income.getTicketRevenue().add(revenue));
        
        // Accumulate attendance (tickets sold today added to total)
        int currentAttendance = income.getAttendance() != null ? income.getAttendance() : 0;
        income.setAttendance(currentAttendance + ticketsSold);
        
        // Set match date and type (should be same for all days)
        if (match.getMatchDateTime() != null) {
            income.setMatchDate(match.getMatchDateTime());
        }
        if (match.getMatchType() != null) {
            income.setMatchType(match.getMatchType());
        }
        
        System.out.println("MatchScheduler: Updated match income - Match ID: " + match.getId() + 
                          ", Total Revenue: $" + df.format(income.getTicketRevenue()) + 
                          ", Total Attendance: " + income.getAttendance() + 
                          ", Today's Tickets: " + ticketsSold);
    }
    
    /**
     * Find existing MatchIncome for a match, or create a new one if it doesn't exist
     */
    private MatchIncome findOrCreateMatchIncome(Match match, Club homeClub) {
        List<MatchIncome> matchIncomes = homeClub.getMatchIncomes();
        
        // Search for existing MatchIncome with this match ID
        if (matchIncomes != null) {
            for (MatchIncome existing : matchIncomes) {
                if (existing.getMatchId() != null && existing.getMatchId().equals(match.getId())) {
                    // Found existing record, return it to update
                    return existing;
                }
            }
        }
        
        // No existing record found, create new one
        MatchIncome income = new MatchIncome();
        income.setMatchId(match.getId());
        income.setClubId(homeClub.getId());
        income.setTicketRevenue(BigDecimal.ZERO);
        income.setAttendance(0);
        income.setMatchDate(match.getMatchDateTime() != null ? match.getMatchDateTime() : LocalDateTime.now());
        income.setMatchType(match.getMatchType());
        
        // Add to club's match income list
        homeClub.addMatchIncome(income);
        
        return income;
    }
}