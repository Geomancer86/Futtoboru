package com.rndmodgames.futtoboru.match.ecs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;

/**
 * One match: one Ashley {@link Engine}, no Gdx, no statics (docs/design/MATCH_ENGINE_ECS.md §2, E-10).
 *
 * <ul>
 * <li>{@link #tick()} runs every {@link SimSystem} once, in {@link SystemPriority} order: one tick of
 * {@link #tickSeconds()}. Systems never see a frame delta.</li>
 * <li>{@link DrawPass} systems are added to the engine with processing off, so a tick never runs them;
 * {@link #draw(FrameContext)} runs them once per frame. {@link #frame(FixedStepClock, double)} is the simplest
 * viewer loop; {@code match.ecs.view.MatchViewer} is the real one (time budget, interpolation).</li>
 * <li>A tick runs inside {@link ArtGuard#enterSimulation()}: a simulation system that asks for art throws.</li>
 * <li>Entities are created only through {@link #spawn(SpawnRole)}, in {@link SpawnRole} order and before the first
 * tick, so iteration order (and the state hash) is fixed.</li>
 * <li>{@link #stateHash()} hashes the tick and every {@link HashedComponent} in creation order.</li>
 * </ul>
 *
 * Not thread-safe; one world per thread.
 */
public final class MatchWorld {

    private final Engine engine = new Engine();
    private final PhysicsParams params;
    private final PitchConditions pitch;
    private final RandomSource random;
    private final double tickSeconds;

    private final List<DrawPass> drawPasses = new ArrayList<>();
    private final List<Integer> drawPriorities = new ArrayList<>();
    private final List<Integer> simPriorities = new ArrayList<>();
    private final ComponentMapper<? extends Component>[] hashedMappers;
    private final StateHash hash = new StateHash();
    private final PlainFrame plainFrame = new PlainFrame();

    private long tick;
    private int nextIndex;
    private SpawnRole lastRole;
    private boolean drawGuard;

    /**
     * @param random the match fork ({@code game.random().fork("match:" + key)}); systems take named sub-forks
     */
    @SuppressWarnings("unchecked")
    public MatchWorld(PhysicsParams params, PitchConditions pitch, RandomSource random) {
        if (params == null || pitch == null || random == null) {
            throw new IllegalArgumentException("params, pitch and random are required");
        }
        this.params = params.validate();
        this.pitch = pitch;
        this.random = random;
        this.tickSeconds = params.tickSeconds();
        List<Class<? extends Component>> hashed = MatchSystems.HASHED_COMPONENTS;
        hashedMappers = new ComponentMapper[hashed.size()];
        for (int i = 0; i < hashedMappers.length; i++) {
            hashedMappers[i] = ComponentMapper.getFor(hashed.get(i));
        }
    }

    // ---- entities -------------------------------------------------------------------------------------------------

    /** Creates an entity with its {@link IdentityComponent}; roles must come in {@link SpawnRole} order. */
    public Entity spawn(SpawnRole role) {
        if (tick > 0) {
            throw new IllegalStateException("entities are created before the first tick");
        }
        if (lastRole != null && role.ordinal() < lastRole.ordinal()) {
            throw new IllegalStateException(role + " spawned after " + lastRole + ": creation order is fixed");
        }
        lastRole = role;
        Entity e = new Entity();
        e.add(new IdentityComponent(nextIndex++, role));
        engine.addEntity(e);
        return e;
    }

    // ---- systems -------------------------------------------------------------------------------------------------

    /** Adds a simulation system; its Ashley priority must be a unique {@link SystemPriority} value. */
    public <T extends EntitySystem & SimSystem> T addSimulation(T system) {
        if (system instanceof DrawPass) {
            throw new IllegalArgumentException(name(system) + " is both a SimSystem and a DrawPass");
        }
        checkUnique(system, simPriorities);
        engine.addSystem(system);
        return system;
    }

    /** Adds a draw pass; it never runs inside {@link #tick()}. */
    public <T extends EntitySystem & DrawPass> T addDrawPass(T pass) {
        if (pass instanceof SimSystem) {
            throw new IllegalArgumentException(name(pass) + " is both a SimSystem and a DrawPass");
        }
        checkUnique(pass, drawPriorities);
        pass.setProcessing(false);
        engine.addSystem(pass);
        // checkUnique appended the priority; move it so both lists stay sorted by priority.
        drawPriorities.remove(drawPriorities.size() - 1);
        int at = 0;
        while (at < drawPriorities.size() && drawPriorities.get(at) < pass.priority) {
            at++;
        }
        drawPriorities.add(at, pass.priority);
        drawPasses.add(at, pass);
        return pass;
    }

    private void checkUnique(EntitySystem system, List<Integer> priorities) {
        if (priorities.contains(system.priority)) {
            throw new IllegalArgumentException(name(system) + ": priority " + system.priority + " already used");
        }
        priorities.add(system.priority);
    }

    private static String name(Object o) {
        return o.getClass().getSimpleName();
    }

    // ---- running ------------------------------------------------------------------------------------------------

    /** One fixed simulation step. */
    public void tick() {
        ArtGuard.enterSimulation();
        try {
            engine.update((float) tickSeconds);
        } finally {
            ArtGuard.exitSimulation();
        }
        tick++;
    }

    public void runTicks(long ticks) {
        for (long i = 0; i < ticks; i++) {
            tick();
        }
    }

    /** Runs every draw pass once with only an interpolation share (no frame time); see {@link #draw(FrameContext)}. */
    public void draw(double alpha) {
        plainFrame.set(alpha, 0, tick);
        draw(plainFrame);
    }

    /** Runs every draw pass once; with the draw guard on, throws if a pass changed the simulation state. */
    public void draw(FrameContext frame) {
        for (int i = 0; i < drawPasses.size(); i++) {
            DrawPass pass = drawPasses.get(i);
            if (drawGuard) {
                long before = stateHash();
                pass.draw(frame);
                if (stateHash() != before) {
                    throw new IllegalStateException("draw pass " + name(pass) + " changed the simulation state");
                }
            } else {
                pass.draw(frame);
            }
        }
    }

    /** Simplest viewer loop for one frame: the clock's steps, then one draw. Returns the number of ticks run. */
    public int frame(FixedStepClock clock, double frameSeconds) {
        int steps = clock.advance(frameSeconds);
        runTicks(steps);
        plainFrame.set(clock.alpha(), frameSeconds, tick);
        draw(plainFrame);
        return steps;
    }

    /** The frame context of {@link #draw(double)} and {@link #frame(FixedStepClock, double)}; one per world. */
    private static final class PlainFrame implements FrameContext {
        private double alpha;
        private double frameSeconds;
        private double viewSeconds;
        private long tick;

        void set(double alpha, double frameSeconds, long tick) {
            this.alpha = alpha;
            this.frameSeconds = frameSeconds;
            this.viewSeconds += frameSeconds;
            this.tick = tick;
        }

        @Override
        public double alpha() {
            return alpha;
        }

        @Override
        public double frameSeconds() {
            return frameSeconds;
        }

        @Override
        public double viewSeconds() {
            return viewSeconds;
        }

        @Override
        public long tick() {
            return tick;
        }
    }

    /** Hash the state around every draw pass (tests, debug viewer). */
    public void setDrawGuard(boolean on) {
        drawGuard = on;
    }

    // ---- state hash -----------------------------------------------------------------------------------------------

    /** Hash of the tick number and all hashed components; allocation-free. */
    public long stateHash() {
        return computeHash(null);
    }

    /** The values behind {@link #stateHash()}, one per line, for diffing two runs. */
    public String describeState() {
        StringBuilder sb = new StringBuilder();
        long h = computeHash(sb);
        sb.append("hash=").append(Long.toHexString(h)).append('\n');
        return sb.toString();
    }

    private long computeHash(StringBuilder describe) {
        hash.begin(describe);
        hash.mix("tick", tick);
        ImmutableArray<Entity> entities = engine.getEntities();
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            for (int c = 0; c < hashedMappers.length; c++) {
                Component component = hashedMappers[c].get(e);
                if (component != null) {
                    ((HashedComponent) component).hashInto(hash);
                }
            }
        }
        return hash.value();
    }

    // ---- accessors ------------------------------------------------------------------------------------------------

    public Engine engine() {
        return engine;
    }

    public PhysicsParams params() {
        return params;
    }

    public PitchConditions pitch() {
        return pitch;
    }

    /** The match fork; take a named sub-fork per purpose ({@code fork("ai:" + id)}), never draw from it directly. */
    public RandomSource random() {
        return random;
    }

    public double tickSeconds() {
        return tickSeconds;
    }

    /** Ticks run so far. */
    public long tickCount() {
        return tick;
    }

    /** Simulated seconds so far. */
    public double elapsedSeconds() {
        return tick * tickSeconds;
    }
}
