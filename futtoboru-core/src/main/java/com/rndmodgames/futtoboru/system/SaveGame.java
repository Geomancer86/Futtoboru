package com.rndmodgames.futtoboru.system;

import java.beans.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
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

    /**
     * Owner
     *      > Current Club
     *      
     */
    private Person owner;
    
    /**
     * This is the selection of countries that the player selected to be simulated on a New Game
     */
    private List<Country> selectedCountries = new ArrayList<>();
    
    // Game Start Date and Current Game Date (Working)
    private LocalDateTime gameStartDate;
    private LocalDateTime gameDate;
    
    /**
     * We need to Save and Track updated data, the data on Database is mostly static
     * 
     *  - TODO
     *      - save current main authority and keep track of authority hierarchies as new authorities are created and power shifts
     *      - save current leagues
     *      - save current clubs
     *      
     *  - TODO: when the game is being played we have to make sure we always use the most recent/updated data version and not the original static data from the DatabaseLoader
     */
    private Authority mainAuthority;
    
    /**
     * Competitions
     */
    private List<Competition> allCups = new ArrayList<>();
    private List<Competition> allLeagues = new ArrayList<>();
    
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
     *  - Some events will allow the player to decide, for example: other clubs got historically relegated, but the player can choose to keep it in the league 
     *  
     *  
     * Inbox Manager
     * 
     * Scripts Manager
     *  - Basic Script:
     *      - Forms The League on [X DATE] with [X CLUBS] and [X RULES]
     */
    private List<Message> allMessages = new ArrayList<>();
    
    /**
     * Scheduled Messages (v2.0)
     * 
     * Messages that are scheduled for future delivery.
     * These are moved to allMessages when their scheduledDate is reached.
     */
    private List<Message> scheduledMessages = new ArrayList<>();
    
    /**
     * Scripts Support
     */
    private List<BasicScript> gameScripts = new ArrayList<>();
    
    /**
     * Clubs Database
     */
    private List<Club> allClubs = new ArrayList<>();
    
    /**
     * Person Database
     */
    private List<Person> allPersons = new ArrayList<>();
    
    /**
     * Player Database
     */
    private List<Player> allPlayers = new ArrayList<>();
    
    /**
     * Job System Data (v1.0)
     */
    private List<JobOpening> activeJobOpenings = new ArrayList<>();
    private List<JobApplication> allApplications = new ArrayList<>();
    private List<JobOffer> pendingOffers = new ArrayList<>();
    
    /**
     * Attribute Tracking System (v1.0)
     * Stores weekly snapshots of player attributes for change tracking
     */
    private List<PlayerAttributeSnapshot> playerAttributeSnapshots = new ArrayList<>();
    
    /**
     * Proposed Matches
     */
    
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

    /**
     * Get Current Club Utility Method
     * 
     * Returns null if player is unemployed (no current club)
     */
    public Club getCurrentClub() {
        if (owner == null || owner.getCurrentClubId() == null) {
            return null; // Player is unemployed
        }
        return getClubById(owner.getCurrentClubId());
    }
    
    /**
     * Get Club Utility Method:
     * 
     *  - During ingame, do not use DatabaseLoader as the clubs returned will be static and not the saved on file
     */
    public Club getClubById(Long id) {
        if (id == null) {
            return null; // Null ID means no club
        }
        
        if (allClubs == null) {
            return null; // No clubs loaded
        }
        
        for (Club club : allClubs) {
            if (club == null || club.getId() == null) {
                continue; // Skip null clubs or clubs with null IDs
            }
            
            if (club.getId().equals(id)) {
                return club;
            }
        }
        
        return null; // Club not found
    }
    
    //
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

    public List<Competition> getAllCups() {
        return allCups;
    }

    public void setAllCups(List<Competition> allCups) {
        this.allCups = allCups;
    }

    public List<Competition> getAllLeagues() {
        return allLeagues;
    }

    public void setAllLeagues(List<Competition> allLeagues) {
        this.allLeagues = allLeagues;
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

    public List<Club> getAllClubs() {
        return allClubs;
    }

    public void setAllClubs(List<Club> allClubs) {
        this.allClubs = allClubs;
    }

    public List<Person> getAllPersons() {
        return allPersons;
    }

    public void setAllPersons(List<Person> allPersons) {
        this.allPersons = allPersons;
    }

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    public void setAllPlayers(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
    }
    
    /**
     * Job System Getters and Setters (v1.0)
     */
    public List<JobOpening> getActiveJobOpenings() {
        if (activeJobOpenings == null) {
            activeJobOpenings = new ArrayList<>();
        }
        return activeJobOpenings;
    }

    public void setActiveJobOpenings(List<JobOpening> activeJobOpenings) {
        this.activeJobOpenings = activeJobOpenings;
    }

    public List<JobApplication> getAllApplications() {
        if (allApplications == null) {
            allApplications = new ArrayList<>();
        }
        return allApplications;
    }

    public void setAllApplications(List<JobApplication> allApplications) {
        this.allApplications = allApplications;
    }

    public List<JobOffer> getPendingOffers() {
        if (pendingOffers == null) {
            pendingOffers = new ArrayList<>();
        }
        return pendingOffers;
    }

    public void setPendingOffers(List<JobOffer> pendingOffers) {
        this.pendingOffers = pendingOffers;
    }
    
    /**
     * Inbox Messages Getters and Setters
     */
    public List<Message> getAllMessages() {
        if (allMessages == null) {
            allMessages = new ArrayList<>();
        }
        return allMessages;
    }

    public void setAllMessages(List<Message> allMessages) {
        this.allMessages = allMessages;
    }
    
    /**
     * Get scheduled messages (not yet delivered)
     */
    public List<Message> getScheduledMessages() {
        if (scheduledMessages == null) {
            scheduledMessages = new ArrayList<>();
        }
        return scheduledMessages;
    }
    
    /**
     * Set scheduled messages
     */
    public void setScheduledMessages(List<Message> scheduledMessages) {
        this.scheduledMessages = scheduledMessages;
    }
    
    /**
     * Attribute Tracking System Getters/Setters (v1.0)
     */
    public List<PlayerAttributeSnapshot> getPlayerAttributeSnapshots() {
        if (playerAttributeSnapshots == null) {
            playerAttributeSnapshots = new ArrayList<>();
        }
        return playerAttributeSnapshots;
    }

    public void setPlayerAttributeSnapshots(List<PlayerAttributeSnapshot> playerAttributeSnapshots) {
        this.playerAttributeSnapshots = playerAttributeSnapshots;
    }
    
    /**
     * Get snapshots for a specific player
     */
    public List<PlayerAttributeSnapshot> getPlayerSnapshots(Long playerId) {
        List<PlayerAttributeSnapshot> result = new ArrayList<>();
        if (playerAttributeSnapshots == null || playerId == null) {
            return result;
        }
        for (PlayerAttributeSnapshot snapshot : playerAttributeSnapshots) {
            if (snapshot != null && snapshot.getPlayerId() != null && snapshot.getPlayerId().equals(playerId)) {
                result.add(snapshot);
            }
        }
        return result;
    }
}