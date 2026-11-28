package com.rndmodgames.futtoboru.engine.temporal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;

/**
 * P2-3: a cup tie of a later round sits in every entrant's list until its clubs are known. Only the two clubs
 * of a tie sell tickets for it, and a tie whose clubs are not known yet sells none (it used to log a
 * "cannot find club" error every day). P2-12: of those two, only the home club sells tickets and books the gate,
 * for league and cup matches as for friendlies (U-08).
 */
class TicketSalesTests extends BaseTest {

    @Test
    void onlyTheClubsOfATieSellTickets() {
        EngineTestWorld world = new EngineTestWorld(application, 3);
        Club home = world.club(0);
        Club away = world.club(1);
        Club other = world.club(2);
        for (Club c : world.clubs) {
            c.setClubBalance(BigDecimal.ZERO);
        }

        Match tie = world.leagueMatch(home, away);
        tie.setMatchType(Match.CUP_MATCH);
        tie.setMatchDateTime(world.game.getGameDate().plusDays(3));
        other.getScheduledMatches().add(tie); // as AuthorityManager.generateCupDraw does for later rounds

        MatchScheduler scheduler = new MatchScheduler(application);
        scheduler.checkClubSheduledMatches(other);
        assertEquals(0, BigDecimal.ZERO.compareTo(other.getSeasonIncome()), "a club not in the tie sells nothing");
        assertEquals(0, tie.getAttendance());

        scheduler.checkClubSheduledMatches(home);
        assertTrue(home.getSeasonIncome().signum() > 0, "the home club sells tickets");
        assertTrue(tie.getAttendance() > 0);
    }

    @Test
    void anUndecidedTieSellsNothing() {
        EngineTestWorld world = new EngineTestWorld(application, 2);
        Club club = world.club(0);
        club.setClubBalance(BigDecimal.ZERO);
        Match tie = world.leagueMatch(club, world.club(1));
        tie.setMatchType(Match.CUP_MATCH);
        tie.setAwayClubId(null); // winner of an earlier tie, not known yet
        tie.setMatchDateTime(world.game.getGameDate().plusDays(2));

        new MatchScheduler(application).checkClubSheduledMatches(club);
        assertEquals(0, BigDecimal.ZERO.compareTo(club.getSeasonIncome()));
        assertEquals(0, tie.getAttendance());
    }

    @Test
    void onlyTheHomeClubSellsLeagueAndCupTickets() {
        for (int type : new int[] { Match.LEAGUE_MATCH, Match.CUP_MATCH }) {
            EngineTestWorld world = new EngineTestWorld(application, 2);
            Club home = world.club(0);
            Club away = world.club(1);
            for (Club c : world.clubs) {
                c.setClubBalance(BigDecimal.ZERO);
            }
            Match match = world.leagueMatch(home, away);
            match.setMatchType(type);
            match.setMatchDateTime(world.game.getGameDate().plusDays(3));

            MatchScheduler scheduler = new MatchScheduler(application);
            scheduler.checkClubSheduledMatches(away);
            assertEquals(0, BigDecimal.ZERO.compareTo(away.getClubBalance()), type + ": away balance");
            assertEquals(0, BigDecimal.ZERO.compareTo(away.getSeasonIncome()), type + ": away season income");
            assertEquals(0, BigDecimal.ZERO.compareTo(away.getMonthIncome()), type + ": away month income");
            assertTrue(away.getMatchIncomes().isEmpty(), type + ": away gate entry");
            assertEquals(0, match.getAttendance(), type + ": the away club sold no tickets");
            assertEquals(0, BigDecimal.ZERO.compareTo(match.getMatchRevenue()), type + ": match revenue");

            scheduler.checkClubSheduledMatches(home);
            scheduler.checkClubSheduledMatches(away);
            assertEquals(1, home.getMatchIncomes().size(), type + ": one home gate entry");
            assertEquals(0, match.getMatchRevenue().compareTo(home.getMatchIncomes().get(0).getTicketRevenue()),
                    type + ": the home entry holds the whole gate");
            assertEquals(0, match.getMatchRevenue().compareTo(home.getClubBalance()), type + ": home balance");
            assertTrue(match.getMatchRevenue().signum() > 0, type + ": the home club sold tickets");
            assertEquals(match.getAttendance(), home.getMatchIncomes().get(0).getAttendance(), type + ": attendance");
            assertTrue(away.getMatchIncomes().isEmpty(), type + ": still no away gate entry");
            assertEquals(0, BigDecimal.ZERO.compareTo(away.getClubBalance()), type + ": away balance untouched");
        }
    }
}
