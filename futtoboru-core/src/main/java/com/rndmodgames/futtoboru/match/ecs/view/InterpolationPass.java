package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Arrays;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;

/**
 * First draw pass of every frame (docs/design/MATCH_VIEWER_PLAN.md §2.3; DarkBlade has no equivalent, it draws at
 * the simulation rate). The 30 Hz simulation is shown at the display rate by blending each drawn entity's last two
 * simulated positions by the frame's alpha into its {@link ViewTransform}.
 *
 * <ul>
 * <li>{@link #capture()} stores the current simulation position as "current" and the old current as "previous".
 * {@link MatchViewer} calls it after every tick, so with several ticks in one frame "previous" is still the tick
 * just before the last one. A frame drawn after ticks nobody captured (a caller ticking the world directly) captures
 * itself: after exactly one tick that is the same; after a jump of several ticks the entity snaps (no blend across
 * the jump).</li>
 * <li>Writes only its own {@link ViewTransform}s; reads simulation components through {@link PoseSource}s. The draw
 * guard proves the simulation state is untouched.</li>
 * <li>No allocation per frame once every drawn entity has its slot.</li>
 * </ul>
 */
public final class InterpolationPass extends EntitySystem implements DrawPass {

    private final MatchWorld world;
    private final PoseSource[] sources;
    private final ImmutableArray<?>[] entities;
    private final ComponentMapper<IdentityComponent> ids = ComponentMapper.getFor(IdentityComponent.class);
    private final double[] xyz = new double[3];

    /** Slot per entity creation index. */
    private ViewTransform[] slots = new ViewTransform[32];
    /** Slots in first-capture order, for iteration. */
    private ViewTransform[] live = new ViewTransform[32];
    private int liveCount;
    private long capturedTick = -1;

    public InterpolationPass(MatchWorld world, PoseSource... sources) {
        super(SystemPriority.DRAW_INTERPOLATION);
        if (world == null || sources.length == 0) {
            throw new IllegalArgumentException("a world and at least one pose source are required");
        }
        this.world = world;
        this.sources = sources.clone();
        this.entities = new ImmutableArray<?>[sources.length];
    }

    @Override
    public void addedToEngine(Engine engine) {
        for (int i = 0; i < sources.length; i++) {
            entities[i] = engine.getEntitiesFor(sources[i].family());
        }
    }

    /** Stores the simulation positions of the current tick (once per tick; a second call in the same tick is free). */
    public void capture() {
        long now = world.tickCount();
        if (now == capturedTick) {
            return;
        }
        for (int s = 0; s < sources.length; s++) {
            PoseSource source = sources[s];
            ImmutableArray<?> list = entities[s];
            if (list == null) {
                continue;
            }
            for (int i = 0; i < list.size(); i++) {
                Entity e = (Entity) list.get(i);
                ViewTransform t = slot(e);
                source.read(e, xyz);
                if (t.tick >= 0 && t.tick == now - 1) {
                    t.prevX = t.currX;
                    t.prevY = t.currY;
                    t.prevZ = t.currZ;
                } else {
                    t.prevX = xyz[0];
                    t.prevY = xyz[1];
                    t.prevZ = xyz[2];
                }
                t.currX = xyz[0];
                t.currY = xyz[1];
                t.currZ = xyz[2];
                t.tick = now;
            }
        }
        capturedTick = now;
    }

    @Override
    public void draw(FrameContext frame) {
        capture();
        double a = frame.alpha();
        a = a < 0 ? 0 : (a > 1 ? 1 : a);
        for (int i = 0; i < liveCount; i++) {
            ViewTransform t = live[i];
            t.x = (float) (t.prevX + (t.currX - t.prevX) * a);
            t.y = (float) (t.prevY + (t.currY - t.prevY) * a);
            t.z = (float) (t.prevZ + (t.currZ - t.prevZ) * a);
        }
    }

    /** The entity's view transform, or null when no source reads it (or nothing was captured yet). */
    public ViewTransform of(Entity entity) {
        IdentityComponent id = ids.get(entity);
        if (id == null || id.index >= slots.length) {
            return null;
        }
        return slots[id.index];
    }

    /** Number of entities with a view transform. */
    public int size() {
        return liveCount;
    }

    /**
     * The {@code i}-th view transform (0..size-1), in first-capture order (pose sources in order, each in creation
     * order).
     */
    public ViewTransform get(int i) {
        if (i < 0 || i >= liveCount) {
            throw new IndexOutOfBoundsException(i + " of " + liveCount);
        }
        return live[i];
    }

    private ViewTransform slot(Entity e) {
        IdentityComponent id = ids.get(e);
        if (id == null) {
            throw new IllegalStateException("drawn entity without IdentityComponent");
        }
        int index = id.index;
        if (index >= slots.length) {
            slots = Arrays.copyOf(slots, Math.max(index + 1, slots.length * 2));
        }
        ViewTransform t = slots[index];
        if (t == null) {
            t = new ViewTransform(e);
            slots[index] = t;
            if (liveCount == live.length) {
                live = Arrays.copyOf(live, live.length * 2);
            }
            live[liveCount++] = t;
        }
        return t;
    }
}
