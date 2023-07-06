package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;

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
}