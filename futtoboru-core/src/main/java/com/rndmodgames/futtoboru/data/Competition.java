package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Competitions v1
 * 
 *  - TODO:
 *      - Leagues are a Competition, rework
 *      
 *      - Add the competition type ID:
 *      
 *          - 1: Knockout / Cup
 *          - 2: League
 *          - X: Flexible for other cup types (for example leagues with playoffs)
 *          
 *          - NOTE: we can use the competition type to implement PROMOTION/RELEGATION matches,
 *                   as they are a single match competition on their own with a defined prize besides money
 *          
 *      - Authority ID
 *      
 *      - Historic Dates, cup draw date, initial match date, final match date
 *      
 *      - Historic Participants:
 *          - Invited
 *          - Accepting
 *          
 *      - Winner, Runner Up, Etc.
 *      - Final Match Result and Attendance
 * 
 * @author Geomancer86
 */
public class Competition implements Serializable {

    //
    private static final long serialVersionUID = 1028475113111090045L;

    private Long id;
    private String name;
    private String fullname;
    private String urlSource;
    private Long authorityId;
    private Long countryId;
    private Integer level;
    private LocalDateTime creationDate;
    
    /**
     * Each yearly occurence:
     *  - Leagues call them a Season
     *  - Cups call them a Staging
     */
    private List<CompetitionEdition> editions = new ArrayList<>();
    
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

    public String getFullname() {
        return fullname;
    }

    public String getUrlSource() {
        return urlSource;
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public List<CompetitionEdition> getEditions() {
        return editions;
    }

    public void setEditions(List<CompetitionEdition> editions) {
        this.editions = editions;
    }
}