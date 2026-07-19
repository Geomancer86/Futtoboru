# Analysis & Design — Pre-Game Menu Screens Audit

**Document type:** Analysis + design (Layer 4 — research archive; do not bulk-load into agent context)
**Date:** 2026-07-19
**Requested by:** PO — "analyze everything, design proper menus for each screen, make sure all options work
as intended, add new options if relevant, give us a top level report per screen: what is needed and what
is missing"
**Scope:** Every screen/dialog between launching the game and starting a game session: Main Menu,
Settings/Options, the 3-step New Game wizard (Season → Countries → Overview), Create Manager, and
Save/Load. All findings below are from direct code reading, with file:line citations, not guesses.

**Status:** Analysis + design complete. No code changes made yet — this is the report the PO asked for.
Implementation is a separate, follow-up decision (see §9).

---

## 0. Live navigation flow (as the code actually routes today)

```
Futtoboru.create() → MenuScreen                         (SplashScreen exists but is disabled)
                        │
                        ├─ Settings ─────────► SettingsScreen ─── Main Menu ──┐
                        │                                                     │
                        ├─ Load Game ────────► LoadGameDialog ─► GAME_SCREEN  │
                        │                                                     │
                        ├─ Quit ─────────────► Gdx.app.exit()  (no confirm)   │
                        │                                                     │
                        └─ New Game ─────────► NewGameSeasonScreen ◄──────────┘
                                                   │        │
                                          Create Manager   Start
                                                   │        │
                                                   ▼        ▼
                                          NewManagerScreen  NewGameSetupScreen (countries)
                                                   │                 │
                                          (saves + returns)         Next
                                                   │                 │
                                                   ▼                 ▼
                                          NewGameSeasonScreen   NewGameOverviewScreen
                                                                      │
                                                                 Start Game
                                                                      │
                                                                      ▼
                                                                 GAME_SCREEN
```

`NewGameScreen.java` exists in the codebase but is **dead code** — nothing ever constructs it;
`Futtoboru.NEW_GAME_SCREEN` (constant `10`) actually routes to `NewGameSeasonScreen`. Recommend deleting
the orphan file in the implementation pass to stop it misleading future readers.

**Missing from the whole flow:** there is no "Continue" (resume most recent save) option anywhere, and no
`Futtoboru` screen constant for one. Every session starts from the Main Menu and requires an explicit
Load Game click even to resume yesterday's save.

---

## 1. Main Menu — `MenuScreen.java`

### Current state

| Element | Status | Evidence |
|---|---|---|
| New Game button | Works | Routes to `NEW_GAME_SCREEN` |
| Load Game button | Works (happy path) | Opens `LoadGameDialog` |
| Settings button | Works | `SettingsButton` component |
| Match Engine Debug button | Works, dev-only | Gated by `MatchEngineVersion.DEBUG_SCREEN_UI_ENABLED` |
| Quit button | **No confirmation** | `QuitGameButton.java:32` calls `Gdx.app.exit()` directly, comment on the line above literally says `// TODO: validate with Dialog?` |
| Build info label | Works | Bottom-left, from `MatchEngineVersion` |
| Social/Patreon links | Hardcoded English | `SocialNetworkLinksComponent`; TODO comment in `MenuScreen.java:160-164` admits missing i18n |
| **Continue (resume last save)** | **Missing entirely** | No such button, no such screen constant |
| Title/logo/branding | **Missing** | The very first screen a player sees is five text buttons on a black background — no game title, no logo, no art |

### Proposed design

1. Add a **Continue** button above New Game: loads the most recently modified save directly (one click,
   no dialog). Hide/disable it (not just leave it clickable-but-broken) when no saves exist. This is the
   single highest-value *new* option for this screen — it's a standard genre convention CM93 has, that
   Futtoboru currently lacks.
2. Wrap Quit in a confirmation dialog ("Quit Futtoboru? / Yes / Cancel") — one misclick currently closes
   the whole application with zero recovery.
3. Add minimal branding: game title/logo above the button stack. Even a text `VisLabel` with a large
   font (e.g. "FUTTOBORU 1888") is better than nothing — this is a placeholder-quality first impression
   today.
4. Localize the social-links caption text (finish the TODO already flagged in the file).
5. Final button order: **Continue, New Game, Load Game, Settings, (Credits — optional new, low
   priority), Quit.**

**Priority: P0** (Quit confirmation, Continue button) / **P2** (branding, localization).

---

## 2. Settings / Options — `SettingsScreen.java` + `SoundSettingsTable.java` + `ModsSettingsTable.java` + `DebugLoggingSettingsTable.java`

### Current state

This screen is the worst-affected: most of its "Graphics" panel and all of its "Sound" panel are
untouched leftovers from **DarkBlade**, a different (non-football) game this codebase was forked from —
confirmed by the package name itself (`com.rndmodgames.darkblade.screens.tables`) and by every listener
body being either empty or a commented-out `((DarkBlade) game).toggleX()` call.

| Element | Status | Evidence |
|---|---|---|
| Resolution select box | **Works** | `SettingsScreen.java:135-156`, calls `updateScreenResolution` |
| Fullscreen checkbox | **Works** | `:160-166`, calls `updateFullScreen` |
| UI scale select box | **Works** | `:169-184`, calls `updateUiScale` |
| Day/Night tint checkbox | **Dead stub** | `:189-199` — listener body is a comment, `setChecked()` call is also commented out |
| Shadows checkbox | **Dead stub** | `:203-212`, same pattern |
| Damage Counters checkbox | **Dead stub** | `:216-226` — makes no sense for a football manager at all |
| Debug Physics checkbox | **Dead stub** | `:230-241` |
| Debug Path Finding checkbox | **Dead stub** | `:244-254` |
| Debug Sight checkbox | **Dead stub** | `:259-268` |
| Table Debug checkbox | **Dead stub** | `:271-288` |
| "GAMEPLAY SETTINGS" panel | **Empty placeholder** | `:84`, one label, zero controls |
| Master sound slider | **Dead** | `SoundSettingsTable.java:39-49` — only updates its own label; save call is commented out; never loads the existing, working `PreferencesManager.getMasterSoundLevel()` on open |
| Music / Effects / UI sound | **Dead** | `SoundSettingsTable.java:67-73` — orphan labels, no controls at all |
| Language select box | **Dead** | `ModsSettingsTable.java:32-48` — prints to console only; `updateLanguage` call commented out; never calls the existing, working `PreferencesManager.saveLocale()`; never pre-selects the current saved locale on open |
| Debug logging checkboxes + Enable/Disable All | **Works** | Via `DebugLogManager`, refreshes correctly in `show()` |
| Main Menu button | Works | |

**Root cause check confirmed by reading `PreferencesManager.java` directly:** `saveLocale()`/
`getSavedLocale()` and `getMasterSoundLevel()`/`updateMasterSoundLevel()` are fully implemented and
already used correctly elsewhere (locale is loaded at boot — see the `LOCALE RESTORED: en_US` log line).
The UI for both simply never calls them. This is a wiring gap, not a missing-infrastructure problem.

**Also confirmed: there is no audio system anywhere in the codebase.** A repo-wide search for
`Gdx.audio`, `Sound`, or `Music` usage returns nothing. The entire Sound panel controls a feature that
does not exist yet — shipping volume sliders for audio that is never played is actively misleading.

### Proposed design

Reorganize into sections that actually apply to a football management sim:

1. **Display** (keep, unchanged — already fully functional): Resolution, Fullscreen, UI Scale.
2. **Language**: fix the wiring — on screen open, pre-select `PreferencesManager.getSavedLocale()`; on
   change, call `saveLocale()` and show a small note ("Restart to fully apply" if live bundle-reload
   across all currently-built screens isn't feasible for v1 — that's an acceptable, honest v1 answer;
   silently doing nothing is not).
3. **Gameplay** (new, real content instead of an empty label) — candidates, pick what the engine
   actually supports without new scope: autosave on/off or frequency, a "confirm before ending a phase"
   toggle if applicable. If nothing is ready to wire yet, remove the empty panel rather than ship a
   placeholder label with no controls.
4. **Remove entirely**: Day/Night tint, Shadows, Damage Counters, Debug Physics, Debug Path Finding,
   Debug Sight. None of these apply to Futtoboru's 2D UI/match-sim presentation — they are DarkBlade
   debris, not a "todo," and should be deleted rather than fixed.
5. **Remove or clearly gate as "Coming soon"**: the entire Sound panel, until an actual audio system
   exists. Shipping non-functional sliders is worse than shipping nothing.
6. **Keep, unchanged**: Debug Logging panel (already correctly implemented and tested).
7. Add a **Reset to Defaults** button.
8. Add a brief confirmation toast/label ("Settings saved") when leaving via Main Menu, since every other
   change here is silent auto-save with no feedback at all today.

**Priority: P1** (dead controls are actively misleading; not data-loss-risk, so not P0) — but high-value
and low-risk to fix, since most of the fix is *deletion* of dead code plus two small wiring calls
(`saveLocale`, `updateMasterSoundLevel` — or delete the sound panel instead, per the recommendation
above).

---

## 3. New Game — Step 1: Season & Manager — `NewGameSeasonScreen.java`

### Current state

| Element | Status | Evidence |
|---|---|---|
| Season select box | Works | Updates description/date/country-count/URL on change |
| Season description/date/country-count labels | Works | Dynamic, pulled from `Season` |
| Season source URL link | **Cosmetic bug** | `:286-288` — the label text is hardcoded to the literal string `"Wikipedia"` regardless of the season's actual `getUrlSource()` |
| Existing-manager select box | **Bug: wrong array size** | `:165` — `savedManagers.toArray(new String[allSeasons.size()])` sizes the array to the *seasons* list, not the *managers* list; if there are ever more seasons than managers this throws/produces trailing `null` entries |
| "Select Manager" button | **Redundant** | Its click handler (`:180-187`) does exactly what the select box's own (never-registered) change would do — there's no listener on the select box itself, so this button is the *only* way selection updates, which is non-obvious UX (nothing indicates you must click Select, not just pick from the dropdown) |
| Auto-select first manager | Works, but silent | `:190` — happens with zero UI feedback about which manager got picked |
| Create Manager button | Works, but destructive | `:213` creates a brand-new empty `SaveGame` unconditionally, discarding anything already chosen on this screen (season selection is not carried into `NewManagerScreen` at all) |
| **Start button** | **P0 bug: no null guard** | `:230-254` — if there are zero saved managers, the manager-selection block (`:157-195`) is skipped entirely, so `selectedManager` stays `null`; clicking Start proceeds anyway, setting `currentGame.setOwner(null)` and routing to the Countries screen. The failure only actually surfaces two screens later, in `NewGameOverviewScreen`'s `Step 4` null-check (`:411-414`), as a caught exception with no user-facing message (see §5) |
| Main Menu (back) button | Works | |
| `hide()` / `dispose()` | **Bug: stage never disposed** | Both are empty `// TODO Auto-generated method stub` — every other screen in this flow properly disposes its stage in `hide()`; this one leaks |

### Proposed design

1. Add a visible **step indicator** ("Step 1 of 3 — Season & Manager") — none of the three New Game
   screens currently signal that this is a multi-step wizard.
2. If there is only one season available (true for R-1888's scope today), replace the dropdown with a
   plain label — a select box with exactly one, unchangeable option is confusing UI, not a real choice.
3. Fix the hardcoded `"Wikipedia"` label to either read a real source-name field or be removed (the URL
   itself is already shown as a clickable link; the "Wikipedia" caption adds nothing today and is
   provably wrong that the moment a mod adds a season sourced elsewhere).
4. Fix the array-size bug (`allSeasons.size()` → `savedManagers.size()`).
5. Manager section redesign:
   - If zero saved managers exist, show "No managers yet — create one to get started" and make the
     Start button **disabled**, not just silently likely-to-fail.
   - Show a one-line manager summary (name + nationality, or name + birth year) next to the dropdown
     instead of just the raw filename.
   - Merge the redundant dropdown + Select-button pair into one control: either wire a `ChangeListener`
     on the dropdown itself (removing the button) or keep the button but disable the dropdown's implicit
     first-item auto-select ambiguity — pick one interaction model, not both.
   - Add a small **Delete Manager** icon per saved manager (there is currently no way to clean up old
     test/throwaway managers short of manually deleting files on disk).
6. **Start button**: disable whenever `selectedSeason == null || selectedManager == null`, with a
   tooltip explaining which is missing. This converts a currently-silent, two-screens-later failure into
   an immediate, obvious one.
7. Preserve season selection across the Create-Manager round trip (currently lost, since Create Manager
   discards all screen state and starts a fresh `SaveGame`).
8. Fix `hide()`/`dispose()` to actually dispose the stage.

**Priority: P0** (null-manager Start, stage leak) / **P1** (redundant control, hardcoded label,
no-managers messaging).

---

## 4. New Game — Step 2: Choose Countries — `NewGameSetupScreen.java`

### Current state

| Element | Status | Evidence |
|---|---|---|
| Continent/country checkbox list | Works, but forced | `:170-171` — every checkbox is force-set to checked immediately after its own change-listener is registered, with an explicit `// TODO: unhardcode this default selection` comment; this doesn't disable the checkbox (the listener still fires and updates `selectedCountries` correctly on manual toggles), but it means the screen always *opens* with everything checked regardless of what a caller passed in as pre-selected |
| Next button | **P0 bug: no validation** | `:199-211` — proceeds unconditionally, including with **zero** countries selected, which would leave `NewGameOverviewScreen` and the eventual `Start Game` with no clubs/leagues to populate at all |
| Back button | Works | Returns to Season screen (loses profession/club state downstream, which is expected/correct since those aren't chosen yet at this point) |
| Country flags / icons | **Never implemented** | The class's own doc-comment (`:70-79`) describes showing "Country Flag" per row; the actual row only ever adds a checkbox + plain text label |
| Live selection count | **Missing** | No "(N countries, M clubs selected)" feedback anywhere on this screen — the player picks blind |

### Proposed design

1. Add step indicator ("Step 2 of 3 — Choose Countries").
2. Remove the forced default-check override; pick one deliberate default (all checked, or none) and
   document it as intentional rather than leaving live, working listener logic fighting a hardcoded
   override.
3. **Validate**: disable Next (with a tooltip, "Select at least one country") when `selectedCountries`
   is empty.
4. Add a live counter: "X countries, Y clubs selected" — the club-count math already exists downstream
   in `NewGameOverviewScreen`; either pull it forward or compute a cheap duplicate here.
5. Add **Select All / Select None** quick-action buttons — useful now, more useful once mods add more
   countries.
6. (P2, nice-to-have) Either implement the promised country flags using existing club-crest assets, or
   remove the aspiration from the class doc-comment so it stops reading as an open TODO nobody is
   tracking.

**Priority: P0** (zero-country Next) / **P1** (forced-default TODO, missing counter) / **P2** (flags).

---

## 5. New Game — Step 3: Overview & Start — `NewGameOverviewScreen.java`

### Current state

This is, by far, the **most mature** screen in the flow — `Start Game`'s handler (`:365-819`) already
has genuinely good engineering: 14 explicit, logged steps, defensive null-checks at nearly every stage,
and a broad `try/catch` so a failure can't crash the whole window. The gaps here are about **surfacing**
that quality to the player, not about the underlying logic being broken.

| Element | Status | Evidence |
|---|---|---|
| Selected countries list | Works | Display-only |
| Cups/Leagues/Teams counts | **Misleading numbers** | `:117-133` — `activeCups`/`activeLeagues` are counted over **every** competition in the entire database, not filtered to `selectedCountries` at all (only `activeTeams`/`activePlayers`, a few lines later, correctly filter by country) |
| Estimated player count | Works, but silent about being an estimate | `:148-155, :176` — hardcoded `20` players per club-without-real-players and a flat `* 1.5f` free-agent multiplier, with no "(estimated)" label or tooltip telling the player these are guesses |
| Starting country select box | Works, but fragile | `:257-264` — a "hacky" double `setSelectedIndex(1); setSelectedIndex(0);` to force the change listener to fire; falls into an `else` branch that skips this trick entirely when there's only one country, which happens to work today only because `updateSelectableTeamsByLeagueAndCountry()` gets called again later from the profession listener anyway |
| Profession select box | Works | Defaults to first entry (typically "Unemployed") |
| Starting club select box | Works, but unlocalized placeholder | `:917` — literal `"UNAVAILABLE"` string, not run through `LanguageModLoader` |
| Back button | Works | Comment at `:346-350` admits profession/league selections aren't preserved on Back — acceptable today since this is the last step, but worth knowing if a 4th step is ever added |
| **Start Game button — success path** | **Works, and works well** | Extensive validation + logging + welcome-message delivery + attribute generation |
| **Start Game button — failure path** | **P0 bug: silent failure** | `:776-816` — on any exception, the handler logs extensively to `System.err`/`Gdx.app.error` and then simply `return`s from the button handler. **Nothing is shown to the player.** From the player's chair, clicking Start Game and having it silently fail looks identical to the game being frozen or the button being broken — there is a `// TODO: Show error dialog to user` comment (`:811`) confirming this was already known and never done |

### Proposed design

1. Add step indicator ("Step 3 of 3 — Confirm & Start").
2. **Highest-value single fix in this whole audit**: replace the failure path's silent `return` with a
   `VisDialog` shown to the player — "Could not start the game: `<short reason>`. Please try again or
   report this." This is the difference between "the game is broken" and "something went wrong, here's
   what."
3. Filter the Cups/Leagues counts to the selected countries, matching how Teams/Players are already
   correctly filtered a few lines below in the same method.
4. Label the player-count estimate as an estimate (append "(estimated)" or add a `(?)` tooltip).
5. Localize `"UNAVAILABLE"`.
6. Replace the "hacky" double-`setSelectedIndex` trick with a direct call to
   `updateSelectableTeamsByLeagueAndCountry()` right after setting `startingCountry`, for both the
   single- and multi-country branches — removes the fragile dependency on Scene2D's exact
   change-detection semantics.
7. Add proactive validation (disable Start Game while `primaryProfession` is unset, or while a
   club-requiring profession has no club chosen) instead of relying purely on the reactive exception
   path — the exception path should become a true last-resort safety net, not the primary UX.
8. Show a brief busy/spinner state while `Start Game` is processing — steps 1-14 do real work
   (attribute generation for every player in every club) and could take a noticeable moment on a large
   mod; right now there's no indication the click registered until the screen changes.

**Priority: P0** (silent failure on Start) / **P1** (misleading cup/league counts, fragile country
select trick) / **P2** (estimate labeling, localization, busy indicator).

---

## 6. Create Manager — `NewManagerScreen.java`

### Current state

| Element | Status | Evidence |
|---|---|---|
| Name / Last Name fields | **Validated for non-empty only** | `FormValidator.notEmpty(...)` at `:277-279` — no length cap, no charset restriction, despite the last name being used verbatim as a filename a few lines later |
| Birth date field | **P0 bug: format never validated** | `:271-273` — the class's own comment says `"TODO: Date format validation"`; the `FormValidator` only calls `notEmpty` on it (`:279`), never a format/regex check. If the text doesn't parse as `dd/MM/yyyy`, `SimpleDateFormat.parse()` throws, is caught at `:239-241` with only `e.printStackTrace()`, and **the manager is still saved and the screen still navigates away** — with a silently-null birth date. The player sees no error and believes manager creation succeeded correctly. |
| Place of birth (country + state) | Works, partially | `CountryAndCitySelectBox` |
| Place of birth — **city** | **Dead, unimplemented** | `CountryAndCitySelectBox.java` has a full `selectedCity`/`getSelectedCity()`/`setSelectedCity()` API but **no city `VisSelectBox` is ever built or added** — only country and state exist in the actual UI. The class's own constructor doc-comment (`:24-27`) promises "Automatic Loading of Countries and Cities," which never happened. |
| Nationality / 2nd nationality / Languages spoken | **Never built** | Listed in the class-level doc-comment (`:44-51`) as planned sections; no corresponding UI exists at all |
| Picture/Icon | **Never built** | Same — listed in the doc-comment, no UI |
| Create Manager button | **P0 bug: silent overwrite** | `:255` — `SaveLoadSystem.saveManagerDataFile(manager, manager.getLastname().toLowerCase())` writes unconditionally by lastname; the method's own preceding comment (`:250-252`) says `"TODO: we need to check if there is another manager file with the same name and ask to rewrite or save to separate file"` — meaning creating "Alex Ferguson" twice silently destroys the first one with no warning |
| Manager ↔ game ownership | **Inconsistent state** | This screen never calls anything equivalent to `currentGame.setOwner(manager)` — ownership is only wired up later, back on `NewGameSeasonScreen`, by re-loading the just-saved manager from disk by filename. Works today only because the round trip happens to be reliable; fragile by construction. |
| `LocalTime` import | Dead | Only referenced inside a fully commented-out alternate implementation (`:233-236`) — confirms this class has had abandoned in-progress edits left in place |
| Back button | **Missing** | Only a "Main Menu" exit exists — there's no way to return to the Season screen (to re-pick the season, for instance) without abandoning the entire New Game flow |

### Proposed design

1. Add **real date-format validation**: VisUI's `FormValidator` supports custom validators beyond
   `notEmpty` (e.g. `matches(field, regex, message)` or a custom `FormInputValidator`) — add one so the
   Create button is disabled until the text actually parses as a valid, sane date (also reject
   obviously-impossible dates, e.g. birth year in the future or absurdly far in the past for a 1888
   manager).
2. **Remove the reachable-but-silent parse-failure path.** If format validation (above) makes it
   unreachable in the UI, keep the `catch` block only as defense-in-depth, but it should never be the
   primary gate.
3. Add an **overwrite confirmation dialog** when `SaveLoadSystem.listSavedManagers()` already contains
   the entered last name — "A manager named X already exists. Overwrite? / Rename instead?"
4. Either **finish the City select box** (mirror the State box's file-driven pattern with a
   `mods/cities/<state>.txt` convention) **or delete** the dead `getSelectedCity()`/`setSelectedCity()`
   API and the doc-comment's promise of it — don't leave a half-built feature that looks finished from
   the data model side.
5. Either **build Nationality / Languages / Picture** as real, minimal fields (Nationality can default
   from the selected country with an override dropdown; Languages/Picture can be deferred to a later
   release) **or remove them from the class doc-comment** so the audit trail doesn't keep suggesting
   they're "almost done."
6. Attach ownership directly here: set `currentGame.getOwner()` (or an equivalent) at creation time in
   memory, rather than requiring a save-to-file-then-reload-by-filename round trip just to get a `Person`
   reference back to the caller.
7. Add input sanity limits on Name/Last Name (reasonable max length, since it's used as a raw filename).
8. Add a **Back** button (to the Season screen), not just Main Menu.

**Priority: P0** (silent-null-birthdate, silent overwrite) / **P1** (missing Back, ownership wiring) /
**P2** (city/nationality/languages/picture — genuinely new scope, not just a bug fix).

---

## 7. Load Game — `LoadGameDialog.java`

### Current state

| Element | Status | Evidence |
|---|---|---|
| Save list + Load button per row | Works (happy path) | `loadGame()` → `activateSaveGame()` → `GAME_SCREEN` |
| Empty-list message | **Hardcoded, unlocalized** | `:36` — literal `"No saved games found."`, not via `LanguageModLoader` |
| Save row content | **Filename only** | No in-game date, manager name, club, or real-world last-modified timestamp shown — just the raw save name |
| Delete save | **Missing entirely** | No such control |
| Load failure feedback | **Silent no-op** | `:55-60` — if `SaveLoadSystem.loadGame(savedGameName)` returns `null` (the corruption case `SaveLoadCorruptionTest` already covers at the persistence layer), the `if (loaded != null)` guard just... does nothing. The player clicks Load and nothing happens, with no explanation. |
| Save ordering | Unclear/unverified | Whatever order `SaveLoadSystem.getSavedGames()` returns — not confirmed to be most-recent-first |
| Close button | Works | |

### Proposed design

1. Localize the empty-state message.
2. Show real metadata per row: in-game date, manager name, club (if any). This likely needs either a
   lightweight metadata read (peek a few top-level JSON fields without full deserialization) or accepting
   the cost of a full load per row (fine for a handful of saves, worth revisiting if save counts grow).
3. Add a **Delete** button per row, behind a confirmation dialog.
4. On load failure, show a dialog: "This save appears to be corrupted and could not be loaded." — turn
   the existing silent `null` guard into user-facing feedback instead of a no-op.
5. Sort by most-recently-modified first.

**Priority: P1** (silent load failure is the standout issue here — not P0 only because the underlying
persistence layer already fails safe, per `SaveLoadCorruptionTest`; the dialog just needs to say so).

---

## 8. Save Game — `SaveGameDialog.java` (in-game only, not reachable pre-game, included for completeness since it shares all its infrastructure with Load)

### Current state

| Element | Status | Evidence |
|---|---|---|
| Name field, pre-filled with a sensible default | Works | `defaultSaveName()` builds `<club>_<date>` or `savegame_<date>` |
| Save button | **No overwrite confirmation** | `:39-40` — `SaveLoadSystem.saveGame(saveGame, name)` writes unconditionally if the name collides with an existing file |
| Status label after save | Works, but dialog never closes | `:42` sets `"Saved as X.json"` but the dialog itself stays open indefinitely — no auto-close, no explicit "Close" affordance beyond Cancel |
| Cancel button | Works | |
| Hardcoded strings | `"Save name:"`, `"Saved as " + name + ".json"` | Not localized |

### Proposed design

1. Add an overwrite-confirmation dialog when the entered name already matches an existing save file.
2. After a successful save, change the Cancel button's label to "Close" (or auto-hide after a short
   delay) instead of leaving the dialog open with no clear next action.
3. Localize the remaining hardcoded strings.

**Priority: P2** (no data-loss risk today since overwriting a save the player explicitly named is a much
lower-stakes mistake than the P0s above, but still worth the small fix).

---

## 8.1 Adjacent finding (not pre-game, flagged because it's the same conceptual surface)

`MainGameMenuTable.java` (`:123-158`) — the in-game Options dropdown has 6 entries; entries 0-3 (main
label / Save / Load / Settings) all work correctly. Entries 4 and 5, **"Quit to Menu" and "Quit to
Desktop," both fall through to the `default:` case and only log `"OPTION NOT IMPLEMENTED!"`** — clicking
either does nothing visible at all. This is the first thing a player will try when they want to stop
playing mid-session, and it's a complete no-op today. Recommend fixing alongside this audit's
implementation pass even though it's technically outside the strict "pre-game" boundary, since it shares
the exact same "Settings/Options" conceptual surface the PO asked about.

**Priority: P0** (a fully non-functional, discoverable menu option).

---

## 9. Cross-cutting recommendations

1. **No confirmation-dialog convention exists anywhere.** Quit, manager overwrite, save overwrite, and
   delete-save all need one, and none currently have it. Build one reusable `ConfirmDialog` (or a small
   static helper around `VisDialog`) once, and use it in all four places instead of four bespoke
   solutions.
2. **No error-dialog convention exists anywhere.** New Game start failure and Load failure both need
   one. Same recommendation — one reusable `ErrorDialog` helper, used everywhere a background operation
   can fail after the click.
3. **Zero automated test coverage** for any of these 8 screens/dialogs (confirmed by search — the only
   related tests are `PreferenceManagerTests`, which tests the `PreferencesManager` object directly, not
   any screen). At minimum, headless-GDX smoke tests (the pattern already exists via
   `HeadlessGdxTestSupport`/`GameTestHarness` elsewhere in the suite) for: "New Game with valid inputs
   reaches `GAME_SCREEN` without throwing," "New Game Start with null manager is rejected before
   `Start Game` is even reachable," "Load Game with a corrupted save shows an error, not a silent no-op."
4. **DarkBlade leftovers should be deleted, not "fixed."** `com.rndmodgames.darkblade.screens.tables.*`
   and everything in `SettingsScreen`'s graphics/sound panels that references a non-existent
   `DarkBlade`-cast game object are inherited scaffolding from an unrelated game. A cleanup pass that
   deletes rather than repairs these is both less work and the more honest outcome.
5. **No step/wizard indicator** exists across the 3-screen New Game flow (Season → Countries →
   Overview) — a simple "Step N of 3" label, consistently placed, would meaningfully reduce
   first-time-player confusion about how much setup is left.
6. **Localization gaps are scattered, not systemic** — most labels correctly use `LanguageModLoader`,
   but hardcoded English strings recur often enough (this audit found at least 9 distinct instances
   across 6 files) that a dedicated sweep is worth scheduling once the functional fixes above are done,
   rather than trying to catch every one opportunistically.

---

## 10. Priority summary (all findings, one list)

**P0 — fix before any human playtest of the New Game flow (crash risk, data loss, or fully broken,
discoverable controls):**

- New Game Start allows a `null` selected manager through (`NewGameSeasonScreen`)
- Create Manager: invalid birth-date text is silently swallowed into a `null` birth date with no
  feedback, and the manager still saves (`NewManagerScreen`)
- Create Manager: saving a manager with an existing last name silently overwrites the old file
  (`NewManagerScreen`)
- Countries step: Next is reachable with zero countries selected, which breaks the rest of setup
  (`NewGameSetupScreen`)
- Start Game: any failure during the 14-step startup process is completely silent to the player
  (`NewGameOverviewScreen`)
- Main Menu Quit has no confirmation (`QuitGameButton`)
- In-game "Quit to Menu" / "Quit to Desktop" are both no-ops (`MainGameMenuTable`)

**P1 — fix soon after (misleading, confusing, or a discoverable-but-non-critical dead control):**

- Settings screen: DarkBlade graphics-debug toggles and the entire Sound panel are dead, misleading
  controls for features that don't exist in this game
- Settings screen: Language dropdown doesn't persist or preload the current selection
- Load Game: no metadata shown, no delete option, silent no-op on a corrupted save
- Overview screen: Cups/Leagues counts include the whole database, not just selected countries
- Overview screen: fragile "hacky" double-`setSelectedIndex` trick for the country select box
- New Game Season screen: wrong array-size bug on the saved-managers list; stage never disposed
- Create Manager: no Back button; ownership wiring depends on a fragile save-then-reload round trip
- Save Game dialog: no overwrite confirmation; dialog never closes itself after a successful save

**P2 — polish, once P0/P1 are done:**

- No step-of-3 indicator anywhere in the New Game wizard
- Scattered hardcoded/unlocalized strings (≈9 instances found)
- Create Manager: missing City selector (data model exists, UI doesn't), missing
  Nationality/Languages/Picture sections promised in the class doc-comment
- Countries step: promised-but-missing country flags; no live "N countries / M clubs" counter
- Overview screen: player-count estimate not labeled as an estimate; unlocalized "UNAVAILABLE" placeholder
- No branding/logo on the Main Menu
- Delete-old-manager UI missing
- `NewGameScreen.java` is dead code and should be deleted

---

*This document is an analysis + design deliverable only — no code has been changed as part of it.
Implementation should be scoped as its own feature-spec pass per `AGENTS.md`'s spec-driven workflow
(`docs/specs/{id}/spec.md`) if/when the PO approves proceeding, likely split by priority tier above rather
than as one single change.*
