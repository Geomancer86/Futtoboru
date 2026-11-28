package com.rndmodgames.futtoboru.match.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.action.ActionRecord;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.laws.LawRecord;
import com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * The crowd (D22, E-18): {@link #SECTIONS} sections round the pitch, plain arrays on one entity
 * ({@link CrowdComponent}), not people. Density comes from the attendance and where the section is (the touchlines
 * fill first), the home share from the ground (a neutral ground splits the ends, a home ground puts the away
 * supporters behind one goal).
 *
 * <ul>
 * <li><b>Mood</b> (-1..1 per section) reacts to what this tick's records show: goals, disputed decisions, fouls
 * against the home side, refused appeals, shots (the near misses and saves). Each event has a value from the home
 * side's point of view; a home section feels it as it is, an away section the other way, a mixed one hardly at all,
 * and sections near the event feel it more. The mood then settles back toward calm ({@link #SETTLE_SECONDS}).</li>
 * <li><b>Encroachment</b> (0..1) rises where a touchline section is worked up, and falls slowly.</li>
 * <li><b>Confidence</b> ({@link #support(double, double, Team)}): the sections within {@link #REACH_M} of a player
 * give him a small share of their feeling; {@link ActionSystem} scales his action noise by
 * {@code 1 - }{@link #CONFIDENCE_GAIN}{@code * support}, so at most a few per cent.</li>
 * </ul>
 *
 * It reads the simulation's records only and draws no random numbers; a world without the crowd entity is untouched.
 * Per tick it allocates nothing.
 */
public final class CrowdSystem extends EntitySystem implements SimSystem {

    public static final int SECTIONS = 100;
    /** Attendance at which the ground counts as full. */
    public static final double FULL_ATTENDANCE = 10_000;
    /** Attendance when the input gives none. */
    public static final int DEFAULT_ATTENDANCE = 3_000;
    /** The mood settles to calm with this time constant. */
    public static final double SETTLE_SECONDS = 45;
    /** Largest change of an action's noise factor from the crowd (a share). */
    public static final double CONFIDENCE_GAIN = 0.06;
    /** Sections farther than this from a player give him nothing (m). */
    public static final double REACH_M = 30;

    /** Event values, from the home side's point of view. */
    static final double GOAL = 0.7;
    static final double DISPUTE = 0.4;
    static final double FOUL_AGAINST = 0.2;
    static final double REFUSED_APPEAL = 0.3;
    static final double SHOT = 0.12;
    /** Share of an event's value a section far from it still feels. */
    static final double FAR_FEEL = 0.5;
    /** Beyond this distance from an event a section feels only {@link #FAR_FEEL}. */
    static final double FEEL_M = 60;
    /** Margin of the sections round the pitch (m), and how many sections stand behind each goal. */
    static final double MARGIN_X_M = 6;
    static final double MARGIN_Y_M = 4;
    static final double ENCROACH_FROM = 0.5;
    static final double ENCROACH_PER_SECOND = 0.02;
    static final double ENCROACH_SETTLE_SECONDS = 150;

    private final MatchWorld world;
    private final double settle;
    private final double encroachSettle;
    private final double dt;
    private final ComponentMapper<CrowdComponent> crowdMap = ComponentMapper.getFor(CrowdComponent.class);
    private ImmutableArray<Entity> crowds;
    private ImmutableArray<Entity> bodies;
    private CrowdComponent crowd;
    private int[] teamOf = new int[0];
    private ActionSystem actions;
    private LawsSystem laws;

    public CrowdSystem(MatchWorld world) {
        super(SystemPriority.CROWD);
        this.world = world;
        this.dt = world.tickSeconds();
        this.settle = StrictMath.exp(-dt / SETTLE_SECONDS);
        this.encroachSettle = StrictMath.exp(-dt / ENCROACH_SETTLE_SECONDS);
    }

    @Override
    public void addedToEngine(Engine engine) {
        crowds = engine.getEntitiesFor(Family.all(CrowdComponent.class).get());
        bodies = engine.getEntitiesFor(Family.all(BodyComponent.class, IdentityComponent.class).get());
    }

    /**
     * Spawns the crowd entity (role {@link SpawnRole#CROWD_SECTION}): sections spread evenly round the pitch.
     *
     * @param attendance spectators (0 or less: {@link #DEFAULT_ATTENDANCE})
     * @param neutral    a neutral ground: both sides' supporters share the ends
     */
    public static CrowdComponent spawn(MatchWorld world, PitchGeometry pitch, int attendance, boolean neutral) {
        CrowdComponent c = new CrowdComponent(SECTIONS);
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        double rx = hl + MARGIN_X_M;
        double ry = hw + MARGIN_Y_M;
        double perimeter = 4 * (rx + ry);
        double fill = Math.min(1, Math.max(0.05, (attendance > 0 ? attendance : DEFAULT_ATTENDANCE) / FULL_ATTENDANCE));
        double full = StrictMath.sqrt(fill);
        for (int i = 0; i < SECTIONS; i++) {
            double s = (i + 0.5) / SECTIONS * perimeter;
            double x;
            double y;
            if (s < 2 * rx) { // far touchline, west to east
                x = -rx + s;
                y = ry;
            } else if (s < 2 * rx + 2 * ry) { // east end, far to near
                x = rx;
                y = ry - (s - 2 * rx);
            } else if (s < 4 * rx + 2 * ry) { // near touchline, east to west
                x = rx - (s - 2 * rx - 2 * ry);
                y = -ry;
            } else { // west end, near to far
                x = -rx;
                y = -ry + (s - 4 * rx - 2 * ry);
            }
            boolean side = Math.abs(x) < hl;
            c.x[i] = x;
            c.y[i] = y;
            c.touch[i] = side ? 1.0 : 0.35;
            c.density[i] = full * (side ? 1.0 : 0.65);
            c.homeShare[i] = neutral ? (x > 0 ? 0.2 : 0.8) : (x >= hl ? 0.1 : 0.88);
        }
        world.spawn(SpawnRole.CROWD_SECTION).add(c);
        return c;
    }

    private CrowdComponent crowd() {
        if (crowd == null && crowds != null && crowds.size() > 0) {
            crowd = crowdMap.get(crowds.get(0));
        }
        return crowd;
    }

    /** The arrays (tests). */
    CrowdComponent crowdForTest() {
        return crowd();
    }

    /** True once the world has a crowd entity. */
    public boolean present() {
        return crowd() != null;
    }

    /** Mood of section {@code i} (-1..1), 0 when the world has no crowd. */
    public double mood(int i) {
        CrowdComponent c = crowd();
        return c == null ? 0 : c.mood[i];
    }

    /** Encroachment of section {@code i} (0..1). */
    public double encroachment(int i) {
        CrowdComponent c = crowd();
        return c == null ? 0 : c.encroach[i];
    }

    public int sections() {
        CrowdComponent c = crowd();
        return c == null ? 0 : c.count;
    }

    /**
     * Mood (-1..1) of the crowd at a ground point, for the viewer: the section nearest to it. Allocation free.
     */
    public double moodAt(double x, double y) {
        CrowdComponent c = crowd();
        if (c == null) {
            return 0;
        }
        int best = 0;
        double bd = Double.MAX_VALUE;
        for (int i = 0; i < c.count; i++) {
            double dx = c.x[i] - x;
            double dy = c.y[i] - y;
            double d = dx * dx + dy * dy;
            if (d < bd) {
                bd = d;
                best = i;
            }
        }
        return c.mood[best];
    }

    /**
     * How much the crowd round a ground point is for {@code team} (-1 against .. 1 for): the sections within
     * {@link #REACH_M}, each weighted by its density, nearness, mood and which side its people are on. 0 when the
     * world has no crowd.
     */
    public double support(double x, double y, Team team) {
        CrowdComponent c = crowd();
        if (c == null) {
            return 0;
        }
        double sum = 0;
        double norm = 0;
        for (int i = 0; i < c.count; i++) {
            double dx = c.x[i] - x;
            double dy = c.y[i] - y;
            double d = StrictMath.sqrt(dx * dx + dy * dy);
            if (d >= REACH_M) {
                continue;
            }
            double w = 1 - d / REACH_M;
            sum += w * c.density[i] * c.mood[i] * (2 * c.homeShare[i] - 1);
            norm += w;
        }
        if (norm <= 0) {
            return 0;
        }
        double home = sum / norm;
        return team == Team.HOME ? home : -home;
    }

    @Override
    public void update(float deltaTime) {
        CrowdComponent c = crowd();
        if (c == null) {
            return;
        }
        if (actions == null) {
            actions = getEngine().getSystem(ActionSystem.class);
            laws = getEngine().getSystem(LawsSystem.class);
            teamOf = new int[bodies.size() + 64];
            java.util.Arrays.fill(teamOf, -1);
            for (int k = 0; k < bodies.size(); k++) {
                Entity e = bodies.get(k);
                int index = e.getComponent(IdentityComponent.class).index;
                if (index >= teamOf.length) {
                    teamOf = java.util.Arrays.copyOf(teamOf, index + 64);
                    java.util.Arrays.fill(teamOf, teamOf.length - 64, teamOf.length, -1);
                }
                teamOf[index] = e.getComponent(BodyComponent.class).team.ordinal();
            }
        }
        if (actions != null) {
            for (int k = 0, n = actions.recordCount(); k < n; k++) {
                ActionRecord r = actions.record(k);
                if (r.outcome == ActionRecord.KICKED && r.intent == ActionIntent.SHOT) {
                    react(c, r.targetX, r.targetY, r.team == Team.HOME.ordinal() ? SHOT : -SHOT);
                }
            }
        }
        if (laws != null) {
            for (int k = 0, n = laws.recordCount(); k < n; k++) {
                law(c, laws.record(k));
            }
        }
        double rise = ENCROACH_PER_SECOND * dt;
        for (int i = 0; i < c.count; i++) {
            c.mood[i] *= settle;
            double heat = Math.abs(c.mood[i]) - ENCROACH_FROM;
            double e = c.encroach[i] * encroachSettle;
            if (heat > 0) {
                e += rise * heat / (1 - ENCROACH_FROM) * c.touch[i] * c.density[i] * 10;
            }
            c.encroach[i] = e > 1 ? 1 : e;
        }
    }

    private void law(CrowdComponent c, LawRecord r) {
        int home = Team.HOME.ordinal();
        switch (r.type) {
            case LawRecord.GOAL:
                react(c, r.x, r.y, r.scoringTeam == home ? GOAL : -GOAL);
                break;
            case LawRecord.DISPUTED_GOAL:
                if (r.scoringTeam >= 0) {
                    react(c, r.x, r.y, r.scoringTeam == home ? GOAL : -GOAL);
                    // the appealing side's people are angry that it stood
                    react(c, r.x, r.y, r.appealingTeam == home ? -DISPUTE : DISPUTE);
                } else {
                    react(c, r.x, r.y, r.appealingTeam == home ? DISPUTE : -DISPUTE);
                }
                break;
            case LawRecord.FOUL:
            case LawRecord.HANDBALL:
            case LawRecord.CARRYING:
                if (r.appealed && !r.given) {
                    react(c, r.x, r.y, r.appealingTeam == home ? -REFUSED_APPEAL : REFUSED_APPEAL);
                } else if (r.type == LawRecord.FOUL && r.given) {
                    int victim = team(r.victim);
                    if (victim == home) {
                        react(c, r.x, r.y, -FOUL_AGAINST);
                    } else if (victim >= 0) {
                        react(c, r.x, r.y, FOUL_AGAINST * 0.5);
                    }
                }
                break;
            case LawRecord.OFFSIDE:
                if (r.appealed && !r.given) {
                    react(c, r.x, r.y, r.appealingTeam == home ? -REFUSED_APPEAL : REFUSED_APPEAL);
                }
                break;
            default:
                break;
        }
    }

    private int team(int index) {
        return index >= 0 && index < teamOf.length ? teamOf[index] : -1;
    }

    /** An event of {@code value} (home point of view) at (x, y): each section feels it by its side and nearness. */
    private void react(CrowdComponent c, double x, double y, double value) {
        for (int i = 0; i < c.count; i++) {
            double dx = c.x[i] - x;
            double dy = c.y[i] - y;
            double d = StrictMath.sqrt(dx * dx + dy * dy);
            double near = 1 - (1 - FAR_FEEL) * Math.min(1, d / FEEL_M);
            double m = c.mood[i] + value * (2 * c.homeShare[i] - 1) * near;
            c.mood[i] = m > 1 ? 1 : (m < -1 ? -1 : m);
        }
    }
}
