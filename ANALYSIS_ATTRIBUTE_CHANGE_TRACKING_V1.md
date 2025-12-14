# Attribute Change Tracking Analysis & Design v1.0

**Analysis Date:** 2025-01-14  
**Branch:** feature/match-simulation-v1  
**Priority:** HIGH - Needed for meaningful attribute visualization

---

## Executive Summary

**Current State:** Attribute changes are tracked day-to-day, showing tiny fluctuations that are not meaningful for gameplay decisions. Players need to see trends over longer periods to understand player development, form, and training effectiveness.

**Goal:** Design a configurable attribute change tracking system that shows meaningful trends over appropriate time periods (days/weeks), similar to how Football Manager and other sports management games display player form and development.

**Key Insight:** Day-to-day attribute changes are too granular. Players need to see:
- **Short-term form** (7-15 days): Recent performance fluctuations
- **Medium-term development** (30-60 days): Training effects, natural development
- **Long-term trends** (90+ days): Seasonal changes, age-related decline

---

## 1. RESEARCH & BEST PRACTICES

### 1.1 Industry Standards

**Fantasy Premier League (FPL):**
- Form calculated as average points over **last 30 days**
- Emphasizes recent performance while smoothing daily fluctuations

**FormBaller:**
- Uses **last 10 matches** with recency weighting
- Most recent match weighted 1.5x more than 10th match back
- Identifies hot streaks and cold spells

**FootballCritic:**
- FC score based on **last 20 matches**
- Combines various performance metrics

**Football Manager Series:**
- Shows attribute changes over **last month** (30 days)
- Displays arrows (↑↓→) indicating improvement, decline, or stability
- Separate tracking for:
  - **Form**: Recent match performance (last 5-10 matches)
  - **Development**: Attribute changes over time (30+ days)
  - **Condition**: Current fitness/condition (real-time)

### 1.2 Real-World Context

**Training Effects:**
- Noticeable changes appear after **2-4 weeks** of consistent training
- Short-term fluctuations (daily) are often due to:
  - Fatigue/condition
  - Match load
  - Minor injuries
  - Natural variance

**Player Development:**
- Young players (18-23): Changes visible over **1-3 months**
- Prime players (24-30): Stable, changes over **3-6 months**
- Declining players (30+): Gradual decline over **6-12 months**

---

## 2. RECOMMENDED TIME PERIODS

### 2.1 Short-Term Form (7-15 days)
**Purpose:** Show recent performance fluctuations, match fitness, current condition

**Use Cases:**
- Quick assessment of player's current form
- Match selection decisions
- Injury recovery tracking
- Training intensity adjustments

**Default:** **14 days** (2 weeks)
- Long enough to smooth daily noise
- Short enough to show recent changes
- Aligns with typical match frequency (1-2 matches per week)

### 2.2 Medium-Term Development (30-60 days)
**Purpose:** Show training effectiveness, natural development, attribute improvements

**Use Cases:**
- Evaluating training program effectiveness
- Player development tracking
- Transfer decisions
- Contract negotiations

**Default:** **30 days** (1 month)
- Standard period in Football Manager
- Aligns with monthly training cycles
- Shows meaningful development trends

### 2.3 Long-Term Trends (90+ days)
**Purpose:** Show seasonal changes, age-related decline, career progression

**Use Cases:**
- Career planning
- Long-term development assessment
- Age-related decline monitoring
- Transfer market evaluation

**Default:** **90 days** (3 months / 1 season quarter)
- Shows seasonal patterns
- Useful for long-term planning
- Aligns with transfer windows

---

## 3. SYSTEM DESIGN

### 3.1 Attribute History Storage

**Option A: Snapshot System (Recommended)**
- Store attribute snapshots at regular intervals (e.g., weekly)
- Pros: Efficient storage, easy to query
- Cons: Less granular, requires periodic snapshot creation

**Option B: Change Log System**
- Store every attribute change with timestamp
- Pros: Complete history, very granular
- Cons: Storage intensive, complex queries

**Option C: Hybrid System (Best)**
- Store weekly snapshots for all attributes
- Store daily snapshots for "form" attributes (condition, fitness)
- Pros: Balance between storage and granularity
- Cons: More complex implementation

### 3.2 Data Model

```java
/**
 * Player Attribute Snapshot
 * Stores a snapshot of all player attributes at a specific date
 */
public class PlayerAttributeSnapshot implements Serializable {
    private Long playerId;
    private LocalDateTime snapshotDate;
    
    // Physical Attributes
    private Float acceleration;
    private Float speed;
    // ... all other attributes
    
    // Metadata
    private String snapshotType; // "WEEKLY", "DAILY", "MANUAL"
}
```

**Storage Strategy:**
- **Weekly snapshots**: Created every 7 days for all players
- **Daily snapshots**: Created daily for condition/fitness (future)
- **Retention**: Keep last 365 days (1 year) of snapshots
- **Cleanup**: Remove snapshots older than 1 year

### 3.3 Configuration Constants

```java
/**
 * Attribute Change Tracking Constants
 */
public class AttributeTrackingConstants {
    
    // Default time periods (in days)
    public static final int DEFAULT_SHORT_TERM_DAYS = 14;      // 2 weeks
    public static final int DEFAULT_MEDIUM_TERM_DAYS = 30;     // 1 month
    public static final int DEFAULT_LONG_TERM_DAYS = 90;        // 3 months
    
    // Minimum change threshold (to avoid showing noise)
    public static final float MIN_CHANGE_THRESHOLD = 0.5f;      // 0.5 points
    
    // Snapshot frequency
    public static final int SNAPSHOT_INTERVAL_DAYS = 7;         // Weekly snapshots
    
    // Maximum snapshots to keep per player
    public static final int MAX_SNAPSHOTS_PER_PLAYER = 52;     // 1 year (52 weeks)
}
```

### 3.4 Change Calculation Algorithm

```java
/**
 * Calculate attribute change over a specified period
 * 
 * @param playerId Player ID
 * @param attributeName Attribute name (e.g., "Acceleration")
 * @param periodDays Number of days to look back
 * @param currentValue Current attribute value
 * @return AttributeChange object with change amount and trend
 */
public AttributeChange calculateAttributeChange(
    Long playerId, 
    String attributeName, 
    int periodDays, 
    Float currentValue
) {
    // 1. Find snapshot closest to (currentDate - periodDays)
    LocalDateTime targetDate = currentGameDate.minusDays(periodDays);
    PlayerAttributeSnapshot oldSnapshot = findSnapshot(playerId, targetDate);
    
    // 2. Get old value from snapshot
    Float oldValue = getAttributeValue(oldSnapshot, attributeName);
    
    // 3. Calculate change
    if (oldValue == null) {
        return new AttributeChange(0f, "N/A", "No historical data");
    }
    
    float change = currentValue - oldValue;
    
    // 4. Determine trend
    String trend;
    if (Math.abs(change) < MIN_CHANGE_THRESHOLD) {
        trend = "STABLE";
    } else if (change > 0) {
        trend = "IMPROVING";
    } else {
        trend = "DECLINING";
    }
    
    return new AttributeChange(change, trend, formatChange(change, periodDays));
}
```

### 3.5 Display Format

**Change Indicators:**
- **↑↑** (Double up arrow): Significant improvement (>2.0 points)
- **↑** (Single up arrow): Improvement (0.5-2.0 points)
- **→** (Right arrow): Stable (change < 0.5 points)
- **↓** (Single down arrow): Decline (0.5-2.0 points)
- **↓↓** (Double down arrow): Significant decline (>2.0 points)

**Color Coding:**
- 🟢 Green: Improvement
- 🟡 Yellow: Stable
- 🔴 Red: Decline

**Text Format:**
- `[+2.3 ↑↑]` - Improved by 2.3 points over period
- `[-1.1 ↓]` - Declined by 1.1 points over period
- `[= →]` - No significant change

---

## 4. IMPLEMENTATION PLAN

### Phase 1: Core Infrastructure (v1.0)
1. Create `AttributeTrackingConstants` class
2. Create `PlayerAttributeSnapshot` data model
3. Add snapshot storage to `SaveGame`
4. Implement weekly snapshot creation in `FuttoboruGameEngine`

### Phase 2: Change Calculation (v1.0)
1. Create `AttributeChangeCalculator` utility class
2. Implement change calculation algorithm
3. Add period selection (short/medium/long-term)
4. Integrate with `PlayerDetailScreenTable`

### Phase 3: UI Enhancement (v1.0)
1. Add period selector to Player Detail Screen
2. Update change indicators with new format
3. Add tooltips showing exact dates and values
4. Add trend visualization (optional)

### Phase 4: Optimization (v2.0)
1. Implement snapshot cleanup (remove old snapshots)
2. Add daily snapshots for condition/fitness
3. Add caching for frequently accessed changes
4. Performance optimization for large player databases

---

## 5. CONFIGURATION & CUSTOMIZATION

### 5.1 User-Configurable Parameters

**Settings Screen Options:**
- **Attribute Change Period**: Dropdown with options:
  - Short-term (7, 14 days)
  - Medium-term (30, 60 days)
  - Long-term (90, 180 days)
  - Custom (user-defined days)

**Default:** Medium-term (30 days)

### 5.2 Per-Player Override (Future)
- Allow users to set different periods for different players
- Useful for tracking specific development goals

---

## 6. RECOMMENDATIONS

### 6.1 Default Configuration
- **Primary Display**: Medium-term (30 days) - balances recent changes with meaningful trends
- **Secondary Display**: Short-term (14 days) - for form assessment
- **Threshold**: 0.5 points minimum change to display

### 6.2 Implementation Priority
1. **HIGH**: Weekly snapshot system + 30-day change calculation
2. **MEDIUM**: Period selector UI + multiple period support
3. **LOW**: Long-term trends, advanced visualization

### 6.3 Storage Considerations
- **52 snapshots per player per year**: ~2KB per snapshot = ~100KB per player per year
- **10,000 players**: ~1GB per year (acceptable)
- **Cleanup Strategy**: Keep last 365 days, remove older snapshots

---

## 7. TESTING STRATEGY

### 7.1 Unit Tests
- Snapshot creation and storage
- Change calculation with various periods
- Edge cases (no historical data, missing snapshots)

### 7.2 Integration Tests
- Weekly snapshot creation in game engine
- Change calculation in player detail screen
- Period selector functionality

### 7.3 User Testing
- Verify 30-day period shows meaningful changes
- Verify 14-day period shows recent form
- Verify change indicators are clear and useful

---

## 8. FUTURE ENHANCEMENTS (v2.0+)

1. **Graph Visualization**: Show attribute trends as line graphs
2. **Predictive Analytics**: Predict future attribute changes based on trends
3. **Comparison Mode**: Compare attribute changes across multiple players
4. **Training Correlation**: Show which training programs caused which changes
5. **Match Performance Correlation**: Link attribute changes to match performance

---

## CONCLUSION

**Recommended Approach:**
- Implement **weekly snapshot system** for efficient storage
- Use **30-day period** as default for meaningful trend visualization
- Allow **configurable periods** (14, 30, 60, 90 days) for user preference
- Display changes with **clear indicators** (arrows, colors, text)

**Expected Outcome:**
- Players see meaningful attribute trends over appropriate time periods
- Better decision-making for training, transfers, and team selection
- More engaging gameplay with visible player development

**Next Steps:**
1. Review and approve this analysis
2. Implement Phase 1 (Core Infrastructure)
3. Test with 30-day period
4. Iterate based on user feedback

