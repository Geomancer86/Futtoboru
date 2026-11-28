package com.rndmodgames.futtoboru.engine.temporal;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;

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
     * G-12a: the admission a club charges, by its standing and the kind of match (economy.json; before, a fixed 1d
     * friendly, 2d League, 2.5d Cup for every club).
     */
    private BigDecimal ticketPrice(Club seller, Match match) {
        return economyOf(seller, match).admission(match.getMatchType());
    }

    private com.rndmodgames.futtoboru.data.ClubEconomy economyOf(Club seller, Match match) {
        java.time.LocalDate day = match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate()
                : game.getCurrentGame().getGameDate().toLocalDate();
        return com.rndmodgames.futtoboru.engine.finances.ClubEconomies.of(game.getCurrentGame(), seller, day);
    }

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
        
        Log.info(Log.ENGINE_SCHEDULING, "CHECKING IF TODAY IS A MATCH DAY!");
        
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
            
            // Find the next match that is ready to play (both teams determined and date matches)
            LocalDateTime today = game.getCurrentGame().getGameDate();
            
            for (Match match : club.getScheduledMatches()) {
                // Skip matches that aren't ready (teams not determined)
                if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
                    continue; // Future round match, teams not yet determined
                }
                
                // Skip matches already played
                if (match.getIsPlayed() != null && match.getIsPlayed()) {
                    continue;
                }
                
                // Check if match date is today
                if (match.getMatchDateTime() != null && match.getMatchDateTime().toLocalDate().equals(today.toLocalDate())) {
                    Log.info(Log.ENGINE_SCHEDULING, "MATCH DAY! " + match.getBracketPath() + " on " + today);
                    return true;
                }
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
        Log.info(Log.ENGINE_SCHEDULING, "CLUB SCHEDULED MATCHES: " + club.getScheduledMatches().size());
        
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
         *  P2-12: only the home club sells tickets (no gate share in 1888-89; see below)
         *  TODO: friendly match fees
         *  TODO: differentiate between match and league matches so the ticket values are different
         */
        for (Match scheduled : club.getScheduledMatches()) {
            
            // BUG FIX: Skip matches that have already been played
            if (scheduled.getIsPlayed() != null && scheduled.getIsPlayed()) {
                Log.debug(Log.ENGINE_SCHEDULING, "MatchScheduler: Skipping already played match");
                continue;
            }
            
            // P2-3: only the two clubs of a match sell tickets for it. A cup tie of a later round sits in every
            // entrant's list until its clubs are known: it logged a "cannot find club" error every day, and once
            // its clubs were known every other entrant sold tickets for it too.
            if (club.getId() == null || !(club.getId().equals(scheduled.getHomeClubId())
                    || club.getId().equals(scheduled.getAwayClubId()))) {
                continue;
            }
            if (scheduled.getHomeClubId() == null || scheduled.getAwayClubId() == null) {
                continue; // the opponent is not decided yet: no tickets until it is
            }
            // U-08 / P2-12: only the home club sells tickets and books the gate, for friendlies, league and cup
            // matches alike: the visitors play at its ground. Before P2-12 the away club of a league or cup match
            // sold tickets too (against its own ground's capacity), so each gate was booked twice and the away club
            // was paid for it. No gate share or visitors' guarantee is modelled for 1888-89 (none sourced for that
            // season). The FA required a gate share for the visitors in cup ties only after Chatham's open-ground
            // run of 1888-89 (docs/data/history/GROUNDS_1888-89.md, "Chatham: the Great Lines"; Wikipedia,
            // Chatham Town F.C.): a later season's rule, to come from data when seasons after 1888-89 need it.
            // P2-14: at a neutral ground the club drawn at home runs the sale; the gate follows the season's rule.
            if (!club.getId().equals(scheduled.getHomeClubId())) {
                continue;
            }
            
            // BUG FIX: Only sell tickets for matches scheduled in the future
            if (scheduled.getMatchDateTime() == null) {
                Log.warn(Log.ENGINE_SCHEDULING, "MatchScheduler: Skipping match with no date");
                continue;
            }
            
            // BUG FIX: Only sell tickets starting 7 days before the match
            // This prevents selling tickets for matches scheduled months in advance
            java.time.temporal.ChronoUnit daysUnit = java.time.temporal.ChronoUnit.DAYS;
            long daysUntilMatch = daysUnit.between(currentDate, scheduled.getMatchDateTime());
            
            if (daysUntilMatch > 7) {
                // Match is more than 7 days away, don't sell tickets yet
                Log.debug(Log.ENGINE_SCHEDULING, "MatchScheduler: Match is " + daysUntilMatch + " days away, not selling tickets yet");
                continue;
            }
            
            if (daysUntilMatch < 0) {
                // Match date has passed but hasn't been marked as played yet
                // This shouldn't happen, but skip it to be safe
                Log.warn(Log.ENGINE_SCHEDULING, "MatchScheduler: Match date has passed but not marked as played, skipping");
                continue;
            }
            
            /**
             * RANDOM TICKETS:
             * 
             *  - number of max random tickets should be less or equal to the number of available tickets for sale
             *  - we need all the stadiums to compare or this wont work
             */
            // Handle null attendance (should default to 0)
            Integer currentAttendance = scheduled.getAttendance();
            if (currentAttendance == null) {
                currentAttendance = 0;
                scheduled.setAttendance(0);
            }
            
            // P2-14: the ground the match is played at: a neutral venue (cup semi-final, final) or the home club's.
            // At a neutral ground the club drawn at home still runs the sale (once per match and day), and the gate
            // goes where the cup season's rule says (1888-89: to the Football Association, NeutralVenues.Gate).
            com.rndmodgames.futtoboru.data.Stadium ground = NeutralVenues.stadiumOf(scheduled, game.getCurrentGame());
            if (ground == null || ground.getCapacity() == null) {
                ground = club.getStadium();
            }
            NeutralVenues.Gate gateRule = NeutralVenues.gateOf(scheduled, game.getCurrentGame());
            int maxTickets = ground.getCapacity() - currentAttendance;
            if (scheduled.getMatchType() != null && scheduled.getMatchType() == Match.FRIENDLY_MATCH) {
                // G-12a: a friendly draws its local following, not a full ground (economy.json friendlyCrowdMax)
                int most = economyOf(club, scheduled).getFriendlyCrowdMax();
                if (most > 0) {
                    maxTickets = Math.min(maxTickets, most - currentAttendance);
                }
            }

            // CRITICAL FIX: Add null checks to prevent NullPointerException
            Club homeClub = game.getCurrentGame().getClubById(scheduled.getHomeClubId());
            Club awayClub = game.getCurrentGame().getClubById(scheduled.getAwayClubId());
            
            if (homeClub == null || awayClub == null) {
                Log.error(Log.ENGINE_SCHEDULING, "MatchScheduler", 
                    "CRITICAL: Cannot find club(s) for match. HomeClubId: " + scheduled.getHomeClubId() + 
                    ", AwayClubId: " + scheduled.getAwayClubId() + ", Match ID: " + scheduled.getId());
                continue; // Skip this match to prevent crash
            }
            
            Log.info(Log.ENGINE_SCHEDULING, "MATCH            : " + homeClub.getName() + " vs " + awayClub.getName());
            Log.info(Log.ENGINE_SCHEDULING, "STADIUM NAME     : " + ground.getName());
            Log.info(Log.ENGINE_SCHEDULING, "STADIUM CAPACITY : " + ground.getCapacity());
            Log.info(Log.ENGINE_SCHEDULING, "TICKETS SOLD     : " + currentAttendance);
            Log.info(Log.ENGINE_SCHEDULING, "TICKETS AVAILABLE: " + maxTickets);
            
            if (maxTickets > 0) {
                
                /**
                 * Calculate base ticket sales per day
                 * Friendly matches have drastically reduced attendance (15% of league matches)
                 * Based on historical research: 1888-89 friendlies had 500-700 vs much higher league attendance
                 */
                int baseMaxPerDay = ground.getCapacity() / 7; // sell out on 7 days
                
                // Apply match type modifier
                double attendanceMultiplier = 1.0;
                int baseMinPerDay = 20;
                
                if (scheduled.getMatchType() != null && scheduled.getMatchType() == Match.FRIENDLY_MATCH) {
                    // Friendly matches: a share of a league match's sale, by the club's standing (G-12a, economy.json)
                    attendanceMultiplier = economyOf(club, scheduled).getFriendlyCrowd();
                    baseMinPerDay = 5; // Lower minimum for friendlies
                }
                // League and Cup matches use full multiplier (1.0)
                
                int maxPerDay = (int)(baseMaxPerDay * attendanceMultiplier);
                
                // Ensure maxPerDay is at least 1 for friendlies (even if capacity is small)
                if (maxPerDay < 1) {
                    maxPerDay = 1;
                }
                
                // Cap it at max capacity just in case
                if (maxPerDay > maxTickets) {
                    maxPerDay = maxTickets;
                }
                
                int minPerDay = Math.max((int)(baseMinPerDay * attendanceMultiplier), 1); // At least 1 ticket
                
                // Ensure minPerDay doesn't exceed maxPerDay
                if (minPerDay > maxPerDay) {
                    minPerDay = maxPerDay;
                }
                
                //
                Log.info(Log.ENGINE_SCHEDULING, "MATCH TYPE: " + (scheduled.getMatchType() == Match.FRIENDLY_MATCH ? "FRIENDLY" : 
                                                   scheduled.getMatchType() == Match.LEAGUE_MATCH ? "LEAGUE" : "CUP"));
                Log.info(Log.ENGINE_SCHEDULING, "ATTENDANCE MULTIPLIER: " + attendanceMultiplier);
                Log.info(Log.ENGINE_SCHEDULING, "TICKETS TO SELL TODAY: min: " + minPerDay + ", max: " + maxPerDay);
                
                int randomTickets;
                
                // Sell tickets: if range is valid, use random; otherwise sell what's available
                if (minPerDay < maxPerDay) {
                    randomTickets = Futtoboru.randomOf(game).fork("tickets:" + scheduled.getId() + ":" + currentDate.toLocalDate())
                            .nextInt(minPerDay, maxPerDay + 1); // +1 because nextInt is exclusive
                } else {
                    // min equals max, sell that amount
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
                BigDecimal ticketPrice = ticketPrice(club, scheduled);
                BigDecimal dayCash = new BigDecimal(randomTickets).multiply(ticketPrice);
                
                Log.info(Log.ENGINE_SCHEDULING, "SOLD TICKETS: " + randomTickets);
                Log.info(Log.ENGINE_SCHEDULING, "MATCH DAY CASH IS: " + Money.format(dayCash));
                
                // Record match revenue on Match object (v1.0)
                BigDecimal currentRevenue = scheduled.getMatchRevenue();
                scheduled.setMatchRevenue(currentRevenue.add(dayCash));
                
                // P2-14: who books the day's gate (home ground: the home club; neutral ground: the season's rule)
                switch (gateRule) {
                case ASSOCIATION:
                    // the Football Association takes the gate of its semi-finals and final: no club books it
                    Log.info(Log.ENGINE_SCHEDULING, "GATE TO THE ASSOCIATION: " + Money.format(dayCash));
                    break;
                case SHARED: {
                    BigDecimal half = dayCash.divide(new BigDecimal(2), dayCash.scale(), java.math.RoundingMode.DOWN);
                    book(scheduled, club, randomTickets, half);
                    book(scheduled, awayClub, 0, dayCash.subtract(half));
                    break;
                }
                default:
                    book(scheduled, club, randomTickets, dayCash);
                    break;
                }

            } else {
                
                //
                Log.info(Log.ENGINE_SCHEDULING, "NOT SELLING TICKETS FOR MATCH, SOLD OUT!");
            }
        }
    }
    
    /**
     * Friendlies (U-08): call off a club's friendlies that clash with a league or cup match, then let the other
     * clubs answer its invitations ({@link FriendlyArranger}).
     */
    public void checkClubProposedMatches(Club club) {
        if (club == null) {
            return;
        }
        FriendlyArranger friendlies = new FriendlyArranger(game);
        friendlies.cancelClashes(club);
        friendlies.answerProposals(club);
    }
    
    /** Books a share of a match's gate to a club: its gate entry, balance and period income (P2-12: once a day). */
    private void book(Match match, Club club, int tickets, BigDecimal cash) {
        recordMatchIncome(match, club, tickets, cash);
        club.setClubBalance(club.getClubBalance().add(cash));
        club.setSeasonIncome(club.getSeasonIncome().add(cash));
        club.setMonthIncome(club.getMonthIncome().add(cash));
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