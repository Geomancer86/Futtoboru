package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.loaders.DerbyRegistry;

/**
 * FM-inspired attendance model (MVP-4): table position, opponent, form, derby.
 *
 * @see PM_V1_MVP_AND_1892_MASTER_PLAN.md §5.3
 */
public final class AttendanceCalculator {

    public static final double MIN_FILL = 0.08;
    public static final double MAX_FILL = 0.98;

    private AttendanceCalculator() {
    }

    public static int calculate(SaveGame save, Match match, Club homeClub, Club awayClub) {
        if (homeClub == null || homeClub.getStadium() == null || homeClub.getStadium().getCapacity() == null) {
            return defaultAttendance(match);
        }
        int capacity = homeClub.getStadium().getCapacity();
        AttendanceInputs inputs = buildInputs(save, match, homeClub, awayClub, capacity);
        return calculateFromInputs(inputs);
    }

    static int calculateFromInputs(AttendanceInputs inputs) {
        double fill = inputs.baseFill
                * inputs.positionFactor
                * inputs.opponentFactor
                * inputs.formFactor
                * inputs.derbyFactor;
        fill = clamp(fill, MIN_FILL, MAX_FILL);
        return (int) Math.round(inputs.stadiumCapacity * fill);
    }

    static AttendanceInputs buildInputs(SaveGame save, Match match, Club homeClub, Club awayClub, int capacity) {
        int clubCount = resolveLeagueClubCount(save, match, homeClub);
        int homeRank = resolveLeagueRank(save, match, homeClub, clubCount);
        int awayRank = resolveLeagueRank(save, match, awayClub, clubCount);
        double formRate = computeFormRate(homeClub);
        boolean derby = DerbyRegistry.isDerby(homeClub.getId(), awayClub != null ? awayClub.getId() : null);

        return new AttendanceInputs(
                capacity,
                baseFill(match != null ? match.getMatchType() : null),
                positionFactor(homeRank, clubCount),
                opponentFactor(awayRank),
                formFactor(formRate),
                derby ? 1.18 : 1.0);
    }

    static double baseFill(Integer matchType) {
        if (matchType == null) {
            return 0.55;
        }
        switch (matchType) {
            case Match.FRIENDLY_MATCH:
                return 0.12;
            case Match.CUP_MATCH:
                return 0.65;
            case Match.LEAGUE_MATCH:
            default:
                return 0.55;
        }
    }

    static double positionFactor(int rank, int clubCount) {
        if (clubCount < 2 || rank < 1) {
            return 1.0;
        }
        double topBonus = 0.12;
        double bottomPenalty = 0.08;
        double span = topBonus + bottomPenalty;
        double step = span / (clubCount - 1);
        return 1.12 - (rank - 1) * step;
    }

    static double opponentFactor(int awayRank) {
        if (awayRank >= 1 && awayRank <= 3) {
            return 1.10;
        }
        return 1.0;
    }

    static double formFactor(double formRate) {
        if (formRate >= 0.8) {
            return 1.08;
        }
        if (formRate <= 0.2) {
            return 0.90;
        }
        return 1.0;
    }

    /**
     * How many of the most-recently-played matches to look at when hunting for the last 5
     * non-friendly results. Generous enough to skip past the occasional friendly without missing
     * a real result, while staying a small constant regardless of career length.
     */
    private static final int RECENT_FORM_SCAN_WINDOW = 20;

    static double computeFormRate(Club club) {
        if (club == null) {
            return 0.5;
        }
        // playedMatches is appended to in the order matches are finalized (chronological), and
        // is never trimmed across a career, so it can hold thousands of entries by late seasons.
        // The old code copied the *entire* list plus scheduledMatches into a new ArrayList and
        // sorted it by date on every single call (once per match simulated) just to read the
        // last 5 results — found 2026-07-15 as the dominant remaining late-soak slowdown (worse
        // than a plain scan because of the O(n log n) sort). Since the list is already in
        // chronological order, we only need its tail — no copy of old history, no sort of it.
        List<Match> recent = new ArrayList<>();
        collectRecentResults(club.getScheduledMatches(), recent);
        List<Match> played = club.getPlayedMatches();
        if (played != null) {
            int tailStart = Math.max(0, played.size() - RECENT_FORM_SCAN_WINDOW);
            for (int i = tailStart; i < played.size(); i++) {
                recent.add(played.get(i));
            }
        }
        recent.removeIf(m -> m == null || !Boolean.TRUE.equals(m.getIsPlayed()));
        recent.sort(Comparator.comparing(Match::getMatchDateTime, Comparator.nullsLast(Comparator.reverseOrder())));

        int points = 0;
        int maxPoints = 0;
        int counted = 0;
        for (Match match : recent) {
            if (counted >= 5) {
                break;
            }
            if (match.getMatchType() == null || match.getMatchType() == Match.FRIENDLY_MATCH) {
                continue;
            }
            Long clubId = club.getId();
            if (clubId == null) {
                continue;
            }
            int home = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
            int away = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
            boolean isHome = clubId.equals(match.getHomeClubId());
            if (home == away) {
                points += 1;
            } else if ((isHome && home > away) || (!isHome && away > home)) {
                points += 2;
            }
            maxPoints += 2;
            counted++;
        }
        if (maxPoints == 0) {
            return 0.5;
        }
        return (double) points / (double) maxPoints;
    }

    private static void collectRecentResults(List<Match> source, List<Match> target) {
        if (source != null) {
            target.addAll(source);
        }
    }

    static int resolveLeagueRank(SaveGame save, Match match, Club club, int clubCount) {
        if (club == null || club.getId() == null || clubCount < 1) {
            return (clubCount + 1) / 2;
        }
        League league = findLeagueForMatch(save, match, club);
        if (league == null) {
            return (clubCount + 1) / 2;
        }
        List<Club> standings = new LeagueStandingsManager().calculateStandings(league);
        for (int i = 0; i < standings.size(); i++) {
            Club row = standings.get(i);
            if (row != null && club.getId().equals(row.getId())) {
                return i + 1;
            }
        }
        return (clubCount + 1) / 2;
    }

    static int resolveLeagueClubCount(SaveGame save, Match match, Club homeClub) {
        League league = findLeagueForMatch(save, match, homeClub);
        if (league == null || league.getLeagueClubs() == null) {
            return 12;
        }
        return Math.max(2, league.getLeagueClubs().size());
    }

    private static League findLeagueForMatch(SaveGame save, Match match, Club homeClub) {
        if (save == null || save.getMainAuthority() == null || save.getMainAuthority().getLeagues() == null) {
            return null;
        }
        Long homeId = homeClub != null ? homeClub.getId() : match != null ? match.getHomeClubId() : null;
        Long awayId = match != null ? match.getAwayClubId() : null;
        for (League league : save.getMainAuthority().getLeagues()) {
            if (league == null || league.getLeagueClubs() == null) {
                continue;
            }
            boolean hasHome = false;
            boolean hasAway = awayId == null;
            for (Club c : league.getLeagueClubs()) {
                if (c == null || c.getId() == null) {
                    continue;
                }
                if (c.getId().equals(homeId)) {
                    hasHome = true;
                }
                if (awayId != null && c.getId().equals(awayId)) {
                    hasAway = true;
                }
            }
            if (hasHome && hasAway) {
                return league;
            }
        }
        if (homeClub != null && homeClub.getCurrentLeague() != null) {
            return homeClub.getCurrentLeague();
        }
        return save.getMainAuthority().getLeagues().isEmpty()
                ? null : save.getMainAuthority().getLeagues().get(0);
    }

    private static int defaultAttendance(Match match) {
        if (match != null && match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH) {
            return 600;
        }
        return 3000;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class AttendanceInputs {
        final int stadiumCapacity;
        final double baseFill;
        final double positionFactor;
        final double opponentFactor;
        final double formFactor;
        final double derbyFactor;

        AttendanceInputs(int stadiumCapacity, double baseFill, double positionFactor,
                double opponentFactor, double formFactor, double derbyFactor) {
            this.stadiumCapacity = stadiumCapacity;
            this.baseFill = baseFill;
            this.positionFactor = positionFactor;
            this.opponentFactor = opponentFactor;
            this.formFactor = formFactor;
            this.derbyFactor = derbyFactor;
        }
    }
}
