# Research — Football Injury & Medical Care by Historical Era

**Document type:** Primary/secondary research synthesis  
**Date:** 2026-06-25  
**Status:** **Active** — authoritative for historical injury accuracy (1888 start mod)  
**Audience:** PO, Match/Sim, World/Data, manual authors  

**Feeds:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](../planning/DESIGN_INJURY_HISTORICAL_ERA_GATING.md) · [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](../planning/ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9  

**Related:** [ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](../planning/ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md) (wage scale) · [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md) (1993 gameplay reference)

---

## 1. Research question

Futtoboru’s default career begins in **1888–89** (Football League inaugural season). B5.2 shipped **CM93 (1993) injury management** — Lilleshall, weekly insurance premiums, “Physio” staff, surgeon abroad — with mod costs labelled “1888 money” but **CM93 magnitudes**. What did football injury care actually look like across history, and how should we partition time so the sim stays **period-accurate** without rewriting the CM93 engine every season?

---

## 2. Executive findings

| Finding | Implication for Futtoboru |
|---------|---------------------------|
| **Medical practice changed in institutional steps**, not yearly increments | Model **medical eras** (~8 bands), not per-year tables |
| **1888–89** had trainers and folk remedies; **qualified physio** and **national rehab centres** came decades later | Gate staff labels, treatment menu, and national rehab by era |
| **Player injury insurance** as a weekly premium product aligns with **late Edwardian / interwar** employer-liability culture, not 1888 | Gate Insure/Uninsure from **≥1906** (Workmen’s Compensation Act) |
| **Lilleshall** = FA National Sports Centre (**1951**); FA injury rehab centre branding **~1986** | Gate Lilleshall from **≥1951** (conservative) or **≥1986** (strict) |
| **No substitutes** until **1965–66** (England) | Match engine / tactics note — injured XI player = play with 10 (future milestone) |
| Victorian pitches, boots, and ball weight increased **severity and duration** vs modern estimates | Era **recovery multiplier** > 1.0 for ME-1 |
| Top 1888 wage ≈ **£2.50/week**; average pro **£0.50–1.00** | B5.2 rehab **£25–75** and Lilleshall **£300** are **50–600×** weekly wage — economically broken for 1888 |

**Bottom line:** CM93 parity and **1888 historical accuracy are orthogonal claims**. B5.2 satisfies the former; **M-B5.3-HIST** (era gating) is required for the latter.

---

## 3. Why medical eras, not per-year tables

| Approach | Pros | Cons | Verdict |
|----------|------|------|---------|
| **Per calendar year** | Maximum precision | 130+ rows of duplicate data; false precision (no source says “1893 ankle care differed from 1894”) | ❌ Reject |
| **Per decade** | Simple | Blurs 1906 Act, 1914 war, 1965 subs — real step changes fall mid-decade | ⚠️ Too coarse alone |
| **Medical eras** (institutional milestones) | Matches historiography; stable mod files; one row per gameplay band | Boundary years need PO call (e.g. Lilleshall 1951 vs 1986) | ✅ **Recommended** |
| **Single “1888 mode” toggle** | Fast PO demo | Breaks 60-season save when reaching 1950s | ❌ Insufficient alone |

Eras are defined by **when a treatment, role, or legal framework became plausible in English league football**, not by CM release dates. CM/FM columns in §6 map **game design history** onto the same era grid for traceability.

---

## 4. Medical era timeline (ME-0 → ME-7)

| Era ID | Years (inclusive start – end) | Label | Football injury context |
|--------|-------------------------------|-------|-------------------------|
| **ME-0** | pre-1885 | Amateur & pre-League | No legal professionalism; “trainer” often volunteer; no club doctor norm |
| **ME-1** | 1885–1905 | **Early League / Victorian pro** | **1888–89 start lives here.** Trainers (often ex-athletes); embrocation, massage, rest; rare honorary doctors; play hurt / 10 men; fatalities reported in press |
| **ME-2** | 1906–1914 | Compensation & club doctor | **Workmen’s Compensation Act 1906** — employer liability; big clubs employ or retain **club doctor**; still no insurance menu as CM93 models it |
| **ME-3** | 1919–1939 | Interwar trainer tradition | Trainer + doctor at wealthy clubs; **“magic sponge”** cultural trope grows; no national rehab centre |
| **ME-4** | 1945–1970 | Post-war NSC & early physio profession | **Lilleshall NSC 1951**; physiotherapy professionalises; still **no subs** until 1965 |
| **ME-5** | 1971–1992 | Sports medicine institutional | Subs routine; fitness science; **FA Lilleshall rehab centre ~1986**; richer treatment options |
| **ME-6** | 1993–1999 | **CM93 pop layer** | Insurance + Lilleshall + physio estimate as shipped in CM93 — Futtoboru B5.2 clone target |
| **ME-7** | 2000+ | Modern FM medical depth | Body areas, scans, medical centre, sickness — out of Layer B per PO direction |

**Boundary notes (PO decisions captured in design doc):**

- **ME-1 → ME-2:** 1906 Act — use **1906-07 season** as first season with compensation framing (not necessarily CM93 Insure menu yet).
- **Lilleshall gate:** ME-4 opens **1951** (NSC exists); ME-5 **1986** if we require FA injury-rehab branding parity with CM93 flavour text.
- **“Physio” vs “Trainer”:** Rename staff and estimate strings in ME-1–ME-3; underlying multiplier can stay engine-identical until research warrants different curves.

---

## 5. Era ME-1 deep profile — 1888–89 (game start)

This is the **default Futtoboru start mod** medical reality. Sources are weighted P1 (contemporary/rule) → P3 (retrospective).

### 5.1 Who treated injured players?

| Role | 1888–89 reality | In game today (B5.2) | Gap |
|------|-----------------|----------------------|-----|
| **Trainer** | Common at League clubs — massage, liniment, strapping, cold water | Labelled **Physio** (profession id 9) | Anachronistic naming |
| **Club doctor** | Rare; **Everton** retained Dr **James Baxter** from **1889** (honorary) | No separate doctor role | Optional future staff type |
| **Hospital / specialist** | General hospitals; orthopaedic surgery primitive | **Surgeon abroad** menu | Anachronistic — defer to ME-5+ |
| **National rehab** | Does not exist | **Lilleshall** | Impossible in 1888 |

**Trainer practice (secondary but consistent):** Carter (*Medicine, Sport and the Body*, 2012); McPherson on embrocation culture (*Playing Pasts*); contemporary accounts describe rubbing, rest, and playing through pain — not structured rehab weeks.

### 5.2 Match and squad constraints

| Rule / norm | Date | Game impact |
|-------------|------|-------------|
| **No substitutes** | Until **1965–66** (Football League) | Injured player during match → team continues with 10; CM/FM later auto-replace |
| **Heavy ball / boots / pitches** | 1880s–1920s | Higher trauma risk; longer layoffs — justify ME-1 recovery multiplier |
| **Fatalities** | Documented early League seasons | Historical fact; **gameplay fatality** is PO call (likely narrative only / out of scope) |

### 5.3 Economics vs B5.2 costs

From [ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](../planning/ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md):

| Economic fact | Typical value |
|---------------|---------------|
| Average pro wage | £0.50–1.00 / week |
| Top wage (e.g. Nick Ross, Everton 1888) | £2.50 / week |
| Club doctor visit (inferred from general medical fees) | **Shillings**, not pounds — order **£0.10–0.50** |

| B5.2 mod default | Cost | Weeks of avg wage (£0.75) |
|------------------|------|---------------------------|
| Rehabilitate MINOR | £25 | **~33 weeks** |
| Rehabilitate MODERATE | £75 | **~100 weeks** |
| Lilleshall | £300 | **~400 weeks** |
| Surgeon abroad | £500 | **~667 weeks** |
| Insurance premium | 5% wage | Mechanism OK; **concept** wrong pre-1906 |

**Conclusion:** File comments say “1888 money” but values are **CM93-scale pounds**. Era gating must include **cost tables per era**, not labels alone.

### 5.4 Treatment actions — historical analogues

| CM93 / B5.2 action | ME-1 analogue | Available? |
|--------------------|---------------|------------|
| Rest (implicit) | Home rest; trainer visit | ✅ Always |
| **Rehabilitate** (paid) | Trainer rub + embrocation; occasional doctor call-out | ✅ **Rename + rescale cost** |
| **Insure / Uninsure** | Club may continue paying injured pro informally — **no premium product** | ❌ Gate off |
| **Lilleshall** | — | ❌ Gate off |
| **Surgeon abroad** | — | ❌ Gate off |
| Physio **estimate** | Trainer’s guess (“a fortnight”) | ✅ **Rename**; same mechanic OK |

---

## 6. Cross-era feature matrix (history × CM × FM × Futtoboru)

**Legend:** ✅ norm · ⚠️ rare/emerging · ❌ anachronistic · 🔮 FM-era only

| Capability | ME-1 1888 | ME-2 1906+ | ME-4 1951+ | ME-6 CM93 | Modern FM | Futtoboru B5.2 |
|------------|-----------|------------|------------|-----------|-----------|----------------|
| Trainer / rub & rest | ✅ | ✅ | ✅ | ✅ (as physio) | ✅ | ✅ (as physio) |
| Club doctor | ⚠️ | ✅ big clubs | ✅ | ⚠️ implied | 🔮 dept | ❌ |
| Hidden injury proneness | — | — | — | ✅ | ✅ | ✅ |
| Recovery week estimate | ⚠️ folk | ⚠️ | ✅ | ✅ | ✅ + % | ✅ |
| Paid expedited rehab | ⚠️ small fee | ⚠️ | ✅ | ✅ | ✅ | ✅ CM93 costs |
| Weekly injury insurance | ❌ | ⚠️ legal frame | ⚠️ | ✅ | ⚠️ | ✅ |
| National rehab (Lilleshall) | ❌ | ❌ | ⚠️ NSC | ✅ | 🔮 | ✅ |
| Surgeon abroad | ❌ | ❌ | ⚠️ | ✅ | ✅ | ✅ |
| In-match injury / sub | ❌ no subs | ❌ | ❌ until 1965 | ✅ commentary | ✅ | ❌ post-match only |
| Body-part / scan sim | ❌ | ❌ | ❌ | ❌ | 🔮 | ❌ |

---

## 7. Recovery time & severity — historical modifiers (design inputs)

Not primary-sourced to exact multipliers — **design hypotheses** for era mods (PO tune in playtest):

| Factor | ME-1 | ME-6 (CM93 baseline) | Rationale |
|--------|------|----------------------|-----------|
| Base weeks from injury roll | ×**1.20** | ×1.00 | Slower healing, misdiagnosis |
| Physio/trainer multiplier spread | Same formula, **lower ability ceiling** | B5.2 shipped | Few skilled trainers |
| Rehabilitate week reduction | −1 / −2 (keep) | −1 / −2 | Fewer effective treatments in ME-1 → optional **−0 / −1** |
| SERIOUS → career end risk | Higher (narrative) | CM93 insurance retirement | T3-6 deferred |

Injury **types** (bruise, strain, etc.) can stay the five CM93 flavour strings; ME-1 descriptions may swap vocabulary (“knock”, “strain”, “laid up”) via era string table without changing severity enum.

---

## 8. Source ledger

| ID | Claim | Source | Conf. |
|----|-------|--------|-------|
| R-HME-1 | Football League founded **1888** | FA / League histories | P1 |
| R-HME-2 | Professionalism **1885** | FA legalisation | P1 |
| R-HME-3 | **No subs** until **1965–66** | Football League rules | P1 |
| R-HME-4 | Everton **Dr Baxter 1889** | Club history / Baxter obituaries | P2 |
| R-HME-5 | **Workmen’s Compensation Act 1906** | UK legislation | P1 |
| R-HME-6 | Lilleshall **NSC opened 1951** | English Heritage / FA histories | P2 |
| R-HME-7 | FA rehab / Lilleshall injury centre **~1986** | FA press / Wikipedia cross-check | P2 |
| R-HME-8 | Trainer tradition — massage, embrocation | Carter 2012; McPherson | P2 |
| R-HME-9 | Early League fatalities in press | Contemporary newspapers (aggregated in football history lit.) | P2 |
| R-HME-10 | 1888 wages £0.50–2.50/week | [ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](../planning/ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md) | P2 |
| R-HME-11 | CM93 insurance / Lilleshall / physio | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md) | P1 manual |

**Not yet in repo (future scan):** Victorian sports medicine monographs (e.g. Vamplew); British Medical Journal letters on football injuries 1890s.

---

## 9. Relationship to `HistoricalMode`

[PM_REPORT_HISTORICAL_MODE_PO_LOCK.md](../planning/PM_REPORT_HISTORICAL_MODE_PO_LOCK.md) locks **Sandbox vs Historical First** for **fork votes and news latency** — not medical technology. Medical era resolution should use **`gameDate` + start mod config**, independent of fork mode. A sandbox fork in 1910 still advances medical era to ME-2 when calendar ≥ 1906.

---

## 10. Changelog

| Date | Change |
|------|--------|
| 2026-06-25 | Initial medical-era research — ME-0→ME-7, 1888 profile, cost mismatch analysis |

---

*Update when new primary sources change era boundaries or ME-1 cost hypotheses.*
