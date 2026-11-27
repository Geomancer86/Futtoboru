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
 * "cannot find club" error every day).
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
}
