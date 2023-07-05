package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Stadium v1
 *  
 *   TODO WIP:
 *      - load stadiums during club load, from file system or hardcoded
 *      - research the 12 teams attendance capacity
 *      - every day until the match day, some tickets are sold randomly
 *      - winning games add to team hidden popularity values
 *      - losing games substracts to team hidden popularity
 *      - 
 *     
 *  TODO:
 *      - Location: name and coordinates for traveling distances calculation
 *      - Built year:
 *      - Renovations Years:
 *      -
 * 
 * @author Geomancer86
 */
public class Stadium implements Serializable {

    //
    private static final long serialVersionUID = -9129242792820668828L;

    //
    private Long id;
    private String name;
    private String fullname;
    private String nickname;

    // 
    private Long clubOwnerId;
    private Long clubOperatorId;
    
    //
    private Integer capacity;
}
