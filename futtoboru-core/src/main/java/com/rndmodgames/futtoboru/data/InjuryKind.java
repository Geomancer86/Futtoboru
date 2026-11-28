package com.rndmodgames.futtoboru.data;

import java.util.Locale;

/**
 * G-02 (D19-3): how a man is hurt in a match. No substitutes existed in 1888, so a hurt man either plays on (knock),
 * stays on worth less (passenger) or leaves and his side plays short (carried off; he may come back on).
 */
public enum InjuryKind {

    KNOCK("knock"),
    PASSENGER("passenger"),
    CARRIED_OFF("carried-off");

    private final String dataId;

    InjuryKind(String dataId) {
        this.dataId = dataId;
    }

    /** The {@code kind} in {@code injuries.json}. */
    public String dataId() {
        return dataId;
    }

    /** Bundle key of the kind's name ({@code injury_kind_knock} ...). */
    public String labelKey() {
        return "injury_kind_" + name().toLowerCase(Locale.ROOT);
    }

    /** @return the kind with that data id, or null */
    public static InjuryKind fromDataId(String id) {
        for (InjuryKind k : values()) {
            if (k.dataId.equals(id)) {
                return k;
            }
        }
        return null;
    }
}
