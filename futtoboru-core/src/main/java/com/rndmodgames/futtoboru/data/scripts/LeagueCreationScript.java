package com.rndmodgames.futtoboru.data.scripts;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.rndmodgames.futtoboru.data.Country;

/**
 * League Creation Script v1
 * 
 * - TODO WIP: League Rules Scripting Support
 * 
 * NOTE: not in use so far, as we are relying on the hash map and keyed entries on Basic Script
 * 
 * @author Geomancer86
 */
public class LeagueCreationScript implements Serializable {

    //
    private static final long serialVersionUID = -5424844818760913075L;

    private Long id;
    private String leagueName;
    private String leagueDescription;
    private LocalDateTime leagueCreationDate;
    private Country leagueCountry;
    private List<Long> leagueTeamsIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public String getLeagueDescription() {
        return leagueDescription;
    }

    public void setLeagueDescription(String leagueDescription) {
        this.leagueDescription = leagueDescription;
    }

    public LocalDateTime getLeagueCreationDate() {
        return leagueCreationDate;
    }

    public void setLeagueCreationDate(LocalDateTime leagueCreationDate) {
        this.leagueCreationDate = leagueCreationDate;
    }

    public Country getLeagueCountry() {
        return leagueCountry;
    }

    public void setLeagueCountry(Country leagueCountry) {
        this.leagueCountry = leagueCountry;
    }

    public List<Long> getLeagueTeamsIds() {
        return leagueTeamsIds;
    }

    public void setLeagueTeamsIds(List<Long> leagueTeamsIds) {
        this.leagueTeamsIds = leagueTeamsIds;
    }
}