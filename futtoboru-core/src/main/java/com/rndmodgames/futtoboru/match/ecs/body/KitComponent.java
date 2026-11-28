package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * What one player wears that changes the physics (BALL_AND_KIT_PHYSICS.md §2): boots that soak up water over a wet
 * match (heavier: slower acceleration and top speed, more fatigue, harder kicks), the sole (grip), shin pads (lower
 * contact injury risk, a little mass).
 */
public final class KitComponent implements Component, HashedComponent {

    public final double bootMassDryKg;
    public final double bootWetGain;
    public final double bootSoakTauMin;
    public final Studs studs;
    public final boolean shinPads;
    public final double shinPadMassKg;

    /** Minutes of effective exposure to water so far (integral of wetness over time), like the ball. */
    public double exposureMin;
    /** Current mass of one boot, kg. */
    public double bootMassKg;

    public KitComponent(double bootMassDryKg, double bootWetGain, double bootSoakTauMin, Studs studs,
            boolean shinPads, double shinPadMassKg) {
        if (!(bootMassDryKg > 0) || studs == null) {
            throw new IllegalArgumentException("boot mass and studs are required");
        }
        this.bootMassDryKg = bootMassDryKg;
        this.bootWetGain = bootWetGain;
        this.bootSoakTauMin = bootSoakTauMin;
        this.studs = studs;
        this.shinPads = shinPads;
        this.shinPadMassKg = shinPads ? shinPadMassKg : 0;
        this.bootMassKg = bootMassDryKg;
    }

    /** This season's kit for one player. */
    public static KitComponent of(KitParams kit, boolean shinPads) {
        return new KitComponent(kit.bootMassDryKg, kit.bootWetGain, kit.bootSoakTauMin, kit.studs, shinPads,
                kit.shinPadMassKg);
    }

    /** Soaks the boots for {@code seconds} at {@code wetness} (0..1): first order toward dry x (1 + wet gain). */
    public void soak(double wetness, double seconds) {
        exposureMin += wetness * seconds / 60.0;
        double saturation = 1 - StrictMath.exp(-exposureMin / bootSoakTauMin);
        bootMassKg = bootMassDryKg * (1 + bootWetGain * saturation);
    }

    /** Mass the player carries in kit that matters here (both boots, pads), kg. */
    public double carriedMassKg() {
        return 2 * bootMassKg + shinPadMassKg;
    }

    /** Boot pair mass above the reference boot, kg (negative for lighter boots). */
    public double pairExcessKg(BodyParams p) {
        return 2 * (bootMassKg - p.bootReferenceKg);
    }

    /**
     * Effective foot mass of a kick with these boots (for E-14): the ball's {@code footEffectiveMassKg} assumes the
     * reference boot; a heavier (wet) boot adds its extra mass, so heavy boots kick harder (BKP §1.5).
     */
    public double kickEffectiveMassKg(double footEffectiveMassKg, BodyParams p) {
        return footEffectiveMassKg + (bootMassKg - p.bootReferenceKg);
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("kit.bootDry", bootMassDryKg);
        h.mix("kit.wetGain", bootWetGain);
        h.mix("kit.tau", bootSoakTauMin);
        h.mix("kit.studs", studs.ordinal());
        h.mix("kit.pads", shinPads ? 1 : 0);
        h.mix("kit.padMass", shinPadMassKg);
        h.mix("kit.exposure", exposureMin);
        h.mix("kit.boot", bootMassKg);
    }
}
