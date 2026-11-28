package com.rndmodgames.futtoboru.engine.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.cup.CupBracketManager;
import com.rndmodgames.futtoboru.engine.simulation.GoalMinutes;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * What a match report is written from (G-03): read from a played {@link Match} and the game, never stored. Old matches
 * (no goal minutes) and other clubs' matches give facts too.
 *
 * @param goals in minute order when {@code minutesKnown}, else the home goals then the away goals (minute 0)
 * @param competition the league's or cup's name, null for a friendly or when not found
 * @param round the cup round number (0 when not a cup match); {@code roundTies} the ties drawn in that round
 * @param ground the name of the ground played at (P2-14: a neutral venue, else the home club's), or null
 * @param attendance 0 when unknown
 * @param date null when unknown
 */
public record MatchFacts(String matchKey, long seed, String home, String away, int homeGoals, int awayGoals,
        List<Goal> goals, boolean minutesKnown, int matchType, String competition, int round, int roundTies,
        boolean replay, boolean cupFinal, String ground, int attendance, LocalDate date) {

    /** One goal: its minute (0 when unknown), its side and its scorer (null when not recorded). */
    public record Goal(int minute, boolean home, String scorer) {
    }

    /** @return the facts of a played match of {@code game} */
    public static MatchFacts of(SaveGame game, Match match) {
        Club homeClub = game.getClubById(match.getHomeClubId());
        Club awayClub = game.getClubById(match.getAwayClubId());
        String home = homeClub != null && homeClub.getName() != null ? homeClub.getName() : "?";
        String away = awayClub != null && awayClub.getName() != null ? awayClub.getName() : "?";
        int hg = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
        int ag = match.getAwayGoals() != null ? match.getAwayGoals() : 0;

        boolean known = GoalMinutes.hasMinutes(match);
        List<Goal> goals = new ArrayList<>();
        addSide(goals, game, match, true, hg, known, homeClub, awayClub);
        addSide(goals, game, match, false, ag, known, homeClub, awayClub);
        if (known) {
            goals.sort((x, y) -> Integer.compare(x.minute(), y.minute()));
        }

        int type = match.getMatchType() != null ? match.getMatchType() : Match.FRIENDLY_MATCH;
        String competition = null;
        int round = 0;
        int roundTies = 0;
        boolean replay = false;
        boolean cupFinal = false;
        if (type == Match.LEAGUE_MATCH) {
            League league = leagueOf(game, match);
            competition = league != null ? league.getName() : null;
        } else if (type == Match.CUP_MATCH) {
            Competition cup = cupOf(game, match);
            competition = cup != null ? cup.getName() : null;
            replay = isReplay(match);
            Match tie = replay ? matchById(game, match.getParentMatch1Id()) : match;
            if (tie == null) {
                tie = match;
            }
            cupFinal = new CupBracketManager(game).isBracketFinal(tie);
            round = match.getRound() != null ? match.getRound() : 0;
            roundTies = tiesInRound(game, match);
        }
        // P2-14: the ground played at: the match's neutral venue (a cup semi-final or final), else the home club's
        com.rndmodgames.futtoboru.data.Stadium venue =
                com.rndmodgames.futtoboru.engine.cup.NeutralVenues.stadiumOf(match, game);
        if (venue == null && homeClub != null) {
            venue = homeClub.getStadium();
        }
        String ground = venue != null ? venue.getName() : null;
        if (ground != null && ground.isBlank()) {
            ground = null;
        }
        int attendance = match.getAttendance() != null ? Math.max(0, match.getAttendance()) : 0;
        LocalDate date = match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null;
        long seed = game.getSeed() != null ? game.getSeed() : 0L;
        return new MatchFacts(key(match), seed, home, away, hg, ag, Collections.unmodifiableList(goals), known, type,
                competition, round, roundTies, replay, cupFinal, ground, attendance, date);
    }

    /** Number of ties (replays left out) of the match's round: its round name needs it. */
    static int tiesInRound(SaveGame game, Match match) {
        if (match.getCompetitionEditionId() == null || match.getRound() == null) {
            return 0;
        }
        int ties = 0;
        for (Match m : editionMatches(game, match.getCompetitionEditionId())) {
            if (match.getRound().equals(m.getRound()) && !isReplay(m)) {
                ties++;
            }
        }
        return ties;
    }

    /** The key a match's random streams are named by (the one {@code MatchSimulator} uses). */
    public static String key(Match match) {
        return match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();
    }

    private static void addSide(List<Goal> goals, SaveGame game, Match match, boolean home, int count, boolean known,
            Club homeClub, Club awayClub) {
        List<Long> scorers = home ? match.getHomeScorerIds() : match.getAwayScorerIds();
        List<Integer> minutes = home ? match.getHomeGoalMinutes() : match.getAwayGoalMinutes();
        Club own = home ? homeClub : awayClub;
        Club other = home ? awayClub : homeClub;
        for (int i = 0; i < count; i++) {
            Long id = scorers != null && i < scorers.size() ? scorers.get(i) : null;
            int minute = known && i < minutes.size() && minutes.get(i) != null ? minutes.get(i) : 0;
            goals.add(new Goal(minute, home, surname(game, id, own, other)));
        }
    }

    /** The scorer's surname: his club's players first, then every club, then the game's people. */
    private static String surname(SaveGame game, Long personId, Club own, Club other) {
        if (personId == null) {
            return null;
        }
        Person person = findIn(own, personId);
        if (person == null) {
            person = findIn(other, personId);
        }
        if (person == null && game.getAllClubs() != null) {
            for (Club club : game.getAllClubs()) {
                person = findIn(club, personId);
                if (person != null) {
                    break;
                }
            }
        }
        if (person == null && game.getAllPersons() != null) {
            for (Person p : game.getAllPersons()) {
                if (p != null && personId.equals(p.getId())) {
                    person = p;
                    break;
                }
            }
        }
        if (person == null) {
            return null;
        }
        if (person.getLastname() != null && !person.getLastname().isBlank()) {
            return person.getLastname().trim();
        }
        return person.getName() != null && !person.getName().isBlank() ? person.getName().trim() : null;
    }

    private static Person findIn(Club club, Long personId) {
        if (club == null || club.getPlayers() == null) {
            return null;
        }
        for (Player player : club.getPlayers()) {
            if (player != null && player.getPerson() != null && personId.equals(player.getPerson().getId())) {
                return player.getPerson();
            }
        }
        return null;
    }

    private static League leagueOf(SaveGame game, Match match) {
        if (game.getMainAuthority() == null || game.getMainAuthority().getLeagues() == null) {
            return null;
        }
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league == null || league.getLeagueClubs() == null) {
                continue;
            }
            boolean hasHome = false;
            boolean hasAway = false;
            for (Club club : league.getLeagueClubs()) {
                if (club != null && club.getId() != null) {
                    hasHome |= club.getId().equals(match.getHomeClubId());
                    hasAway |= club.getId().equals(match.getAwayClubId());
                }
            }
            if (hasHome && hasAway) {
                return league;
            }
        }
        return null;
    }

    private static Competition cupOf(SaveGame game, Match match) {
        if (match.getCompetitionId() == null || game.getAllCups() == null) {
            return null;
        }
        for (Competition cup : game.getAllCups()) {
            if (cup != null && match.getCompetitionId().equals(cup.getId())) {
                return cup;
            }
        }
        return null;
    }

    static boolean isReplay(Match m) {
        return m.getParentMatch1Id() != null && m.getBracketPath() != null && m.getBracketPath().endsWith("R");
    }

    private static Match matchById(SaveGame game, Long id) {
        if (id == null || game.getAllClubs() == null) {
            return null;
        }
        for (Club club : game.getAllClubs()) {
            if (club == null) {
                continue;
            }
            for (List<Match> list : java.util.Arrays.asList(club.getPlayedMatches(), club.getScheduledMatches())) {
                if (list == null) {
                    continue;
                }
                for (Match m : list) {
                    if (m != null && id.equals(m.getId())) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private static List<Match> editionMatches(SaveGame game, Long editionId) {
        List<Match> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        if (game.getAllClubs() == null) {
            return out;
        }
        for (Club club : game.getAllClubs()) {
            if (club == null) {
                continue;
            }
            for (List<Match> list : java.util.Arrays.asList(club.getPlayedMatches(), club.getScheduledMatches())) {
                if (list == null) {
                    continue;
                }
                for (Match m : list) {
                    if (m != null && m.getId() != null && editionId.equals(m.getCompetitionEditionId())
                            && seen.add(m.getId())) {
                        out.add(m);
                    }
                }
            }
        }
        return out;
    }
}
