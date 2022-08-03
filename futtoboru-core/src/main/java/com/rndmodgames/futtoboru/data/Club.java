package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Club v1
 * 
 * @author Geomancer86
 */
public class Club implements Serializable {

    private static final long serialVersionUID = -2626184195364944334L;

    private Long id;
    private String name;
    private String fullName;
    private String initials;
    
    /**
     * Foundation Year
     */
    private Integer year;
    private Integer month;
    private Integer day;
    
    /**
     * Club City
     */
    private Country country;
    private State state;
    private City city;
    
    /**
     * Current Club League/Division
     */
    private League currentLeague;
    
    public Club() {
        
    }
    
    /**
     * Used to Hack an Unavailable/Unselectable on Club Selectboxes
     */
    public Club(String name) {
        
        this.name = name;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
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

    public League getCurrentLeague() {
        return currentLeague;
    }

    public void setCurrentLeague(League currentLeague) {
        this.currentLeague = currentLeague;
    }

    @Override
    public String toString() {
        return name;
    }
}