package com.rndmodgames.futtoboru.engine.cup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import com.badlogic.gdx.utils.JsonValue;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * P2-14: cup ties played on a neutral ground. The rule and the candidate grounds come from the cup's season data
 * (the edition's {@code neutralVenues} in {@code season.json}): 1888-89 played its semi-finals at Bramall Lane and
 * Crewe and the final at Kennington Oval. A later season without its own rule keeps the latest earlier one's.
 *
 * <p>A tie of a neutral round gets its ground when its two clubs are known (when the round is drawn): one of the
 * round's grounds, drawn from {@code fork("venue:" + matchId)}, never a club's own ground nor one another tie of the
 * same round uses that day. The real pairings and grounds per tie are never read (D17). A replay stays at the tie's
 * ground. {@link Match#getVenue()} empty means the home club's ground.
 *
 * <p>The gate at a neutral ground follows the rule's {@link Gate}: in 1888-89 the semi-finals and final were the
 * Association's matches and the FA took the gate (see the data's notes and sources).
 */
public final class NeutralVenues {

    /** Who books the gate of a tie at a neutral ground */
    public enum Gate {
        /** the Football Association keeps it: neither club books anything */
        ASSOCIATION,
        /** the two clubs share it equally */
        SHARED,
        /** the club drawn at home books it, as at its own ground */
        HOME;

        static Gate of(String dataValue) {
            return dataValue == null ? HOME : Gate.valueOf(dataValue.toUpperCase(Locale.ROOT));
        }
    }

    /** The neutral rounds of one cup season: round name ({@code final}, {@code semi-final}...) -> grounds */
    public static final class Rules {
        final Map<String, List<String>> grounds;
        final Gate gate;

        public Rules(Map<String, List<String>> grounds, Gate gate) {
            this.grounds = Collections.unmodifiableMap(new LinkedHashMap<>(grounds));
            this.gate = gate;
        }

        public Gate getGate() {
            return gate;
        }

        /** @return the candidate grounds of a round, or an empty list when the round is played at home grounds */
        public List<String> groundsFor(String roundName) {
            List<String> g = roundName == null ? null : grounds.get(roundName);
            return g == null ? Collections.emptyList() : g;
        }
    }

    /** Competition data id -> season start year -> rules (installed by DatabaseLoader from the game data) */
    private static volatile Map<String, NavigableMap<Integer, Rules>> registry = new HashMap<>();

    private NeutralVenues() {
    }

    public static void install(Map<String, NavigableMap<Integer, Rules>> rules) {
        Map<String, NavigableMap<Integer, Rules>> copy = new HashMap<>();
        for (Map.Entry<String, NavigableMap<Integer, Rules>> e : rules.entrySet()) {
            copy.put(e.getKey(), new TreeMap<>(e.getValue()));
        }
        registry = copy;
    }

    /** The rules installed now (tests restore them after installing their own) */
    static Map<String, NavigableMap<Integer, Rules>> installed() {
        return new HashMap<>(registry);
    }

    /** The rules of a cup's season: its own, else the latest earlier season's, else null (no neutral rounds) */
    public static Rules rulesFor(String competitionDataId, int startYear) {
        NavigableMap<Integer, Rules> bySeason = competitionDataId == null ? null : registry.get(competitionDataId);
        Map.Entry<Integer, Rules> e = bySeason == null ? null : bySeason.floorEntry(startYear);
        return e == null ? null : e.getValue();
    }

    /** Reads an edition's {@code neutralVenues} (checked by GameData); {@code groundRefs} gives round i's grounds. */
    public static Rules read(JsonValue neutralVenues, Function<Integer, List<String>> groundRefs) {
        Map<String, List<String>> grounds = new LinkedHashMap<>();
        JsonValue rounds = neutralVenues.get("rounds");
        int i = 0;
        for (JsonValue r = rounds == null ? null : rounds.child; r != null; r = r.next, i++) {
            List<String> ids = groundRefs.apply(i);
            if (ids != null && !ids.isEmpty()) {
                grounds.put(r.getString("round"), new ArrayList<>(ids));
            }
        }
        return new Rules(grounds, Gate.of(neutralVenues.getString("gate", null)));
    }

    // ------------------------------------------------------------------------------------------- rounds

    /** The name of a round counted back from the last: {@code final}, {@code semi-final}, {@code quarter-final} */
    public static String roundName(int round, int lastRound) {
        switch (lastRound - round) {
        case 0:
            return "final";
        case 1:
            return "semi-final";
        case 2:
            return "quarter-final";
        default:
            return null;
        }
    }

    /** True when the match is played on a neutral ground (it has a venue) */
    public static boolean isNeutral(Match match) {
        return match != null && match.getVenue() != null && !match.getVenue().isBlank();
    }

    /**
     * The ground a match is played at: its neutral venue, else the home club's season ground (an older save's
     * match has no venue: the home ground). Null when neither is known.
     */
    public static com.rndmodgames.futtoboru.data.Stadium stadiumOf(Match match, SaveGame game) {
        if (match == null) {
            return null;
        }
        if (isNeutral(match)) {
            com.rndmodgames.futtoboru.data.Stadium venue =
                    com.rndmodgames.futtoboru.system.DatabaseLoader.getGroundByDataId(match.getVenue());
            if (venue != null) {
                return venue;
            }
            Log.warn(Log.ENGINE_CUP, "NeutralVenues", "Unknown venue `" + match.getVenue() + "` of match "
                    + match.getId() + "; showing the home ground");
        }
        if (match.getHomeClubId() == null) {
            return null;
        }
        Club home = game != null ? game.getClubById(match.getHomeClubId()) : null;
        if (home == null) {
            home = com.rndmodgames.futtoboru.system.DatabaseLoader.getClubById(match.getHomeClubId());
        }
        return home != null ? home.getStadium() : null;
    }

    /**
     * Gives a tie of a neutral round its ground, if its rules have one. Does nothing for a tie that already has a
     * venue, whose clubs are not both known, or whose round is played at home grounds.
     *
     * @param sameRound the other ties of the edition (grounds already taken on the tie's day are avoided)
     * @param lastRound the edition's last round (the final)
     * @return true when the tie got a venue
     */
    public static boolean assign(SaveGame game, Competition cup, Match tie, Collection<Match> sameRound,
            int lastRound, RandomSource root) {
        if (tie == null || isNeutral(tie) || tie.getHomeClubId() == null || tie.getAwayClubId() == null
                || tie.getRound() == null || cup == null || tie.getMatchDateTime() == null) {
            return false;
        }
        Rules rules = rulesFor(cup.getDataId(), SeasonCalendar.startYearOf(tie.getMatchDateTime().toLocalDate()));
        String roundName = roundName(tie.getRound(), lastRound);
        List<String> candidates = rules == null ? Collections.emptyList() : rules.groundsFor(roundName);
        if (candidates.isEmpty()) {
            return false;
        }
        Set<String> avoid = new java.util.HashSet<>();
        avoid.add(homeGround(game, tie.getHomeClubId()));
        avoid.add(homeGround(game, tie.getAwayClubId()));
        Set<String> taken = new java.util.HashSet<>();
        for (Match m : sameRound) {
            if (m != null && m != tie && isNeutral(m) && m.getMatchDateTime() != null
                    && m.getMatchDateTime().toLocalDate().equals(tie.getMatchDateTime().toLocalDate())) {
                taken.add(m.getVenue());
            }
        }
        List<String> pool = new ArrayList<>();
        for (String g : candidates) {
            if (!avoid.contains(g) && !taken.contains(g)) {
                pool.add(g);
            }
        }
        if (pool.isEmpty()) {
            for (String g : candidates) { // every ground is taken or a club's own: at least not a club's own
                if (!avoid.contains(g)) {
                    pool.add(g);
                }
            }
        }
        if (pool.isEmpty()) {
            Log.warn(Log.ENGINE_CUP, "NeutralVenues", "No neutral ground for " + tie.getBracketPath() + ": every "
                    + roundName + " ground is a club's own; played at the home ground");
            return false;
        }
        RandomSource rng = (root != null ? root : RandomSource.seeded(0L)).fork("venue:" + tie.getId());
        tie.setVenue(pool.get(rng.nextInt(pool.size())));
        Log.info(Log.ENGINE_CUP, "NeutralVenues", tie.getBracketPath() + " (" + roundName + ") at " + tie.getVenue());
        return true;
    }

    private static String homeGround(SaveGame game, Long clubId) {
        Club club = game != null ? game.getClubById(clubId) : null;
        if (club == null) {
            club = com.rndmodgames.futtoboru.system.DatabaseLoader.getClubById(clubId);
        }
        return club != null && club.getStadium() != null ? club.getStadium().getDataId() : null;
    }

    // ------------------------------------------------------------------------------------------- gate

    /** The gate rule of a match: {@link Gate#HOME} at a home ground, else its cup season's rule */
    public static Gate gateOf(Match match, SaveGame game) {
        if (!isNeutral(match)) {
            return Gate.HOME;
        }
        String competition = null;
        if (game != null && game.getAllCups() != null) {
            for (Competition c : game.getAllCups()) {
                if (c != null && c.getId() != null && c.getId().equals(match.getCompetitionId())) {
                    competition = c.getDataId();
                    break;
                }
            }
        }
        if (competition == null && match.getCompetitionId() != null) {
            for (Competition c : com.rndmodgames.futtoboru.system.DatabaseLoader.getCompetitions()) {
                if (match.getCompetitionId().equals(c.getId())) {
                    competition = c.getDataId();
                    break;
                }
            }
        }
        int year = match.getMatchDateTime() != null ? SeasonCalendar.startYearOf(match.getMatchDateTime().toLocalDate())
                : Integer.MAX_VALUE;
        Rules rules = rulesFor(competition, year);
        return rules != null ? rules.gate : Gate.HOME;
    }

    /** The clubs that book the gate of a match (home ground: the home club; neutral: by its rule) */
    public static List<Long> gateClubs(Match match, SaveGame game) {
        List<Long> out = new ArrayList<>();
        switch (gateOf(match, game)) {
        case ASSOCIATION:
            break;
        case SHARED:
            out.add(match.getHomeClubId());
            out.add(match.getAwayClubId());
            break;
        default:
            out.add(match.getHomeClubId());
            break;
        }
        return out;
    }
}
