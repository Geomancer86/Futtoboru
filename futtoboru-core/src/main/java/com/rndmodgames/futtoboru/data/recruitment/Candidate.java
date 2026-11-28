package com.rndmodgames.futtoboru.data.recruitment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Player;

/**
 * R-03 (docs/design/RECRUITMENT_AND_TRIALS.md §3.2, §6.4): a man in a region's candidate pool. The {@link Player}
 * holds the truth (attributes, his {@code Person} with name, birth, languages); the rest is how he came to be there.
 * Saved in {@code SaveGame.candidates} (optional fields: a save without them loads with fresh pools). Not in any
 * squad and not in {@code SaveGame.allPlayers}: the squad, training and match code never see him.
 */
public class Candidate implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Where he stands; only {@link #AVAILABLE} until R-05/R-07 add letters, trials and signing. */
    public static final String AVAILABLE = "available";

    private Player player;
    /** pool region data id ({@code recruitment/pools.json}) */
    private String region;
    /** §3.1 source kind ({@code local-junior}, {@code scottish-import}...) */
    private String sourceKind;
    /** origin backstory data id ({@code recruitment/origins.json}, D28 Q12), null for a real man */
    private String origin;
    /** R-02 origin club data id when he plays for a sourced period club, else null */
    private String originClub;
    /** the club he plays for now, as shown (a sourced name or a generated local one); null = no club */
    private String playsFor;
    /** the football culture he learned the game in ({@code upbringing.style}), null when not from the region */
    private String upbringing;
    /** the upbringing tendencies he took (attribute names), D28 Q14 */
    private List<String> tendencies = new ArrayList<>();
    /** age he came to Britain at, for a man born abroad (D28 Q13); null when born in the region's country */
    private Integer arrivedAge;
    /** true for a real man of {@code people.json} (D28 Q2) */
    private Boolean real;
    /** the season (start year) of the pool he joined */
    private Integer season;
    private LocalDateTime since;
    private String status = AVAILABLE;

    public Candidate() {
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(String sourceKind) {
        this.sourceKind = sourceKind;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginClub() {
        return originClub;
    }

    public void setOriginClub(String originClub) {
        this.originClub = originClub;
    }

    public String getPlaysFor() {
        return playsFor;
    }

    public void setPlaysFor(String playsFor) {
        this.playsFor = playsFor;
    }

    public String getUpbringing() {
        return upbringing;
    }

    public void setUpbringing(String upbringing) {
        this.upbringing = upbringing;
    }

    public List<String> getTendencies() {
        if (tendencies == null) {
            tendencies = new ArrayList<>();
        }
        return tendencies;
    }

    public void setTendencies(List<String> tendencies) {
        this.tendencies = tendencies == null ? new ArrayList<>() : tendencies;
    }

    public Integer getArrivedAge() {
        return arrivedAge;
    }

    public void setArrivedAge(Integer arrivedAge) {
        this.arrivedAge = arrivedAge;
    }

    public boolean isReal() {
        return Boolean.TRUE.equals(real);
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    public String getStatus() {
        return status != null ? status : AVAILABLE;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
