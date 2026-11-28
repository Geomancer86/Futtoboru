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


    // Data ids of the professions code depends on (professions.json); compare with is(...), never by runtime id
    public static final String PLAYER = "player";
    public static final String RETIRED_PLAYER = "retired-player";
    public static final String MANAGER = "manager";
    public static final String DIRECTOR = "director";
    public static final String SCOUT = "scout";
    public static final String AGENT = "agent";
    public static final String INVESTOR = "investor";
    public static final String CLUB_OWNER = "club-owner";
    // R-09 staff roles (docs/design/RECRUITMENT_AND_TRIALS.md §7)
    public static final String TRAINER = "trainer";
    public static final String SECRETARY = "secretary";
    public static final String COMMITTEE_MEMBER = "committee-member";

    // stable data id from the game data (P2-4, D11); null for objects made during play
    private String dataId;
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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /** @return true when this is the profession with that data id (e.g. {@link #MANAGER}) */
    public boolean is(String professionDataId) {
        return professionDataId != null && professionDataId.equals(dataId);
    }
}
