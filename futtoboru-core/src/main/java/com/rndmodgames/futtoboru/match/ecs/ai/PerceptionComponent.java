package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * What one player believes (E-15, §4.1): where the ball and every other player were a reaction time ago, with noise
 * that grows with distance and behind his back. Refreshed by {@link PerceptionSystem} at the think rate; the
 * {@link DecisionSystem} decides on beliefs, never on truth (passes go to where a man was). Player arrays are indexed
 * by the AI's player order (creation order of the player entities); his own entry is the truth.
 */
public final class PerceptionComponent implements Component, HashedComponent {

    public double ballX, ballY, ballZ, ballVx, ballVy, ballVz;
    public final double[] x, y, vx, vy;
    /** Tick of the snapshot the beliefs come from, -1 = never refreshed. */
    public long seenTick = -1;

    public PerceptionComponent(int players) {
        x = new double[players];
        y = new double[players];
        vx = new double[players];
        vy = new double[players];
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("per.seen", seenTick);
        h.mix("per.bx", ballX);
        h.mix("per.by", ballY);
        h.mix("per.bz", ballZ);
        h.mix("per.bvx", ballVx);
        h.mix("per.bvy", ballVy);
        h.mix("per.bvz", ballVz);
        for (int j = 0; j < x.length; j++) {
            h.mix("per.x", x[j]);
            h.mix("per.y", y[j]);
            h.mix("per.vx", vx[j]);
            h.mix("per.vy", vy[j]);
        }
    }
}
