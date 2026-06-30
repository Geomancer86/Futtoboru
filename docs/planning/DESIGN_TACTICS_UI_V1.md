# Design — Tactics & XI Selection UI (Layer B4 + MVP-1 UI)

**Document type:** Engineering design  
**Date:** 2026-06-30  
**Status:** **Approved for implementation**  
**MVP IDs:** MVP-1 (UI), MVP-2, MVP-9  
**Parent:** [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) · [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)

---

## 1. Problem

- Data model exists (`ClubTacticsSetup`, `FormationRegistry`) and sim consumes it.
- **Squad** only has **Best XI** button — no manual slot assignment.
- **Tactics** screen is a placeholder; **not in manager sidebar**.
- CM1 loop requires **Tactics menu + pick 11**.

---

## 2. Tactics screen layout (MVP-2, MVP-9)

Replace `TacticsScreenTable` placeholder.

### 2.1 Wire sidebar

In `MainMenuManager.setDynamicButtonsMenu()` for profession id **3 (manager)**:

```
Home → Inbox → Squad → Schedule → Tactics → … (existing buttons)
```

Add `TacticsButton` → `TACTICS_SCREEN` constant (new screen id if missing).

### 2.2 Screen sections

```
┌─────────────────────────────────────────────────┐
│ TACTICS — {Club Name}                           │
│ Formation: [ dropdown: 2-3-5-1888 ▼ ]           │
│ [ Best XI ]  [ Clear XI ]                       │
├─────────────────────────────────────────────────┤
│ Slot      │ Role │ Player                        │
│ gk        │ GK   │ [ dropdown: players ▼ ]       │
│ lhb       │ DEF  │ [ ... ]                       │
│ … (11 rows from FormationTemplate.slots)        │
├─────────────────────────────────────────────────┤
│ Team instructions (future — read-only stub)     │
└─────────────────────────────────────────────────┘
```

### 2.3 Formation dropdown (MVP-2)

| id | Display name |
|----|--------------|
| `2-3-5-1888` | Pyramid 2-3-5 (default) |
| `2-2-6-1888` | Double centre-half |
| `3-2-5-1888` | Three half-back |

On change:

1. Update `ClubTacticsSetup.formationTemplateId`
2. Re-render slot rows from new template
3. Drop slot assignments that no longer map (or keep by player id if still valid)

**Test:** `FormationSimWeightTest` — same XI, two templates → different `LineupStrengthCalculator` output.

### 2.4 Player pick per slot (MVP-1 UI)

- Each row: `VisSelectBox<Player>` or compact dropdown filtered to squad players.
- On select: `setup.assignPlayer(slotId, playerId)`
- Validation: same player cannot occupy two slots (clear previous slot or show error)
- Persist on `Club.setTacticsSetup(setup)` — autosave on change or on screen exit

**Best XI:** calls existing `BestElevenSelector.selectBestEleven(club)` and refreshes UI.

**Clear XI:** `setup.clearSlotAssignments()`.

### 2.5 Squad screen enhancement

In `PlayersListTable` or squad header:

- Column or badge **XI** for players in `ClubTacticsSetup.orderedPlayerIds()`
- After Best XI, refresh list

Optional for wrap-up: clicking player toggles inclusion (advanced) — **slot UI on Tactics is required**; squad toggle is optional.

---

## 3. Interaction with preview (MVP-7)

| Screen | Responsibility |
|--------|----------------|
| **Tactics** | Primary place to set formation + XI |
| **Preview** | Read-only opponent XI; **quick edit** user XI optional (link “Edit on Tactics” or inline same slot widgets) |

Minimum for B-9: edits on Tactics persist; preview shows current setup; kick off uses it.

---

## 4. i18n keys (add to language mod)

| Key | English |
|-----|---------|
| `main_tactics` | Tactics |
| `formation_label` | Formation |
| `best_xi` | Best XI |
| `clear_xi` | Clear XI |
| `slot_player` | Player |
| `in_starting_xi` | XI |

---

## 5. Files to touch

| File | Change |
|------|--------|
| `TacticsScreenTable.java` | Full implementation |
| `menu/buttons/TacticsButton.java` | New (mirror `SquadButton`) |
| `MainMenuManager.java` | Sidebar + screen routing |
| `FormationRegistry.java` | Ensure 3 templates registered |
| `SquadScreenTable.java` | XI indicator / link to Tactics |
| `FormationSimWeightTest.java` | New unit test |

---

## 6. Acceptance

- [ ] Tactics visible in manager sidebar  
- [ ] Formation dropdown switches template  
- [ ] 11 slots assignable; persists after save/load  
- [ ] Best XI fills slots  
- [ ] Sim uses assigned players (`StartingElevenIT` still green)  
- [ ] Different formation changes sim outcome (`FormationSimWeightTest`)

---

## 7. Out of scope

- Drag-drop pitch view
- Mentality / width affecting sim (schema only on `TeamInstructions`)
- Opposition instructions
- In-match subs

---

*Implement after B3 match-day flow or in parallel if different owners.*
