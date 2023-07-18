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
 *  TBD: Future versions need to support different stadium zones with different prices/qualities/season pass/etc
 *          - also differentiate between standing and seating people.
 *          
 *      
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
    private Long clubOwnerId;
    private Integer capacity;

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

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getClubOwnerId() {
        return clubOwnerId;
    }

    public void setClubOwnerId(Long clubOwnerId) {
        this.clubOwnerId = clubOwnerId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
