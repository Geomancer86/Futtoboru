# Pre-game menu screens — fixes & redesign (excl. Settings)

**Feature id:** `pregame_menus_polish`
**Release:** R-1888
**Status:** draft — analysis + design complete, implementation not started; PO to decide dev order per screen

---

## Summary (PO reads this line only)

Fix the seven pre-game screens/dialogs between launch and Start Game (everything except Settings, which
is deferred) so every button does what it says, nothing silently fails, and no path allows a crash or
data loss — full findings and rationale in
[ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md](../../planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md).

---

## Problem

The 2026-07-19 audit ([ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md](../../planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md))
found that most pre-game screens were built quickly and never revisited: several buttons are stubs, several
failure paths are completely silent to the player, and at least two paths (garbage birth-date input,
duplicate manager names) can silently corrupt or lose data with no warning. This spec turns that audit's
findings into a concrete, screen-by-screen task list so implementation can be picked up in any order.

**Explicitly excluded from this spec, by PO decision 2026-07-19:** the Settings/Options screen
(`SettingsScreen` + `SoundSettingsTable` + `ModsSettingsTable`). That screen's fixes (mostly deleting dead
DarkBlade-game leftovers and wiring the language/sound prefs that already work) are tracked separately in
the same analysis doc §2, to be scheduled later.

---

## Scope

### In (7 units of work, independently orderable)

1. Main Menu (`MenuScreen`)
2. New Game Step 1 — Season & Manager (`NewGameSeasonScreen`)
3. New Game Step 2 — Choose Countries (`NewGameSetupScreen`)
4. New Game Step 3 — Overview & Start (`NewGameOverviewScreen`)
5. Create Manager (`NewManagerScreen`)
6. Load/Save Game (`LoadGameDialog` + `SaveGameDialog`)
7. In-game Options "Quit to Menu" / "Quit to Desktop" (`MainGameMenuTable`) — adjacent, bundled with #6
   since it shares the confirm-dialog work

### Out

- Settings/Options screen (deferred, see Problem above)
- Any brand-new gameplay feature not already flagged in the audit (transfers, difficulty levels, etc.)
- City/Nationality/Languages/Picture fields for Create Manager are listed as options *within* unit 5
  below but are genuinely new scope, not bug fixes — flagged separately so they can be deferred without
  blocking the P0/P1 fixes in the same screen

---

## Shared foundation (build once; whichever screen is implemented first builds it, others reuse it)

Two small reusable UI helpers are needed by multiple screens below. Building them ad hoc per-screen would
mean four/five bespoke one-off dialogs instead of one of each:

- **`ConfirmDialog`** — a small `VisDialog` wrapper: message + Yes/Cancel. Needed by: Main Menu Quit,
  Create Manager overwrite, Save Game overwrite, Load Game delete, in-game Quit to Menu/Desktop.
- **`ErrorDialog`** — a small `VisDialog` wrapper: message + OK. Needed by: New Game Start failure, Load
  Game failure on a corrupted save.

Whichever unit is scheduled first should add these two helpers (e.g. under `com.rndmodgames.futtoboru.dialogs`)
and the rest should reuse them rather than re-implementing.

---

## Per-screen acceptance criteria & tasks

### 1. Main Menu (`MenuScreen`)

**Acceptance criteria**
- [ ] A "Continue" button loads the most recently modified save directly, with no intermediate dialog
- [ ] "Continue" is hidden or disabled (not just present-but-broken) when no saves exist
- [ ] Quit shows a Yes/Cancel confirmation before calling `Gdx.app.exit()`
- [ ] The screen shows a game title/logo, not just a bare stack of buttons
- [ ] Social/Patreon link caption text is localized via `LanguageModLoader`, not hardcoded

**Tasks**
- [ ] Add `Continue` button + wiring to load most-recent save (reuse `SaveLoadSystem`/`LoadGameDialog`
      logic for "load by name" against the most-recent entry)
- [ ] Wrap `QuitGameButton`'s `Gdx.app.exit()` call in the shared `ConfirmDialog`
- [ ] Add a title/logo `VisLabel` or image above the button stack
- [ ] Localize `SocialNetworkLinksComponent` caption text
- [ ] Final button order: Continue, New Game, Load Game, Settings, Quit

**Priority:** P0 (Quit confirmation, Continue) / P2 (branding, localization)

---

### 2. New Game Step 1 — Season & Manager (`NewGameSeasonScreen`)

**Acceptance criteria**
- [ ] Start is disabled whenever `selectedSeason == null || selectedManager == null` (never reachable with
      a null manager)
- [ ] When zero saved managers exist, the screen clearly says so and Start stays disabled until one is
      created
- [ ] The saved-managers select box is sized correctly (no `allSeasons.size()` bug)
- [ ] Selecting a manager from the dropdown updates the summary without requiring a separate "Select"
      click (single, unambiguous interaction model)
- [ ] A one-line manager summary (name + birth year or nationality) is shown next to the dropdown, not
      just the raw filename
- [ ] A Delete-manager control exists per saved manager, behind the shared `ConfirmDialog`
- [ ] Season selection is preserved across a round trip through Create Manager and back
- [ ] `hide()`/`dispose()` actually disposes the stage
- [ ] If only one season exists, it is shown as a label, not a single-item dropdown
- [ ] The season source-URL caption reflects the real source name (or is removed), not a hardcoded
      `"Wikipedia"` string
- [ ] A "Step 1 of 3" indicator is visible

**Tasks**
- [ ] Add the null-guard + disabled state to the Start button
- [ ] Add "no managers yet" messaging
- [ ] Fix `savedManagers.toArray(new String[allSeasons.size()])` → correct size
- [ ] Consolidate dropdown + "Select Manager" button into one interaction (pick one, remove the other or
      merge behavior)
- [ ] Add manager summary label
- [ ] Add per-manager Delete control (uses shared `ConfirmDialog`)
- [ ] Preserve `selectedSeason` when routing to/from `NewManagerScreen`
- [ ] Implement `hide()`/`dispose()` properly
- [ ] Single-season case: replace dropdown with a label
- [ ] Fix/remove the hardcoded `"Wikipedia"` label
- [ ] Add step indicator (shared small component/label used by all 3 New Game screens)

**Priority:** P0 (null-manager Start, stage leak) / P1 (redundant control, hardcoded label, messaging)

---

### 3. New Game Step 2 — Choose Countries (`NewGameSetupScreen`)

**Acceptance criteria**
- [ ] Next is disabled (with a tooltip) whenever zero countries are selected
- [ ] The forced "always check every box on open" override is removed; default state is deliberate and
      documented, not fought against by live listener logic
- [ ] A live "N countries / M clubs selected" counter is visible
- [ ] Select All / Select None quick actions exist
- [ ] "Step 2 of 3" indicator is visible

**Tasks**
- [ ] Add validation + disabled state to Next
- [ ] Remove the hardcoded `countryCheckBox.setChecked(true)` override; decide and document the real
      default
- [ ] Add live selection counter (reuse the club-count math already in `NewGameOverviewScreen`, or a cheap
      local duplicate)
- [ ] Add Select All / Select None buttons
- [ ] Add step indicator

**Out of this unit (P2, genuinely new scope, defer):** country flags/icons per row

**Priority:** P0 (zero-country Next) / P1 (forced-default override, missing counter)

---

### 4. New Game Step 3 — Overview & Start (`NewGameOverviewScreen`)

**Acceptance criteria**
- [ ] Any failure during `Start Game`'s 14-step process shows the shared `ErrorDialog` to the player,
      instead of only logging to console — this is the single highest-value fix in the whole spec
- [ ] Cups/Leagues counts are filtered to the selected countries, matching how Teams/Players are already
      correctly filtered
- [ ] The player-count figure is visibly labeled as an estimate
- [ ] The starting-club placeholder text ("UNAVAILABLE") is localized
- [ ] The starting-country select box's "hacky" double-`setSelectedIndex` workaround is replaced with a
      direct call to `updateSelectableTeamsByLeagueAndCountry()`
- [ ] Start Game is proactively disabled while required selections (profession; club, if the profession
      requires one) are incomplete, not just reactively caught by the exception path
- [ ] A busy/spinner indicator is shown while Start Game's 14 steps are processing
- [ ] "Step 3 of 3" indicator is visible

**Tasks**
- [ ] Replace the silent `return` in the failure `catch` block with the shared `ErrorDialog`
- [ ] Filter the Cups/Leagues counting loop by `selectedCountries`
- [ ] Add "(estimated)" label/tooltip to the player-count figure
- [ ] Localize `"UNAVAILABLE"`
- [ ] Replace the double-`setSelectedIndex` trick with a direct update call
- [ ] Add proactive disabled-state validation on Start Game
- [ ] Add a busy indicator during the Start Game click handler
- [ ] Add step indicator

**Priority:** P0 (silent failure on Start) / P1 (misleading counts, fragile select-box trick) / P2
(estimate labeling, localization, busy indicator)

---

### 5. Create Manager (`NewManagerScreen`)

**Acceptance criteria**
- [ ] The Create Manager button is disabled until the birth-date field actually parses as a valid date
      (not just non-empty) — reject unparseable and implausible dates (e.g. future birth year)
- [ ] The previously-reachable "invalid date silently becomes null and manager still saves" path is
      eliminated
- [ ] Creating a manager whose last name matches an existing saved manager shows the shared
      `ConfirmDialog` ("already exists — overwrite?") before writing
- [ ] The created manager is attached to `currentGame`'s owner directly in memory, without depending on a
      save-to-file-then-reload-by-filename round trip
- [ ] Name/Last Name fields have a sane max length (they're used as a literal save filename)
- [ ] A Back button returns to the Season screen (in addition to the existing Main Menu exit)

**Tasks**
- [ ] Add a custom `FormValidator` date-format + plausibility check on the birth-date field
- [ ] Remove reliance on the `catch (ParseException)` block as the primary gate (keep only as
      defense-in-depth once the above validator is unreachable in normal use)
- [ ] Add overwrite-confirmation check against `SaveLoadSystem.listSavedManagers()`
- [ ] Wire ownership directly at creation time instead of via reload-by-filename
- [ ] Add max-length validation on Name/Last Name
- [ ] Add a Back button

**Decide separately, genuinely new scope (not required for the acceptance criteria above):**
- [ ] Finish the City select box (data model exists in `CountryAndCitySelectBox`, UI does not) — or
      remove the dead `getSelectedCity()`/`setSelectedCity()` API if city won't be built
- [ ] Build Nationality / Languages Spoken / Picture sections promised in the class doc-comment, or
      remove the promise from the comment

**Priority:** P0 (silent-null-birthdate, silent overwrite) / P1 (missing Back, ownership wiring) / P2
(city/nationality/languages/picture — separate scope decision)

---

### 6. Load/Save Game (`LoadGameDialog` + `SaveGameDialog`) + adjacent in-game Quit stubs

**Acceptance criteria — Load**
- [ ] Each save row shows real metadata (in-game date, manager name, club) rather than just the raw
      filename
- [ ] A Delete control exists per save row, behind the shared `ConfirmDialog`
- [ ] Attempting to load a corrupted save shows the shared `ErrorDialog` instead of silently doing nothing
- [ ] Saves are listed most-recent-first
- [ ] The empty-list message is localized via `LanguageModLoader`

**Acceptance criteria — Save**
- [ ] Saving over an existing filename shows the shared `ConfirmDialog` before overwriting
- [ ] The dialog changes to a clear "Close" affordance (or auto-closes) after a successful save, instead
      of staying open indefinitely
- [ ] Remaining hardcoded strings ("Save name:", "Saved as...") are localized

**Acceptance criteria — adjacent in-game Quit (bundled here since it reuses `ConfirmDialog`)**
- [ ] In-game Options "Quit to Menu" routes to `MenuScreen` (behind the shared `ConfirmDialog` if unsaved
      progress exists)
- [ ] In-game Options "Quit to Desktop" calls `Gdx.app.exit()` behind the shared `ConfirmDialog`, matching
      the Main Menu Quit UX

**Tasks**
- [ ] Add metadata display to `LoadGameDialog` rows (lightweight partial read, or accept a full load per
      row for the current small save counts)
- [ ] Add Delete control + confirmation
- [ ] Add `ErrorDialog` on `loadGame(...) == null`
- [ ] Sort `SaveLoadSystem.getSavedGames()` results most-recent-first (or sort at the call site)
- [ ] Localize the empty-list message
- [ ] Add overwrite confirmation to `SaveGameDialog`
- [ ] Change post-save UX to a clear close state
- [ ] Localize remaining `SaveGameDialog` strings
- [ ] Implement the two `MainGameMenuTable` `default:` cases (Quit to Menu / Quit to Desktop) instead of
      logging "OPTION NOT IMPLEMENTED!"

**Priority:** P0 (in-game Quit stubs are fully non-functional, discoverable controls) / P1 (silent load
failure, no delete, no overwrite confirm)

---

## Release & historical notes

- **Minimum release:** R-1888 — these are pre-existing screens already in the R-1888 flow, not new
  historical content; no historical-plausibility concerns apply.
- **Registry:** `docs/registries/features.yaml` `pregame_menus_polish` row tracks this; update its
  `status` as units ship (`partial` → `shipped` once all 7 units + Settings are done).

---

## References (agents only)

- Full audit with file:line citations for every finding above:
  [ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md](../../planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md)
- Settings screen fixes (explicitly deferred, not in this spec): same analysis doc, §2

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-19 | Created — turns the pre-game menu audit into a per-screen task list; Settings screen explicitly excluded per PO decision. Dev order not yet decided. |
