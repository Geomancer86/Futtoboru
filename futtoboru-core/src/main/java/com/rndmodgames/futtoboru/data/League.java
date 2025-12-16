package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.util.List;

/**
 * League v1
 * 
 * NOTE: Country and Parent are marked as transient fields to avoid an infinite serializing cycle 
 * 
 * @author Geomancer86
 */
public class League implements Serializable {

    private static final long serialVersionUID = -7242166000324792981L;

    private Long id;
    private String name;
    private Integer level;
    private String sourceLink;
    
    private List<Club> leagueClubs;
    
    /**
     * Competition Rules (v1.0)
     * Defines points system, tie-breaking criteria, and match format
     */
    private CompetitionRules rules;
    
    private transient Country country;
    private transient League parent;
    
    //
    public League() {
        
    }

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

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public List<Club> getLeagueClubs() {
        return leagueClubs;
    }

    public void setLeagueClubs(List<Club> leagueClubs) {
        this.leagueClubs = leagueClubs;
    }

    public League getParent() {
        return parent;
    }

    public void setParent(League parent) {
        this.parent = parent;
    }
    
    /**
     * Get competition rules for this league
     * @return CompetitionRules, or null if not set
     */
    public CompetitionRules getRules() {
        return rules;
    }
    
    /**
     * Set competition rules for this league
     * @param rules CompetitionRules to set
     */
    public void setRules(CompetitionRules rules) {
        this.rules = rules;
    }
    
    /**
     * Get competition rules with default fallback
     * If rules are not set, returns default modern rules (3-1-0, goal difference)
     * @return CompetitionRules (never null)
     */
    public CompetitionRules getRulesOrDefault() {
        if (rules == null) {
            return CompetitionRules.createDefaultRules();
        }
        return rules;
    }
    
    @Override
    public String toString() {
        return name;
    }
}