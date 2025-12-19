# Analysis: Season Scripting Mechanics & Season 2 (1889-90) Design

**Analysis Date:** 2025-12-14  
**Project:** Futtoboru  
**Focus:** Scripting Engine Evolution & Historical Accuracy for Season 2

---

## 📊 Executive Summary

This document analyzes the current "Season Scripting" mechanics and outlines the requirements for **Season 2 (1889-1890)**. To evolve from a static simulation to a dynamic historical journey, the scripting engine must move from hardcoded logic to a data-driven event system that handles club foundings, league re-elections, rule changes, and player progression.

---

## 1. SEASON 1 RECAP (1888-89) - REAL LIFE CHAMPIONS

To provide context for the start of Season 2, the game must recognize the historic winners of the first season:

| Competition | Real-Life Winner | Runner-Up |
| :--- | :--- | :--- |
| **English Football League** | **Preston North End** (Invincibles) | Aston Villa |
| **FA Cup** | **Preston North End** | Wolverhampton Wanderers |
| **Scottish Cup** | **Third Lanark** | Celtic |
| **Welsh Cup** | **Bangor** | Northwich Victoria |
| **British Home Championship** | **Scotland** | England |

---

## 2. SEASON 2 (1889-1890) HISTORICAL RESEARCH

### 2.1 League Composition & Changes
The Football League maintained its **12-team format**. 
*   **Bottom 4 Re-election:** Burnley, Derby County, Notts County, and Stoke were the bottom four in 1888-89.
*   **AGM Result:** All four were successfully re-elected for the 1889-90 season.
*   **The Combination:** A rival league (The Combination) collapsed in April 1889, leading to the formation of the **Football Alliance** in 1889, which featured teams like Sheffield Wednesday, Nottingham Forest, and Newton Heath (later Man Utd).

### 2.2 Continental Europe: Active Championships
To serve as a "de facto source for football history," Season 2 must include the active competitions in the Netherlands and Denmark, moving them beyond "ghost" status:

1.  **Netherlands (KNVB):**
    *   **Founded:** Dec 8, 1889 (First FA in mainland Europe).
    *   **Competition:** 1889-90 Netherlands Football League Championship (7 teams).
    *   **Winner:** **Koninklijke HFC** (Haarlem).
2.  **Denmark (DBU):**
    *   **Founded:** May 18, 1889.
    *   **Competition:** 1889-90 Football Tournament (7 teams, Copenhagen-based).
    *   **Winner:** **Akademisk Boldklub (AB)**.

### 2.3 New Clubs Founded in 1889
The scripting engine should trigger the creation of these clubs during the season:
*   **England:** Sheffield United (Mar 22), Brentford FC (Oct 10), Wimbledon FC, Gillingham FC (May).
*   **Scotland:** Dundee FC (merger), St Johnstone FC.
*   **Denmark:** Akademisk Boldklub (AB).

---

## 3. UNIFIED GAME ARCHITECTURE: "SEASONS AS RELEASES"

The user's vision is a single, evolving game platform where new releases add both **Seasons** and **Engine Features**.

### 3.1 Script Backporting & Compatibility
To ensure Season 1 players benefit from Season 2 features, the scripting engine must be **stateless and global**:
1.  **Global Event Registry:** Instead of hardcoding scripts inside a season folder, scripts are registered in a global `historical_events.json`.
2.  **Date-Triggered Logic:** The engine checks the global registry every day. If the current game date matches an event (e.g., "Founding of Sheffield United"), the event fires regardless of whether the user started in 1888 or 1889.
3.  **Engine Evolution:** As new match engines (2D/3D) are released, the script simply points to the new logic (e.g., `1891-01-01: ENABLE_GOAL_NETS`).

---

## 4. GAME MECHANICS: SIMULATION INSPIRATION

### 4.1 Football Manager / Championship Manager (Realism)
*   **The AGM (Annual General Meeting):** Implement a screen at the end of May where re-election votes happen. This is high-drama for bottom-table teams.
*   **Scouting in 1889:** No "internet" or data. Scouting is done via local newspapers and physical travel.

### 4.2 PC Futbol (Tycoon Elements)
*   **Stadium Expansion:** Clubs in 1889 were rapidly building stands (terraces). Scripts should increase stadium maintenance costs as capacity grows.
*   **Ticketing:** The 1889 season saw the first standard "Shilling" entry fee.

---

## 5. MEDIA & COMMUNICATION (1888-1899)

The in-game inbox must reflect the historically accurate media landscape of the Victorian era.

### 5.1 Historical Outlets & Journalists
*   **The UK "Football Bible":** *Athletic News* (Manchester). 
    *   *Key Journalist:* **James Catton** (pioneer of football reporting).
*   **Italy:** *La Gazzetta dello Sport* (Founded 1896). Its pink paper is iconic; the UI should reflect this for Italian news.
*   **Spain:** *La Vanguardia* (Barcelona). *Key Journalist:* **Alberto Serra**.

### 5.2 Information Lag & Transmission
*   **Local Results:** Transmitted via **Telegraph**. Results were available in cities within hours via "Special Evening Editions" (e.g., *The Football Pink*).
*   **Intercontinental Lag (South America <-> Europe):** 
    *   **Transmission:** Mostly via **Steamship Mail**.
    *   **Lag Time:** **14 to 21 Days**. 
    *   *Gameplay Impact:* If River Plate wins a match in Buenos Aires, the European manager only sees the news in their inbox 3 weeks later. The cable was too expensive for routine sports scores.
*   **Public Gathering:** Before radio, fans gathered outside newspaper offices to hear telegrams read through megaphones. We can add a "Crowd Noise" or "Public Interest" stat based on office gatherings.

---

## 6. TECHNICAL IMPLEMENTATION STEPS FOR SEASON 2

1.  **Data Structure:** Create `mods/seasons/19/` folder (Season ID 19 = 1889-90).
2.  **Transition Script:**
    ```json
    {
      "event": "SEASON_TRANSITION",
      "date": "1889-06-01",
      "actions": ["INCREMENT_AGES", "APPLY_RE_ELECTION_RESULTS", "GENERATE_NEW_REGENS"]
    }
    ```
3.  **Club Founding Scripts:**
    ```csv
    # date, action, club_id, name, city
    1889-03-22,FOUND_CLUB,100,Sheffield United,Sheffield
    ```

---

## 6. RECOMMENDATIONS FOR MVP & ROAD TO 1.0

1.  **Prioritize the "English Pyramid":** Even if other countries aren't playable, having their clubs "exist" for FA Cup invitation is critical for historical depth.
2.  **Implement Rule Scripting:** Don't hardcode rules. Use a `RuleSet` object that changes via script (e.g., `ruleSet.setPenaltyEnabled(true)` in 1891).
3.  **Player Progression:** Focus on the 3d6 attribute system. Daily small fluctuations + Season-end "Major Review" based on performance.

---

*Analysis completed by Project Manager Agent.*

