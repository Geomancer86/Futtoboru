package com.rndmodgames.futtoboru.engine.simulation;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
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
 * Plays a game fixture on the ECS engine (E-17a, D20): builds the {@link EcsMatchInput} from the two clubs and turns
 * the {@link EcsMatchResult} into a {@link MatchOutcome}. {@code match.ecs} never reads game data; this is the one
 * place that copies it out.
 *
 * - Eleven: there is no team sheet in the game yet (G-01), so each side fields its best XI in a 2-3-5 by
 *   {@link Position}: per slot the best free player of that position, then of that line, then anyone; a squad short of
 *   eleven fills the gap with an average stand-in who never scores.
 * - Style: the dribbling game for every side (placeholder until club styles are data).
 * - Ground and weather: {@link MatchFixture#weather()}; home side defends west and kicks off; 2 x 45 minutes.
 * - Scorers: the last player of the scoring side to play the ball; an own goal has no scorer.
 */
public final class EcsMatchEngineAdapter implements MatchEngine {

    static final Shape SHAPE = Shape.TWO_THREE_FIVE;
    static final int HALF_MINUTES = 45;

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
        Eleven home = Eleven.pick(fixture.home(), fixture);
        Eleven away = Eleven.pick(fixture.away(), fixture);
        EcsMatchInput input = new EcsMatchInput(fixture.matchKey(),
                new UniformPitch(fixture.weather().surface(), fixture.weather().wetness()), 0.0, HALF_MINUTES,
                new EcsMatchInput.Side(home.sheet, TeamStyle.DRIBBLING),
                new EcsMatchInput.Side(away.sheet, TeamStyle.DRIBBLING));
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

    /** A side's eleven: the team sheet and the person id of the player in each slot (null for a stand-in). */
    static final class Eleven {
        final TeamSheet sheet;
        final List<Long> personIds;

        private Eleven(TeamSheet sheet, List<Long> personIds) {
            this.sheet = sheet;
            this.personIds = personIds;
        }

        static Eleven pick(Club club, MatchFixture fixture) {
            List<Player> squad = new ArrayList<>();
            if (club.getPlayers() != null) {
                for (Player p : club.getPlayers()) {
                    if (p != null) {
                        squad.add(p);
                    }
                }
            }
            List<Shape.Slot> slots = SHAPE.slots();
            Player[] chosen = new Player[slots.size()];
            boolean[] used = new boolean[squad.size()];
            for (int pass = 0; pass < 3; pass++) {
                for (int s = 0; s < slots.size(); s++) {
                    if (chosen[s] != null) {
                        continue;
                    }
                    Position want = slots.get(s).position();
                    int best = -1;
                    double bestRating = Double.NEGATIVE_INFINITY;
                    for (int i = 0; i < squad.size(); i++) {
                        Position has = squad.get(i).getPosition();
                        boolean fits = pass == 0 ? has == want
                                : pass == 1 ? has != null && has.getLine() == want.getLine() : true;
                        if (used[i] || !fits) {
                            continue;
                        }
                        double rating = rating(squad.get(i), want);
                        if (rating > bestRating) {
                            best = i;
                            bestRating = rating;
                        }
                    }
                    if (best >= 0) {
                        used[best] = true;
                        chosen[s] = squad.get(best);
                    }
                }
            }
            List<AthleteInputs> inputs = new ArrayList<>(slots.size());
            List<Long> ids = new ArrayList<>(slots.size());
            for (int s = 0; s < slots.size(); s++) {
                Position pos = slots.get(s).position();
                Player p = chosen[s];
                inputs.add(p == null ? AthleteInputs.stub(pos) : inputs(p, pos, fixture));
                ids.add(p != null && p.getPerson() != null ? p.getPerson().getId() : null);
            }
            return new Eleven(new TeamSheet(SHAPE, inputs), ids);
        }
    }

    /** How good {@code p} is in {@code slot}: handling for keepers, otherwise body, kicking and close control. */
    static double rating(Player p, Position slot) {
        if (slot == Position.GOALKEEPER) {
            return (v(p.getShotStopping()) + v(p.getHandToHand()) + v(p.getStrength())) / 3;
        }
        return (v(p.getSpeed()) + v(p.getAcceleration()) + v(p.getStamina()) + v(p.getStrength())
                + v(p.getKicking()) + v(p.getPassing()) + v(p.getDribbling())) / 7;
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
