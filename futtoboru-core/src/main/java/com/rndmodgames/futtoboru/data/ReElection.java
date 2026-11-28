package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * G-08: the re-election of a League season, kept on the season's {@link CompetitionEdition} (saved with it).
 * Opened when the season completes (the bottom clubs must retire and seek re-election, other clubs apply), held at
 * the annual meeting: the member clubs that are not up for re-election vote, the clubs with the most votes fill the
 * places. Candidates are the retiring clubs first, then the applicants; {@link #getVotes()} is in the same order.
 */
public class ReElection implements Serializable {

    private static final long serialVersionUID = 1L;

    /** League places put to the vote (the number of retiring clubs). */
    private Integer places;
    /** The annual meeting: the vote is held on the first game day at or after it. */
    private LocalDateTime meeting;
    /** The bottom clubs of the final table, best placed first. */
    private List<Long> retiringIds = new ArrayList<>();
    /** Clubs from outside the League that applied, in application order. */
    private List<Long> applicantIds = new ArrayList<>();
    /** True once the meeting has voted. */
    private Boolean held = Boolean.FALSE;
    /** Votes per candidate (retiring, then applicants), filled at the meeting. */
    private List<Integer> votes = new ArrayList<>();
    /** The clubs elected to the places, most votes first. */
    private List<Long> electedIds = new ArrayList<>();

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    public LocalDateTime getMeeting() {
        return meeting;
    }

    public void setMeeting(LocalDateTime meeting) {
        this.meeting = meeting;
    }

    public List<Long> getRetiringIds() {
        if (retiringIds == null) {
            retiringIds = new ArrayList<>();
        }
        return retiringIds;
    }

    public void setRetiringIds(List<Long> retiringIds) {
        this.retiringIds = retiringIds;
    }

    public List<Long> getApplicantIds() {
        if (applicantIds == null) {
            applicantIds = new ArrayList<>();
        }
        return applicantIds;
    }

    public void setApplicantIds(List<Long> applicantIds) {
        this.applicantIds = applicantIds;
    }

    public boolean isHeld() {
        return Boolean.TRUE.equals(held);
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public List<Integer> getVotes() {
        if (votes == null) {
            votes = new ArrayList<>();
        }
        return votes;
    }

    public void setVotes(List<Integer> votes) {
        this.votes = votes;
    }

    public List<Long> getElectedIds() {
        if (electedIds == null) {
            electedIds = new ArrayList<>();
        }
        return electedIds;
    }

    public void setElectedIds(List<Long> electedIds) {
        this.electedIds = electedIds;
    }

    /** @return the retiring clubs, then the applicants */
    public List<Long> candidates() {
        List<Long> all = new ArrayList<>(getRetiringIds());
        all.addAll(getApplicantIds());
        return all;
    }
}
