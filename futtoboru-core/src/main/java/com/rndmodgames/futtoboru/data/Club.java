package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;

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
    
    /**
     * Financial History Tracking (v1.0)
     * 
     * Tracks financial snapshots over time for charting and analysis
     */
    private List<FinancialSnapshot> financialHistory = new ArrayList<>();
    
    /**
     * Match Income Tracking (v1.0)
     * 
     * Tracks income from individual matches
     */
    private List<MatchIncome> matchIncomes = new ArrayList<>();
    
    /**
     * Club Expenses Tracking (v1.0)
     * 
     * Tracks detailed expense breakdown over time
     */
    private List<ClubExpenses> expensesHistory = new ArrayList<>();
    
    /**
     * Player Contracts (v1.0)
     * 
     * Tracks all player contracts at the club
     */
    private List<PlayerContract> playerContracts = new ArrayList<>();
    
    /**
     * Current Period Financial Tracking (v1.0)
     * 
     * Tracks income and expenditure for current season/month
     */
    private BigDecimal seasonIncome = BigDecimal.ZERO;
    private BigDecimal seasonExpenditure = BigDecimal.ZERO;
    private BigDecimal monthIncome = BigDecimal.ZERO;
    private BigDecimal monthExpenditure = BigDecimal.ZERO;
    
    /**
     * Club Staff Tracking (v1.0)
     * 
     * Maps profession ID to person ID holding that position.
     * null value means position is vacant.
     */
    private Map<Long, Long> staff = new HashMap<>(); // Profession ID -> Person ID
    
    /**
     * Club Statistics (v1.0)
     * 
     * Track club performance across all competitions
     */
    private Integer matchesPlayed = 0;
    private Integer matchesWon = 0;
    private Integer matchesDrawn = 0;
    private Integer matchesLost = 0;
    private Integer goalsScored = 0;
    private Integer goalsConceded = 0;
    private Integer points = 0; // For league competitions (3 for win, 1 for draw, 0 for loss)
    
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

    /**
     * Club Staff Management (v1.0)
     */
    public Map<Long, Long> getStaff() {
        if (staff == null) {
            staff = new HashMap<>();
        }
        return staff;
    }

    public void setStaff(Map<Long, Long> staff) {
        this.staff = staff;
    }

    /**
     * Utility: Get staff member ID for a profession
     */
    public Long getStaffId(Long professionId) {
        return staff != null ? staff.get(professionId) : null;
    }

    /**
     * Utility: Set staff member ID for a profession
     */
    public void setStaffId(Long professionId, Long personId) {
        if (staff == null) {
            staff = new HashMap<>();
        }
        if (personId == null) {
            staff.remove(professionId);
        } else {
            staff.put(professionId, personId);
        }
    }

    /**
     * Utility: Check if position is vacant
     */
    public boolean isPositionVacant(Long professionId) {
        return staff == null || staff.get(professionId) == null;
    }

    /**
     * Club Statistics Getters and Setters
     */
    public Integer getMatchesPlayed() {
        return matchesPlayed != null ? matchesPlayed : 0;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed != null ? matchesPlayed : 0;
    }

    public Integer getMatchesWon() {
        return matchesWon != null ? matchesWon : 0;
    }

    public void setMatchesWon(Integer matchesWon) {
        this.matchesWon = matchesWon != null ? matchesWon : 0;
    }

    public Integer getMatchesDrawn() {
        return matchesDrawn != null ? matchesDrawn : 0;
    }

    public void setMatchesDrawn(Integer matchesDrawn) {
        this.matchesDrawn = matchesDrawn != null ? matchesDrawn : 0;
    }

    public Integer getMatchesLost() {
        return matchesLost != null ? matchesLost : 0;
    }

    public void setMatchesLost(Integer matchesLost) {
        this.matchesLost = matchesLost != null ? matchesLost : 0;
    }

    public Integer getGoalsScored() {
        return goalsScored != null ? goalsScored : 0;
    }

    public void setGoalsScored(Integer goalsScored) {
        this.goalsScored = goalsScored != null ? goalsScored : 0;
    }

    public Integer getGoalsConceded() {
        return goalsConceded != null ? goalsConceded : 0;
    }

    public void setGoalsConceded(Integer goalsConceded) {
        this.goalsConceded = goalsConceded != null ? goalsConceded : 0;
    }

    public Integer getPoints() {
        return points != null ? points : 0;
    }

    public void setPoints(Integer points) {
        this.points = points != null ? points : 0;
    }
    
    /**
     * Utility: Get goal difference (goals scored - goals conceded)
     */
    public Integer getGoalDifference() {
        return getGoalsScored() - getGoalsConceded();
    }
    
    /**
     * Financial History Getters and Setters (v1.0)
     */
    public List<FinancialSnapshot> getFinancialHistory() {
        if (financialHistory == null) {
            financialHistory = new ArrayList<>();
        }
        return financialHistory;
    }

    public void setFinancialHistory(List<FinancialSnapshot> financialHistory) {
        this.financialHistory = financialHistory;
    }
    
    /**
     * Add a financial snapshot to history
     */
    public void addFinancialSnapshot(FinancialSnapshot snapshot) {
        if (financialHistory == null) {
            financialHistory = new ArrayList<>();
        }
        financialHistory.add(snapshot);
    }
    
    /**
     * Match Income Getters and Setters (v1.0)
     */
    public List<MatchIncome> getMatchIncomes() {
        if (matchIncomes == null) {
            matchIncomes = new ArrayList<>();
        }
        return matchIncomes;
    }

    public void setMatchIncomes(List<MatchIncome> matchIncomes) {
        this.matchIncomes = matchIncomes;
    }
    
    /**
     * Add match income
     */
    public void addMatchIncome(MatchIncome income) {
        if (matchIncomes == null) {
            matchIncomes = new ArrayList<>();
        }
        matchIncomes.add(income);
    }
    
    /**
     * Club Expenses Getters and Setters (v1.0)
     */
    public List<ClubExpenses> getExpensesHistory() {
        if (expensesHistory == null) {
            expensesHistory = new ArrayList<>();
        }
        return expensesHistory;
    }

    public void setExpensesHistory(List<ClubExpenses> expensesHistory) {
        this.expensesHistory = expensesHistory;
    }
    
    /**
     * Add expense record
     */
    public void addExpense(ClubExpenses expense) {
        if (expensesHistory == null) {
            expensesHistory = new ArrayList<>();
        }
        expensesHistory.add(expense);
    }
    
    /**
     * Player Contracts Getters and Setters (v1.0)
     */
    public List<PlayerContract> getPlayerContracts() {
        if (playerContracts == null) {
            playerContracts = new ArrayList<>();
        }
        return playerContracts;
    }

    public void setPlayerContracts(List<PlayerContract> playerContracts) {
        this.playerContracts = playerContracts;
    }
    
    public void addPlayerContract(PlayerContract contract) {
        if (contract == null) {
            com.badlogic.gdx.Gdx.app.error("Club", "Cannot add null contract to club: " + getName());
            return;
        }
        getPlayerContracts().add(contract);
        com.badlogic.gdx.Gdx.app.debug("Club", "Added contract ID " + contract.getId() + 
            " for player ID " + contract.getPlayerId() + " to club " + getName() + 
            " (total contracts: " + getPlayerContracts().size() + ")");
    }
    
    /**
     * Get contract for a specific player
     */
    public PlayerContract getContractForPlayer(Long playerId) {
        if (playerId == null) {
            com.badlogic.gdx.Gdx.app.debug("Club", "getContractForPlayer called with null playerId for club: " + getName());
            return null;
        }
        if (playerContracts == null || playerContracts.isEmpty()) {
            com.badlogic.gdx.Gdx.app.debug("Club", "No contracts found for club: " + getName() + " (playerContracts is " + 
                (playerContracts == null ? "null" : "empty") + ")");
            return null;
        }
        
        com.badlogic.gdx.Gdx.app.debug("Club", "Looking for contract for player ID " + playerId + 
            " in club " + getName() + " (total contracts: " + playerContracts.size() + ")");
        
        for (PlayerContract contract : playerContracts) {
            if (contract.getPlayerId() != null && contract.getPlayerId().equals(playerId)) {
                com.badlogic.gdx.Gdx.app.debug("Club", "Found contract ID " + contract.getId() + 
                    " for player ID " + playerId);
                return contract;
            }
        }
        
        com.badlogic.gdx.Gdx.app.debug("Club", "No contract found for player ID " + playerId + 
            " in club " + getName() + ". Contract player IDs: " + 
            playerContracts.stream()
                .map(c -> c.getPlayerId() != null ? c.getPlayerId().toString() : "null")
                .reduce((a, b) -> a + ", " + b)
                .orElse("none"));
        
        return null;
    }
    
    /**
     * Current Period Financial Tracking Getters and Setters (v1.0)
     */
    public BigDecimal getSeasonIncome() {
        return seasonIncome != null ? seasonIncome : BigDecimal.ZERO;
    }

    public void setSeasonIncome(BigDecimal seasonIncome) {
        this.seasonIncome = seasonIncome != null ? seasonIncome : BigDecimal.ZERO;
    }

    public BigDecimal getSeasonExpenditure() {
        return seasonExpenditure != null ? seasonExpenditure : BigDecimal.ZERO;
    }

    public void setSeasonExpenditure(BigDecimal seasonExpenditure) {
        this.seasonExpenditure = seasonExpenditure != null ? seasonExpenditure : BigDecimal.ZERO;
    }

    public BigDecimal getMonthIncome() {
        return monthIncome != null ? monthIncome : BigDecimal.ZERO;
    }

    public void setMonthIncome(BigDecimal monthIncome) {
        this.monthIncome = monthIncome != null ? monthIncome : BigDecimal.ZERO;
    }

    public BigDecimal getMonthExpenditure() {
        return monthExpenditure != null ? monthExpenditure : BigDecimal.ZERO;
    }

    public void setMonthExpenditure(BigDecimal monthExpenditure) {
        this.monthExpenditure = monthExpenditure != null ? monthExpenditure : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return name;
    }
}