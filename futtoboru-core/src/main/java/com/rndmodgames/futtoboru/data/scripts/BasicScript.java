package com.rndmodgames.futtoboru.data.scripts;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Basic Script v1
 * 
 *  - LEAGUE_CREATION:
 *  
 *      - This script creates a League
 *      
 *      - Requires:
 *          - League name,
 *          - Description,
 *          - Foundation year (default by game script run date)
 *          - Country,
 *          - Division Level
 *          - Conforming Teams (default to all in country / add 1 by 1)
 *          
 *          - League Rules:
 *              - These need to come from sub-scripts or similar
 *              - Match order (both scripted and random)
 *              - Match results (if loading historic league or starting on a newer league)
 *              - Play once or home and away rule
 *              - Table position and tie break rules
 *                  - We need a specific script amending the rules in this case for the first english league (points added later in the league vs matches won system)
 * 
 * @author Geomancer86
 */
public class BasicScript implements Serializable {

    //
    private static final long serialVersionUID = -1705493931523661871L;

    private Long id;
    
    // useful for script editors
    private String name;
    private String description;
    
    // script type needs to be coded in the manager
    private Integer scriptType;
    
    // script values
    private HashMap<String, Object> scriptValues = new HashMap<>();
    
    // script ingame execution datetime
    private LocalDateTime executionTime;
    
    // keep track to avoid executing more than once (defaults to false)
    private Boolean isExecuted = false;
    
    //
    public static final int LEAGUE_CREATION_SCRIPT = 1000;

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

    public Integer getScriptType() {
        return scriptType;
    }

    public void setScriptType(Integer scriptType) {
        this.scriptType = scriptType;
    }

    public HashMap<String, Object> getScriptValues() {
        return scriptValues;
    }

    public void setScriptValues(HashMap<String, Object> scriptValues) {
        this.scriptValues = scriptValues;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public Boolean getIsExecuted() {
        return isExecuted;
    }

    public void setIsExecuted(Boolean isExecuted) {
        this.isExecuted = isExecuted;
    }
}