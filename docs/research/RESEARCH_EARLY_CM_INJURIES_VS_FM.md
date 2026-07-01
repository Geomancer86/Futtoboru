# Research Note — Early CM Injury Systems vs Modern FM

**Date:** 2026-06-30  
**Purpose:** Primary-source backup for [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](../planning/ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) §4.1  
**Status:** Closed — sufficient for Layer B injury scope decisions  

---

## Question

Do CM1/CM93 simulate body parts like modern Football Manager?

## Answer

**No.** Early Championship Manager used **text injury descriptions**, **recovery week estimates**, and **management options** (insurance, Lilleshall, surgeon abroad). There is **no evidence** in CM1 or CM93 manuals of per-body-part injury simulation. That depth belongs to **later CM/FM generations**, not the 1992–1994 benchmark Futtoboru targets for Layer B.

---

## CM1 (1992)

| Evidence | Detail |
|----------|--------|
| [CM1_Manual_Menu_Extract.md](../manuals/cm1/CM1_Manual_Menu_Extract.md) | Squad icon `[IJ]` = injured; post-match ratings screen shows IJ flag |
| Manual scope | No insurance chapter, no Lilleshall, no physio recovery estimate paragraph |
| Team aggregates | Defence/mid/attack totals; morale; **energy average** (CM_MANUALS_DEEP_ANALYSIS §CM1) |

**Depth label for CM1 injuries:** **Partial** — state exists; subsystem lighter than CM93.

---

## CM93/94 (1993)

| Evidence | Detail |
|----------|--------|
| [CM93_Manual_LemonAmiga_Extract.md](../manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md) §Squad (4) | “If injured a **description of the condition** and an **estimate** given by the **physio**, of the time to recover” |
| Same manual §Player Injuries | Lilleshall rehab; surgeon abroad for serious injuries |
| Same manual §Insurance | Wages during treatment; operation fees; Lilleshall fees; forced retirement compensation |
| Same manual §Attributes | **INJURY PRONE** — “Likelihood of player getting injured” |
| Same manual §Staff (ii) | Physio “helping players recover from injury” |
| Match commentary | “taken off injured” + `[IJ]` marker |

**Depth label for CM93 injuries:** **Core** — full management subsystem without anatomical simulation.

---

## Modern FM (context only — out of Layer B scope)

Later Football Manager titles added richer injury **types**, **body areas**, rehab timelines, and medical staff roles. Community guides describe FM medical centres as substantially deeper than CM93. Futtoboru must **not** confuse that horizon with the CM93 floor.

---

## Futtoboru implementation (2026-06-30)

| Build | Depth | Doc |
|-------|-------|-----|
| B5 | Minimal stub | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) |
| B5.1 | Partial — Tier 1 existence | Same §9 |
| **M-B5.2** (planned) | **CM93 parity target** | [PM_MILESTONE_CM93_INJURY_PARITY.md](../planning/PM_MILESTONE_CM93_INJURY_PARITY.md) |

**Full CM93 ledger:** [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md)

---

## External sources (secondary)

| Source | URL | Used for |
|--------|-----|----------|
| Lemon Amiga CM93 manual | https://www.lemonamiga.com/doc/championship-manager-93/315 | Primary manual cross-check |
| StrategyWiki CM93 Your team | https://strategywiki.org/wiki/Championship_Manager_93/Your_team | Physio ability → recovery |
| MobyGames CM93 | https://www.mobygames.com/game/1757/championship-manager-93/ | Reserve squads, injury time feature list |
| Good Old Days retrospective | https://www.goodolddays.net/en/game/Championship-Manager-93/ | Insurance → hospital gameplay note |

---

*No further web research required for Layer B injury scope unless CM1 primary manual pages on injury treatment are scanned.*
