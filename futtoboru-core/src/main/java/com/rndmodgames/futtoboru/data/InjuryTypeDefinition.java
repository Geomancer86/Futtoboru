package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Mod-driven injury type (B5.1).
 */
public final class InjuryTypeDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String displayName;
    private final int minWeeks;
    private final int maxWeeks;
    private final InjurySeverity severity;
    private final int weight;

    public InjuryTypeDefinition(String id, String displayName, int minWeeks, int maxWeeks,
            InjurySeverity severity, int weight) {
        this.id = id;
        this.displayName = displayName;
        this.minWeeks = minWeeks;
        this.maxWeeks = maxWeeks;
        this.severity = severity;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinWeeks() {
        return minWeeks;
    }

    public int getMaxWeeks() {
        return maxWeeks;
    }

    public InjurySeverity getSeverity() {
        return severity;
    }

    public int getWeight() {
        return weight;
    }
}
