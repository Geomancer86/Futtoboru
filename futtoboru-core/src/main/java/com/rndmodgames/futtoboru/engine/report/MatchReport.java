package com.rndmodgames.futtoboru.engine.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.GoalMinutes;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventType;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * The match report stand-in (G-03, D6) until the ECS engine's own events (E-04): a headline and a short paragraph in
 * the style of an 1888 newspaper, plus the goals in minute order with the half-time score.
 *
 * Written on demand from the stored match ({@link MatchFacts}); the text is never saved, so old matches and other
 * clubs' matches get one too. Every sentence is a template from the language files ({@code report_*} keys, see
 * {@link #keys()}); which variant a match gets comes from {@code fork("report:" + match)} of the game seed, so a match
 * always reads the same and a season does not repeat itself. A match without goal minutes (played before G-03) gets a
 * report without minutes or half-time score.
 *
 * Era: no penalty kicks, no cards; each club's umpire appeals and the referee settles; no goal nets before 1891-92,
 * so now and then a goal is disputed (an appeal that the ball went over the bar or outside the post).
 */
public final class MatchReport {

    /** A goal as the result screen lists it. {@code minute} 0 = unknown; the score is the score after this goal. */
    public record GoalLine(int minute, boolean home, String scorer, int homeScore, int awayScore) {
    }

    /** Variants per template group: the keys are {@code <group>_1 .. <group>_n}. */
    static final Map<String, Integer> VARIANTS = new LinkedHashMap<>();
    static {
        for (Situation s : Situation.values()) {
            VARIANTS.put("report_headline_" + s.key, 5);
        }
        VARIANTS.put("report_occasion_league", 5);
        VARIANTS.put("report_occasion_cup", 4);
        VARIANTS.put("report_occasion_cup_replay", 3);
        VARIANTS.put("report_occasion_friendly", 4);
        VARIANTS.put("report_crowd", 5);
        VARIANTS.put("report_half_goalless", 4);
        VARIANTS.put("report_half_level", 3);
        VARIANTS.put("report_half_lead", 4);
        VARIANTS.put("report_half_early", 3);
        VARIANTS.put("report_play_goalless", 5);
        VARIANTS.put("report_play_draw", 5);
        VARIANTS.put("report_play_late_equaliser", 3);
        VARIANTS.put("report_play_rout", 5);
        VARIANTS.put("report_play_late_winner", 4);
        VARIANTS.put("report_play_comeback", 4);
        VARIANTS.put("report_play_led", 4);
        VARIANTS.put("report_play_win", 5);
        VARIANTS.put("report_disputed", 3);
        VARIANTS.put("report_scorers_single", 3);
        VARIANTS.put("report_scorers_one", 3);
        VARIANTS.put("report_scorers_both", 3);
        VARIANTS.put("report_close", 4);
        VARIANTS.put("report_close_cup_through", 3);
        VARIANTS.put("report_close_cup_final", 2);
        VARIANTS.put("report_close_cup_replay", 2);
        // E-17: sentences from the ECS events
        VARIANTS.put("report_weather_fine", 3);
        VARIANTS.put("report_weather_rain", 3);
        VARIANTS.put("report_weather_wet", 3);
        VARIANTS.put("report_weather_muddy", 2);
        VARIANTS.put("report_weather_frozen", 2);
        VARIANTS.put("report_press", 3);
        VARIANTS.put("report_disputed_denied", 2);
        VARIANTS.put("report_injury_off", 3);
        VARIANTS.put("report_injury_passenger", 2);
        VARIANTS.put("report_lace_cut", 2);
        VARIANTS.put("report_sent_off", 2);
        VARIANTS.put("report_heavy_ball", 2);
        VARIANTS.put("report_keeper", 3);
    }

    /** E-17: most sentences a report from events may have (one without events has 3 to 6). */
    static final int MAX_SENTENCES_WITH_EVENTS = 8;
    /** E-17: a keeper this busy gets a sentence. */
    static final int BUSY_KEEPER_SAVES = 4;
    /** E-17: a side with this many shots in the opening minutes (and twice the other's) pressed from the kick-off. */
    static final int PRESS_SHOTS = 2;
    /** E-17: wetness from which a dry ground still saw rain ({@code PitchConditions.DRIZZLE}). */
    static final double RAIN_WETNESS = 0.15;

    /** Numbers up to this are written as words ("two goals to one"). */
    static final int MAX_WORD = 12;
    /** A first goal up to this minute is an early goal. */
    static final int EARLY = 15;
    /** A deciding goal or an equaliser from this minute is a late one. */
    static final int LATE = 75;
    /** Share of matches with a goal (before nets) whose report has a disputed goal. */
    static final double DISPUTED_CHANCE = 0.12;
    /** Goal nets came in League-wide in 1891-92 (ERA_TIMELINE.md): after that no "was it in?" disputes. */
    static final LocalDate NETS = LocalDate.of(1891, 9, 1);

    enum Situation {
        GOALLESS("goalless"), DRAW("draw"), NARROW("narrow"), COMFORTABLE("comfortable"), ROUT("rout");

        final String key;

        Situation(String key) {
            this.key = key;
        }

        static Situation of(int home, int away) {
            int margin = Math.abs(home - away);
            if (home == 0 && away == 0) {
                return GOALLESS;
            }
            if (margin == 0) {
                return DRAW;
            }
            if (margin >= 4 || (Math.max(home, away) >= 5 && margin >= 3)) {
                return ROUT;
            }
            return margin >= 2 ? COMFORTABLE : NARROW;
        }
    }

    private final String headline;
    private final String paragraph;
    private final List<GoalLine> goals;
    private final boolean minutesKnown;
    private final int halfTimeHome;
    private final int halfTimeAway;
    private final int sentences;
    private final boolean disputed;

    private MatchReport(String headline, String paragraph, List<GoalLine> goals, boolean minutesKnown,
            int halfTimeHome, int halfTimeAway, int sentences, boolean disputed) {
        this.sentences = sentences;
        this.disputed = disputed;
        this.headline = headline;
        this.paragraph = paragraph;
        this.goals = goals;
        this.minutesKnown = minutesKnown;
        this.halfTimeHome = halfTimeHome;
        this.halfTimeAway = halfTimeAway;
    }

    /** The report of a played match in the game's current language. */
    public static MatchReport of(SaveGame game, Match match) {
        return of(game, match, LanguageModLoader::getValue);
    }

    /** @param text the language: key to template ({@code LanguageModLoader::getValue}, or one bundle in tests) */
    public static MatchReport of(SaveGame game, Match match, Function<String, String> text) {
        return write(MatchFacts.of(game, match), text);
    }

    public static MatchReport write(MatchFacts facts, Function<String, String> text) {
        return new Writer(facts, text).write();
    }

    /** Every language key a report can use (for the bundle tests). */
    public static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Integer> group : VARIANTS.entrySet()) {
            for (int i = 1; i <= group.getValue(); i++) {
                keys.add(group.getKey() + "_" + i);
            }
        }
        for (int d = 1; d <= 7; d++) {
            keys.add("report_weekday_" + d);
        }
        for (int n = 0; n <= MAX_WORD; n++) {
            keys.add("report_num_" + n);
        }
        for (int n = 1; n <= 6; n++) {
            keys.add("report_ordinal_" + n);
        }
        keys.addAll(List.of("report_goals_to", "report_goals_to_one", "report_level", "report_level_one",
                "report_number_group", "report_ground_of", "report_the_league", "report_the_cup",
                "report_last_minute", "report_minute_from_time_one", "report_minutes_from_time", "report_scrimmage",
                "report_scorer_minutes", "report_scorer_count", "report_and", "report_round_final",
                "report_round_semi", "report_round_quarter", "report_round_ordinal", "report_round_number",
                "report_man_of", "report_a_man_of", "report_when_early", "report_when_first", "report_when_second",
                "report_when_late"));
        return keys;
    }

    /** A short headline ("A Comfortable Victory for Preston North End"). */
    public String getHeadline() {
        return headline;
    }

    /** The report: 3 to 6 sentences, up to {@link #MAX_SENTENCES_WITH_EVENTS} when the match has events (E-17). */
    public String getParagraph() {
        return paragraph;
    }

    /** The goals in minute order (minutes known) or home goals then away goals (minute 0). */
    public List<GoalLine> getGoals() {
        return goals;
    }

    /** False for a match played before G-03: its goals have no minutes and there is no half-time score. */
    public boolean isMinutesKnown() {
        return minutesKnown;
    }

    /** Home goals at half-time, -1 when not known. */
    public int getHalfTimeHome() {
        return halfTimeHome;
    }

    /** Away goals at half-time, -1 when not known. */
    public int getHalfTimeAway() {
        return halfTimeAway;
    }

    /** Sentences in the paragraph (tests: 3 to 6). */
    int sentenceCount() {
        return sentences;
    }

    /** True when the report tells of a disputed goal (tests). */
    boolean hasDisputedGoal() {
        return disputed;
    }

    // ------------------------------------------------------------------------------------------------ writing

    private static final class Writer {
        private final MatchFacts f;
        private final Function<String, String> text;
        private final RandomSource random;

        Writer(MatchFacts facts, Function<String, String> text) {
            this.f = facts;
            this.text = text;
            this.random = RandomSource.seeded(facts.seed()).fork("report:" + facts.matchKey());
        }

        MatchReport write() {
            int h = f.homeGoals();
            int a = f.awayGoals();
            List<GoalLine> lines = new ArrayList<>();
            int hs = 0;
            int as = 0;
            int htH = 0;
            int htA = 0;
            for (MatchFacts.Goal g : f.goals()) {
                if (g.home()) {
                    hs++;
                } else {
                    as++;
                }
                if (g.minute() > 0 && g.minute() <= 45) {
                    if (g.home()) {
                        htH++;
                    } else {
                        htA++;
                    }
                }
                lines.add(new GoalLine(g.minute(), g.home(), g.scorer(), hs, as));
            }
            boolean known = f.minutesKnown();

            Situation s = Situation.of(h, a);
            boolean homeWins = h >= a;
            String winner = homeWins ? f.home() : f.away();
            String loser = homeWins ? f.away() : f.home();
            String headline = format(variant("report_headline_" + s.key), winner, loser, f.home(), f.away());

            List<String> sentences = new ArrayList<>();
            MatchFacts.Play play = f.play();
            sentences.add(occasion());
            if (play != null) {
                sentences.add(weather(play));
            }
            if (known && h + a > 0) {
                sentences.add(half(htH, htA));
            }
            // E-17: the optional sentences from the events, most telling first, as many as the length allows
            List<MatchFacts.Incident> told = new ArrayList<>();
            boolean press = false;
            boolean keeper = false;
            if (play != null) {
                int room = MAX_SENTENCES_WITH_EVENTS - sentences.size() - 2 - (h + a > 0 ? 1 : 0);
                MatchFacts.Incident dispute = null;
                List<MatchFacts.Incident> others = new ArrayList<>();
                for (MatchFacts.Incident i : play.incidents()) {
                    if (i.type() == MatchEventType.DISPUTED_GOAL) {
                        dispute = dispute == null ? i : dispute;
                    } else {
                        others.add(i);
                    }
                }
                others.sort((x, y) -> Integer.compare(rank(y), rank(x)));
                if (dispute != null && room > 0) {
                    told.add(dispute);
                    room--;
                }
                if (!others.isEmpty() && room > 0) {
                    told.add(others.get(0));
                    room--;
                }
                if (pressing(play) != 0 && room > 0) {
                    press = true;
                    room--;
                }
                if (others.size() > 1 && rank(others.get(1)) >= 2 && room > 0) {
                    told.add(others.get(1));
                    room--;
                }
                if (busyKeeper(play) != 0 && room > 0) {
                    keeper = true;
                }
                told.sort((x, y) -> Integer.compare(x.minute(), y.minute()));
            }
            if (press) {
                boolean home = pressing(play) > 0;
                sentences.add(format(variant("report_press"), team(home), team(!home)));
            }
            sentences.add(play(s, known, winner, loser));
            boolean disputed = false;
            if (play != null) {
                for (MatchFacts.Incident i : told) {
                    disputed |= i.type() == MatchEventType.DISPUTED_GOAL;
                    sentences.add(incident(i));
                }
                if (keeper) {
                    boolean home = busyKeeper(play) > 0;
                    sentences.add(format(variant("report_keeper"), home ? play.homeKeeper() : play.awayKeeper(),
                            team(home), num(home ? play.homeSaves() : play.awaySaves())));
                }
            } else if (h + a > 0 && beforeNets() && random.nextDouble() < DISPUTED_CHANCE) {
                disputed = true;
                String appealing = s == Situation.DRAW ? f.away() : loser;
                String scoring = s == Situation.DRAW ? f.home() : winner;
                sentences.add(format(variant("report_disputed"), appealing, scoring));
            }
            if (h + a > 0) {
                sentences.add(scorers(known));
            }
            sentences.add(close(s, winner, loser));

            StringBuilder paragraph = new StringBuilder();
            for (String sentence : sentences) {
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(capitalise(sentence));
            }
            return new MatchReport(capitalise(headline), paragraph.toString(), Collections.unmodifiableList(lines),
                    known, known ? htH : -1, known ? htA : -1, sentences.size(), disputed);
        }

        // ------------------------------------------------------------------------------------- sentences

        private String occasion() {
            String ground = f.ground() != null ? f.ground() : format("report_ground_of", f.home());
            int weekday = f.date() != null ? f.date().getDayOfWeek().getValue() : 6;
            String day = t("report_weekday_" + weekday);
            String crowd = f.attendance() > 0
                    ? " " + format(variant("report_crowd"), number(roundCrowd(f.attendance())))
                    : "";
            if (f.matchType() == Match.LEAGUE_MATCH) {
                String competition = f.competition() != null ? f.competition() : t("report_the_league");
                return format(variant("report_occasion_league"), f.home(), f.away(), competition, ground, day, crowd);
            }
            if (f.matchType() == Match.CUP_MATCH) {
                String competition = f.competition() != null ? f.competition() : t("report_the_cup");
                String group = f.replay() ? "report_occasion_cup_replay" : "report_occasion_cup";
                return format(variant(group), f.home(), f.away(), competition, ground, day, crowd, roundName());
            }
            return format(variant("report_occasion_friendly"), f.home(), f.away(), "", ground, day, crowd);
        }

        private String half(int htH, int htA) {
            if (htH == 0 && htA == 0) {
                return format(variant("report_half_goalless"), f.home(), f.away());
            }
            if (htH == htA) {
                return format(variant("report_half_level"), level(htH), f.home(), f.away());
            }
            boolean homeLeads = htH > htA;
            String leader = homeLeads ? f.home() : f.away();
            String other = homeLeads ? f.away() : f.home();
            String phrase = goalsTo(Math.max(htH, htA), Math.min(htH, htA));
            MatchFacts.Goal first = f.goals().get(0);
            if (first.home() == homeLeads && first.scorer() != null && first.minute() >= 2
                    && first.minute() <= EARLY) {
                return format(variant("report_half_early"), leader, phrase, first.scorer(), num(first.minute()),
                        other);
            }
            return format(variant("report_half_lead"), leader, phrase, other);
        }

        private String play(Situation s, boolean known, String winner, String loser) {
            List<MatchFacts.Goal> goals = f.goals();
            switch (s) {
            case GOALLESS:
                return format(variant("report_play_goalless"), f.home(), f.away());
            case DRAW: {
                MatchFacts.Goal last = goals.get(goals.size() - 1);
                if (known && last.minute() >= LATE && last.scorer() != null) {
                    String equalising = last.home() ? f.home() : f.away();
                    String other = last.home() ? f.away() : f.home();
                    return format(variant("report_play_late_equaliser"), equalising, other, last.scorer(),
                            fromTime(last.minute()));
                }
                return format(variant("report_play_draw"), f.home(), f.away());
            }
            case ROUT:
                return format(variant("report_play_rout"), winner, loser);
            default:
                break;
            }
            if (known) {
                boolean winnerHome = f.homeGoals() > f.awayGoals();
                int diff = 0;
                boolean trailed = false;
                int deciding = -1;
                for (int i = 0; i < goals.size(); i++) {
                    int before = diff;
                    diff += goals.get(i).home() == winnerHome ? 1 : -1;
                    trailed |= diff < 0;
                    if (before == 0 && diff == 1) {
                        deciding = i;
                    }
                }
                MatchFacts.Goal decider = deciding >= 0 ? goals.get(deciding) : null;
                if (s == Situation.NARROW && decider != null && decider.minute() >= LATE && decider.scorer() != null) {
                    return format(variant("report_play_late_winner"), winner, loser, decider.scorer(),
                            fromTime(decider.minute()));
                }
                if (trailed) {
                    return format(variant("report_play_comeback"), winner, loser);
                }
                if (deciding == 0) {
                    return format(variant("report_play_led"), winner, loser);
                }
            }
            return format(variant("report_play_win"), winner, loser);
        }

        private String scorers(boolean known) {
            int h = f.homeGoals();
            int a = f.awayGoals();
            if (h + a == 1) {
                MatchFacts.Goal only = f.goals().get(0);
                return format(variant("report_scorers_single"), side(only.home(), known), team(only.home()));
            }
            if (h > 0 && a > 0) {
                return format(variant("report_scorers_both"), side(true, known), f.home(), side(false, known),
                        f.away());
            }
            boolean home = h > 0;
            return format(variant("report_scorers_one"), side(home, known), team(home));
        }

        private String close(Situation s, String winner, String loser) {
            Object[] args = { f.home(), f.homeGoals(), f.away(), f.awayGoals(), winner, loser };
            if (f.matchType() == Match.CUP_MATCH) {
                if (s != Situation.DRAW && s != Situation.GOALLESS) {
                    return format(variant(f.cupFinal() ? "report_close_cup_final" : "report_close_cup_through"),
                            args);
                }
                if (!f.replay()) {
                    return format(variant("report_close_cup_replay"), args);
                }
            }
            return format(variant("report_close"), args);
        }

        // ------------------------------------------------------------------------------------- E-17 events

        /** The ground and the weather, from the surface and wetness the match was played on. */
        private String weather(MatchFacts.Play play) {
            Surface surface = play.surface() >= 0 && play.surface() < Surface.values().length
                    ? Surface.values()[play.surface()] : Surface.DRY;
            switch (surface) {
                case FROZEN:
                    return format(variant("report_weather_frozen"), f.home(), f.away());
                case MUDDY:
                    return format(variant("report_weather_muddy"), f.home(), f.away());
                case WET:
                    return format(variant("report_weather_wet"), f.home(), f.away());
                default:
                    return format(variant(play.wetness() >= RAIN_WETNESS ? "report_weather_rain"
                            : "report_weather_fine"), f.home(), f.away());
            }
        }

        /** 1 when the home side pressed from the kick-off, -1 the away side, 0 neither. */
        private static int pressing(MatchFacts.Play play) {
            int h = play.openingHome();
            int a = play.openingAway();
            if (h >= PRESS_SHOTS && h >= 2 * a) {
                return 1;
            }
            return a >= PRESS_SHOTS && a >= 2 * h ? -1 : 0;
        }

        /** 1 when the home keeper was the busier and busy enough (named), -1 the away keeper, 0 neither. */
        private static int busyKeeper(MatchFacts.Play play) {
            boolean home = play.homeSaves() >= play.awaySaves();
            int saves = home ? play.homeSaves() : play.awaySaves();
            String name = home ? play.homeKeeper() : play.awayKeeper();
            return saves >= BUSY_KEEPER_SAVES && name != null ? (home ? 1 : -1) : 0;
        }

        /** How telling an incident is: a sending-off over a man carried off over a lace cut over a passenger. */
        private static int rank(MatchFacts.Incident i) {
            switch (i.type()) {
                case SENDING_OFF:
                    return 5;
                case INJURY:
                    return i.detail() >= 3 ? 4 : 2;
                case LACE_CUT:
                    return 3;
                default:
                    return 1; // the ball grown heavy
            }
        }

        private String incident(MatchFacts.Incident i) {
            String when = when(i.minute());
            switch (i.type()) {
                case DISPUTED_GOAL: {
                    String attackers = team(i.home());
                    String defenders = team(!i.home());
                    return i.detail() == 1 ? format(variant("report_disputed"), defenders, attackers)
                            : format(variant("report_disputed_denied"), attackers, defenders);
                }
                case SENDING_OFF:
                    return format(variant("report_sent_off"), man(i), when);
                case INJURY:
                    return format(variant(i.detail() >= 3 ? "report_injury_off" : "report_injury_passenger"), man(i),
                            when);
                case LACE_CUT:
                    return format(variant("report_lace_cut"), man(i), when);
                default:
                    return format(variant("report_heavy_ball"), f.home(), f.away());
            }
        }

        /** "Ross of Preston North End", or "a Preston North End man" when his name is not known. */
        private String man(MatchFacts.Incident i) {
            return i.name() != null ? format("report_man_of", i.name(), team(i.home()))
                    : format("report_a_man_of", team(i.home()));
        }

        /** "early in the game", "before the interval", "after the interval", "late in the game" */
        private String when(int minute) {
            if (minute <= EARLY) {
                return t("report_when_early");
            }
            if (minute <= GoalMinutes.HALF) {
                return t("report_when_first");
            }
            return minute >= LATE ? t("report_when_late") : t("report_when_second");
        }

        // ------------------------------------------------------------------------------------- pieces

        /** One side's scorers in order of their first goal: "Ross (12, 55 min.) and Goodall (40 min.)". */
        private String side(boolean home, boolean known) {
            Map<String, List<Integer>> byScorer = new LinkedHashMap<>();
            for (MatchFacts.Goal g : f.goals()) {
                if (g.home() == home) {
                    String name = g.scorer() != null ? g.scorer() : t("report_scrimmage");
                    byScorer.computeIfAbsent(name, k -> new ArrayList<>()).add(g.minute());
                }
            }
            List<String> entries = new ArrayList<>();
            for (Map.Entry<String, List<Integer>> e : byScorer.entrySet()) {
                if (known) {
                    StringBuilder minutes = new StringBuilder();
                    for (Integer m : e.getValue()) {
                        if (minutes.length() > 0) {
                            minutes.append(", ");
                        }
                        minutes.append(m);
                    }
                    entries.add(format("report_scorer_minutes", e.getKey(), minutes));
                } else if (e.getValue().size() > 1) {
                    entries.add(format("report_scorer_count", e.getKey(), e.getValue().size()));
                } else {
                    entries.add(e.getKey());
                }
            }
            return joinAnd(entries);
        }

        private String joinAnd(List<String> items) {
            if (items.size() <= 1) {
                return items.isEmpty() ? "" : items.get(0);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size() - 1; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(items.get(i));
            }
            return sb.append(' ').append(t("report_and")).append(' ').append(items.get(items.size() - 1)).toString();
        }

        private String team(boolean home) {
            return home ? f.home() : f.away();
        }

        /** "the second round", "the semi-final", ... (with its article: Spanish rounds differ in gender and number) */
        private String roundName() {
            int ties = f.roundTies();
            if (ties == 1) {
                return t("report_round_final");
            }
            if (ties == 2) {
                return t("report_round_semi");
            }
            if (ties == 4) {
                return t("report_round_quarter");
            }
            int round = Math.max(1, f.round());
            if (round <= 6) {
                return format("report_round_ordinal", t("report_ordinal_" + round));
            }
            return format("report_round_number", round);
        }

        /** "two goals to one", "one goal to nil" */
        private String goalsTo(int more, int fewer) {
            return more == 1 ? format("report_goals_to_one", num(fewer)) : format("report_goals_to", num(more),
                    num(fewer));
        }

        /** "one goal each", "two goals each" */
        private String level(int each) {
            return each == 1 ? t("report_level_one") : format("report_level", num(each));
        }

        /** "five minutes from time", "a minute from time", "in the last minute" */
        private String fromTime(int minute) {
            int left = 90 - minute;
            if (left <= 0) {
                return t("report_last_minute");
            }
            return left == 1 ? t("report_minute_from_time_one") : format("report_minutes_from_time", num(left));
        }

        private String num(int n) {
            return n >= 0 && n <= MAX_WORD ? t("report_num_" + n) : String.valueOf(n);
        }

        /** A press estimate: to the hundred below 1,000, else to the 500. */
        private static int roundCrowd(int attendance) {
            if (attendance < 1000) {
                return Math.max(100, (int) Math.round(attendance / 100.0) * 100);
            }
            return (int) Math.round(attendance / 500.0) * 500;
        }

        /** Digits grouped by three with the language's separator ("6,000" / "6.000"). */
        private String number(int n) {
            String digits = String.valueOf(n);
            String sep = t("report_number_group");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && (digits.length() - i) % 3 == 0) {
                    sb.append(sep);
                }
                sb.append(digits.charAt(i));
            }
            return sb.toString();
        }

        private boolean beforeNets() {
            return f.date() == null || f.date().isBefore(NETS);
        }

        private String variant(String group) {
            return group + "_" + (1 + random.nextInt(VARIANTS.get(group)));
        }

        private String t(String key) {
            String value = text.apply(key);
            return value == null ? key : value;
        }

        private String format(String key, Object... args) {
            String result = t(key);
            for (int i = 0; i < args.length; i++) {
                result = result.replace("{" + i + "}", String.valueOf(args[i]));
            }
            return result;
        }

        private static String capitalise(String s) {
            if (s == null || s.isEmpty() || !Character.isLowerCase(s.charAt(0))) {
                return s;
            }
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}
