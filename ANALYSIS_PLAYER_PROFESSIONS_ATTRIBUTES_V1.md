# Player Professions & Attribute Bonuses Analysis v1.0

**Analysis Date:** 2025-01-XX  
**Feature:** Player Profession System with Attribute Bonuses  
**Priority:** HIGH - Enhances realism and RPG-like character depth

---

## Executive Summary

This document designs a profession system for amateur and semi-professional players, where their day jobs provide realistic attribute bonuses. Based on historical 1888-89 data, players had to live within 6 miles of the ground and often worked in local industries (mills, railways, factories, etc.).

---

## 1. HISTORICAL RESEARCH FINDINGS

### 1.1 Real Player Professions (1888-89)

**Examples from Research:**

| Player | Club | Profession | Notes |
|--------|------|------------|-------|
| **Fergie Suter** | Blackburn Rovers | Stonemason / Cotton Mill Sizer | Moved to England for work |
| **Johnny Graham** | Preston North End | Quarryman | Half-back, league winner |
| **Billy Townley** | Blackburn Rovers | Schoolmaster | Outside left, fast winger |
| **Jack Holland** | Notts County | Tramcar Conductor | Goalkeeper |
| **Various** | Leicester Fosse | Factory Workers | Local employers |

**Common Professions (1888-89 England):**
- **Textile Industry:** Spinner, piecer, scavenger, sizer, weaver, dyer
- **Railway:** Railway worker, signalman, guard, porter, engineer
- **Mining/Quarrying:** Miner, quarryman, coal miner
- **Construction:** Stonemason, bricklayer, carpenter, builder
- **Factory:** Factory worker, foundry worker, steelworker
- **Transport:** Tramcar conductor, coach driver, cart driver
- **Education:** Schoolmaster, teacher
- **Skilled Trades:** Blacksmith, tailor, shoemaker, baker
- **Agriculture:** Farmer, farm laborer
- **Service:** Publican, shopkeeper, clerk

### 1.2 Geographic Constraint

**1888-89 Rule:** Players had to be born or have lived for 2+ years within 6 miles of the ground.

**Implication:** Professions must be local to the club's area. For example:
- **Blackburn/Preston:** Textile mills, cotton industry
- **Derby/Nottingham:** Railway, lace industry
- **Birmingham:** Metalworking, foundries
- **Stoke:** Pottery, mining
- **Bolton:** Textiles, engineering

---

## 2. ATTRIBUTE SYSTEM DESIGN

### 2.1 Attribute Scale

**Base Range:** 1-20 (standard D&D-style)
- **1-5:** Poor
- **6-10:** Below Average
- **11-15:** Average to Good
- **16-18:** Excellent
- **19-20:** World Class

**Exceptional Range:** 21-22 (rare, exceptional players)
- **21:** Exceptional (1888 Messi-level)
- **22:** Legendary (once-in-a-generation)

**Profession Bonuses:** +1 to +3 to specific attributes
- Allows players to exceed normal maximums
- Realistic bonuses based on profession characteristics

### 2.2 Attribute Categories

**Physical Attributes:**
- Strength
- Endurance
- Stamina
- Speed
- Acceleration
- Jumping
- Dexterity

**Mental Attributes:**
- Concentration
- Courage
- Determination
- Leadership
- Perception
- Positioning
- Teamwork

**Technical Attributes:**
- Passing
- Kicking
- Long Shots
- Trick Shots
- Heading
- Marking
- Tackling
- (Goalkeeper attributes)

---

## 3. PROFESSION BONUSES DESIGN

### 3.1 Textile Industry Professions

**Spinner / Weaver / Sizer:**
- **Endurance:** +2 (long hours, repetitive work)
- **Dexterity:** +1 (hand-eye coordination)
- **Concentration:** +1 (attention to detail)

**Rationale:** Textile work required stamina, fine motor skills, and focus.

---

### 3.2 Railway Professions

**Railway Worker / Signalman / Guard:**
- **Strength:** +1 (lifting, manual work)
- **Endurance:** +2 (long shifts, physical work)
- **Perception:** +1 (awareness, safety)
- **Teamwork:** +1 (coordination with team)

**Rationale:** Railway work was physically demanding and required teamwork and awareness.

---

### 3.3 Mining/Quarrying Professions

**Miner / Quarryman:**
- **Strength:** +3 (very physically demanding)
- **Endurance:** +2 (long, grueling work)
- **Courage:** +1 (dangerous work)
- **Determination:** +1 (tough conditions)

**Rationale:** Mining/quarrying was extremely physically demanding and dangerous.

---

### 3.4 Construction Professions

**Stonemason / Bricklayer / Carpenter:**
- **Strength:** +2 (heavy materials)
- **Dexterity:** +1 (precision work)
- **Concentration:** +1 (attention to detail)
- **Positioning:** +1 (spatial awareness)

**Rationale:** Construction work required strength, precision, and spatial awareness.

---

### 3.5 Factory/Foundry Professions

**Factory Worker / Foundry Worker / Steelworker:**
- **Strength:** +2 (heavy machinery, materials)
- **Endurance:** +2 (long shifts, heat)
- **Courage:** +1 (dangerous environment)
- **Teamwork:** +1 (coordinated work)

**Rationale:** Factory work was physically demanding and required coordination.

---

### 3.6 Transport Professions

**Tramcar Conductor / Coach Driver:**
- **Endurance:** +1 (long hours on feet)
- **Perception:** +1 (awareness of surroundings)
- **Teamwork:** +1 (interaction with public)
- **Leadership:** +1 (managing passengers)

**Rationale:** Transport work required endurance, awareness, and people skills.

---

### 3.7 Education Professions

**Schoolmaster / Teacher:**
- **Perception:** +2 (observing students)
- **Leadership:** +2 (managing class)
- **Concentration:** +1 (teaching requires focus)
- **Teamwork:** +1 (working with colleagues)

**Rationale:** Teaching requires mental acuity, leadership, and perception.

---

### 3.8 Skilled Trades

**Blacksmith:**
- **Strength:** +2 (hammering, forging)
- **Endurance:** +1 (heat, physical work)
- **Dexterity:** +1 (precision work)
- **Concentration:** +1 (attention to detail)

**Tailor / Shoemaker:**
- **Dexterity:** +2 (fine hand work)
- **Concentration:** +1 (precision)
- **Perception:** +1 (attention to detail)

**Baker:**
- **Strength:** +1 (kneading dough)
- **Endurance:** +1 (early hours, standing)
- **Dexterity:** +1 (precision work)

---

### 3.9 Agriculture Professions

**Farmer / Farm Laborer:**
- **Strength:** +2 (heavy farm work)
- **Endurance:** +2 (long days, physical labor)
- **Stamina:** +1 (sustained physical activity)
- **Determination:** +1 (hard work, weather)

**Rationale:** Farming was extremely physically demanding.

---

### 3.10 Service Professions

**Publican / Shopkeeper:**
- **Teamwork:** +1 (customer service)
- **Leadership:** +1 (managing business)
- **Perception:** +1 (reading customers)

**Clerk:**
- **Concentration:** +1 (office work)
- **Perception:** +1 (attention to detail)
- **Dexterity:** +1 (writing, paperwork)

---

## 4. PROFESSION DATA MODEL

### 4.1 PlayerProfession Data Model

```java
public class PlayerProfession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String description;
    
    // Attribute Bonuses (can be positive or negative)
    private Integer strengthBonus = 0;
    private Integer enduranceBonus = 0;
    private Integer staminaBonus = 0;
    private Integer speedBonus = 0;
    private Integer accelerationBonus = 0;
    private Integer jumpingBonus = 0;
    private Integer dexterityBonus = 0;
    
    private Integer concentrationBonus = 0;
    private Integer courageBonus = 0;
    private Integer determinationBonus = 0;
    private Integer leadershipBonus = 0;
    private Integer perceptionBonus = 0;
    private Integer positioningBonus = 0;
    private Integer teamworkBonus = 0;
    
    // Profession Category (for grouping)
    private String category; // "TEXTILE", "RAILWAY", "MINING", "CONSTRUCTION", etc.
    
    // Historical Context
    private String historicalNotes; // Description of profession in 1888-89
    private Boolean isCommon = true; // Common profession for the era
}
```

### 4.2 Integration with Player

```java
// Add to Player.java
private PlayerProfession playerProfession; // Day job (for amateur/semi-pro)

// Method to apply profession bonuses
public void applyProfessionBonuses() {
    if (playerProfession == null) {
        return; // No profession, no bonuses
    }
    
    // Apply bonuses (capped at 22 maximum)
    if (strength != null && playerProfession.getStrengthBonus() != null) {
        strength = Math.min(22, strength + playerProfession.getStrengthBonus());
    }
    // ... apply all bonuses
}
```

### 4.3 Integration with PlayerContract

```java
// In PlayerContract.java
// Profession is linked to contract type
// Amateur/Semi-Pro players have professions
// Professional players typically don't (full-time football)
```

---

## 5. PROFESSION LIST (1888-89)

### 5.1 Textile Industry (10 professions)
1. **Spinner** - Endurance +2, Dexterity +1, Concentration +1
2. **Weaver** - Endurance +2, Dexterity +1, Concentration +1
3. **Sizer** - Endurance +1, Dexterity +1
4. **Piecer** - Dexterity +1, Concentration +1
5. **Dyer** - Dexterity +1, Concentration +1
6. **Scavenger** - Endurance +1, Dexterity +1
7. **Loom Operator** - Endurance +2, Dexterity +1
8. **Cotton Carder** - Strength +1, Endurance +1
9. **Textile Inspector** - Perception +1, Concentration +1
10. **Mill Foreman** - Leadership +1, Teamwork +1

### 5.2 Railway Industry (8 professions)
1. **Railway Worker** - Strength +1, Endurance +2, Teamwork +1
2. **Signalman** - Perception +2, Concentration +1
3. **Railway Guard** - Endurance +1, Leadership +1, Teamwork +1
4. **Porter** - Strength +1, Endurance +1
5. **Railway Engineer** - Perception +1, Concentration +1, Dexterity +1
6. **Track Layer** - Strength +2, Endurance +2
7. **Station Master** - Leadership +2, Perception +1
8. **Railway Clerk** - Concentration +1, Perception +1

### 5.3 Mining/Quarrying (6 professions)
1. **Coal Miner** - Strength +3, Endurance +2, Courage +1, Determination +1
2. **Quarryman** - Strength +3, Endurance +2, Courage +1
3. **Iron Miner** - Strength +3, Endurance +2, Courage +1
4. **Mine Foreman** - Strength +1, Leadership +1, Courage +1
5. **Mine Engineer** - Perception +1, Concentration +1, Dexterity +1
6. **Mine Clerk** - Concentration +1, Perception +1

### 5.4 Construction (8 professions)
1. **Stonemason** - Strength +2, Dexterity +1, Concentration +1, Positioning +1
2. **Bricklayer** - Strength +2, Dexterity +1, Positioning +1
3. **Carpenter** - Strength +1, Dexterity +2, Concentration +1
4. **Builder** - Strength +2, Endurance +1, Teamwork +1
5. **Plumber** - Dexterity +1, Concentration +1
6. **Painter** - Dexterity +1, Concentration +1
7. **Construction Foreman** - Leadership +1, Teamwork +1
8. **Architect** - Perception +2, Concentration +1, Positioning +2

### 5.5 Factory/Foundry (8 professions)
1. **Factory Worker** - Strength +2, Endurance +2, Teamwork +1
2. **Foundry Worker** - Strength +2, Endurance +2, Courage +1
3. **Steelworker** - Strength +2, Endurance +2, Courage +1
4. **Metalworker** - Strength +1, Dexterity +1, Endurance +1
5. **Machine Operator** - Dexterity +1, Concentration +1
6. **Factory Foreman** - Leadership +1, Teamwork +1
7. **Quality Inspector** - Perception +1, Concentration +1
8. **Engineer** - Perception +1, Concentration +1, Dexterity +1

### 5.6 Transport (6 professions)
1. **Tramcar Conductor** - Endurance +1, Perception +1, Teamwork +1, Leadership +1
2. **Coach Driver** - Endurance +1, Perception +1, Concentration +1
3. **Cart Driver** - Strength +1, Endurance +1
4. **Cab Driver** - Endurance +1, Perception +1
5. **Delivery Man** - Strength +1, Endurance +1, Stamina +1
6. **Transport Manager** - Leadership +1, Teamwork +1

### 5.7 Education (4 professions)
1. **Schoolmaster** - Perception +2, Leadership +2, Concentration +1, Teamwork +1
2. **Teacher** - Perception +1, Leadership +1, Concentration +1
3. **Headmaster** - Leadership +2, Perception +1
4. **Tutor** - Perception +1, Concentration +1

### 5.8 Skilled Trades (10 professions)
1. **Blacksmith** - Strength +2, Endurance +1, Dexterity +1, Concentration +1
2. **Tailor** - Dexterity +2, Concentration +1, Perception +1
3. **Shoemaker** - Dexterity +2, Concentration +1
4. **Baker** - Strength +1, Endurance +1, Dexterity +1
5. **Butcher** - Strength +1, Dexterity +1
6. **Cobbler** - Dexterity +1, Concentration +1
7. **Watchmaker** - Dexterity +2, Concentration +2, Perception +1
8. **Jeweler** - Dexterity +2, Concentration +1, Perception +1
9. **Barber** - Dexterity +1, Teamwork +1
10. **Tinsmith** - Dexterity +1, Strength +1

### 5.9 Agriculture (6 professions)
1. **Farmer** - Strength +2, Endurance +2, Stamina +1, Determination +1
2. **Farm Laborer** - Strength +2, Endurance +2, Stamina +1
3. **Shepherd** - Endurance +1, Perception +1, Determination +1
4. **Dairy Worker** - Strength +1, Endurance +1
5. **Agricultural Worker** - Strength +1, Endurance +1, Stamina +1
6. **Farm Manager** - Leadership +1, Determination +1

### 5.10 Service (8 professions)
1. **Publican** - Teamwork +1, Leadership +1, Perception +1
2. **Shopkeeper** - Teamwork +1, Leadership +1, Perception +1
3. **Clerk** - Concentration +1, Perception +1, Dexterity +1
4. **Bookkeeper** - Concentration +1, Perception +1
5. **Accountant** - Concentration +2, Perception +1
6. **Bank Clerk** - Concentration +1, Perception +1
7. **Postman** - Endurance +1, Stamina +1
8. **Messenger** - Speed +1, Endurance +1, Stamina +1

### 5.11 Other (6 professions)
1. **Policeman** - Strength +1, Courage +2, Leadership +1, Perception +1
2. **Fireman** - Strength +2, Courage +2, Endurance +1, Teamwork +1
3. **Soldier** - Strength +1, Endurance +1, Courage +1, Determination +1, Teamwork +1
4. **Sailor** - Strength +1, Endurance +1, Courage +1, Teamwork +1
5. **Dock Worker** - Strength +2, Endurance +1, Teamwork +1
6. **Warehouse Worker** - Strength +1, Endurance +1

**Total: 80 professions**

---

## 6. SCRIPTABLE PROFESSION SYSTEM

### 6.1 Profession Script Format

**File:** `mods/seasons/18/player_professions.txt`

```
# Player Professions for 1888-89 Season
# Format: id,name,category,strengthBonus,enduranceBonus,staminaBonus,speedBonus,accelerationBonus,jumpingBonus,dexterityBonus,concentrationBonus,courageBonus,determinationBonus,leadershipBonus,perceptionBonus,positioningBonus,teamworkBonus,historicalNotes

1,Spinner,TEXTILE,0,2,0,0,0,0,1,1,0,0,0,0,0,0,"Textile mill worker operating spinning machines"
2,Weaver,TEXTILE,0,2,0,0,0,0,1,1,0,0,0,0,0,0,"Textile mill worker operating looms"
3,Quarryman,MINING,3,2,0,0,0,0,0,0,1,1,0,0,0,0,"Extremely physically demanding work extracting stone"
4,Stonemason,CONSTRUCTION,2,0,0,0,0,0,1,1,0,0,0,0,1,0,"Skilled craftsman working with stone"
5,Schoolmaster,EDUCATION,0,0,0,0,0,0,1,0,0,0,2,2,0,1,"Teacher and school administrator"
...
```

### 6.2 Profession Loader

```java
public class PlayerProfessionsLoader {
    
    public static Map<Long, PlayerProfession> loadProfessions(String seasonPath) {
        // Load from mods/seasons/{seasonId}/player_professions.txt
        // Return map of profession ID to PlayerProfession
    }
}
```

---

## 7. IMPLEMENTATION PLAN

### Phase 1: Data Models (Day 1)
1. Create `PlayerProfession.java` data model
2. Add `playerProfession` field to `Player.java`
3. Create profession loading system

### Phase 2: Profession List & Bonuses (Day 1-2)
1. Create comprehensive profession list (80+ professions)
2. Assign realistic attribute bonuses
3. Create profession script file
4. Implement profession loader

### Phase 3: Integration (Day 2)
1. Apply profession bonuses to player attributes
2. Link professions to contract types (amateur/semi-pro)
3. Random profession assignment for players without contracts
4. Display profession in player detail screen

### Phase 4: Testing (Day 2-3)
1. Test profession bonuses
2. Verify attribute caps (max 22)
3. Test profession loading from scripts
4. Verify historical accuracy

---

## 8. ATTRIBUTE CAP SYSTEM

### 8.1 Base Attributes
- **Minimum:** 1
- **Maximum:** 20 (normal)
- **Exceptional:** 21-22 (rare, exceptional players)

### 8.2 Profession Bonus Application

```java
public void applyProfessionBonuses() {
    if (playerProfession == null) {
        return;
    }
    
    // Apply bonuses, but cap at 22
    strength = Math.min(22, Math.max(1, strength + playerProfession.getStrengthBonus()));
    // ... for all attributes
}
```

### 8.3 Example

**Player Base Attributes:**
- Strength: 15
- Endurance: 12
- Dexterity: 10

**Profession: Quarryman**
- Strength Bonus: +3
- Endurance Bonus: +2

**Result:**
- Strength: 18 (15 + 3)
- Endurance: 14 (12 + 2)
- Dexterity: 10 (no bonus)

**If player had Strength 20 + Quarryman (+3):**
- Strength: 22 (capped at 22, not 23)

---

## 9. CONCLUSION

This profession system:
- ✅ Adds realism (1888-89 historical accuracy)
- ✅ Provides RPG-like depth (attribute bonuses)
- ✅ Supports 80+ professions
- ✅ Scriptable for moddability
- ✅ Realistic bonuses (not overpowered)
- ✅ Respects attribute caps (1-22)

**Next Steps:**
1. Implement PlayerProfession data model
2. Create profession list with bonuses
3. Integrate with Player attributes
4. Create profession loader
5. Test and refine

---

**Implementation Priority:** HIGH  
**Estimated Effort:** 2-3 days  
**Dependencies:** Player data model (✅ Complete), Contract system (✅ Complete)
