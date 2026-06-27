# PO Decision Brief — 1892 League Membership & Division Two

**Document type:** PO decision support (plain language)  
**Date:** 2026-06-27  
**Audience:** PO, PM  
**Context:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md §6](PM_V1_MVP_AND_1892_MASTER_PLAN.md) · [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)

---

## 1. What you are deciding

When a player starts in **1888** and plays forward until **1892**, the Football League historically gained a **second division** and **new clubs** joined the league structure.

You are **not** deciding whether to add 1892 content (that is planned). You are deciding:

> **When Division Two appears in the save, who is in Division One vs Division Two — and does that follow real history or your save’s league table?**

---

## 2. Historical background (short)

### 1888–1892 (what the game does today)

- **One league**, **12 clubs** (the Football League founders).
- **No automatic relegation.** Each year, the **bottom four** had to apply for **re-election** at the league’s annual meeting. They usually stayed; sometimes a non-league club could be voted in instead.
- Accrington, Preston, etc. — the same 12 names every season unless you script a change.

### 1892–93 (the expansion)

- The league **split into two divisions**:
  - **Division One** — the top tier (still 12 clubs, mostly the established names).
  - **Division Two** — a **new second tier** with **about 12 additional clubs** (e.g. Small Heath, Sheffield United, Ardwick/Man City, Burton Swifts, etc.).
- These **new clubs did not exist in the 1888 league mod as members** — they must **enter the save** when their era arrives ([world evolution](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)).
- **Modern-style promotion/relegation** (automatic up/down every season) came **later** — not on day one of Division Two.

So 1892 is two changes at once:

1. **New clubs** appear in the world (content + scripts).  
2. **League structure** becomes two divisions instead of one.

---

## 3. The three membership models (options)

### Option A — **Historical script (recommended for authenticity)**

**What happens:** On the real-world date (1892), a timeline script:

- Creates **Division Two** as a new competition.
- Adds **~12 new clubs** (with squads) into the save.
- Places clubs into **Div 1 and Div 2 exactly as in the real 1892–93 season**.

**Your save’s story does not matter** for who goes where. If Accrington won your league ten times in a row, the 1892 split still matches history.

| Pros | Cons |
|------|------|
| Correct names, divisions, researchable | Immersion break if your table differed from history |
| Simplest to author and test | Less “your choices matter” |
| Matches “historical sim” product goal | |

**Best if:** Futtoboru is primarily **historical recreation** with procedural seasons on top.

---

### Option B — **Re-election through 1891, then historical 1892 split**

**What happens:**

- **1888–1891:** Each season end, bottom four face a **re-election event** (inbox + script; can be message-only at first).
- **1892:** Division Two still created by **historical script** (new clubs + real D1/D2 split).

Re-election might **occasionally** swap a club before 1892, but the **1892 two-division layout** still follows history unless PO adds a vote for who enters Div 2.

| Pros | Cons |
|------|------|
| Teaches early-league flavour | More systems work before 1892 |
| Authentic 1888–91 era | 1892 split may still need Option A script |
| Good narrative inbox content | PO must accept complexity |

**Best if:** You want **1888 authenticity** before expansion, without full modern relegation.

---

### Option C — **Save-driven tables (promotion / relegation from your results)**

**What happens:**

- New clubs still appear in 1892 (you need them in the database).
- **Who is in Div 1 vs Div 2** is taken from **your league table** at end of 1891–92 (e.g. bottom 12 drop to new Div 2, top 12 stay in Div 1 — exact split PO defines).
- Future seasons: automatic promotion/relegation between divisions.

| Pros | Cons |
|------|------|
| “Your career” drives structure | **Historically wrong** names in wrong divisions |
| Emergent stories | Harder to research/lint |
| FM-like | Conflicts with “start in real 1888 world” |

**Best if:** Futtoboru prioritises **sandbox career** over historical accuracy.

---

### Option D — **Hybrid (common compromise)**

**What happens:**

- **New clubs + Division Two creation:** always **historical script** (real teams enter the save).
- **Initial 1892–93 membership:** PO chooses sub-option:
  - **D1-hist:** Div 1/Div 2 lists = real life (like Option A).
  - **D1-save:** Top 12 from **your** 1891–92 table in Div 1; new clubs fill Div 2 (blend).
- **Promotion/relegation between divisions:** **off** until a later year PO picks (e.g. 1899), then scripted or automatic.

| Pros | Cons |
|------|------|
| Flexible PO dial | Two code paths to maintain |
| Real clubs always appear | D1-save still ahistorical |

---

## 4. Comparison table

| Question | A Historical | B Re-election | C Save-driven | D Hybrid |
|----------|--------------|---------------|---------------|----------|
| New clubs appear in 1892? | ✅ Script | ✅ Script | ✅ Script | ✅ Script |
| Div 1/Div 2 split matches real 1892? | ✅ | ✅ (usually) | ❌ | PO choice |
| Your league wins affect 1892 split? | ❌ | ⚠️ Rarely | ✅ | Optional |
| Engineering effort | Low | Medium | High | Medium |
| Research / lint burden | Low | Medium | High | Medium |

---

## 5. What we recommend PO answer (one line each)

| Question | Suggested PO answer |
|----------|---------------------|
| **New clubs in 1892** | Always yes — via world evolution scripts (not negotiable for 1892 pack). |
| **Div 1 vs Div 2 membership in 1892–93** | **Option A** (historical split) unless product is sandbox-first. |
| **Re-election 1888–91** | **Option B message-only** (inbox flavour) — defer simulated votes. |
| **Promotion/relegation after 1892** | **Defer** until post-1892 milestone; do not build FM-style loops in first 1892 ship. |

---

## 6. PO decision form (fill in)

| # | Decision | Your choice (A / B / C / D / D1-hist / D1-save) |
|---|----------|--------------------------------------------------|
| 1 | How is Division Two created in 1892? | *(default: historical script)* |
| 2 | Who is in Div 1 vs Div 2 for 1892–93 season? | |
| 3 | Re-election for bottom four 1888–1891? | None / Message only / Simulated |
| 4 | Automatic promotion/relegation from when? | Never / Year _____ / Defer |

---

## 7. After PO decides

Update [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) §8 row 4 with locked choice → unlock **1892-R2** research pack and **WE-4** script authoring.

---

*This brief exists so PO can decide without reading engine docs.*
