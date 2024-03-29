package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.rndmodgames.futtoboru.data.scripts.BasicScript;

/**
 * Season v1
 * 
 *  - This is the base of all Football Games, without a Season you cannot have anything else
 *      
 *      - This will be the first thing Players Pick on New Game
 *      - Every Season must have one or more databases
 *      
 *  - Alpha Season
 *  
 *      - 1888-1889 England Football Season
 *      - 12 Teams:
 *          - TODO:
 *          
 *      - Home and Away Games
 *      - Standings by Win, Draws and Losses, Goal Average to break ties
 *      - Points were decided mid Season with 2 for wins and 1 for ties
 *      - Average goals was something like 4.44 per match [TODO: citation needed]
 *      
 * 
 *  - A Season will always need to be distributed with it's own data
 *  - We might need a method to start a new season but importing data if that season exists
 *      - This will be useful when new Teams are Founded or to introduce new real life players, rules or events, but data might clash
 * 
 * 
 * @author Geomancer86
 */
public class Season implements Serializable {

    private static final long serialVersionUID = -72888293779564961L;

    private Long id;
    private String name;
    private String description;
    private String urlSource;
    private LocalDateTime startDate;
     
    // Data
    private List<Country> countries;
    private List<League> leagues;
    private List<Club> clubs;
    
    // Scripts
    private List<BasicScript> seasonScripts;
    
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

    public List<Country> getCountries() {
        return countries;
    }
    
    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }
    
    public List<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<League> leagues) {
        this.leagues = leagues;
    }

    public List<Club> getClubs() {
        return clubs;
    }

    public void setClubs(List<Club> clubs) {
        this.clubs = clubs;
    }

    public List<BasicScript> getSeasonScripts() {
        return seasonScripts;
    }

    public void setSeasonScripts(List<BasicScript> seasonScripts) {
        this.seasonScripts = seasonScripts;
    }

    @Override
    public String toString() {
        return name != null ? name : "empty_season_name";
    }
}