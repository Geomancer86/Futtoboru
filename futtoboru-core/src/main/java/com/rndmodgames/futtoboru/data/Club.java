package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Club v1
 * 
 * @author Geomancer86
 */
public class Club implements Serializable {

    private static final long serialVersionUID = -2626184195364944334L;

    private Long id;
    
    /**
     * Club Official Names
     * 
     * TODO: club nicknames need to be temporal/script based
     */
    private String name;
    private String fullName;
    private String shortName;
    private String initials;
    private String urlSource;
    
    /**
     * Foundation Date
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
     * Club Ground
     */
    private Stadium stadium;
    
    /**
     * Current Club League/Division (might be null if there is not a league)
     */
    private League currentLeague;
    
    /**
     * TODO WIP
     * 
     * Players At Club
     * 
     * This doesn't indicate a true relation as there is no contracts yet
     */
    private List<Player> players = new ArrayList<>();
    
    /**
     * Proposed Matches (this will be almost always clean/empty)
     *  - AI or Player needs to reply them for YES/NO and move them to scheduled or delete them
     */
    
    /**
     * Club Proposed, Scheduled and Played Matches
     */
    private List<Match> proposedMatches = new ArrayList<>();
    private List<Match> scheduledMatches = new ArrayList<>();
    private List<Match> playedMatches = new ArrayList<>();
    
    /**
     * Club Finances
     */
    private BigDecimal clubBalance;
    
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getUrlSource() {
        return urlSource;
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
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

    public Stadium getStadium() {
        return stadium;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public League getCurrentLeague() {
        return currentLeague;
    }

    public void setCurrentLeague(League currentLeague) {
        this.currentLeague = currentLeague;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Match> getProposedMatches() {
        return proposedMatches;
    }

    public void setProposedMatches(List<Match> proposedMatches) {
        this.proposedMatches = proposedMatches;
    }

    public List<Match> getScheduledMatches() {
        return scheduledMatches;
    }

    public void setScheduledMatches(List<Match> scheduledMatches) {
        this.scheduledMatches = scheduledMatches;
    }

    public List<Match> getPlayedMatches() {
        return playedMatches;
    }

    public void setPlayedMatches(List<Match> playedMatches) {
        this.playedMatches = playedMatches;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public BigDecimal getClubBalance() {
        return clubBalance;
    }

    public void setClubBalance(BigDecimal clubBalance) {
        this.clubBalance = clubBalance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Club other = (Club) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}