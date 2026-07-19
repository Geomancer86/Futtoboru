# Pre-game menu screens — fixes & redesign (excl. Settings)

**Feature id:** `pregame_menus_polish`
**Release:** R-1888
**Status:** implemented — all 7 units done 2026-07-19; full test suite + nightly profile green;
Settings screen still explicitly deferred per PO decision

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
- [x] A "Continue" button loads the most recently modified save directly, with no intermediate dialog
- [x] "Continue" is hidden or disabled (not just present-but-broken) when no saves exist
- [x] Quit shows a Yes/Cancel confirmation before calling `Gdx.app.exit()`
- [x] The screen shows a game title/logo, not just a bare stack of buttons
- [x] Social/Patreon link caption text is localized via `LanguageModLoader`, not hardcoded (confirmed
      the caption is a raw brand name with no i18n concern; left as-is, see `SocialNetworkLinksComponent`)

**Tasks**
- [x] Add `Continue` button + wiring to load most-recent save (reuse `SaveLoadSystem`/`LoadGameDialog`
      logic for "load by name" against the most-recent entry)
- [x] Wrap `QuitGameButton`'s `Gdx.app.exit()` call in the shared `ConfirmDialog`
- [x] Add a title/logo `VisLabel` or image above the button stack
- [x] Localize `SocialNetworkLinksComponent` caption text
- [x] Final button order: Continue, New Game, Load Game, Settings, Quit

**Priority:** P0 (Quit confirmation, Continue) / P2 (branding, localization)

---

### 2. New Game Step 1 — Season & Manager (`NewGameSeasonScreen`)

**Acceptance criteria**
- [x] Start is disabled whenever `selectedSeason == null || selectedManager == null` (never reachable with
      a null manager)
- [x] When zero saved managers exist, the screen clearly says so and Start stays disabled until one is
      created
- [x] The saved-managers select box is sized correctly (no `allSeasons.size()` bug)
- [x] Selecting a manager from the dropdown updates the summary without requiring a separate "Select"
      click (single, unambiguous interaction model)
- [x] A one-line manager summary (name + birth year or nationality) is shown next to the dropdown, not
      just the raw filename
- [x] A Delete-manager control exists per saved manager, behind the shared `ConfirmDialog`
- [x] Season selection is preserved across a round trip through Create Manager and back
- [x] `hide()`/`dispose()` actually disposes the stage
- [x] If only one season exists, it is shown as a label, not a single-item dropdown
- [x] The season source-URL caption reflects the real source name (or is removed), not a hardcoded
      `"Wikipedia"` string
- [x] A "Step 1 of 3" indicator is visible

**Tasks**
- [x] Add the null-guard + disabled state to the Start button
- [x] Add "no managers yet" messaging
- [x] Fix `savedManagers.toArray(new String[allSeasons.size()])` → correct size
- [x] Consolidate dropdown + "Select Manager" button into one interaction (pick one, remove the other or
      merge behavior)
- [x] Add manager summary label
- [x] Add per-manager Delete control (uses shared `ConfirmDialog`)
- [x] Preserve `selectedSeason` when routing to/from `NewManagerScreen`
- [x] Implement `hide()`/`dispose()` properly
- [x] Single-season case: replace dropdown with a label
- [x] Fix/remove the hardcoded `"Wikipedia"` label
- [x] Add step indicator (shared small component/label used by all 3 New Game screens)

**Priority:** P0 (null-manager Start, stage leak) / P1 (redundant control, hardcoded label, messaging)

---

### 3. New Game Step 2 — Choose Countries (`NewGameSetupScreen`)

**Acceptance criteria**
- [x] Next is disabled (with a tooltip) whenever zero countries are selected
- [x] The forced "always check every box on open" override is removed; default state is deliberate and
      documented, not fought against by live listener logic
- [x] A live "N countries / M clubs selected" counter is visible
- [x] Select All / Select None quick actions exist
- [x] "Step 2 of 3" indicator is visible

**Tasks**
- [x] Add validation + disabled state to Next
- [x] Remove the hardcoded `countryCheckBox.setChecked(true)` override; decide and document the real
      default
- [x] Add live selection counter (reuse the club-count math already in `NewGameOverviewScreen`, or a cheap
      local duplicate)
- [x] Add Select All / Select None buttons
- [x] Add step indicator

**Out of this unit (P2, genuinely new scope, defer):** country flags/icons per row

**Priority:** P0 (zero-country Next) / P1 (forced-default override, missing counter)

---

### 4. New Game Step 3 — Overview & Start (`NewGameOverviewScreen`)

**Acceptance criteria**
- [x] Any failure during `Start Game`'s 14-step process shows the shared `ErrorDialog` to the player,
      instead of only logging to console — this is the single highest-value fix in the whole spec
- [x] Cups/Leagues counts are filtered to the selected countries, matching how Teams/Players are already
      correctly filtered
- [x] The player-count figure is visibly labeled as an estimate
- [x] The starting-club placeholder text ("UNAVAILABLE") is localized
- [x] The starting-country select box's "hacky" double-`setSelectedIndex` workaround is replaced with a
      direct call to `updateSelectableTeamsByLeagueAndCountry()`
- [x] Start Game is proactively disabled while required selections (profession; club, if the profession
      requires one) are incomplete, not just reactively caught by the exception path
- [x] A busy/spinner indicator is shown while Start Game's 14 steps are processing
- [x] "Step 3 of 3" indicator is visible

**Tasks**
- [x] Replace the silent `return` in the failure `catch` block with the shared `ErrorDialog`
- [x] Filter the Cups/Leagues counting loop by `selectedCountries`
- [x] Add "(estimated)" label/tooltip to the player-count figure
- [x] Localize `"UNAVAILABLE"`
- [x] Replace the double-`setSelectedIndex` trick with a direct update call
- [x] Add proactive disabled-state validation on Start Game
- [x] Add a busy indicator during the Start Game click handler
- [x] Add step indicator

**Priority:** P0 (silent failure on Start) / P1 (misleading counts, fragile select-box trick) / P2
(estimate labeling, localization, busy indicator)

---

### 5. Create Manager (`NewManagerScreen`)

**Acceptance criteria**
- [x] The Create Manager button is disabled until the birth-date field actually parses as a valid date
      (not just non-empty) — reject unparseable and implausible dates (e.g. future birth year)
- [x] The previously-reachable "invalid date silently becomes null and manager still saves" path is
      eliminated
- [x] Creating a manager whose last name matches an existing saved manager shows the shared
      `ConfirmDialog` ("already exists — overwrite?") before writing
- [x] The created manager is attached to `currentGame`'s owner directly in memory, without depending on a
      save-to-file-then-reload-by-filename round trip
- [x] Name/Last Name fields have a sane max length (they're used as a literal save filename)
- [x] A Back button returns to the Season screen (in addition to the existing Main Menu exit)

**Tasks**
- [x] Add a custom `FormValidator` date-format + plausibility check on the birth-date field
- [x] Remove reliance on the `catch (ParseException)` block as the primary gate (keep only as
      defense-in-depth once the above validator is unreachable in normal use)
- [x] Add overwrite-confirmation check against `SaveLoadSystem.listSavedManagers()`
- [x] Wire ownership directly at creation time instead of via reload-by-filename
- [x] Add max-length validation on Name/Last Name
- [x] Add a Back button

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
- [x] Each save row shows real metadata (in-game date, manager name, club) rather than just the raw
      filename
- [x] A Delete control exists per save row, behind the shared `ConfirmDialog`
- [x] Attempting to load a corrupted save shows the shared `ErrorDialog` instead of silently doing nothing
- [x] Saves are listed most-recent-first
- [x] The empty-list message is localized via `LanguageModLoader`

**Acceptance criteria — Save**
- [x] Saving over an existing filename shows the shared `ConfirmDialog` before overwriting
- [x] The dialog changes to a clear "Close" affordance (or auto-closes) after a successful save, instead
      of staying open indefinitely
- [x] Remaining hardcoded strings ("Save name:", "Saved as...") are localized

**Acceptance criteria — adjacent in-game Quit (bundled here since it reuses `ConfirmDialog`)**
- [x] In-game Options "Quit to Menu" routes to `MenuScreen` (behind the shared `ConfirmDialog` if unsaved
      progress exists)
- [x] In-game Options "Quit to Desktop" calls `Gdx.app.exit()` behind the shared `ConfirmDialog`, matching
      the Main Menu Quit UX

**Tasks**
- [x] Add metadata display to `LoadGameDialog` rows (lightweight partial read, or accept a full load per
      row for the current small save counts)
- [x] Add Delete control + confirmation
- [x] Add `ErrorDialog` on `loadGame(...) == null`
- [x] Sort `SaveLoadSystem.getSavedGames()` results most-recent-first (or sort at the call site)
- [x] Localize the empty-list message
- [x] Add overwrite confirmation to `SaveGameDialog`
- [x] Change post-save UX to a clear close state
- [x] Localize remaining `SaveGameDialog` strings
- [x] Implement the two `MainGameMenuTable` `default:` cases (Quit to Menu / Quit to Desktop) instead of
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
| 2026-07-19 | Implemented all 7 units in one pass: `ConfirmDialog`/`ErrorDialog`/`WizardStepLabel` shared components; Main Menu Continue/Quit-confirm/branding; New Game Steps 1–3 (null-guards, manager UX, country validation+counter, filtered counts, busy indicator, error dialog on Start failure); Create Manager (`ManagerBirthDateValidator` with `STRICT`/`uuuu` date resolution, overwrite confirm, direct ownership wiring, Back button); `LoadGameDialog`/`SaveGameDialog` (metadata rows, delete, overwrite confirm, close-state, most-recent-first sort); in-game Quit to Menu/Desktop wired up in `MainGameMenuTable`. Added ~60 new i18n keys to all 3 language bundles. Full default suite (165 tests) + nightly profile green. |
