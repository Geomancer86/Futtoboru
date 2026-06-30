# PM Report — Layer B Sprint Progress (B2)

**Document type:** PM sprint report  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Phase:** **B2 complete** — crowds (MVP-4, H-1) + economy early-warning

---

## 1. Executive summary

**B2 shipped on `analyze-ai-branch`.** Home attendance is driven by an FM-style `AttendanceCalculator` (table position, opponent rank, form, derby). Ticket sales in `MatchScheduler` target that fill rate; `MatchSimulator` no longer overwrites pre-sold attendance (H-1). Long-run harness scans Football League club balances and fails early if any go negative (bankruptcy feature deferred).

| Track | Status |
|-------|--------|
| **B1 — Sim core** | ✅ Done |
| **B2 — Crowds + economy watch** | ✅ **Done** |
| **B3 — Feedback** | 🔲 Next — MVP-5/6/7 |
| **B4 — Tactics UX** | 🔲 MVP-2/9 |

---

## 2. B2 deliverables

| ID | Deliverable | Status |
|----|-------------|--------|
| **MVP-4** | `AttendanceCalculator`, `DerbyRegistry`, `mods/derbies.txt` | ✅ |
| **H-1** | Single attendance authority — sim keeps scheduler attendance | ✅ |
| **ECON-B2** | `EconomyHealthMonitor` — negative balance scan (FL roster + report) | ✅ |
| **Tests** | Unit + IT + long-run economy gate | ✅ |

---

## 3. Code touchpoints

| Area | Files |
|------|-------|
| Attendance | `engine/simulation/AttendanceCalculator.java`, `DerbyRegistry.java` |
| Mod data | `mods/derbies.txt` |
| Scheduling | `MatchScheduler` — target fill from calculator; daily ticket sales |
| Sim | `MatchSimulator.applyFinalAttendance()` — H-1 |
| Economy watch | `engine/economy/EconomyHealthMonitor.java` |
| Harness | `GameTestHarness.averageHomeAttendance`, `assertNoNegativePrimaryLeagueBalances`, `MultiSeasonSimulationReport.recordEconomyScan` |
| Tests | `AttendanceCalculatorTest`, `AttendanceModelIT`, `AttendanceOverwriteIT`, `EconomyHealthMonitorTest`, `LongRunEconomySoakIT` |

---

## 4. Test evidence

```bash
mvn test -pl futtoboru-core
mvn test -pl futtoboru-core -Psoak   # LongRunSoakIT (league + cup multi-season)
```

| Test | Proves |
|------|--------|
| `AttendanceCalculatorTest` | position/opponent/form/derby factors; clamp 0.08–0.98 |
| `AttendanceModelIT` | Top-table avg home attendance > bottom after full season |
| `AttendanceOverwriteIT` | Post-sim attendance equals pre-sold (not re-rolled) |
| `EconomyHealthMonitorTest` | Negative balance detection; league-scoped scan |
| `LongRunEconomySoakIT` | 3 seasons + champions; FL balances stay ≥ 0 |
| `MultiSeasonSoakIT` | 3 procedural champions + FL economy gate |
| `TenSeasonReportIT` | 10-season report + FL economy fields |

**Economy scope:** Non–Football League clubs (id 13+) may have negative balances (no league income). Gates assert **primary league roster only** plus player club solvency. Report also tracks global minimum for diagnostics.

---

## 5. Next steps

1. **B3** — Post-match 0–10 ratings + result/preview UI  
2. **B4** — Formation picker + Tactics sidebar  

---

*Next PM update: after B3 MatchFeedbackIT green.*
