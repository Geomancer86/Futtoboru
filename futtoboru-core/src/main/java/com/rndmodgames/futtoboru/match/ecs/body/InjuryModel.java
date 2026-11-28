package com.rndmodgames.futtoboru.match.ecs.body;

/**
 * Contact injury chance and severity (§3.3, D19-3), pure functions of the contact and {@link BodyParams}: no roll
 * below the threshold; above it the chance grows linearly with the excess impulse x fragility x shin pads, capped;
 * harder hits shift the severity from knock toward passenger and off.
 */
public final class InjuryModel {

    private InjuryModel() {
    }

    /** Chance 0..1 that a contact impulse {@code impulseNs} injures this player. */
    public static double risk(double impulseNs, double fragility, boolean shinPads, BodyParams p) {
        double excess = impulseNs / p.injuryThresholdNs - 1;
        if (!(excess > 0)) {
            return 0;
        }
        double r = p.injuryRiskAtDouble * excess * fragility * (shinPads ? p.shinPadInjuryScale : 1);
        return Math.min(p.injuryRiskMax, r);
    }

    /** Severity ({@link InjuryComponent#KNOCK}..{@link InjuryComponent#OFF}) for a uniform draw {@code u} in [0, 1). */
    public static int severity(double u, double impulseNs, BodyParams p) {
        double excess = Math.max(0, impulseNs / p.injuryThresholdNs - 1);
        double shift = Math.min(p.knockShare - p.knockShareMin, p.severityShiftPerExcess * excess);
        double knock = p.knockShare - shift;
        double passenger = p.passengerShare + shift * 0.6;
        if (u < knock) {
            return InjuryComponent.KNOCK;
        }
        return u < knock + passenger ? InjuryComponent.PASSENGER : InjuryComponent.OFF;
    }
}
