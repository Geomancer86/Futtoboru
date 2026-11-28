package com.rndmodgames.futtoboru.match.ecs.laws;

/**
 * The offside law as geometry (E-16; V2 §1): a player is in an offside position when, at the moment a team-mate plays
 * the ball, he is nearer the opponents' goal line than the ball <b>and</b> fewer than {@code n} opponents (keeper
 * included; 3 until 1925, then 2) are level with him or nearer their goal line. Level is onside. From 1907 a player in
 * his own half is never offside. Distances are "depths": metres from the goal line the side attacks, measured along
 * the pitch ({@code halfLength - attackSign * x}).
 *
 * The result is a signed margin in metres: {@code > 0} offside by that much, {@code <= 0} onside by that much.
 */
public final class OffsideRule {

    private OffsideRule() {
    }

    /** Depth of {@code x} for a side attacking toward {@code attackSign} (+1 = east goal). */
    public static double depth(double x, int attackSign, double halfLength) {
        return halfLength - attackSign * x;
    }

    /**
     * @param player        depth of the player
     * @param ball          depth of the ball when it was played
     * @param nthOpponent   depth of the n-th opponent nearest their goal line ({@link Double#POSITIVE_INFINITY} if
     *                      fewer than n are on the field)
     * @param ownHalfExempt the 1907 law: no offside in his own half
     * @param halfLength    half the pitch length (the halfway line is at this depth)
     */
    public static double margin(double player, double ball, double nthOpponent, boolean ownHalfExempt,
            double halfLength) {
        double m = Math.min(ball - player, nthOpponent - player);
        if (ownHalfExempt) {
            m = Math.min(m, halfLength - player);
        }
        return m;
    }

    /**
     * Convenience for tests and tools: the margin of a player at {@code playerX} when the ball is played at
     * {@code ballX}, with opponents at {@code opponentX[0..count)}.
     */
    public static double margin(double playerX, double ballX, double[] opponentX, int count, int attackSign,
            RulesProfile rules, double halfLength) {
        NthSmallest nth = new NthSmallest(rules.offsideOpponents());
        for (int i = 0; i < count; i++) {
            nth.add(depth(opponentX[i], attackSign, halfLength));
        }
        return margin(depth(playerX, attackSign, halfLength), depth(ballX, attackSign, halfLength), nth.value(),
                rules.offsideOwnHalfExempt(), halfLength);
    }

    /** The n-th smallest of a stream of values (n = 1..3), allocation-free after construction. */
    public static final class NthSmallest {
        private final int n;
        private final double[] best = new double[3];
        private int count;

        public NthSmallest(int n) {
            if (n < 1 || n > 3) {
                throw new IllegalArgumentException("n must be 1..3");
            }
            this.n = n;
        }

        public void reset() {
            count = 0;
        }

        public void add(double v) {
            int at = Math.min(count, n);
            if (at == n && v >= best[n - 1]) {
                return;
            }
            if (at == n) {
                at = n - 1;
            } else {
                count++;
            }
            while (at > 0 && best[at - 1] > v) {
                best[at] = best[at - 1];
                at--;
            }
            best[at] = v;
        }

        /** The n-th smallest value added, or +infinity if fewer than n were added. */
        public double value() {
            return count >= n ? best[n - 1] : Double.POSITIVE_INFINITY;
        }
    }
}
