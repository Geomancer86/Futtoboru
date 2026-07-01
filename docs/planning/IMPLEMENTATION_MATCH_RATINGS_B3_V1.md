# Implementation — Match Ratings B3 (as-built)

**Document type:** Implementation record  
**Date:** 2026-06-30  
**Design:** [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md)  
**Manual:** [docs/manual/README.md](../manual/README.md) §5 (draft)

---

## Shipped behaviour

| Item | Detail |
|------|--------|
| Scale | 4.0–9.5 float (display one decimal) |
| Inputs | Win/loss/draw + goals scored per player |
| Scope | Starting XI only |
| Storage | `Match.homePlayerRatings` / `awayPlayerRatings` |
| UI | User club XI on `MatchResultScreenTable` |

**Formula (per XI player):** base 6.0 + win +0.3 / loss −0.3 + goals × 0.5, clamped.

---

## Code

| Component | Path |
|-----------|------|
| Calculator | `engine/simulation/MatchRatingsCalculator.java` |
| Sim hook | `MatchSimulator.java` — rates after score |
| Morale hook | `PlayerConditionService` — rating thresholds ±1 |
| Result UI | `tables/match/result/MatchResultScreenTable.java` |

---

## Tests

`MatchRatingsTest`, `MatchFeedbackIT`, `MatchRatingsSaveLoadIT`, `MoraleUpdateTest`

---

## Gaps vs CM1

- No opponent-strength adjustment  
- Away/user ratings for both teams not shown  
- Not driven by match events / commentary  

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial as-built record |
