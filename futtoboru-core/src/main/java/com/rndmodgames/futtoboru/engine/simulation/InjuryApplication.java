package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.data.InjurySeverity;

/**
 * Result of a post-match injury roll (B5.2).
 */
public final class InjuryApplication {

    private final boolean applied;
    private final InjurySeverity severity;
    private final String displayName;
    private final int estimateWeeks;
    private final int physioAbility;
    private final String playerLabel;

    public InjuryApplication(boolean applied, InjurySeverity severity, String displayName,
            int estimateWeeks, int physioAbility, String playerLabel) {
        this.applied = applied;
        this.severity = severity;
        this.displayName = displayName;
        this.estimateWeeks = estimateWeeks;
        this.physioAbility = physioAbility;
        this.playerLabel = playerLabel;
    }

    public static InjuryApplication none() {
        return new InjuryApplication(false, null, null, 0, 0, null);
    }

    public boolean isApplied() {
        return applied;
    }

    public InjurySeverity getSeverity() {
        return severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getEstimateWeeks() {
        return estimateWeeks;
    }

    public int getPhysioAbility() {
        return physioAbility;
    }

    public String getPlayerLabel() {
        return playerLabel;
    }

    public boolean isModerateOrWorse() {
        return severity == InjurySeverity.MODERATE || severity == InjurySeverity.SERIOUS;
    }
}
