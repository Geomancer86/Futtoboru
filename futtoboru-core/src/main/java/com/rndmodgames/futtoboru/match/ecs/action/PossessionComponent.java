package com.rndmodgames.futtoboru.match.ecs.action;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * Who last played the ball and whether a keeper holds it (E-14), on the ball entity. Read by the laws (E-16:
 * offside "when the ball is played", carrying) and events (E-17: possession). {@link ActionSystem} adds it to the ball
 * on its first tick if the ball has none. While {@link #held}, the ball system leaves the ball to the keeper's hands.
 */
public final class PossessionComponent implements Component, HashedComponent {

    /** Creation index ({@code IdentityComponent.index}) of the last player to play the ball, -1 = nobody yet. */
    public int lastTouchIndex = -1;
    /** {@code Team.ordinal()} of that player, -1 = nobody yet. */
    public int lastTouchTeam = -1;
    /** {@link ActionIntent} ordinal of the last touch. */
    public int lastTouchIntent;
    /** Tick of the last touch, -1 = none. */
    public long lastTouchTick = -1;
    /** Touches so far this match. */
    public long touches;

    /** A keeper holds the ball in his hands. */
    public boolean held;
    /** Creation index of the holding keeper, -1 = none. */
    public int holderIndex = -1;
    /** Distance carried since the ball came into his hands (or since his last bounce), m. */
    public double carriedM;
    /** Holder position last tick, for {@link #carriedM}. */
    public double holderLastX, holderLastY;
    /** Times a keeper reached the carrying limit and had to let the ball go (for the laws, E-16). */
    public int carryLimitReleases;

    public void touch(int index, int team, ActionIntent intent, long tick) {
        lastTouchIndex = index;
        lastTouchTeam = team;
        lastTouchIntent = intent.ordinal();
        lastTouchTick = tick;
        touches++;
    }

    public void hold(int keeperIndex, double x, double y) {
        held = true;
        holderIndex = keeperIndex;
        carriedM = 0;
        holderLastX = x;
        holderLastY = y;
    }

    public void release() {
        held = false;
        holderIndex = -1;
        carriedM = 0;
    }

    /** Steps carried so far at {@code stepLengthM} per step. */
    public double carrySteps(double stepLengthM) {
        return carriedM / stepLengthM;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("pos.lastIndex", lastTouchIndex);
        h.mix("pos.lastTeam", lastTouchTeam);
        h.mix("pos.lastIntent", lastTouchIntent);
        h.mix("pos.lastTick", lastTouchTick);
        h.mix("pos.touches", touches);
        h.mix("pos.held", held ? 1 : 0);
        h.mix("pos.holder", holderIndex);
        h.mix("pos.carried", carriedM);
        h.mix("pos.holderX", holderLastX);
        h.mix("pos.holderY", holderLastY);
        h.mix("pos.carryLimit", carryLimitReleases);
    }
}
