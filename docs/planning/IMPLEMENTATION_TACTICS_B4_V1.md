# Implementation — Tactics & XI B4 (as-built)

**Document type:** Implementation record  
**Date:** 2026-06-30  
**Design:** [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md), [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md)  
**Manual:** [docs/manual/README.md](../manual/README.md) §4 (draft)

---

## Shipped behaviour

| Item | Detail |
|------|--------|
| Formations | 3 templates: `2-3-5-1888`, `2-2-6-1888`, `3-2-5-1888` |
| Manual XI | Slot picker per formation position |
| Best XI | Auto-fill by slot aptitude + availability |
| Persistence | `ClubTacticsSetup` on club in save |
| Sim | `MatchLineupBuilder` → `LineupStrengthCalculator` |
| B5.1 | Injured excluded from picker; low-fitness warning on slot |

**Not shipped:** team instructions, push/pull, captain, in-match changes.

---

## Code

| Component | Path |
|-----------|------|
| Data | `data/tactics/ClubTacticsSetup.java`, `MatchLineup.java` |
| Registry | `engine/tactics/FormationRegistry.java` |
| Selector | `engine/tactics/BestElevenSelector.java` |
| Strength | `engine/tactics/LineupStrengthCalculator.java` |
| UI | `tables/main/TacticsScreenTable.java` |
| Sidebar | `menu/buttons/TacticsButton.java` |

---

## Tests

`BestElevenSelectorTest`, `FormationSimWeightTest`, `Cm1ManagementLoopIT`, `InjuredPlayerLineupIT`, `MatchLineupPreviewIT`

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial as-built record; B5.1 tactics guards |
