package com.rndmodgames.futtoboru.system;

import java.beans.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;

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
    
    private LocalDateTime gameStartDate;
    private LocalDateTime gameDate;
    
    private Boolean isSaved = false; // default to unsaved game
    
    /**
     * Inbox Support
     * 
     *  - Inbox Message
     *  - LocalDateTime messageDate;
     *  - Boolean isRead;
     *  - Boolean isDeleted;
     *  
     *  - Person remitent;
     *  
     *  - String messageText;
     *      - Support for Markup & Coloring
     *      - Support for [BUTTONS] redirecting to an existing window + options, for example personDetails(player)
     *      - Support for [OPTIONS] with different outcomes [YES/NO] [MULTIPLE] [ETC] 
     *  
     *
     * Script Support
     *  
     *  - Scripts will be loaded during new game when picking a Season, and will need to be saved within the SaveGame
     *      until they are fired.
     *  
     *  - Some events will allow the player to decide, for example: other team got historically relegated, but the player can choose to keep it in the league 
     *  
     *  
     * Inbox Manager
     * 
     * Scripts Manager
     *  - Basic Script:
     *      - Forms The League on [X DATE] with [X TEAMS] and [X RULES]
     */
    List<Message> allMessages;
    
    /**
     * Scripts Support
     * 
     * 
     */
    private List<BasicScript> gameScripts = new ArrayList<>();
    
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

    public LocalDateTime getGameStartDate() {
        return gameStartDate;
    }

    public void setGameStartDate(LocalDateTime gameStartDate) {
        this.gameStartDate = gameStartDate;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public void setIsSaved(Boolean isSaved) {
        this.isSaved = isSaved;
    }

    public List<BasicScript> getGameScripts() {
        return gameScripts;
    }

    public void setGameScripts(List<BasicScript> gameScripts) {
        this.gameScripts = gameScripts;
    }
}