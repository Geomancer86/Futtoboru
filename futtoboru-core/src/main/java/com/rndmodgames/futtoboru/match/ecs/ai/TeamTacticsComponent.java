package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * One side's tactics (E-15), on that side's {@code SpawnRole.TEAM} entity: the shape and style picked before the match
 * (no tactics changes yet), the direction it attacks this half, and who plays in which slot now. A player is
 * identified by his original slot ({@code BodyComponent.slot}, the team-sheet order).
 *
 * <p>Playing short (V2 §2.7, no substitutes in 1888-89): when a player goes off, the hole is filled from the front,
 * keeper first, then backs, then halves (a forward drops back); the forward line plays with one man fewer. A passenger
 * swaps to a wing (period reports put the hurt man out on the wing).
 */
public final class TeamTacticsComponent implements Component, HashedComponent {

    public static final int NO_SLOT = -1;

    public final Team team;
    public final Shape shape;
    public final TeamStyle style;
    /** +1 when attacking the EAST goal (+x), -1 when attacking WEST. Follows the laws' ends (half-time). */
    public int attackSign;
    /** Shape slot played now by the player of original slot k, or {@link #NO_SLOT} when he is off. */
    public final int[] slotOf = new int[11];
    /** Players of this side already handled as off / moved as passengers (bit per original slot). */
    public int offMask, passengerMask;
    /** Original slot of the player who takes the current restart, -1 none. */
    public int restartTaker = -1;
    /** Ready tick of the restart {@link #restartTaker} was picked for (identifies the dead ball). */
    public long takerForRestart = Long.MIN_VALUE;

    public TeamTacticsComponent(Team team, Shape shape, TeamStyle style, MatchEnds ends) {
        if (team == null || shape == null || style == null || ends == null) {
            throw new IllegalArgumentException("team, shape, style and ends are required");
        }
        this.team = team;
        this.shape = shape;
        this.style = style;
        setEnds(ends);
        for (int k = 0; k < 11; k++) {
            slotOf[k] = k;
        }
    }

    public void setEnds(MatchEnds ends) {
        attackSign = -ends.endDefendedBy(team).sign();
    }

    /** Original slot of the player in shape slot {@code s} now, or -1 when nobody plays there. */
    public int playerIn(int s) {
        for (int k = 0; k < 11; k++) {
            if (slotOf[k] == s) {
                return k;
            }
        }
        return -1;
    }

    /** Players still on the field. */
    public int onField() {
        return 11 - Integer.bitCount(offMask);
    }

    /**
     * Player {@code k} has left the field: his slot is filled from the front. Keeper slot: the outfielder of the
     * highest-handling original slot passed in {@code keeperPick} (-1: none), else a back's or half's slot: the forward
     * nearest across the pitch drops back.
     */
    public void goneOff(int k, int keeperPick) {
        if ((offMask & (1 << k)) != 0) {
            return;
        }
        offMask |= 1 << k;
        int hole = slotOf[k];
        slotOf[k] = NO_SLOT;
        if (hole == NO_SLOT) {
            return;
        }
        if (hole == 0) {
            if (keeperPick >= 0 && slotOf[keeperPick] != NO_SLOT) {
                int vacated = slotOf[keeperPick];
                slotOf[keeperPick] = 0;
                fillFromFront(vacated);
            }
            return;
        }
        fillFromFront(hole);
    }

    /** Fills a back or half-back slot with the forward nearest across; a forward's hole stays open. */
    private void fillFromFront(int hole) {
        if (isForward(shape.slot(hole).position())) {
            return;
        }
        double lateral = shape.slot(hole).lateral();
        int best = -1;
        double bestGap = Double.MAX_VALUE;
        for (int k = 0; k < 11; k++) {
            int s = slotOf[k];
            if (s == NO_SLOT || !isForward(shape.slot(s).position())) {
                continue;
            }
            double gap = Math.abs(shape.slot(s).lateral() - lateral);
            if (gap < bestGap) {
                bestGap = gap;
                best = k;
            }
        }
        if (best >= 0) {
            slotOf[best] = hole;
        }
    }

    /** A passenger goes out to the wing: he swaps slots with the outside forward on his side (not the keeper). */
    public void passenger(int k) {
        if ((passengerMask & (1 << k)) != 0) {
            return;
        }
        passengerMask |= 1 << k;
        int s = slotOf[k];
        if (s <= 0 || isWing(shape.slot(s).position())) {
            return;
        }
        Position wing = shape.slot(s).lateral() >= 0 ? Position.LEFT_WING : Position.RIGHT_WING;
        for (int j = 0; j < 11; j++) {
            int o = slotOf[j];
            if (o > 0 && shape.slot(o).position() == wing && (passengerMask & (1 << j)) == 0) {
                slotOf[j] = s;
                slotOf[k] = o;
                return;
            }
        }
    }

    static boolean isForward(Position p) {
        return p == Position.RIGHT_WING || p == Position.INSIDE_RIGHT || p == Position.CENTRE_FORWARD
                || p == Position.INSIDE_LEFT || p == Position.LEFT_WING;
    }

    static boolean isWing(Position p) {
        return p == Position.RIGHT_WING || p == Position.LEFT_WING;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("tac.team", team.ordinal());
        h.mix("tac.shape", shape.ordinal());
        h.mix("tac.style", style.ordinal());
        h.mix("tac.attack", attackSign);
        for (int k = 0; k < 11; k++) {
            h.mix("tac.slot", slotOf[k]);
        }
        h.mix("tac.off", offMask);
        h.mix("tac.passenger", passengerMask);
        h.mix("tac.taker", restartTaker);
        h.mix("tac.takerTick", takerForRestart);
    }
}
