package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Competition Edition v1
 * 
 *  - we use the editions to represent:
 *  
 *      - Cup Stagings (that's what they are called at least in uk english)
 *      - League Seasons
 * 
 * @author Geomancer86
 */
public class CompetitionEdition implements Serializable {

    //
    private static final long serialVersionUID = -6380157833948278072L;

    //
    private Long id;
    private String name;
    private String description;
    private String urlSource;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // quantities
    private Integer invitedClubs;
    private Integer participantClubs;
    private Integer matchesPlayed;
    private Integer goalsScored;
    
    // NOTE: all TODOs are TBD here
    // TODO: list of participant ids
    // TODO: list of invited ids
    // TODO: list of complete historic match results, goals, cards, attendance, referee, etc.
    // TODO: final game id so it can be linked to historic match result in deeper detail
    
    private Long championsId;
    private Long runnersUpId;
    
    //
    private List<Long> participantClubsIds = new ArrayList<>();

    // 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlSource() {
        return urlSource;
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getInvitedClubs() {
        return invitedClubs;
    }

    public void setInvitedClubs(Integer invitedClubs) {
        this.invitedClubs = invitedClubs;
    }

    public Integer getParticipantClubs() {
        return participantClubs;
    }

    public void setParticipantClubs(Integer participantClubs) {
        this.participantClubs = participantClubs;
    }

    public Integer getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public Integer getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(Integer goalsScored) {
        this.goalsScored = goalsScored;
    }

    public Long getChampionsId() {
        return championsId;
    }

    public void setChampionsId(Long championsId) {
        this.championsId = championsId;
    }

    public Long getRunnersUpId() {
        return runnersUpId;
    }

    public void setRunnersUpId(Long runnersUpId) {
        this.runnersUpId = runnersUpId;
    }

    public List<Long> getParticipantClubsIds() {
        return participantClubsIds;
    }

    public void setParticipantClubsIds(List<Long> participantClubsIds) {
        this.participantClubsIds = participantClubsIds;
    }
}