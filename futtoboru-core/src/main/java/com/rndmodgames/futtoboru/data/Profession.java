package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Profession v1
 * 
 *  - Available Professions:
 *  
 *      - Player
 *      - Retired Player
 *      - Manager
 *      - Assistant Manager
 *      - Head Coach
 *      - Coach
 *      - Fitness Trainer
 *      - Doctor
 *      - Physio
 *      - Scout
 *      - Agent
 *      - Investor
 *      - Club Owner
 *      - Director
 *      - Head of Science
 *      - Head of Player Development
 * 
 * https://www.guidetofm.com/staff/roles/
 * 
 * @author Geomancer86
 */
public class Profession implements Serializable {

    private static final long serialVersionUID = 2807063764098520646L;

    private Long id;
    private String name;
    private String description;
    private Boolean playerSelectable;
    
    // 
    public Profession() {
        
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getPlayerSelectable() {
        return playerSelectable;
    }

    public void setPlayerSelectable(Boolean playerSelectable) {
        this.playerSelectable = playerSelectable;
    }

    @Override
    public String toString() {
        return name;
    }
}