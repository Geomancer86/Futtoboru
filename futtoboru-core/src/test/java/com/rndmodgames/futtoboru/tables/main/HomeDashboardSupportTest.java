package com.rndmodgames.futtoboru.tables.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;

/**
 * Fast, VisUI-free unit tests for the pure logic behind the Home/Overview dashboard
 * ({@link HomeDashboardSupport}). See its class-level javadoc for why this logic is split out of
 * {@link HomeScreenTable} into its own testable class.
 */
class HomeDashboardSupportTest {

    private static Club club(long id) {
        Club club = new Club("Club " + id);
        club.setId(id);
        return club;
    }

    private static Match match(Long homeClubId, Long awayClubId, LocalDateTime dateTime,
            boolean played, Integer homeGoals, Integer awayGoals, Integer matchType) {
        Match match = new Match();
        match.setHomeClubId(homeClubId);
        match.setAwayClubId(awayClubId);
        match.setMatchDateTime(dateTime);
        match.setIsPlayed(played);
        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);
        match.setMatchType(matchType);
        return match;
    }

    // -- findNextFixture --------------------------------------------------------------------

    @Test
    void findNextFixture_picksEarliestUnplayedMatchOnOrAfterGameDate() {
        Club home = club(1);
        LocalDateTime now = LocalDateTime.of(1888, 9, 1, 15, 0);

        Match past = match(1L, 2L, now.minusDays(3), false, null, null, Match.LEAGUE_MATCH);
        Match soonest = match(1L, 3L, now.plusDays(2), false, null, null, Match.LEAGUE_MATCH);
        Match later = match(1L, 4L, now.plusDays(9), false, null, null, Match.LEAGUE_MATCH);
        Match alreadyPlayed = match(1L, 5L, now.plusDays(1), true, 2, 0, Match.LEAGUE_MATCH);

        home.setScheduledMatches(new ArrayList<>(Arrays.asList(past, later, alreadyPlayed, soonest)));

        assertSame(soonest, HomeDashboardSupport.findNextFixture(home, now));
    }

    @Test
    void findNextFixture_returnsNullWhenNoUpcomingMatches() {
        Club home = club(1);
        LocalDateTime now = LocalDateTime.of(1888, 9, 1, 15, 0);
        Match past = match(1L, 2L, now.minusDays(3), false, null, null, Match.LEAGUE_MATCH);
        home.setScheduledMatches(new ArrayList<>(List.of(past)));

        assertNull(HomeDashboardSupport.findNextFixture(home, now));
    }

    @Test
    void findNextFixture_nullClubOrDateReturnsNull() {
        assertNull(HomeDashboardSupport.findNextFixture(null, LocalDateTime.now()));
        assertNull(HomeDashboardSupport.findNextFixture(club(1), null));
    }

    // -- collectRecentPlayedMatches ----------------------------------------------------------

    @Test
    void collectRecentPlayedMatches_returnsLastNInChronologicalOrderExcludingFriendlies() {
        Club home = club(1);
        LocalDateTime base = LocalDateTime.of(1888, 9, 1, 15, 0);

        List<Match> played = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            played.add(match(1L, 2L, base.plusDays(i), true, 1, 0, Match.LEAGUE_MATCH));
        }
        // A friendly slipped in at the very end should be skipped entirely, not counted.
        Match friendly = match(1L, 2L, base.plusDays(20), true, 5, 0, Match.FRIENDLY_MATCH);
        played.add(friendly);
        home.setPlayedMatches(played);

        List<Match> recent = HomeDashboardSupport.collectRecentPlayedMatches(home, 5);

        assertEquals(5, recent.size());
        // oldest-first ordering: index i should be played.get(3+i) for i in [0,5)
        for (int i = 0; i < 5; i++) {
            assertSame(played.get(3 + i), recent.get(i));
        }
    }

    @Test
    void collectRecentPlayedMatches_emptyWhenClubOrHistoryIsNull() {
        assertEquals(0, HomeDashboardSupport.collectRecentPlayedMatches(null, 5).size());
        assertEquals(0, HomeDashboardSupport.collectRecentPlayedMatches(club(1), 5).size());
    }

    // -- resultLetter -------------------------------------------------------------------------

    @Test
    void resultLetter_homeWinIsWForHomeClubAndLForAwayClub() {
        Club home = club(1);
        Club away = club(2);
        Match m = match(1L, 2L, LocalDateTime.now(), true, 3, 1, Match.LEAGUE_MATCH);

        assertEquals('W', HomeDashboardSupport.resultLetter(home, m));
        assertEquals('L', HomeDashboardSupport.resultLetter(away, m));
    }

    @Test
    void resultLetter_drawIsDForBothClubs() {
        Club home = club(1);
        Club away = club(2);
        Match m = match(1L, 2L, LocalDateTime.now(), true, 1, 1, Match.LEAGUE_MATCH);

        assertEquals('D', HomeDashboardSupport.resultLetter(home, m));
        assertEquals('D', HomeDashboardSupport.resultLetter(away, m));
    }

    @Test
    void resultLetter_awayWinIsWForAwayClubAndLForHomeClub() {
        Club home = club(1);
        Club away = club(2);
        Match m = match(1L, 2L, LocalDateTime.now(), true, 0, 2, Match.LEAGUE_MATCH);

        assertEquals('L', HomeDashboardSupport.resultLetter(home, m));
        assertEquals('W', HomeDashboardSupport.resultLetter(away, m));
    }

    // -- positionOf ---------------------------------------------------------------------------

    @Test
    void positionOf_returnsOneBasedIndexMatchingById() {
        Club first = club(10);
        Club second = club(20);
        Club third = club(30);
        List<Club> standings = List.of(first, second, third);

        assertEquals(1, HomeDashboardSupport.positionOf(first, standings));
        assertEquals(2, HomeDashboardSupport.positionOf(second, standings));
        assertEquals(3, HomeDashboardSupport.positionOf(third, standings));
    }

    @Test
    void positionOf_returnsMinusOneWhenClubNotInStandings() {
        List<Club> standings = List.of(club(10), club(20));
        assertEquals(-1, HomeDashboardSupport.positionOf(club(99), standings));
    }

    @Test
    void positionOf_returnsMinusOneForNullInputs() {
        assertEquals(-1, HomeDashboardSupport.positionOf(null, List.of(club(1))));
        assertEquals(-1, HomeDashboardSupport.positionOf(club(1), null));
    }

    // -- ordinal -------------------------------------------------------------------------------

    @Test
    void ordinal_standardSuffixes() {
        assertEquals("1st", HomeDashboardSupport.ordinal(1));
        assertEquals("2nd", HomeDashboardSupport.ordinal(2));
        assertEquals("3rd", HomeDashboardSupport.ordinal(3));
        assertEquals("4th", HomeDashboardSupport.ordinal(4));
        assertEquals("10th", HomeDashboardSupport.ordinal(10));
    }

    @Test
    void ordinal_elevenTwelveThirteenAreAlwaysTh() {
        assertEquals("11th", HomeDashboardSupport.ordinal(11));
        assertEquals("12th", HomeDashboardSupport.ordinal(12));
        assertEquals("13th", HomeDashboardSupport.ordinal(13));
    }

    @Test
    void ordinal_twentyOneIsStAgain() {
        assertEquals("21st", HomeDashboardSupport.ordinal(21));
        assertEquals("22nd", HomeDashboardSupport.ordinal(22));
        assertEquals("23rd", HomeDashboardSupport.ordinal(23));
    }
}
