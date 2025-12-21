package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;

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
 *      - Built year: ✅ Added
 *      - Renovations Years: (Future)
 *      - Value: ✅ Added
 *      - Description: ✅ Added
 *      - URL Source: ✅ Added
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
    
    /**
     * Stadium Historical Data (v1.0)
     */
    private Integer builtYear; // Year stadium was built/opened
    private BigDecimal value; // Estimated value in pounds (for patrimony calculation)
    private String description; // Historical summary/description
    private String urlSource; // Wikipedia or source link
    
    /**
     * Stadium Ownership & Land (v1.0)
     */
    private BigDecimal landValue; // Value of land if owned
    private boolean isOwned; // true if club owns stadium, false if rented
    private BigDecimal annualRent; // Annual rent if stadium is rented (0 if owned)

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
    
    /**
     * Stadium Historical Data Getters and Setters (v1.0)
     */
    public Integer getBuiltYear() {
        return builtYear;
    }

    public void setBuiltYear(Integer builtYear) {
        this.builtYear = builtYear;
    }

    public BigDecimal getValue() {
        return value != null ? value : BigDecimal.ZERO;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlSource() {
        return urlSource;
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
    }
    
    /**
     * Stadium Ownership & Land Getters and Setters (v1.0)
     */
    public BigDecimal getLandValue() {
        return landValue != null ? landValue : BigDecimal.ZERO;
    }

    public void setLandValue(BigDecimal landValue) {
        this.landValue = landValue;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public BigDecimal getAnnualRent() {
        return annualRent != null ? annualRent : BigDecimal.ZERO;
    }

    public void setAnnualRent(BigDecimal annualRent) {
        this.annualRent = annualRent;
    }
    
    /**
     * Calculate total stadium asset value (stadium + land if owned)
     */
    public BigDecimal getTotalAssetValue() {
        BigDecimal total = getValue();
        if (isOwned) {
            total = total.add(getLandValue());
        }
        return total;
    }
    
    /**
     * toString() for VisSelectBox display
     */
    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        return "Unknown Stadium";
    }
}
