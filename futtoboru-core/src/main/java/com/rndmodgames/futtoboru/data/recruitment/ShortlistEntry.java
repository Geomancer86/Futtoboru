package com.rndmodgames.futtoboru.data.recruitment;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3): one man who reached a club, by which source and who suggested
 * him. One entry per (club, man), whatever its status: a man who reaches the club again by another source does not
 * make a second one. Only {@link #LISTED} entries are on the club's shortlist. Saved in {@code SaveGame.shortlist}
 * (optional, saveVersion 3).
 */
public class ShortlistEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    // sources (§3.3)
    /** his own letter asking for a trial */
    public static final String LETTER = "letter";
    /** a reply to the club's advert ({@link #detail} = the paper's data id) */
    public static final String ADVERT = "advert";
    /** a tip from the trainer, the captain or a committee man ({@link #suggestedBy}) */
    public static final String TIP = "tip";
    /** an agent's letter ({@link #detail} = the agent's data id), names given for his fee */
    public static final String AGENT = "agent";
    /** the May "not retained" lists ({@link #detail} = his late club's name) */
    public static final String RELEASED = "released";
    /** the secretary put him on the list himself (Candidates screen) */
    public static final String OWN = "own";

    // statuses
    /** an advert reply not yet arrived (arrives on {@link #due}) */
    public static final String DUE = "due";
    /** in a letter in the inbox, waiting for Add / Ignore */
    public static final String OFFERED = "offered";
    /** an agent's man, nameless until his fee is paid */
    public static final String UNPAID = "unpaid";
    /** on the shortlist */
    public static final String LISTED = "listed";
    /** the letter was ignored, or he was taken off the shortlist */
    public static final String IGNORED = "ignored";

    private Long club;
    private Long player;
    private String source;
    /** the observer role of who suggested him ({@code engine.recruitment.Observers}), or null for a letter */
    private String suggestedRole;
    /** the shown name of who suggested him (a trainer, the agent, the paper), or null */
    private String suggestedBy;
    /** the paper, the agent (data ids) or the late club (a name), by source */
    private String detail;
    private String status;
    private LocalDateTime since;
    /** an advert reply's arrival day */
    private LocalDateTime due;
    /** the inbox letter he came in (a message id), or null */
    private Long letter;
    /** an agent's fee for this man, in pence */
    private Integer feePence;

    public ShortlistEntry() {
    }

    public ShortlistEntry(Long club, Long player, String source, String status, LocalDateTime since) {
        this.club = club;
        this.player = player;
        this.source = source;
        this.status = status;
        this.since = since;
    }

    public Long getClub() {
        return club;
    }

    public Long getPlayer() {
        return player;
    }

    public String getSource() {
        return source;
    }

    public String getSuggestedRole() {
        return suggestedRole;
    }

    public void setSuggestedRole(String suggestedRole) {
        this.suggestedRole = suggestedRole;
    }

    public String getSuggestedBy() {
        return suggestedBy;
    }

    public void setSuggestedBy(String suggestedBy) {
        this.suggestedBy = suggestedBy;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status != null ? status : LISTED;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isListed() {
        return LISTED.equals(getStatus());
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    public LocalDateTime getDue() {
        return due;
    }

    public void setDue(LocalDateTime due) {
        this.due = due;
    }

    public Long getLetter() {
        return letter;
    }

    public void setLetter(Long letter) {
        this.letter = letter;
    }

    public int getFeePence() {
        return feePence != null ? feePence : 0;
    }

    public void setFeePence(Integer feePence) {
        this.feePence = feePence;
    }

    @Override
    public String toString() {
        return "ShortlistEntry[club " + club + ", man " + player + ", " + source + ", " + getStatus() + "]";
    }
}
