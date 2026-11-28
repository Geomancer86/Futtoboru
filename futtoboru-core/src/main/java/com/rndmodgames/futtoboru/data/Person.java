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


    // stable data id from the game data (P2-4, D11); null for objects made during play
    private String dataId;
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

    /**
     * R-11a (design §10.1, saves v3): the languages he speaks, understands and reads/writes (levels 0-4), his home
     * language, the rate area he was born in and the country his parents came from (null = his own). Generated from
     * his upbringing only ({@code LanguageGenerator}), never from football ability.
     */
    private List<com.rndmodgames.futtoboru.data.language.LanguageSkill> languages = new ArrayList<>();
    private String homeLanguage;
    private String birthArea;
    private String parentsOrigin;

    /** R-09: how he judges a footballer (staff, the secretary), null for a man who has never been asked to judge */
    private StaffJudgement judgement;

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

    public String getDataId() {
        return dataId;
    }

    /** @return his languages, home language first (never null; empty until generated) */
    public List<com.rndmodgames.futtoboru.data.language.LanguageSkill> getLanguages() {
        if (languages == null) {
            languages = new ArrayList<>();
        }
        return languages;
    }

    public void setLanguages(List<com.rndmodgames.futtoboru.data.language.LanguageSkill> languages) {
        this.languages = languages == null ? new ArrayList<>() : languages;
    }

    /** @return the skill in one language, or null when he has none */
    public com.rndmodgames.futtoboru.data.language.LanguageSkill getLanguage(String languageId) {
        for (com.rndmodgames.futtoboru.data.language.LanguageSkill s : getLanguages()) {
            if (s.getLanguage() != null && s.getLanguage().equals(languageId)) {
                return s;
            }
        }
        return null;
    }

    /** @return the language data id he grew up speaking at home, or null before generation */
    public String getHomeLanguage() {
        return homeLanguage;
    }

    public void setHomeLanguage(String homeLanguage) {
        this.homeLanguage = homeLanguage;
    }

    /** @return the rate area ({@code areas.json}) he was born in, or null (born abroad, or not generated) */
    public String getBirthArea() {
        return birthArea;
    }

    public void setBirthArea(String birthArea) {
        this.birthArea = birthArea;
    }

    /** @return the country data id his parents came from when not his own, else null */
    public String getParentsOrigin() {
        return parentsOrigin;
    }

    public void setParentsOrigin(String parentsOrigin) {
        this.parentsOrigin = parentsOrigin;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /** @return his judging skills and bias (R-09), or null */
    public StaffJudgement getJudgement() {
        return judgement;
    }

    public void setJudgement(StaffJudgement judgement) {
        this.judgement = judgement;
    }
}
