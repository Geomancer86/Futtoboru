package com.rndmodgames.futtoboru.tables.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.system.SaveGame;

/** U-04c: the Home dashboard data, read from a small hand-built game (no GL, no database). */
class HomeDashboardTests {

    private static final LocalDateTime TODAY = LocalDateTime.of(1888, 9, 8, 9, 0);

    private SaveGame game;
    private Person owner;
    private Club preston;
    private Club bolton;
    private Club derby;
    private League league;

    @BeforeEach
    void setUp() {
        game = new SaveGame();
        game.setGameDate(TODAY);

        owner = new Person();
        owner.setId(1_000_001L);
        owner.setName("William");
        owner.setLastname("Sudell");
        game.setOwner(owner);

        preston = club(1L, "Preston North End", 5, 13, new BigDecimal("125.50"));
        bolton = club(2L, "Bolton Wanderers", 5, 4, BigDecimal.ZERO);
        derby = club(3L, "Derby County", 4, 7, BigDecimal.ZERO);
        game.getAllClubs().addAll(List.of(preston, bolton, derby));

        league = new League();
        league.setId(10L);
        league.setName("Football League");
        league.setLeagueClubs(new ArrayList<>(List.of(bolton, derby, preston)));
        for (Club club : game.getAllClubs()) {
            club.setCurrentLeague(league);
        }
        Competition cup = new Competition();
        cup.setId(20L);
        cup.setName("FA Cup");
        game.getAllCups().add(cup);
    }

    private static Club club(long id, String name, int played, int points, BigDecimal balance) {
        Club club = new Club(name);
        club.setId(id);
        club.setMatchesPlayed(played);
        club.setPoints(points);
        club.setClubBalance(balance);
        club.setScheduledMatches(new ArrayList<>());
        club.setPlayedMatches(new ArrayList<>());
        return club;
    }

    private static Match match(long id, Club home, Club away, LocalDateTime date, int type, Long competitionId) {
        Match match = new Match();
        match.setId(id);
        match.setHomeClubId(home != null ? home.getId() : null);
        match.setAwayClubId(away != null ? away.getId() : null);
        match.setMatchDateTime(date);
        match.setMatchType(type);
        match.setCompetitionId(competitionId);
        match.setIsPlayed(false);
        return match;
    }

    private void employ() {
        owner.setCurrentClubId(preston.getId());
    }

    @Test
    void managerSeesClubLeagueAndBalance() {
        employ();
        HomeDashboard dashboard = HomeDashboard.of(game);
        assertTrue(dashboard.hasClub());
        assertEquals("Preston North End", dashboard.getClubName());
        assertEquals("William Sudell", dashboard.getManagerName());
        assertEquals(TODAY, dashboard.getDate());
        assertEquals(new BigDecimal("125.50"), dashboard.getBalance());

        HomeDashboard.LeaguePosition position = dashboard.getLeaguePosition();
        assertEquals("Football League", position.league());
        assertEquals(1, position.position());
        assertEquals(3, position.clubs());
        assertEquals(5, position.played());
        assertEquals(13, position.points());
    }

    @Test
    void nextFixtureIsTheEarliestUnplayedFromToday() {
        employ();
        Match yesterday = match(1, preston, bolton, TODAY.minusDays(1), Match.LEAGUE_MATCH, 10L);
        Match played = match(2, derby, preston, TODAY, Match.LEAGUE_MATCH, 10L);
        played.setIsPlayed(true);
        Match later = match(3, preston, derby, TODAY.plusDays(14), Match.LEAGUE_MATCH, 10L);
        Match next = match(4, bolton, preston, TODAY.plusDays(7).withHour(15), Match.CUP_MATCH, 20L);
        preston.getScheduledMatches().addAll(List.of(later, yesterday, played, next));

        HomeDashboard.Fixture fixture = HomeDashboard.of(game).getNextFixture();
        assertEquals("Bolton Wanderers", fixture.opponent());
        assertFalse(fixture.home());
        assertEquals(Match.CUP_MATCH, fixture.matchType());
        assertEquals("FA Cup", fixture.competition());
        assertEquals(7, fixture.daysUntil());
    }

    @Test
    void fixtureTodayAndUndrawnOpponent() {
        employ();
        preston.getScheduledMatches().add(match(5, preston, null, TODAY.withHour(15), Match.CUP_MATCH, 99L));
        HomeDashboard.Fixture fixture = HomeDashboard.of(game).getNextFixture();
        assertTrue(fixture.home());
        assertNull(fixture.opponent());
        assertNull(fixture.competition());
        assertEquals(0, fixture.daysUntil());
    }

    @Test
    void lastResultFromTheClubsView() {
        employ();
        Match older = match(6, preston, derby, TODAY.minusDays(14), Match.LEAGUE_MATCH, 10L);
        older.setIsPlayed(true);
        older.setHomeGoals(5);
        older.setAwayGoals(2);
        Match latest = match(7, bolton, preston, TODAY.minusDays(7), Match.LEAGUE_MATCH, 10L);
        latest.setIsPlayed(true);
        latest.setHomeGoals(3);
        latest.setAwayGoals(1);
        preston.getPlayedMatches().addAll(List.of(older, latest));

        HomeDashboard.Result result = HomeDashboard.of(game).getLastResult();
        assertEquals("Bolton Wanderers", result.opponent());
        assertFalse(result.home());
        assertEquals(1, result.goalsFor());
        assertEquals(3, result.goalsAgainst());
    }

    @Test
    void unreadMessagesAndThoseNeedingAction() {
        Message read = new Message();
        read.setIsRead(true);
        Message unread = new Message();
        unread.setIsRead(false);
        Message neverOpened = new Message();
        Message draw = new Message();
        draw.setIsMandatory(true);
        game.getAllMessages().addAll(List.of(read, unread, neverOpened, draw));

        HomeDashboard dashboard = HomeDashboard.of(game);
        assertEquals(3, dashboard.getUnreadMessages());
        assertEquals(1, dashboard.getUnreadNeedingAction());
    }

    @Test
    void jobSeekerHasNoClubCardsButApplications() {
        JobApplication waiting = new JobApplication();
        waiting.setApplicantId(owner.getId());
        waiting.setStatus(ApplicationStatus.PENDING);
        JobApplication rejected = new JobApplication();
        rejected.setApplicantId(owner.getId());
        rejected.setStatus(ApplicationStatus.REJECTED);
        JobApplication someoneElse = new JobApplication();
        someoneElse.setApplicantId(42L);
        someoneElse.setStatus(ApplicationStatus.PENDING);
        game.getAllApplications().addAll(List.of(waiting, rejected, someoneElse));
        JobOffer offer = new JobOffer();
        offer.setRecipientId(owner.getId());
        offer.setStatus(OfferStatus.PENDING);
        game.getPendingOffers().add(offer);

        HomeDashboard dashboard = HomeDashboard.of(game);
        assertFalse(dashboard.hasClub());
        assertNull(dashboard.getNextFixture());
        assertNull(dashboard.getLeaguePosition());
        assertNull(dashboard.getBalance());
        assertEquals(1, dashboard.getPendingApplications());
        assertEquals(1, dashboard.getPendingOffers());
    }

    @Test
    void clubWithoutLeagueOrMatches() {
        employ();
        for (Club club : game.getAllClubs()) {
            club.setCurrentLeague(null);
        }
        HomeDashboard dashboard = HomeDashboard.of(game);
        assertNull(dashboard.getLeaguePosition());
        assertNull(dashboard.getNextFixture());
        assertNull(dashboard.getLastResult());
    }

    @Test
    void noGame() {
        HomeDashboard dashboard = HomeDashboard.of(null);
        assertFalse(dashboard.hasClub());
        assertEquals(0, dashboard.getUnreadMessages());
    }

    @Test
    void labelPatternsAreFilled() {
        assertEquals("v Bolton (away)", HomeScreenTable.fill("v {0} ({1})", "Bolton", "away"));
        assertEquals("Won 3-1 v Derby", HomeScreenTable.fill("{0} {1}-{2} v {3}", "Won", 3, 1, "Derby"));
    }
}
