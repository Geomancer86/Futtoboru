package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

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

    public League getParent() {
        return parent;
    }

    public void setParent(League parent) {
        this.parent = parent;
    }
    
    @Override
    public String toString() {
        return name;
    }
}