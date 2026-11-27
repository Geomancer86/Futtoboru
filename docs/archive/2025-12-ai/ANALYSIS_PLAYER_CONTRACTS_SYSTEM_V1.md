# Player Contracts System Analysis v1.0

**Analysis Date:** 2025-01-XX  
**Feature:** Comprehensive Player Contracts System  
**Priority:** CRITICAL - Core financial and gameplay system

---

## Executive Summary

This document provides comprehensive analysis and implementation plan for a flexible, negotiable player contracts system based on research from Football Manager, FIFA Manager, Championship Manager, PC Football, and historical 1888-89 data. The system will support multiple contract types, bonuses, clauses, and be fully scriptable for historical accuracy.

---

## 1. RESEARCH FINDINGS

### 1.1 Football Manager Contract System

**Core Components:**

1. **Base Salary**
   - Weekly or annual wage
   - Primary component of contract
   - Negotiable based on player value, club finances, market conditions

2. **Signing-On Bonus**
   - Lump sum paid upon contract signing
   - Can offset lower base salary
   - Useful for attracting players

3. **Performance Bonuses**
   - **Goal Bonus:** Per goal scored
   - **Assist Bonus:** Per assist made
   - **Clean Sheet Bonus:** Per clean sheet (goalkeepers/defenders)
   - **Appearance Fee:** Per match played
   - **Unused Substitute Fee:** Per match on bench but not used

4. **Achievement Bonuses**
   - **Promotion Bonus:** One-time payment on promotion
   - **Relegation Wage Drop:** Percentage reduction if relegated
   - **League Win Bonus:** For winning league
   - **Cup Win Bonus:** For winning cup competitions
   - **Top Goalscorer Bonus:** For league top scorer
   - **Player of the Year Bonus:** For individual awards

5. **Contract Clauses**
   - **Minimum Fee Release Clause:** Transfer fee that triggers release
   - **Relegation Release Clause:** Transfer fee if club relegated
   - **Sell-On Fee Percentage:** Player receives % of future transfer fee
   - **Contract Length:** Fixed term or rolling contract
   - **Extension Options:** Automatic or optional extensions

6. **Negotiation Mechanics**
   - Initial offer (club proposes)
   - Player/agent counter-offer
   - Multiple rounds of negotiation
   - Agent fees and loyalty bonuses
   - Role promises (key player, squad player, etc.)

### 1.2 FIFA Manager / Championship Manager

**Similar Systems:**
- Performance-based bonuses
- Appearance fees
- Contract length negotiations
- Release clauses
- Transfer fee negotiations

**Key Differences:**
- Some games have simpler bonus structures
- Focus on base salary + key bonuses
- Less complex clause system

### 1.3 Historical 1888-89 Data

**Contract Types:**

1. **Amateur Players**
   - No wages paid
   - Players maintained other employment
   - Could leave anytime
   - No training requirements
   - Common in early football

2. **Semi-Professional Players**
   - Part-time contracts
   - Low wages (2-5 shillings/week)
   - Limited training (half-day)
   - Could balance with other work
   - Emerging in 1888-89

3. **Professional Players**
   - Full-time contracts
   - Regular salary (10 shillings - £1/week)
   - Full-time training mandatory
   - Dedicated to football
   - Legalized in 1885, common by 1888

**Historical Wage Data (1888-89):**

| Player/Club | Wage | Notes |
|-------------|------|-------|
| **Nick Ross (Everton)** | £10/month | Nearly double average (top player) |
| **Harry Webb (Leicester Fosse)** | 2s 6d/week + travel | First professional at club |
| **Blackburn Rovers (top players)** | £1/week | James Forrest, Joseph Lofthouse |
| **West Bromwich Albion** | 10s/week | No bonuses or expenses |
| **Bolton Wanderers (average)** | 80s/week | Wealthiest clubs |
| **Average Professional (1900)** | ~£3/week | Plus bonuses for wins/draws/cups |

**Historical Context:**
- Professional football legalized in 1885
- Players had to be born or lived 2+ years within 6 miles of ground
- Average professional earned ~2x skilled worker wage
- Bonuses for wins, draws, cup matches (mid-1890s)
- No formal contract clauses (simple agreements)

**Contract Evolution:**
- 1885-1888: Basic wage agreements
- 1888-1890: Introduction of bonuses
- 1890s+: More structured contracts
- 1900s+: Formal contracts with clauses

---

## 2. CONTRACT TYPES FOR IMPLEMENTATION

### 2.1 Amateur Contract

**Characteristics:**
- **Weekly Wage:** £0
- **Training:** Optional (no requirement)
- **Availability:** Part-time (can have other job)
- **Contract Length:** Indefinite (can leave anytime)
- **Bonuses:** None (or minimal, e.g., travel expenses)
- **Release:** No restrictions

**Use Cases:**
- Lower league clubs
- Youth players
- Reserve players
- Historical accuracy (1888-89 had many amateurs)

### 2.2 Semi-Professional Contract

**Characteristics:**
- **Weekly Wage:** £0.25-0.50 (2s 6d - 5s)
- **Training:** Half-day (2-3 sessions/week)
- **Availability:** Part-time (can have other job)
- **Contract Length:** 1-2 years (negotiable)
- **Bonuses:** Limited (appearance, goals)
- **Release:** Standard notice period

**Use Cases:**
- Lower league clubs
- Backup players
- Emerging professionals
- Historical accuracy (transition period)

### 2.3 Professional Contract

**Characteristics:**
- **Weekly Wage:** £0.50-5.00 (10s - £1+)
- **Training:** Full-time (daily sessions)
- **Availability:** Full-time (dedicated to football)
- **Contract Length:** 1-5 years (negotiable)
- **Bonuses:** Full range (goals, assists, clean sheets, achievements)
- **Release:** Contract terms apply

**Use Cases:**
- Top players
- First team regulars
- Historical accuracy (1888-89 professionals)

### 2.4 Additional Contract Types (Future/Research)

**Scholarship Contract (Youth):**
- Very low wage (£0.10-0.25/week)
- Training required
- Limited to youth players
- Can convert to professional

**Non-Contract (Trialist):**
- No wage
- Short-term (weeks)
- For trials/assessments

---

## 3. BONUSES & CLAUSES SYSTEM

### 3.1 Performance Bonuses

**Per-Match Bonuses:**
- **Appearance Fee:** £0.05-0.50 per match played
- **Unused Substitute Fee:** £0.02-0.20 per match on bench
- **Goal Bonus:** £0.10-2.00 per goal scored
- **Assist Bonus:** £0.05-1.00 per assist
- **Clean Sheet Bonus:** £0.10-1.00 per clean sheet (GK/defenders)
- **Man of the Match Bonus:** £0.25-1.00 per MOTM award

**Seasonal Bonuses:**
- **Top Goalscorer Bonus:** £5-50 (league)
- **Most Assists Bonus:** £3-30 (league)
- **Player of the Year Bonus:** £10-100 (club/league)
- **Most Clean Sheets Bonus:** £5-50 (goalkeepers)

### 3.2 Achievement Bonuses

**Competition Bonuses:**
- **League Win Bonus:** £10-200 (one-time)
- **League Runner-Up Bonus:** £5-100 (one-time)
- **Cup Win Bonus:** £5-100 (one-time)
- **Cup Runner-Up Bonus:** £2-50 (one-time)
- **Promotion Bonus:** £5-150 (one-time)
- **Relegation Wage Drop:** -10% to -50% (if relegated)

**Standing Bonuses:**
- **Top 3 Finish Bonus:** £3-50
- **Top Half Finish Bonus:** £1-20
- **Avoid Relegation Bonus:** £2-30

### 3.3 Behavior & Conduct Bonuses

**Positive Behavior:**
- **Good Conduct Bonus:** £1-10 (seasonal, no cards)
- **Gentleman Bonus:** £0.50-5.00 (fair play, sportsmanship)
- **Partnership Bonus:** £0.25-2.50 (team chemistry, assists to specific players)
- **Loyalty Bonus:** £2-20 (years at club)

**Negative Behavior (Penalties):**
- **Disciplinary Fines:** -£0.10-1.00 per card
- **Misconduct Fines:** -£0.50-5.00 per incident
- **Training Absence Fines:** -£0.25-2.00 per absence

### 3.4 Contract Clauses

**Release Clauses:**
- **Minimum Fee Release:** £50-10,000 (transfer fee triggers release)
- **Relegation Release:** £25-5,000 (if club relegated)
- **Promotion Release:** £100-20,000 (if club promoted, player can leave)

**Financial Clauses:**
- **Sell-On Fee:** 5-25% of future transfer fee to player
- **Loyalty Bonus:** £1-50 (paid on contract completion)
- **Agent Fee:** £5-200 (one-time, paid by club)

**Contract Terms:**
- **Contract Length:** 1-5 years (or indefinite)
- **Extension Option:** Automatic or optional
- **Break Clause:** Either party can terminate (with notice)

---

## 4. DATA MODEL DESIGN

### 4.1 PlayerContract Data Model

```java
public class PlayerContract implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Basic Information
    private Long id;
    private Long playerId;
    private Long clubId;
    private Integer contractType; // AMATEUR, SEMI_PRO, PROFESSIONAL, etc.
    
    // Contract Terms
    private LocalDateTime startDate;
    private LocalDateTime endDate; // null for indefinite
    private Integer contractLengthMonths; // null for indefinite
    
    // Financial Terms
    private BigDecimal weeklyWage = BigDecimal.ZERO;
    private BigDecimal signingBonus = BigDecimal.ZERO;
    private BigDecimal loyaltyBonus = BigDecimal.ZERO;
    private BigDecimal agentFee = BigDecimal.ZERO;
    
    // Performance Bonuses (per occurrence)
    private BigDecimal appearanceFee = BigDecimal.ZERO;
    private BigDecimal unusedSubstituteFee = BigDecimal.ZERO;
    private BigDecimal goalBonus = BigDecimal.ZERO;
    private BigDecimal assistBonus = BigDecimal.ZERO;
    private BigDecimal cleanSheetBonus = BigDecimal.ZERO;
    private BigDecimal manOfTheMatchBonus = BigDecimal.ZERO;
    
    // Achievement Bonuses (one-time)
    private BigDecimal leagueWinBonus = BigDecimal.ZERO;
    private BigDecimal leagueRunnerUpBonus = BigDecimal.ZERO;
    private BigDecimal cupWinBonus = BigDecimal.ZERO;
    private BigDecimal cupRunnerUpBonus = BigDecimal.ZERO;
    private BigDecimal promotionBonus = BigDecimal.ZERO;
    private BigDecimal relegationWageDrop = BigDecimal.ZERO; // Percentage (0-100)
    
    // Standing Bonuses
    private BigDecimal top3FinishBonus = BigDecimal.ZERO;
    private BigDecimal topHalfFinishBonus = BigDecimal.ZERO;
    private BigDecimal avoidRelegationBonus = BigDecimal.ZERO;
    
    // Seasonal Performance Bonuses
    private BigDecimal topGoalscorerBonus = BigDecimal.ZERO;
    private BigDecimal mostAssistsBonus = BigDecimal.ZERO;
    private BigDecimal playerOfTheYearBonus = BigDecimal.ZERO;
    private BigDecimal mostCleanSheetsBonus = BigDecimal.ZERO;
    
    // Behavior Bonuses
    private BigDecimal goodConductBonus = BigDecimal.ZERO;
    private BigDecimal gentlemanBonus = BigDecimal.ZERO;
    private BigDecimal partnershipBonus = BigDecimal.ZERO;
    
    // Contract Clauses
    private BigDecimal minimumFeeRelease = null; // null = no clause
    private BigDecimal relegationReleaseFee = null;
    private BigDecimal promotionReleaseFee = null;
    private BigDecimal sellOnFeePercentage = null; // 0-100
    private Boolean hasExtensionOption = false;
    private Integer extensionOptionMonths = null;
    
    // Contract Status
    private Boolean isActive = true;
    private Boolean isNegotiating = false;
    private LocalDateTime lastNegotiationDate = null;
    
    // Historical Tracking
    private BigDecimal totalWagesPaid = BigDecimal.ZERO;
    private BigDecimal totalBonusesPaid = BigDecimal.ZERO;
    private Integer appearances = 0;
    private Integer goals = 0;
    private Integer assists = 0;
    private Integer cleanSheets = 0;
    
    // Methods
    public BigDecimal calculateWeeklyCost() {
        // Base wage + any guaranteed payments
        return weeklyWage;
    }
    
    public BigDecimal calculateTotalValue() {
        // Estimate total contract value (wages + bonuses + signing bonus)
        BigDecimal total = BigDecimal.ZERO;
        if (contractLengthMonths != null) {
            BigDecimal totalWages = weeklyWage.multiply(BigDecimal.valueOf(contractLengthMonths * 4.33));
            total = total.add(totalWages);
        }
        total = total.add(signingBonus);
        total = total.add(loyaltyBonus);
        return total;
    }
    
    public boolean isExpired(LocalDateTime currentDate) {
        if (endDate == null) {
            return false; // Indefinite contract
        }
        return currentDate.isAfter(endDate);
    }
    
    public boolean isExpiringSoon(LocalDateTime currentDate, int monthsWarning) {
        if (endDate == null) {
            return false;
        }
        LocalDateTime warningDate = endDate.minusMonths(monthsWarning);
        return currentDate.isAfter(warningDate) && !isExpired(currentDate);
    }
}
```

### 4.2 Contract Type Constants

```java
public class ContractType {
    public static final int AMATEUR = 0;
    public static final int SEMI_PROFESSIONAL = 1;
    public static final int PROFESSIONAL = 2;
    public static final int SCHOLARSHIP = 3; // Future
    public static final int NON_CONTRACT = 4; // Trialist
    
    public static String getName(Integer type) {
        if (type == null) return "Unknown";
        switch (type) {
            case AMATEUR: return "Amateur";
            case SEMI_PROFESSIONAL: return "Semi-Professional";
            case PROFESSIONAL: return "Professional";
            case SCHOLARSHIP: return "Scholarship";
            case NON_CONTRACT: return "Non-Contract";
            default: return "Unknown";
        }
    }
    
    public static boolean requiresTraining(Integer type) {
        if (type == null) return false;
        return type == SEMI_PROFESSIONAL || type == PROFESSIONAL;
    }
    
    public static boolean isFullTime(Integer type) {
        if (type == null) return false;
        return type == PROFESSIONAL;
    }
}
```

### 4.3 Club Integration

```java
// Add to Club.java
private List<PlayerContract> playerContracts = new ArrayList<>();

public List<PlayerContract> getPlayerContracts() {
    if (playerContracts == null) {
        playerContracts = new ArrayList<>();
    }
    return playerContracts;
}

public PlayerContract getContractForPlayer(Long playerId) {
    if (playerContracts == null) return null;
    return playerContracts.stream()
        .filter(c -> c.getPlayerId().equals(playerId) && c.getIsActive())
        .findFirst()
        .orElse(null);
}

public BigDecimal calculateTotalWeeklyWages() {
    if (playerContracts == null) return BigDecimal.ZERO;
    return playerContracts.stream()
        .filter(c -> c.getIsActive())
        .map(PlayerContract::calculateWeeklyCost)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

---

## 5. SCRIPTABLE CONTRACT SYSTEM

### 5.1 Contract Script Format

**File Structure:**
```
mods/seasons/18/player_contracts/
  - 1.txt (Club ID 1 contracts)
  - 2.txt (Club ID 2 contracts)
  ...
```

**Script Format (CSV-like, with headers):**
```
# Player Contracts for Club [Club Name]
# Format: playerId,contractType,weeklyWage,signingBonus,startDate,endDate,goalBonus,appearanceFee,...
# Contract Types: 0=Amateur, 1=Semi-Pro, 2=Professional
# Dates: YYYY-MM-DD
# All monetary values in pounds (£)

playerId,contractType,weeklyWage,signingBonus,startDate,endDate,goalBonus,appearanceFee,cleanSheetBonus,leagueWinBonus,cupWinBonus,topGoalscorerBonus,minimumFeeRelease
1,2,1.00,5.00,1888-09-01,1891-08-31,0.50,0.10,0.25,20.00,10.00,5.00,100.00
2,2,0.75,3.00,1888-09-01,1890-08-31,0.25,0.05,0.10,15.00,8.00,3.00,75.00
3,1,0.30,0.00,1888-09-01,1889-08-31,0.10,0.02,0.00,5.00,2.00,0.00,25.00
4,0,0.00,0.00,1888-09-01,null,0.00,0.00,0.00,0.00,0.00,0.00,null
```

**Alternative Format (JSON-like, more readable):**
```
# Player Contracts for Club [Club Name]
# Format: playerId|contractType|weeklyWage|signingBonus|startDate|endDate|bonuses|clauses
# Bonuses: goalBonus,appearanceFee,cleanSheetBonus,leagueWinBonus,cupWinBonus,topGoalscorerBonus
# Clauses: minimumFeeRelease,relegationReleaseFee,promotionReleaseFee,sellOnFeePercentage

1|2|1.00|5.00|1888-09-01|1891-08-31|0.50,0.10,0.25,20.00,10.00,5.00|100.00,null,null,null
2|2|0.75|3.00|1888-09-01|1890-08-31|0.25,0.05,0.10,15.00,8.00,3.00|75.00,null,null,null
```

### 5.2 Contract Loader

```java
public class PlayerContractsLoader {
    
    public void loadContractsForClub(Club club, String seasonPath) {
        String filePath = seasonPath + "/player_contracts/" + club.getId() + ".txt";
        // Load and parse contract file
        // Create PlayerContract objects
        // Add to club.getPlayerContracts()
    }
    
    public void initializeContractsForSeason(SaveGame game, String seasonId) {
        // Load contracts for all clubs in the season
        // If contract file doesn't exist, generate random contracts
        // Link contracts to players
    }
}
```

### 5.3 Random Contract Generation (Season 1)

**For clubs without contract scripts:**
- Generate contracts based on:
  - Player skill/attributes
  - Club financial status
  - Historical wage ranges
  - Contract type distribution (mix of amateur/semi-pro/pro)

**Algorithm:**
1. Determine contract type based on player skill and club level
2. Set base wage within historical ranges
3. Add random bonuses (goal, appearance, etc.)
4. Set contract length (1-3 years typically)
5. Add release clauses for top players

---

## 6. FREE PLAYER MARKET FOUNDATION

### 6.1 Player Market Status

```java
public class PlayerMarketStatus {
    public static final int UNDER_CONTRACT = 0;
    public static final int FREE_AGENT = 1;
    public static final int TRANSFER_LISTED = 2;
    public static final int LOAN_LISTED = 3;
    public static final int RETIRED = 4;
}
```

### 6.2 Transfer Offer System (Foundation)

```java
public class TransferOffer implements Serializable {
    private Long id;
    private Long fromClubId; // Offering club
    private Long toClubId; // Receiving club
    private Long playerId;
    private BigDecimal transferFee;
    private BigDecimal weeklyWage; // Proposed wage
    private BigDecimal signingBonus;
    private Integer contractLengthMonths;
    private LocalDateTime offerDate;
    private LocalDateTime expiryDate;
    private Integer status; // PENDING, ACCEPTED, REJECTED, WITHDRAWN
    private String rejectionReason;
}
```

### 6.3 Free Agent System

```java
// Add to Player.java
private Integer marketStatus = PlayerMarketStatus.UNDER_CONTRACT;
private LocalDateTime contractExpiryDate = null;
private Boolean isTransferListed = false;
private Boolean isLoanListed = false;

// Methods
public boolean isFreeAgent(LocalDateTime currentDate) {
    return marketStatus == PlayerMarketStatus.FREE_AGENT ||
           (contractExpiryDate != null && currentDate.isAfter(contractExpiryDate));
}
```

---

## 7. INTEGRATION WITH EXPENSE SYSTEM

### 7.1 Update ExpenseCalculator

```java
// In ExpenseCalculator.calculateWeeklyExpenses()
BigDecimal playerWages = club.calculateTotalWeeklyWages();
expenses.setPlayerWages(playerWages);
```

### 7.2 Bonus Payment System

```java
public class ContractBonusCalculator {
    
    public BigDecimal calculateMatchBonuses(PlayerContract contract, Match match, MatchResult result) {
        BigDecimal total = BigDecimal.ZERO;
        
        // Appearance fee
        if (result.getPlayerPlayed(contract.getPlayerId())) {
            total = total.add(contract.getAppearanceFee());
        }
        
        // Goal bonus
        int goals = result.getGoalsForPlayer(contract.getPlayerId());
        total = total.add(contract.getGoalBonus().multiply(BigDecimal.valueOf(goals)));
        
        // Assist bonus
        int assists = result.getAssistsForPlayer(contract.getPlayerId());
        total = total.add(contract.getAssistBonus().multiply(BigDecimal.valueOf(assists)));
        
        // Clean sheet bonus (for goalkeepers/defenders)
        if (result.isCleanSheet() && result.getPlayerPlayed(contract.getPlayerId())) {
            total = total.add(contract.getCleanSheetBonus());
        }
        
        // Man of the match bonus
        if (result.getManOfTheMatch() != null && result.getManOfTheMatch().equals(contract.getPlayerId())) {
            total = total.add(contract.getManOfTheMatchBonus());
        }
        
        return total;
    }
    
    public BigDecimal calculateSeasonBonuses(PlayerContract contract, Club club, Season season) {
        BigDecimal total = BigDecimal.ZERO;
        
        // League win bonus
        if (season.getLeagueWinner() != null && season.getLeagueWinner().equals(club.getId())) {
            total = total.add(contract.getLeagueWinBonus());
        }
        
        // Cup win bonus
        // ... (similar logic)
        
        // Top goalscorer bonus
        // ... (check if player is top scorer)
        
        return total;
    }
}
```

---

## 8. IMPLEMENTATION PLAN

### Phase 1: Data Models & Constants (Day 1)
1. Create `PlayerContract.java` with all fields
2. Create `ContractType.java` constants
3. Create `PlayerMarketStatus.java` constants
4. Add contract tracking to `Club.java`
5. Add market status to `Player.java`

### Phase 2: Contract Loading System (Day 1-2)
1. Create `PlayerContractsLoader.java`
2. Design contract script format
3. Implement contract file parsing
4. Implement random contract generation (for season 1)
5. Link contracts to players on game load

### Phase 3: Expense Integration (Day 2)
1. Update `ExpenseCalculator` to use player wages
2. Create `ContractBonusCalculator.java`
3. Integrate bonus payments into match simulation
4. Update financial screen to show player wages breakdown

### Phase 4: Free Agent & Market Foundation (Day 2-3)
1. Implement free agent detection (expired contracts)
2. Create `TransferOffer.java` data model
3. Add transfer listing functionality
4. Create foundation for offer system (UI later)

### Phase 5: Testing & Historical Data (Day 3)
1. Test contract loading from scripts
2. Test random contract generation
3. Verify expense calculations
4. Research and add historical 1888-89 contract data to scripts

---

## 9. HISTORICAL ACCURACY NOTES

### 9.1 1888-89 Season Contracts

**Key Points:**
- Mix of amateur and professional players
- Professional wages: 10s - £1/week typically
- Top players: £1-2/week
- No formal bonus structures (simple agreements)
- No release clauses (informal transfers)
- Contracts typically 1-2 years

**Implementation Strategy:**
- Season 1: Use random generation with historical ranges
- Future seasons: Load from scripts with researched data
- Gradually add more structured contracts as game progresses

### 9.2 Contract Evolution

**1888-1890:** Basic wage agreements
**1890-1900:** Introduction of bonuses (wins, draws, cups)
**1900-1910:** More formal contracts
**1910+:** Modern contract structures

---

## 10. FUTURE ENHANCEMENTS

### 10.1 Contract Negotiation UI
- Offer/counter-offer interface
- Agent interactions
- Role promises
- Clause negotiations

### 10.2 Transfer Market
- Transfer offers between clubs
- Player valuations
- Transfer windows
- Loan system

### 10.3 Contract Analytics
- Wage budget tracking
- Contract expiry warnings
- Bonus cost projections
- Market value calculations

---

## 11. CONCLUSION

This contracts system provides:
- ✅ Comprehensive bonus and clause system
- ✅ Historical accuracy (1888-89)
- ✅ Scriptable for future seasons
- ✅ Foundation for transfer market
- ✅ Integration with expense system
- ✅ Flexible and extensible design

**Next Steps:**
1. Implement data models
2. Create contract loading system
3. Integrate with expenses
4. Add free agent system
5. Research and script historical contracts

---

**Implementation Priority:** CRITICAL  
**Estimated Effort:** 3-4 days  
**Dependencies:** Club expenses system (✅ Complete), Player data model (✅ Complete)
