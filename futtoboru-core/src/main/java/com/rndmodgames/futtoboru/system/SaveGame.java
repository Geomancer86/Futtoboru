package com.rndmodgames.futtoboru.system;

import java.beans.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Authority;
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
    
    /**
     * This is the selection of countries that the player selected to be simulated on a New Game
     */
    private List<Country> selectedCountries;
    
    // Game Start Date and Current Game Date (Working)
    private LocalDateTime gameStartDate;
    private LocalDateTime gameDate;
    
    /**
     * We need to Save and Track updated data, the data on Database is mostly static
     * 
     *  - TODO
     *      - save current main authority and keep track of authority hierarchies as new authorities are created and power shifts
     *      - save current leagues
     *      - save current teams
     *      
     *  - TODO: when the game is being played we have to make sure we always use the most recent/updated data version and not the original static data from the DatabaseLoader
     */
    private Authority mainAuthority;
    
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
     * TODO WIP
     */
    private List<BasicScript> gameScripts = new ArrayList<>();
    
    /**
     * Person Database
     */
    private List<Person> allPersons = new ArrayList<>();
    
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

    public Authority getMainAuthority() {
        return mainAuthority;
    }

    public void setMainAuthority(Authority mainAuthority) {
        this.mainAuthority = mainAuthority;
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

    public List<Person> getAllPersons() {
        return allPersons;
    }

    public void setAllPersons(List<Person> allPersons) {
        this.allPersons = allPersons;
    }
}