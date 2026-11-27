package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Country v1
 * 
 * @author Geomancer86
 */
public class Country implements Serializable {

    private static final long serialVersionUID = -6484625123682174509L;

    private Long id;


    // stable data id from the game data (P2-4, D11); null for objects made during play
    private String dataId;
    private String commonName;
    private String formalName;
    private Continent continent;
    
    private Boolean defaultCountry;
    
    private League lowestActiveLeague;
    private League lowestAvailableLeague;
    
    //
    public Country() {
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getFormalName() {
        return formalName;
    }

    public void setFormalName(String formalName) {
        this.formalName = formalName;
    }
    
    public Continent getContinent() {
        return continent;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public Boolean getDefaultCountry() {
        return defaultCountry;
    }

    public void setDefaultCountry(Boolean defaultCountry) {
        this.defaultCountry = defaultCountry;
    }

    public League getLowestActiveLeague() {
        return lowestActiveLeague;
    }

    public void setLowestActiveLeague(League lowestActiveLeague) {
        this.lowestActiveLeague = lowestActiveLeague;
    }

    public League getLowestAvailableLeague() {
        return lowestAvailableLeague;
    }

    public void setLowestAvailableLeague(League lowestAvailableLeague) {
        this.lowestAvailableLeague = lowestAvailableLeague;
    }

    @Override
    public String toString() {
        
        return commonName;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
