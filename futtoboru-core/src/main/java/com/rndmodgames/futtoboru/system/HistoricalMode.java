package com.rndmodgames.futtoboru.system;

/**
 * New-career rule for historical fork resolution (PO locked).
 *
 * <ul>
 *   <li>{@link #SANDBOX_FIRST} — default; fork votes and agency may diverge from IRL</li>
 *   <li>{@link #HISTORICAL_FIRST} — all forks resolve to the IRL default branch</li>
 * </ul>
 */
public enum HistoricalMode {

    SANDBOX_FIRST,
    HISTORICAL_FIRST;

    public static HistoricalMode fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SANDBOX_FIRST;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SANDBOX_FIRST;
        }
    }
}
