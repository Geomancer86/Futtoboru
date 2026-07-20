package com.rndmodgames.futtoboru.tables.main;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;

/**
 * Pure, VisUI-free logic for the Home/Overview dashboard ({@link HomeScreenTable}), pulled out into
 * its own class specifically so it can be unit tested without a graphics context.
 *
 * <p>The current headless test harness ({@code HeadlessGdxTestSupport} / {@code GameTestHarness})
 * boots a real {@code HeadlessApplication} with no GL context, so {@code VisUI.load(...)} never
 * runs there (see {@code Futtoboru.loadVisUiSkin()}'s {@code Gdx.gl == null} guard) -- any class
 * that constructs a {@code VisTable}/{@code VisLabel} directly cannot be exercised by a fast JUnit
 * test today (see {@code docs/planning/ANALYSIS_AGENT_PLAYER_E2E_V1.md}). Keeping the actual
 * decision logic here, instead of inline in {@link HomeScreenTable}, means the parts most likely to
 * have real bugs (which match counts as "next", which letter a result maps to, league position
 * math) are still covered by fast automated tests even though the widget tree itself is not.
 */
final class HomeDashboardSupport {

    private HomeDashboardSupport() {
    }

    /**
     * Earliest not-yet-played scheduled match on or after {@code gameDate} -- a real future
     * fixture, not just "playable today" (see {@code FuttoboruGameEngine.findNextPlayableMatchForClub}
     * for the narrower today-or-earlier variant used by the match preview screen).
     */
    static Match findNextFixture(Club club, LocalDateTime gameDate) {
        if (club == null || gameDate == null) {
            return null;
        }
        List<Match> scheduled = club.getScheduledMatches();
        if (scheduled == null) {
            return null;
        }
        Match best = null;
        for (Match match : scheduled) {
            if (match == null || Boolean.TRUE.equals(match.getIsPlayed()) || match.getMatchDateTime() == null) {
                continue;
            }
            if (match.getMatchDateTime().isBefore(gameDate)) {
                continue;
            }
            if (best == null || match.getMatchDateTime().isBefore(best.getMatchDateTime())) {
                best = match;
            }
        }
        return best;
    }

    /**
     * Last {@code count} *played* non-friendly matches for the club, oldest first, most recent
     * last -- reads only the tail of the already-chronological playedMatches list (no
     * copy-then-sort of full career history; mirrors the perf pattern in
     * {@code AttendanceCalculator.computeFormRate}).
     */
    static List<Match> collectRecentPlayedMatches(Club club, int count) {
        List<Match> result = new ArrayList<>();
        if (club == null) {
            return result;
        }
        List<Match> played = club.getPlayedMatches();
        if (played == null) {
            return result;
        }
        for (int i = played.size() - 1; i >= 0 && result.size() < count; i--) {
            Match match = played.get(i);
            if (match == null || !Boolean.TRUE.equals(match.getIsPlayed())) {
                continue;
            }
            if (match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH) {
                continue;
            }
            result.add(match);
        }
        Collections.reverse(result);
        return result;
    }

    /** 'W'/'D'/'L' from {@code club}'s point of view for an already-played match. */
    static char resultLetter(Club club, Match match) {
        int home = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
        int away = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
        boolean isHome = club.getId() != null && club.getId().equals(match.getHomeClubId());
        if (home == away) {
            return 'D';
        }
        boolean won = (isHome && home > away) || (!isHome && away > home);
        return won ? 'W' : 'L';
    }

    /**
     * 1-based position of {@code club} within {@code standings} (as returned by
     * {@code LeagueStandingsManager.calculateStandings}), or -1 if not found.
     */
    static int positionOf(Club club, List<Club> standings) {
        if (club == null || club.getId() == null || standings == null) {
            return -1;
        }
        int position = 1;
        for (Club candidate : standings) {
            if (candidate != null && candidate.getId() != null && candidate.getId().equals(club.getId())) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /** English ordinal suffix: 1st, 2nd, 3rd, 4th, ..., 11th, 12th, 13th, 21st, ... */
    static String ordinal(int n) {
        if (n % 100 >= 11 && n % 100 <= 13) {
            return n + "th";
        }
        switch (n % 10) {
            case 1: return n + "st";
            case 2: return n + "nd";
            case 3: return n + "rd";
            default: return n + "th";
        }
    }
}
