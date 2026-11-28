package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * G-02 (D19-3): one man hurt in a match, stored on the {@link Match} per side. Decided by the instant result on its own
 * seeded stream; the days out are applied to the player by the match recorder.
 */
public class MatchInjury implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long personId;
    /** {@link InjuryKind#dataId()}. */
    private String kind;
    /** Minute he was hurt (1-90). */
    private Integer minute;
    /** Carried off only: the minute he came back on as a passenger, or null when he did not. */
    private Integer returnedMinute;
    /** Days out after the match (0 = fit for the next one). */
    private Integer daysOut;

    public MatchInjury() {
    }

    public MatchInjury(Long personId, InjuryKind kind, int minute, Integer returnedMinute, int daysOut) {
        this.personId = personId;
        this.kind = kind.dataId();
        this.minute = minute;
        this.returnedMinute = returnedMinute;
        this.daysOut = daysOut;
    }

    public InjuryKind getKindEnum() {
        return InjuryKind.fromDataId(kind);
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getReturnedMinute() {
        return returnedMinute;
    }

    public void setReturnedMinute(Integer returnedMinute) {
        this.returnedMinute = returnedMinute;
    }

    public Integer getDaysOut() {
        return daysOut;
    }

    public void setDaysOut(Integer daysOut) {
        this.daysOut = daysOut;
    }
}
