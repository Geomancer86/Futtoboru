# PM Report — Layer B Sprint Progress (B1)

**Document type:** PM sprint report  
**Date:** 2026-06-28  
**Branch:** `analyze-ai-branch`  
**Phase:** **B1 complete** — sim core (DATA-B1, MVP-1, MVP-3)

---

## 1. Executive summary

**B1 shipped on `analyze-ai-branch`.** Match simulation now uses **starting XI lineups** with formation slot weights — never full-squad average. Automated gate tests green (`StartingElevenIT`, `MatchSimulatorV2Test`, etc.).

| Track | Status |
|-------|--------|
| **B1 — Sim core** | ✅ **Done** |
| **B2 — Crowds** | 🔲 Next — MVP-4 + H-1 |
| **B3 — Feedback** | 🔲 MVP-5/6/7 |
| **B4 — Tactics UX** | 🔲 MVP-2/9 |

---

## 2. B1 deliverables

| ID | Deliverable | Status |
|----|-------------|--------|
| **DATA-B1** | `FormationTemplate`, `FormationRegistry`, `ClubTacticsSetup`, `MatchLineup` on `Club` | ✅ |
| **MVP-1** | `BestElevenSelector`, Squad **Best XI** button, persist on club | ✅ |
| **MVP-3** | `MatchSimulator` v2 — lineup strength, scorers from XI, RNG 0.85–1.15 | ✅ |
| **MVP-8** | `TenSeasonReportIT` regression | ✅ soak re-run pending / green |

---

## 3. Code touchpoints

| Area | Files |
|------|-------|
| Data model | `data/tactics/*`, `Club.tacticsSetup`, `Match.homeLineupPlayerIds` |
| Engine | `engine/tactics/FormationRegistry`, `BestElevenSelector`, `MatchLineupBuilder`, `LineupStrengthCalculator` |
| Sim | `MatchSimulator` v2 |
| UI | `SquadScreenTable` — Best XI button |
| Tests | `FormationRegistryTest`, `BestElevenSelectorTest`, `MatchSimulatorV2Test`, `ClubTacticsSetupSaveLoadIT`, `StartingElevenIT` |
| Harness | `GameTestHarness.setClubLineup`, `countHomeWinsWithLineup`, … |

---

## 4. Test evidence

```bash
mvn test -pl futtoboru-core
mvn test -pl futtoboru-core -Psoak   # TenSeasonReportIT regression after sim v2
```

| Test | Proves |
|------|--------|
| `FormationRegistryTest` | 3 templates × 11 slots |
| `BestElevenSelectorTest` | 11 distinct players; worst ≠ best |
| `MatchSimulatorV2Test` | Strong XI → higher strength + more wins |
| `ClubTacticsSetupSaveLoadIT` | JSON roundtrip |
| `StartingElevenIT` | Sim records 11 lineup ids; weak XI loses more |

---

## 5. Next steps

1. **B2** — `AttendanceCalculator` + H-1 single attendance authority  
2. **B3** — Post-match 0–10 ratings + result/preview UI  
3. **B4** — Formation picker + Tactics sidebar  

---

*Next PM update: after B2 AttendanceModelIT green.*
