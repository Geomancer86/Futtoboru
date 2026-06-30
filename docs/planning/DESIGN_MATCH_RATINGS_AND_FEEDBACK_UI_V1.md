# Design — Match Ratings & Feedback UI (Layer B3)

**Document type:** Engineering design  
**Date:** 2026-06-30  
**Status:** **Approved for implementation** — fills gap identified in [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)  
**MVP IDs:** MVP-5, MVP-6, MVP-7  
**Audience:** Match/Sim, Platform/UX, QA

---

## 1. Problem

- `MatchSimulator` produces scores, scorers, lineups, attendance — **no ratings**.
- `MatchResultScreenTable` shows score + attendance — **no scorers, no ratings**.
- `MatchPreviewScreenTable` shows fixture title only — **no XI edit, no kick-off**.
- Continue **skips preview** and sims immediately via `FuttoboruGameEngine.getMatchResult()`.

---

## 2. Match-day flow (MVP-7)

### 2.1 Target sequence

```
Continue (match day, user club has playable fixture)
    → MainMenuManager.MATCH_PREVIEW_SCREEN
    → User edits XI (optional) on preview OR defers to ClubTacticsSetup
    → "Kick off" button
    → MatchSimulator.simulateMatch(match)  // uses confirmed setup
    → MainMenuManager.MATCH_RESULT_SCREEN
    → User returns to Continue / Home
```

Non-user matches on same date: still auto-sim in `simulateMatchesForDate()` **before or after** user match per existing engine rules — do not block Layer B on full league sim order.

### 2.2 Engine changes

| Location | Change |
|----------|--------|
| `FuttoboruGameEngine.continueGame()` or caller | If user club has playable match **and** preview not yet confirmed for that match id → **navigate preview**, do not call `getMatchResult()` |
| `MatchPreviewScreenTable` | Hold `pendingMatch`; "Kick off" calls new `FuttoboruGameEngine.kickOffMatch(Match)` |
| `FuttoboruGameEngine.kickOffMatch` | Build lineups from `ClubTacticsSetup` → sim → ratings → navigate result |
| `Match` | Optional `Boolean previewConfirmed` or track last previewed match id on session (non-persisted OK for MVP) |

**Rule:** Lineup at kickoff = `MatchLineupBuilder.buildForClub(club, match)` after user edits `ClubTacticsSetup`.

### 2.3 Preview screen content (MVP)

| Section | Content |
|---------|---------|
| Header | Home vs away, date, venue, competition |
| User club XI | 11 rows: slot label, player name (editable → Tactics or inline dropdown) |
| Opponent XI | Auto Best XI (read-only) |
| Formation | Read formation id for both clubs |
| Form / table | Optional one-line stub (“League position: 3rd vs 8th”) — not blocking |
| Actions | **Kick off** (primary), **Back** |

Full FM preview deferred.

### 2.4 Result screen content (MVP-6)

Extend `MatchResultScreenTable`:

| Section | Data source |
|---------|-------------|
| Score block | Existing |
| **Scorers** | `Match.getHomeScorerIds()` / `getAwayScorerIds()` → resolve player names |
| Attendance | Existing |
| **Ratings (user club XI only)** | `Match.getHomePlayerRatings()` or away if user is away |
| Rating row | Player name, rating 0–10 one decimal |

History click (A-4): same layout when `setDisplayMatch(match)`.

---

## 3. Ratings model (MVP-5)

### 3.1 `MatchRatingsCalculator`

```java
public final class MatchRatingsCalculator {
  public static Map<Long, Float> rateLineup(
      Match match,
      MatchLineup lineup,
      Club club,
      boolean won,
      boolean drew);
}
```

### 3.2 Formula (locked — CM1 genre hook)

| Adjustment | Value |
|------------|-------|
| Base (started in XI) | 6.0 |
| Goal scored | +0.5 per goal |
| Win / draw / loss | +0.3 / 0.0 / −0.3 |
| Strength vs opponent lineup (optional light) | ±0.2 max |
| Clamp | 4.0 – 9.5 |

Non-XI players: no rating.

### 3.3 Persistence on `Match`

```java
// Add to Match.java (Serializable)
private Map<Long, Float> homePlayerRatings = new LinkedHashMap<>();
private Map<Long, Float> awayPlayerRatings = new LinkedHashMap<>();
```

LibGDX JSON serializes `Map<Long, Float>`. Extend `SaveLoadIT` or add `MatchRatingsSaveLoadIT` if needed.

### 3.4 Integration point

At end of `MatchSimulator.simulateMatch`, after scorers:

```java
match.setHomePlayerRatings(MatchRatingsCalculator.rateLineup(...));
match.setAwayPlayerRatings(...);
```

### 3.5 Tests

| Test | Assert |
|------|--------|
| `MatchRatingsTest` | Scorer rating > non-scorer; win bonus; clamp |
| `MatchFeedbackIT` | After sim, user club has 11 ratings |
| `MatchLineupPreviewIT` | Edit setup → kick off → lineup ids on match == setup |

---

## 4. Harness API (B3)

Add to `GameTestHarness`:

```java
Map<Long, Float> getLastMatchRatings(long clubId);
void kickOffNextMatchForCurrentClub();
void setClubTacticsSetup(long clubId, String formationId, List<Long> playerIds); // if not present
```

---

## 5. Out of scope (B3)

- Season-average rating columns on squad screen (nice-to-have)
- Opponent ratings visible before match
- xG, heat maps, commentary text
- Ratings affecting morale (B5 COND-5)

---

## 6. Cross-references

- Formula source: [DESIGN_LAYER_B_CM1_CORE_LOOP.md §5.4](DESIGN_LAYER_B_CM1_CORE_LOOP.md)
- PM plan: [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)
- Tests: [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)

---

*Implement B3a (calculator + Match fields) before preview UI wiring.*
