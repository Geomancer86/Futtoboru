package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Person v1
 * 
 *  - Holds the data for each one of the persons in game: Players, Managers, Staff, etc.
 * 
 * @author Geomancer86
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 2212901640133697781L;

    private Long id;
    private String name;
    private String lastname;
    
    /**
     * Birth Date
     */
    private LocalDateTime birthDate;
    
    /**
     * Birth Place
     */
    private Country country;
    private State state;
    private City city;
    
    /**
     * NOTE: we keep track of the IDs for saving and loading / serializing, 
     * 
     *          and then we load the transient objects if/when needed
     */
    private Long currentClubId;
    
    private Profession primaryProfession;
    private Country currentCountry;
    
    /**
     * Reputation (v1.0: basic)
     * Range: 0-100
     * Default: 50 (neutral)
     */
    private Integer reputation = 50;
    
    /**
     * Job Applications (v1.0)
     * List of active application IDs
     */
    private List<Long> activeApplicationIds = new ArrayList<>();
    
    /**
     * Pending Job Offers (v1.0)
     * List of pending offer IDs
     */
    private List<Long> pendingOfferIds = new ArrayList<>();
    
    /**
     * Application tracking (v1.0)
     * Limit: 3 applications per week
     */
    private LocalDateTime lastJobApplicationDate;
    private Integer applicationsThisWeek = 0;

    //
    public Person() {
        
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

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Profession getPrimaryProfession() {
        return primaryProfession;
    }

    public void setPrimaryProfession(Profession primaryProfession) {
        this.primaryProfession = primaryProfession;
    }

    public Country getCurrentCountry() {
        return currentCountry;
    }

    public void setCurrentCountry(Country currentCountry) {
        this.currentCountry = currentCountry;
    }

    public Long getCurrentClubId() {
        return currentClubId;
    }

    public void setCurrentClubId(Long currentClubId) {
        this.currentClubId = currentClubId;
    }
    
    /**
     * Job System Getters and Setters (v1.0)
     */
    public Integer getReputation() {
        if (reputation == null) {
            reputation = 50; // Default
        }
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public List<Long> getActiveApplicationIds() {
        if (activeApplicationIds == null) {
            activeApplicationIds = new ArrayList<>();
        }
        return activeApplicationIds;
    }

    public void setActiveApplicationIds(List<Long> activeApplicationIds) {
        this.activeApplicationIds = activeApplicationIds;
    }

    public List<Long> getPendingOfferIds() {
        if (pendingOfferIds == null) {
            pendingOfferIds = new ArrayList<>();
        }
        return pendingOfferIds;
    }

    public void setPendingOfferIds(List<Long> pendingOfferIds) {
        this.pendingOfferIds = pendingOfferIds;
    }

    public LocalDateTime getLastJobApplicationDate() {
        return lastJobApplicationDate;
    }

    public void setLastJobApplicationDate(LocalDateTime lastJobApplicationDate) {
        this.lastJobApplicationDate = lastJobApplicationDate;
    }

    public Integer getApplicationsThisWeek() {
        if (applicationsThisWeek == null) {
            applicationsThisWeek = 0;
        }
        return applicationsThisWeek;
    }

    public void setApplicationsThisWeek(Integer applicationsThisWeek) {
        this.applicationsThisWeek = applicationsThisWeek;
    }

    /**
     * Utility: Check if can apply for more jobs this week
     * 
     * @param currentGameDate The current game date (not real-world time)
     * @return true if can apply, false if limit reached
     */
    public boolean canApplyForJob(LocalDateTime currentGameDate) {
        if (currentGameDate == null) {
            // Fallback to real-world time if game date not available (shouldn't happen)
            currentGameDate = LocalDateTime.now();
        }
        
        // Reset counter if new week
        if (lastJobApplicationDate != null) {
            LocalDateTime weekAgo = currentGameDate.minusDays(7);
            if (lastJobApplicationDate.isBefore(weekAgo)) {
                applicationsThisWeek = 0;
            }
        }
        return getApplicationsThisWeek() < com.rndmodgames.futtoboru.data.jobs.JobConstants.MAX_APPLICATIONS_PER_WEEK;
    }
    
    /**
     * Legacy method for backward compatibility - uses real-world time (not recommended)
     * @deprecated Use canApplyForJob(LocalDateTime) instead
     */
    @Deprecated
    public boolean canApplyForJob() {
        return canApplyForJob(LocalDateTime.now());
    }

    /**
     * Utility: Increment application counter
     * 
     * @param currentGameDate The current game date (not real-world time)
     */
    public void incrementApplicationCount(LocalDateTime currentGameDate) {
        if (currentGameDate == null) {
            // Fallback to real-world time if game date not available (shouldn't happen)
            currentGameDate = LocalDateTime.now();
        }
        
        if (canApplyForJob(currentGameDate)) {
            setApplicationsThisWeek(getApplicationsThisWeek() + 1);
            lastJobApplicationDate = currentGameDate;
        }
    }
    
    /**
     * Legacy method for backward compatibility - uses real-world time (not recommended)
     * @deprecated Use incrementApplicationCount(LocalDateTime) instead
     */
    @Deprecated
    public void incrementApplicationCount() {
        incrementApplicationCount(LocalDateTime.now());
    }
}