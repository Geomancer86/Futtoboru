package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

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
    private Integer birthDay;
    private Integer birthMonth;
    private Integer birthYear;
    
    /**
     * Birth Place
     */
    private Country country;
    private State state;
    private City city;
    
    //
    private Profession primaryProfession;
    private Country currentCountry;
    private Club currentClub;
    
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

    public Integer getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Integer birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
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

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }

    @Override
    public String toString() {
        return "Person [" + (id != null ? "id=" + id + ", " : "") + (name != null ? "name=" + name + ", " : "")
                + (lastname != null ? "lastname=" + lastname + ", " : "")
                + (birthDay != null ? "birthDay=" + birthDay + ", " : "")
                + (birthMonth != null ? "birthMonth=" + birthMonth + ", " : "")
                + (birthYear != null ? "birthYear=" + birthYear + ", " : "")
                + (country != null ? "country=" + country + ", " : "") + (state != null ? "state=" + state + ", " : "")
                + (city != null ? "city=" + city + ", " : "")
                + (primaryProfession != null ? "primaryProfession=" + primaryProfession + ", " : "")
                + (currentCountry != null ? "currentCountry=" + currentCountry + ", " : "")
                + (currentClub != null ? "currentClub=" + currentClub : "") + "]";
    }
}