package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * An official on the ground (E-16): the referee or one of the two club umpires. A point body on the ground plane
 * (axes as the ball) that walks or jogs to keep a view of play ({@link OfficialSystem}); officials take no part in
 * body or ball contacts. {@link LawsSystem} judges appeals from where each official stands
 * ({@link Adjudicator}).
 */
public final class OfficialComponent implements Component, HashedComponent {

    private static final Team[] TEAMS = Team.values();

    public static final int REFEREE = 0;
    public static final int UMPIRE = 1;

    /** {@link #REFEREE} or {@link #UMPIRE}. */
    public final int role;
    /** {@code Team.ordinal()} of the club that supplied an umpire; -1 for the referee (neutral). */
    public final int club;
    /** Perception noise multiplier, 1 = average. */
    public final double perception;
    /** Lean toward {@link #club}, 0..1 (0 for the referee). */
    public final double partisanship;

    public double x, y;
    public double vx, vy;

    public OfficialComponent(int role, Team club, double perception, double partisanship, double x, double y) {
        if (role != REFEREE && role != UMPIRE) {
            throw new IllegalArgumentException("role " + role);
        }
        if (role == UMPIRE && club == null) {
            throw new IllegalArgumentException("an umpire belongs to a club");
        }
        if (!(perception >= 0) || !(partisanship >= 0 && partisanship <= 1)) {
            throw new IllegalArgumentException("perception >= 0, partisanship 0..1");
        }
        this.role = role;
        this.club = club == null ? -1 : club.ordinal();
        this.perception = perception;
        this.partisanship = club == null ? 0 : partisanship;
        this.x = x;
        this.y = y;
    }

    public boolean isReferee() {
        return role == REFEREE;
    }

    /** The umpire's club, or null for the referee. */
    public Team clubTeam() {
        return club < 0 ? null : TEAMS[club];
    }

    public double speed() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("off.role", role);
        h.mix("off.club", club);
        h.mix("off.perception", perception);
        h.mix("off.partisanship", partisanship);
        h.mix("off.x", x);
        h.mix("off.y", y);
        h.mix("off.vx", vx);
        h.mix("off.vy", vy);
    }
}
