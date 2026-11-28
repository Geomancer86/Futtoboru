package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * G-05: one man on a club's committee (president, treasurer or committee man). Generated and seeded per club
 * ({@code engine.committee.CommitteeManager}); kept inside the club's {@link ClubCommittee}, not a game person (he
 * holds no post on the job market). Role and occupation are bundle-key suffixes ({@code committee_role_*},
 * {@code committee_occupation_*}).
 */
public class CommitteeMember implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PRESIDENT = "president";
    public static final String TREASURER = "treasurer";
    public static final String MEMBER = "member";

    private String name;
    private String lastname;
    private String role;
    private String occupation;
    private Integer birthYear;
    /** 0..1: how long he bears a poor run before he turns against the secretary (placeholder, D30). */
    private Double patience;
    /** 0..1: how readily he votes for a wage above the band (placeholder, D30). */
    private Double generosity;

    public CommitteeMember() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /** @return "first last" */
    public String getFullName() {
        return ((name != null ? name : "") + " " + (lastname != null ? lastname : "")).trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public double getPatience() {
        return patience != null ? patience : 0.5;
    }

    public void setPatience(Double patience) {
        this.patience = patience;
    }

    public double getGenerosity() {
        return generosity != null ? generosity : 0.5;
    }

    public void setGenerosity(Double generosity) {
        this.generosity = generosity;
    }

    /** R-09: how he judges a footballer (character and local knowledge), or null before the staff day gives it */
    private StaffJudgement judgement;

    public StaffJudgement getJudgement() {
        return judgement;
    }

    public void setJudgement(StaffJudgement judgement) {
        this.judgement = judgement;
    }
}
