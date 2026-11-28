package com.rndmodgames.futtoboru.engine.simulation;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
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
 * - Ground and weather: {@link MatchFixture#weather()}; home side defends west and kicks off; 2 x 45 minutes.
 * - Scorers: the last player of the scoring side to play the ball; an own goal has no scorer.
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
        EcsMatchInput input = new EcsMatchInput(fixture.matchKey(),
                new UniformPitch(fixture.weather().surface(), fixture.weather().wetness()), 0.0, HALF_MINUTES,
                new EcsMatchInput.Side(home.sheet, home.style), new EcsMatchInput.Side(away.sheet, away.style));
        EcsMatchResult r = engine.play(input, rng);
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
        return new MatchOutcome(r.homeGoals(), r.awayGoals(), homeScorers, awayScorers);
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
            List<AthleteInputs> inputs = new ArrayList<>(TeamShape.SLOTS);
            List<Long> ids = new ArrayList<>(TeamShape.SLOTS);
            for (int s = 0; s < TeamShape.SLOTS; s++) {
                Position pos = shape.slot(s).position();
                Long playerId = gameSheet.getPlayerId(s);
                Player p = playerId != null ? squad.get(playerId) : null;
                inputs.add(p == null ? standIn(pos) : inputs(p, pos, fixture));
                ids.add(p != null && p.getPerson() != null ? p.getPerson().getId() : null);
            }
            return new Eleven(new TeamSheet(shape, inputs), styleOf(gameSheet.getStyle()), ids);
        }
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
