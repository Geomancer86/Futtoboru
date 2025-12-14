package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Authority v1
 * 
 *  - Hierarchy based
 *  - Very basic version won't need much work until we add either a second division or another country league
 *      - Avoid lots of rework, design with the second country in mind
 *      - Start implementing second country teams as soon as possible
 *      
 * 
 * @author Geomancer86
 */
public class Authority implements Serializable {

    //
    private static final long serialVersionUID = 8829512634721594943L;

    //
    private Long id;
    private String name;
    private String shortName;
    private String description;
    private String sourceUrl;
    private LocalDateTime foundingDate;
    private Country country;
    private Integer level;
    private Authority parent;
    
    // 
    @Deprecated
    private List<League> leagues = new ArrayList<>();

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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getFoundingDate() {
        return foundingDate;
    }

    public void setFoundingDate(LocalDateTime foundingDate) {
        this.foundingDate = foundingDate;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Authority getParent() {
        return parent;
    }

    public void setParent(Authority parent) {
        this.parent = parent;
    }

    public List<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<League> leagues) {
        this.leagues = leagues;
    }
}