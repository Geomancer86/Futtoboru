package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** Test helpers for E-13: worlds and single bodies. */
final class Bodies {

    private Bodies() {
    }

    static MatchWorld world(PhysicsParams params, Surface surface, double wetness, long seed) {
        return new MatchWorld(params, new UniformPitch(surface, wetness), RandomSource.seeded(seed).fork("match:body"));
    }

    /** A world with the full simulation installed. */
    static MatchWorld simWorld(Surface surface, double wetness) {
        MatchWorld w = world(new PhysicsParams(), surface, wetness, 1888);
        com.rndmodgames.futtoboru.match.ecs.MatchSystems.installSimulation(w);
        return w;
    }

    /** A home player body with full components at (x, y). */
    static Entity add(MatchWorld w, AthleteInputs in, KitComponent kit, double x, double y, double facing) {
        BodyParams p = w.params().body;
        int pos = in.position().ordinal();
        return add(w, in, kit, p.massByPosition[pos], x, y, facing);
    }

    static Entity add(MatchWorld w, AthleteInputs in, KitComponent kit, double bodyMass, double x, double y,
            double facing) {
        BodyParams p = w.params().body;
        Entity e = w.spawn(SpawnRole.HOME_PLAYER);
        BodyComponent b = new BodyComponent(Team.HOME, 0, p.radiusByPosition[in.position().ordinal()], bodyMass, x, y,
                facing);
        b.massKg = bodyMass + kit.carriedMassKg();
        e.add(b);
        e.add(AthleteComponent.from(in, p));
        e.add(kit);
        e.add(new InjuryComponent());
        e.add(new MoveCommand());
        return e;
    }

    static Entity add(MatchWorld w, double x, double y) {
        return add(w, AthleteInputs.stub(Position.HALF_BACK), kit(Studs.BARS, false), x, y, 0);
    }

    static KitComponent kit(Studs studs, boolean pads) {
        KitParams k = KitParams.season1888();
        k.studs = studs;
        return KitComponent.of(k, pads);
    }

    static BodyComponent body(Entity e) {
        return e.getComponent(BodyComponent.class);
    }

    static AthleteComponent athlete(Entity e) {
        return e.getComponent(AthleteComponent.class);
    }

    static MoveCommand cmd(Entity e) {
        return e.getComponent(MoveCommand.class);
    }

    /**
     * Stand-in for locomotion in contact tests: keeps each body's velocity and moves it one tick (prev = start), so a
     * test sets velocities and sees only the contact system act.
     */
    static final class Drift extends EntitySystem implements SimSystem {
        final ComponentMapper<BodyComponent> bodies = ComponentMapper.getFor(BodyComponent.class);
        final double dt;
        ImmutableArray<Entity> entities;

        Drift(double dt) {
            super(com.rndmodgames.futtoboru.match.ecs.SystemPriority.LOCOMOTION);
            this.dt = dt;
        }

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(LocomotionSystem.family());
        }

        @Override
        public void update(float delta) {
            for (int i = 0; i < entities.size(); i++) {
                BodyComponent b = bodies.get(entities.get(i));
                b.prevX = b.x;
                b.prevY = b.y;
                if (!b.isDown()) {
                    b.x += b.vx * dt;
                    b.y += b.vy * dt;
                }
            }
        }
    }

    /** A world with only {@link Drift} and the contact system. */
    static MatchWorld contactWorld(PhysicsParams params, long seed) {
        MatchWorld w = world(params, Surface.DRY, 0, seed);
        w.addSimulation(new Drift(w.tickSeconds()));
        w.addSimulation(new ContactSystem(w));
        return w;
    }
}
