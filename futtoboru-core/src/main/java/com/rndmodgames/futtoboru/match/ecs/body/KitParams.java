package com.rndmodgames.futtoboru.match.ecs.body;

/**
 * Era kit of one season (BALL_AND_KIT_PHYSICS.md §2-3: {@code kit.*}); later {@code gamedata/core/kit/<season>.json}
 * (§2.2 {@code KitProfile}). Plain public fields with defaults for 1888-89.
 */
public final class KitParams {

    /** Boot mass per boot, dry, kg: ~500 g, heavy ankle-high leather with hard toecaps (BKP §2, M). */
    public double bootMassDryKg = 0.50;
    /** Boot mass gained when soaked, share of dry mass (BKP §2: "about double" is unmeasured, use +30 %, D). */
    public double bootWetGain = 0.30;
    /** Boot soak time constant at full wetness, minutes (D: same as the ball, BKP §1.3). */
    public double bootSoakTauMin = 39.0;
    /** Sole of the era (D: leather bars the common 1888 boot). */
    public Studs studs = Studs.BARS;
    /** Share of players wearing shin pads (BKP §3 kit.shinPadShare 0.6, D; rises over seasons). */
    public double shinPadShare = 0.6;
    /** Shin pad mass per pair, kg (BKP §2: +100-200 g, D). */
    public double shinPadMassKg = 0.15;

    /** The 1888-89 kit. */
    public static KitParams season1888() {
        return new KitParams();
    }

    public KitParams validate() {
        if (!(bootMassDryKg > 0 && bootWetGain >= 0 && bootSoakTauMin > 0 && studs != null && shinPadShare >= 0
                && shinPadShare <= 1 && shinPadMassKg >= 0)) {
            throw new IllegalArgumentException("invalid kit params");
        }
        return this;
    }
}
