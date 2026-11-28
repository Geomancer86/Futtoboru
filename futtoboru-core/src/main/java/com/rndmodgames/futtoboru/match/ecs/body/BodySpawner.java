package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.SkillComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchEnd;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Creates the 22 player bodies at their kick-off spots (E-13): home 1..11 then away 1..11 ({@link SpawnRole} order),
 * each with body, athlete, kit, injury, an idle {@link MoveCommand}, ball skills and an idle {@link ActionCommand}
 * (E-14).
 *
 * <p>Kick-off (Laws 1888-89, V2 §1): every player in his own half; the side kicking off has its kicker at the ball
 * on the centre spot and its forwards just behind halfway; the other side stays at least 10 yards from the ball.
 * Shin pads: the {@link AthleteInputs#shinPads} value, else a draw on {@code kit:shinpads:<team>:<slot>} against the
 * season's share (spawn time only).
 */
public final class BodySpawner {

    /** Gap kept behind the halfway line by the kicking side's forwards, m (D). */
    public static final double FRONT_GAP_M = 1.0;
    /** The receiving side stands this far beyond the 10-yard limit, m (D). */
    public static final double TEN_YARD_MARGIN_M = 0.5;

    private BodySpawner() {
    }

    /**
     * Spawns both teams. Call before spawning the ball and officials.
     *
     * @return the 22 entities, home slots 0..10 then away slots 0..10
     */
    public static Entity[] spawnTeams(MatchWorld world, PitchGeometry pitch, TeamSheet home, TeamSheet away,
            KitParams kit, MatchEnds ends, Team kickingOff) {
        kit.validate();
        Entity[] out = new Entity[22];
        for (int k = 0; k < 11; k++) {
            out[k] = spawnOne(world, pitch, home, k, Team.HOME, kit, ends, kickingOff);
        }
        for (int k = 0; k < 11; k++) {
            out[11 + k] = spawnOne(world, pitch, away, k, Team.AWAY, kit, ends, kickingOff);
        }
        return out;
    }

    /** Both teams as stubs in 2-3-5, home defending the west goal and kicking off, 1888-89 kit. */
    public static Entity[] spawnDefault(MatchWorld world, PitchGeometry pitch) {
        return spawnTeams(world, pitch, TeamSheet.stub(Shape.TWO_THREE_FIVE), TeamSheet.stub(Shape.TWO_THREE_FIVE),
                KitParams.season1888(), new MatchEnds(Team.HOME), Team.HOME);
    }

    private static Entity spawnOne(MatchWorld world, PitchGeometry pitch, TeamSheet sheet, int slotIndex, Team team,
            KitParams kit, MatchEnds ends, Team kickingOff) {
        BodyParams p = world.params().body;
        Shape.Slot slot = sheet.shape().slot(slotIndex);
        AthleteInputs in = sheet.players().get(slotIndex);
        int pos = in.position().ordinal();
        double radius = p.radiusByPosition[pos];
        double mass = p.massByPosition[pos];

        PitchEnd own = ends.endDefendedBy(team);
        int s = own.sign(); // -1: own goal at west, attacks +x
        double attack = -s;
        double x;
        double y = attack * slot.lateral() * pitch.halfWidth();
        if (slot.depth() < 1) {
            x = s * (pitch.halfLength() - slot.depth() * pitch.halfLength());
        } else if (team == kickingOff) {
            if (slotIndex == sheet.shape().kickerSlot()) {
                double ballRadius = world.params().ballRadiusM;
                x = s * (radius + ballRadius + 0.05);
                y = 0;
            } else {
                x = s * (FRONT_GAP_M + radius);
            }
        } else {
            double tenYards = 10 * PitchParams.YARD + radius + TEN_YARD_MARGIN_M;
            double across = Math.min(Math.abs(y), tenYards);
            x = s * Math.sqrt(tenYards * tenYards - across * across);
            x = s * Math.max(Math.abs(x), FRONT_GAP_M + radius);
        }
        double facing = attack > 0 ? 0 : StrictMath.PI;

        boolean pads = in.shinPads() != null ? in.shinPads()
                : world.random().fork("kit:shinpads:" + team + ":" + slotIndex).nextDouble() < kit.shinPadShare;
        Entity e = world.spawn(team == Team.HOME ? SpawnRole.HOME_PLAYER : SpawnRole.AWAY_PLAYER);
        BodyComponent body = new BodyComponent(team, slotIndex, radius, mass, x, y, facing);
        KitComponent kitComponent = KitComponent.of(kit, pads);
        body.massKg = mass + kitComponent.carriedMassKg();
        e.add(body);
        e.add(AthleteComponent.from(in, p));
        e.add(kitComponent);
        e.add(new InjuryComponent());
        e.add(new MoveCommand());
        e.add(SkillComponent.from(in, p));
        e.add(new ActionCommand());
        return e;
    }
}
