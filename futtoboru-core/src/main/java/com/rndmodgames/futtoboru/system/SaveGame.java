package com.rndmodgames.futtoboru.system;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;

/**
 * Save Game v1
 * 
 *  - Player Owner
 *      - Password if Set
 *  
 *  - Game Creation Time
 *  - Last Saved Time
 *      - If not saved once don't allow to Continue Game
 *  
 *  - Selected Countries
 *  - Selected Leagues
 *  - Clubs & Players Database
 *  - Staff Database
 *  - Other Relevant Data to Ensure 100% Saving & Restoring Game Feature
 * 
 * @author Geomancer86
 */
public class SaveGame implements Serializable {

    private static final long serialVersionUID = -5607039189697722153L;

    private Person owner;
    private List<Country> selectedCountries;
    
    private Date gameStartDate;
    private Date gameDate;
    
//  private Date createdDate;
//  private Date updatedDate;
//  private Date savedDate;
    
    private Boolean isSaved = false; // default to unsaved game
    
    // required for de-serializing
    public SaveGame() {
        
    }
    
    /**
     * Creates New Save Game
     */
    public SaveGame(Person owner, List<Country> selectedCountries) {
        
        this.owner = owner;
        this.setSelectedCountries(selectedCountries);
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @Transient
    public List<Country> getSelectedCountries() {
        return selectedCountries;
    }

    public void setSelectedCountries(List<Country> selectedCountries) {
        this.selectedCountries = selectedCountries;
    }

    public Date getGameStartDate() {
        return gameStartDate;
    }

    public void setGameStartDate(Date gameStartDate) {
        this.gameStartDate = gameStartDate;
    }

    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public void setIsSaved(Boolean isSaved) {
        this.isSaved = isSaved;
    }
}