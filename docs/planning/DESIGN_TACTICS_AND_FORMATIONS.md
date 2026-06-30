# Design — Tactics, Formations & Extensible Match Setup

**Document type:** Product / architecture design  
**Date:** 2026-06-27  
**Status:** **Active** — Layer B MVP (MVP-1, MVP-2)  
**Audience:** PO, Match/Sim, Platform/UX  
**Companion:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) · [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) · [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) (B4 + manual XI UI)

---

## 1. PO direction (locked 2026-06-27)

| Topic | Decision |
|-------|----------|
| **MVP scope** | Basic only: pick **XI (11)** + choose from a **small set of historical formations** |
| **Future-proofing** | Data model and loaders must support **any tactic type later** (mentality, width, roles, set pieces) without save-breaking refactors |
| **Pitch view** | Not required for MVP — table/list UI is enough |
| **Sim integration** | Tactics affect **match sim v2** via formation weights + XI selection |

---

## 2. What “basic” means now (Layer B)

| Capability | MVP | Later versions |
|------------|-----|----------------|
| Pick 11 players | ✅ | Same |
| Formation template (e.g. 2-3-5, 2-2-6) | ✅ 2–3 era-appropriate templates | Any formation string / custom |
| Slot ↔ player assignment | ✅ Fixed slots per template | Drag-drop pitch, multi-position |
| Team instructions (mentality, tempo) | 🔲 Schema only, ignored by sim | CM3+ depth |
| Player roles (inside forward, wing half) | 🔲 Optional tags on slots | FM-style roles |
| In-match changes | ❌ | Subs, tactical tweaks |
| Opposition instructions | ❌ | Pressing, marking |

**Ship:** working basics on the extensible schema below — not a throwaway `formationId` string.

---

## 3. Extensible architecture

### 3.1 Concept layers

```
FormationTemplate (static registry / mod file)
    → defines slots (role, default weights for sim)
ClubTacticsSetup (per club, in SaveGame)
    → formationId + slotAssignments + teamInstructions (extensible)
MatchLineup (snapshot for one fixture)
    → copy of setup at kickoff; used by MatchSimulator
```

**Rule:** `MatchSimulator` reads **`MatchLineup` only** — never hardcodes formation logic.

### 3.2 Formation template (registry — grow via mods)

```java
FormationTemplate {
  String id;                    // "2-3-5-1888"
  String displayName;
  Integer eraFromYear;          // optional filter
  Integer eraToYear;
  List<FormationSlot> slots;    // exactly 11
}

FormationSlot {
  String slotId;                // "gk", "lhb", "rhb", "lwf", ...
  SlotRole role;                // GK, DEF, MID, FWD (enum — extend later)
  Map<String, Double> simWeights; // attribute keys → weight for this slot
}
```

**MVP templates (1888 England):**

| id | Name | Shape |
|----|------|-------|
| `2-3-5-1888` | Pyramid 2-3-5 | Default historical |
| `2-2-6-1888` | Double centre-half variant | Optional |
| `3-2-5-1888` | Three half-back variant | Optional |

Templates live initially in **`FormationRegistry.java`** or `mods/formations/1888.txt` — loader interface is the same for both.

### 3.3 Club tactics setup (SaveGame — mutable)

```java
ClubTacticsSetup {
  String formationTemplateId;              // default "2-3-5-1888"
  Map<String, Long> slotAssignments;       // slotId → playerId (person or player id)
  TeamInstructions teamInstructions;       // extensible blob
}

TeamInstructions {
  // MVP: empty or defaults only
  String mentality;          // future: "attacking", "balanced", "defensive"
  String width;              // future: "narrow", "normal", "wide"
  Map<String, Object> extras; // forward-compatible key-value for new systems
}
```

Persist on **`Club`** (or nested object) inside `SaveGame`. Formal **`saveVersion`** migration deferred (**MVP-10**) — see [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md). Layer B adds fields via existing LibGDX JSON first.

### 3.4 Match lineup (per match snapshot)

```java
MatchLineup {
  Long matchId;
  Long clubId;
  String formationTemplateId;
  List<Long> playerIdsInSlotOrder;   // 11 ids, aligned to template slots
  TeamInstructions instructionsSnapshot;
}
```

Built when match is simulated (or when player confirms preview). AI clubs: auto-fill best XI into default formation.

### 3.5 Sim hook (MatchSimulator v2)

```
lineupStrength = sum over slots ( playerAttr(player, slot.simWeights) )
formationModifier = template balance (DEF/MID/FWD counts vs opponent)
→ feed into MVP-3 match sim
```

Future: `teamInstructions.mentality` adjusts goal rate ±5%; player roles adjust scorer weights — **same pipeline**, new fields.

---

## 4. UI (MVP)

| Screen | Behaviour |
|--------|-----------|
| **Tactics** (sidebar) | Formation dropdown; 11 slot rows (slot label + player select); “Best XI” button |
| **Squad** | Link to tactics; show formation name |
| **Match preview** | Read-only opponent XI; editable own XI before confirm |

Wire `TacticsScreenTable` → `MainMenuManager` (MVP-9). Follow [FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md](FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md).

**Validation:**

- Exactly 11 distinct players  
- Each assigned player in club squad  
- GK slot must be goalkeeper-capable (future profession check; MVP: first slot manual)

---

## 5. Future tactic types (reserved — do not implement in MVP)

| System | Fits in schema | CM/FM era |
|--------|----------------|-----------|
| Mentality / tempo | `TeamInstructions` | CM1+ |
| Offside trap / pressing | `TeamInstructions.extras` | CM93+ |
| Player roles | `FormationSlot.roleVariant` | FM |
| Set-piece takers | `ClubTacticsSetup.setPieces` | CM3+ |
| Opposition instructions | `MatchLineup` pre-match | CM2+ |
| In-match subs | `Match` events | CM1 interrupt |

Adding these = **new fields + sim weights** — not new save format.

---

## 6. Engineering tasks

| ID | Task | Acceptance |
|----|------|------------|
| **TAC-1** | `FormationTemplate` + `FormationRegistry` with 2–3 1888 templates | ✅ `FormationRegistryTest` |
| **TAC-2** | `ClubTacticsSetup` on `Club` + save/load | ✅ `ClubTacticsSetupSaveLoadIT` |
| **TAC-3** | `MatchLineupBuilder` — auto XI + manual override | ✅ `StartingElevenIT` |
| **TAC-4** | `TacticsScreenTable` wired to registry | 🔲 B4 — partial: Squad Best XI |
| **TAC-5** | `MatchSimulator` uses lineup not full squad | ✅ `MatchSimulatorV2Test` |
| **TAC-6** | Stub `mods/formations/` loader interface | Empty dir OK; CI schema test |

---

## 7. Cross-references

| Doc | Role |
|-----|------|
| [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) | MVP-1, MVP-2, phases |
| [ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md](ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md) | Crowds (separate system) |
| [ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md](ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md) | Gap analysis + B1–B5 test map |
| [CM1_REFERENCE.md](CM1_REFERENCE.md) | CM1 team selection benchmark |

---

*Next update: after TAC-1 implementation or PO adds formation templates.*
