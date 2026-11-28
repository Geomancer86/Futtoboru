package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * G-02: the injury a player is out with. He is unavailable on every day before {@code until} (by date) and fit again on
 * it. Saved inside its player (an optional field: an older save has none, so no saveVersion bump).
 */
public class PlayerInjury implements Serializable {

    private static final long serialVersionUID = 1L;

    /** {@link InjuryKind#dataId()} of the match injury. */
    private String kind;
    /** The day of the match he was hurt in. */
    private LocalDateTime since;
    /** The first day he is fit again. */
    private LocalDateTime until;
    /** Days out when he was hurt (the letters and screens show what is left). */
    private Integer daysOut;
    /** The match he was hurt in (may be null). */
    private Long matchId;

    public PlayerInjury() {
    }

    public PlayerInjury(InjuryKind kind, LocalDateTime since, int daysOut, Long matchId) {
        this.kind = kind.dataId();
        this.since = since;
        this.until = since.plusDays(daysOut);
        this.daysOut = daysOut;
        this.matchId = matchId;
    }

    public InjuryKind getKindEnum() {
        return InjuryKind.fromDataId(kind);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public void setUntil(LocalDateTime until) {
        this.until = until;
    }

    public Integer getDaysOut() {
        return daysOut;
    }

    public void setDaysOut(Integer daysOut) {
        this.daysOut = daysOut;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
}
