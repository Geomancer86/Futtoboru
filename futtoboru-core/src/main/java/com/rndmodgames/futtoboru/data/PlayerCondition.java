package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Fitness, morale, and injury state (Layer B5 — CM1/CM93 basic).
 */
public class PlayerCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_FITNESS = 100;
    public static final int DEFAULT_MORALE = 70;
    public static final int DEFAULT_SHARPNESS = 70;

    private Integer fitnessPct = DEFAULT_FITNESS;
    private Integer sharpnessPct = DEFAULT_SHARPNESS;
    private Integer fatiguePct = 0;
    private Integer morale = DEFAULT_MORALE;
    private String injuryTypeId;
    private String injuryDisplayName;
    private LocalDate injuryReturnDate;

    public static PlayerCondition createFitDefault() {
        return new PlayerCondition();
    }

    public boolean isInjured() {
        return isInjured(LocalDate.now());
    }

    public boolean isInjured(LocalDate asOf) {
        if (injuryTypeId == null || injuryTypeId.isEmpty()) {
            return false;
        }
        if (injuryReturnDate == null) {
            return true;
        }
        return !injuryReturnDate.isBefore(asOf);
    }

    public void clearInjury() {
        injuryTypeId = null;
        injuryDisplayName = null;
        injuryReturnDate = null;
    }

    public Integer getFitnessPct() {
        return fitnessPct != null ? fitnessPct : DEFAULT_FITNESS;
    }

    public void setFitnessPct(Integer fitnessPct) {
        this.fitnessPct = fitnessPct;
    }

    public Integer getSharpnessPct() {
        return sharpnessPct != null ? sharpnessPct : DEFAULT_SHARPNESS;
    }

    public void setSharpnessPct(Integer sharpnessPct) {
        this.sharpnessPct = sharpnessPct;
    }

    public Integer getFatiguePct() {
        return fatiguePct != null ? fatiguePct : 0;
    }

    public void setFatiguePct(Integer fatiguePct) {
        this.fatiguePct = fatiguePct;
    }

    public Integer getMorale() {
        return morale != null ? morale : DEFAULT_MORALE;
    }

    public void setMorale(Integer morale) {
        this.morale = morale;
    }

    public String getInjuryTypeId() {
        return injuryTypeId;
    }

    public void setInjuryTypeId(String injuryTypeId) {
        this.injuryTypeId = injuryTypeId;
    }

    public String getInjuryDisplayName() {
        return injuryDisplayName;
    }

    public void setInjuryDisplayName(String injuryDisplayName) {
        this.injuryDisplayName = injuryDisplayName;
    }

    public LocalDate getInjuryReturnDate() {
        return injuryReturnDate;
    }

    public void setInjuryReturnDate(LocalDate injuryReturnDate) {
        this.injuryReturnDate = injuryReturnDate;
    }
}
