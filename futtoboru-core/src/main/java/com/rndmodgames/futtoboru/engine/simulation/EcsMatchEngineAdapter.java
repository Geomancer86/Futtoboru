package com.rndmodgames.futtoboru.engine.simulation;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;
import com.rndmodgames.futtoboru.engine.injuries.MatchInjuries;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchInput;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchResult;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.ai.TeamStyle;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteInputs;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.body.TeamSheet;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventType;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Plays a game fixture on the ECS engine (E-17a, D20): builds the {@link EcsMatchInput} from the two clubs' team
 * sheets frozen at kick-off (G-01) and turns the {@link EcsMatchResult} into a {@link MatchOutcome}. {@code match.ecs}
 * never reads game data; this is the one place that copies it out.
 *
 * - Eleven, shape and style: the frozen {@link com.rndmodgames.futtoboru.data.TeamSheet}: slot i's man plays at the
 *   place's position ({@link TeamShape} maps to the engine's {@link Shape} by label, {@link ClubStyle} to
 *   {@link TeamStyle} by name). The engine needs eleven bodies, so an empty place (a club playing short) gets a
 *   stand-in at the bottom of the rating scale who never scores (interim, until the engine can play with ten).
 * - Ground and weather: {@link MatchFixture#weather()}; home side defends west and kicks off; 2 x 45 minutes; the away
 *   side carries the home advantage ({@code ActionParams.awayNoiseGain}) unless the ground is neutral (E-20).
 * - Scorers: the last player of the scoring side to play the ball; an own goal has no scorer.
 * - Injuries (G-02, E-17): a man on the fixture's injury list starts weakened, by the share of the match he is worth
 *   less ({@link #hurtShare}: a passenger from his minute on, a man carried off nothing while off and a passenger if
 *   he came back; a knock costs nothing); his body ratings (speed, acceleration, dexterity, stamina, endurance,
 *   strength) slide toward the bottom of the scale by that share. The engine's own contact injuries come on top.
 * - Events (E-17): the engine's event log goes on the outcome in its compact saved form, and each goal's minute comes
 *   from its GOAL event, so the match keeps the engine's own timing (no G-03 draw).
 */
public final class EcsMatchEngineAdapter implements MatchEngine {

    static final int HALF_MINUTES = 45;
    /** Ratings of a stand-in for an empty place: the bottom of the 3..23 scale. */
    static final double STAND_IN_RATING = 3;

    private final EcsMatchEngine engine;

    public EcsMatchEngineAdapter() {
        this(new EcsMatchEngine());
    }

    public EcsMatchEngineAdapter(EcsMatchEngine engine) {
        this.engine = engine;
    }

    @Override
    public String id() {
        return ECS;
    }

    @Override
    public MatchOutcome play(MatchFixture fixture, RandomSource rng) {
        Eleven home = Eleven.of(fixture.homeSheet(), fixture.home(), fixture);
        Eleven away = Eleven.of(fixture.awaySheet(), fixture.away(), fixture);
        return outcome(home, away, engine.play(input(fixture, home, away), rng));
    }

    @Override
    public WatchedMatch watch(MatchFixture fixture, RandomSource rng) {
        Eleven home = Eleven.of(fixture.homeSheet(), fixture.home(), fixture);
        Eleven away = Eleven.of(fixture.awaySheet(), fixture.away(), fixture);
        EcsMatchEngine.Live live = engine.start(input(fixture, home, away), rng);
        return new WatchedMatch(live, () -> outcome(home, away, live.result()));
    }

    private static MatchOutcome outcome(Eleven home, Eleven away, EcsMatchResult r) {
        List<Integer> homeMinutes = new ArrayList<>();
        List<Integer> awayMinutes = new ArrayList<>();
        for (MatchEvent e : r.events().of(MatchEventType.GOAL)) {
            (e.side() == Team.HOME ? homeMinutes : awayMinutes).add(e.minute());
        }
        if (homeMinutes.size() != r.homeGoals() || awayMinutes.size() != r.awayGoals()) {
            homeMinutes.clear(); // a goal the log missed: let the simulator draw the minutes
            awayMinutes.clear();
        }
        List<Long> homeScorers = new ArrayList<>();
        List<Long> awayScorers = new ArrayList<>();
        for (EcsMatchResult.Goal g : r.goals()) {
            if (!g.hasScorer()) {
                continue;
            }
            Eleven side = g.scoringTeam() == Team.HOME ? home : away;
            Long id = side.personIds.get(g.slot());
            if (id != null) {
                (g.scoringTeam() == Team.HOME ? homeScorers : awayScorers).add(id);
            }
        }
        return new MatchOutcome(r.homeGoals(), r.awayGoals(), homeScorers, awayScorers, homeMinutes, awayMinutes,
                r.events().encode());
    }

    /** The engine's input for the fixture with these two sides (also the calibration harness's, E-20). */
    static EcsMatchInput input(MatchFixture fixture, Eleven home, Eleven away) {
        return new EcsMatchInput(fixture.matchKey(),
                new UniformPitch(fixture.weather().surface(), fixture.weather().wetness()), 0.0, HALF_MINUTES,
                new EcsMatchInput.Side(home.sheet, home.style), new EcsMatchInput.Side(away.sheet, away.style),
                fixture.neutral());
    }

    /** {@link Shape} of the engine for a sheet's {@link TeamShape} (same label, same slot positions). */
    static Shape shapeOf(TeamShape shape) {
        TeamShape s = shape != null ? shape : TeamShape.TWO_THREE_FIVE;
        for (Shape candidate : Shape.values()) {
            if (candidate.label().equals(s.label())) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("no engine shape for " + s.label());
    }

    /** {@link TeamStyle} of the engine for a club style; dribbling when none is set. */
    static TeamStyle styleOf(ClubStyle style) {
        return style == ClubStyle.COMBINATION ? TeamStyle.COMBINATION : TeamStyle.DRIBBLING;
    }

    /** A side for the engine: its sheet, its style and the person id of the man in each slot (null for a stand-in). */
    static final class Eleven {
        final TeamSheet sheet;
        final TeamStyle style;
        final List<Long> personIds;

        private Eleven(TeamSheet sheet, TeamStyle style, List<Long> personIds) {
            this.sheet = sheet;
            this.style = style;
            this.personIds = personIds;
        }

        static Eleven of(com.rndmodgames.futtoboru.data.TeamSheet gameSheet, Club club, MatchFixture fixture) {
            Shape shape = shapeOf(gameSheet.getShape());
            Map<Long, Player> squad = TeamSheets.squadById(club);
            List<MatchInjury> injuries = club == fixture.home() ? fixture.homeInjuries()
                    : club == fixture.away() ? fixture.awayInjuries() : List.of();
            InjuryRules rules = fixture.injuryRules() != null ? fixture.injuryRules() : InjuryRules.DEFAULTS;
            List<AthleteInputs> inputs = new ArrayList<>(TeamShape.SLOTS);
            List<Long> ids = new ArrayList<>(TeamShape.SLOTS);
            double shift = com.rndmodgames.futtoboru.history.ClubStrength.ratingShift(club.getDataId());
            for (int s = 0; s < TeamShape.SLOTS; s++) {
                Position pos = shape.slot(s).position();
                Long playerId = gameSheet.getPlayerId(s);
                Player p = playerId != null ? squad.get(playerId) : null;
                AthleteInputs in = p == null ? standIn(pos) : shifted(inputs(p, pos, fixture), shift);
                if (p != null && p.getPerson() != null) {
                    in = weakened(in, hurtShare(injuryOf(injuries, p.getPerson().getId()), rules));
                }
                inputs.add(in);
                ids.add(p != null && p.getPerson() != null ? p.getPerson().getId() : null);
            }
            return new Eleven(new TeamSheet(shape, inputs), styleOf(gameSheet.getStyle()), ids);
        }
    }

    /** The man's injury of this match, null when he was not hurt. */
    static MatchInjury injuryOf(List<MatchInjury> injuries, Long personId) {
        if (injuries == null || personId == null) {
            return null;
        }
        for (MatchInjury i : injuries) {
            if (i != null && personId.equals(i.getPersonId())) {
                return i;
            }
        }
        return null;
    }

    /**
     * Share of the match (0..1) a hurt man is lost to his side, as the instant result weighs him
     * ({@link MatchInjuries#effectiveMen}): a passenger loses {@code 1 - strengthWhileHurt} from his minute on; a man
     * carried off is lost while off and a passenger after he comes back; a knock costs nothing.
     */
    static double hurtShare(MatchInjury injury, InjuryRules rules) {
        if (injury == null || injury.getMinute() == null) {
            return 0;
        }
        InjuryKind kind = injury.getKindEnum();
        if (kind == null || kind == InjuryKind.KNOCK) {
            return 0;
        }
        double minutes = MatchInjuries.MINUTES;
        int from = Math.max(0, Math.min(MatchInjuries.MINUTES, injury.getMinute()));
        double s = rules.get(kind).strengthWhileHurt;
        if (kind == InjuryKind.PASSENGER) {
            return Math.max(0, Math.min(1, (1 - s) * (minutes - from) / minutes));
        }
        int back = injury.getReturnedMinute() != null
                ? Math.max(from, Math.min(MatchInjuries.MINUTES, injury.getReturnedMinute()))
                : MatchInjuries.MINUTES;
        return Math.max(0, Math.min(1, (back - from) / minutes + (1 - s) * (minutes - back) / minutes));
    }

    /** The body ratings slid toward the stand-in rating by {@code share} (0 = unchanged); ball skills kept. */
    static AthleteInputs weakened(AthleteInputs a, double share) {
        if (!(share > 0)) {
            return a;
        }
        double k = 1 - Math.min(1, share);
        return new AthleteInputs(a.position(), toward(a.speed(), k), toward(a.acceleration(), k),
                toward(a.dexterity(), k), toward(a.stamina(), k), toward(a.endurance(), k), toward(a.strength(), k),
                a.age(), a.condition(), a.shinPads(), a.kicking(), a.technique(), a.heading(), a.handling(),
                a.dribbling());
    }

    /** D19-1: every rating moved by the club's strength shift (clamped to 3..23); age and condition kept. */
    static AthleteInputs shifted(AthleteInputs a, double shift) {
        if (shift == 0) {
            return a;
        }
        return new AthleteInputs(a.position(), sh(a.speed(), shift), sh(a.acceleration(), shift),
                sh(a.dexterity(), shift), sh(a.stamina(), shift), sh(a.endurance(), shift), sh(a.strength(), shift),
                a.age(), a.condition(), a.shinPads(), sh(a.kicking(), shift), sh(a.technique(), shift),
                sh(a.heading(), shift), sh(a.handling(), shift), sh(a.dribbling(), shift));
    }

    private static double sh(double r, double shift) {
        return Math.max(STAND_IN_RATING, Math.min(23, r + shift));
    }

    private static double toward(double rating, double keep) {
        return rating <= STAND_IN_RATING ? rating : STAND_IN_RATING + (rating - STAND_IN_RATING) * keep;
    }

    static AthleteInputs standIn(Position pos) {
        double r = STAND_IN_RATING;
        return new AthleteInputs(pos, r, r, r, r, r, r, 25, 1.0, null, r, r, r, r, r);
    }

    /** The player's ratings for the body and ball simulation, playing at {@code slot}'s position. */
    static AthleteInputs inputs(Player p, Position slot, MatchFixture fixture) {
        double age = 25;
        if (p.getPerson() != null && p.getPerson().getBirthDate() != null && fixture.kickOff() != null) {
            long years = ChronoUnit.YEARS.between(p.getPerson().getBirthDate(), fixture.kickOff());
            if (years >= 14 && years <= 60) {
                age = years;
            }
        }
        double handling = (v(p.getShotStopping()) + v(p.getHandToHand())) / 2;
        return new AthleteInputs(slot, v(p.getSpeed()), v(p.getAcceleration()), v(p.getDexterity()),
                v(p.getStamina()), v(p.getEndurance()), v(p.getStrength()), age, 1.0, null, v(p.getKicking()),
                v(p.getPassing()), v(p.getHeading()), handling, v(p.getDribbling()));
    }

    /** A rating on the 3..23 scale; average when the player has none. */
    private static double v(Float rating) {
        return rating == null || rating.isNaN() ? AthleteInputs.AVERAGE : rating;
    }
}
