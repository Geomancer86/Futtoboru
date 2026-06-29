# PU-10 — UI Scale / HiDPI — Completion Summary

**Document type:** Platform / UX closure  
**Date:** 2026-06-25  
**Branch:** `analyze-ai-branch`  
**Status:** ✅ **Accepted for Layer A dev/QA** (manual sign-off on Galaxy Book3 Pro)

---

## 1. Problem

On HiDPI laptops (Samsung Galaxy Book3 Pro), Futtoboru UI was unreadably small at default settings. Window resolution alone does not scale fonts or controls.

---

## 2. Solution shipped

| Piece | Implementation |
|-------|----------------|
| **User control** | Settings → **UI scale** (100%, 125%, 150%, 175%, 200%) |
| **Persistence** | Global pref `settings.ui_scale` in `.prefs/Futtuboru Preferences` (not per savegame) |
| **Scaling mechanism** | `UiScale` + `ScreenViewport.setUnitsPerPixel(1 / scale)` — layout, draw, and clicks stay aligned |
| **Default for dev** | **200%** (`DEFAULT_UI_SCALE = 2.0f`; one-time migration `settings.ui_scale_migration_v5_hidpi`) |
| **Clipping mitigation** | `UiScale.addOverflowScroll()` / `newOverflowScrollPane()` on wide standalone screens; Settings panels **stacked vertically** |
| **In-game shell** | `MainGameScreen`: **fixed top menu** + scrollable body only (sidebar + content) — squad wide table no longer stretches Continue button |
| **Excluded** | `MatchEngineScreen` overlay (per PO) |

**Key code:** `futtoboru-core/.../ui/UiScale.java`, `PreferencesManager.java`, `SettingsScreen.java`, `MainGameScreen.java`

---

## 3. Iteration history

| Phase | Approach | Result |
|-------|----------|--------|
| **PU-10 MVP** | `unitsPerPixel` + Settings select | ✅ Scales UI; ❌ clips wide layouts at 150%+ |
| **PU-10-R2** | 1:1 viewport + `Actor.setScale` + root `VisScrollPane` | ❌ **Reverted** — no visual scale, broken clicks |
| **PU-10-R3** | `unitsPerPixel` + overflow scroll **without** actor scale; default 200% | ✅ **QA passed** — readable, clickable |
| **PU-10-R3b** | Main game: pin top menu, scroll body only | ✅ Squad screen Continue button stays on screen |

---

## 4. Manual QA sign-off (2026-06-25)

| Check | Result |
|-------|--------|
| Default 200% readable on Galaxy Book | ✅ |
| Settings UI scale change applies | ✅ |
| Clicks register | ✅ |
| Settings all panels reachable | ✅ |
| In-game screens sampled | ✅ |
| Squad screen top menu not pushed off-screen | ✅ (after R3b) |

---

## 5. Known follow-ups (not blocking PU-10)

| ID | Item |
|----|------|
| **TECH-UPG-1** | LibGDX 1.12 → 1.14.x + VisUI bump (separate PR) |
| **PU-10b** | Desktop launcher `HdpiMode.Pixels` for sharper text on Windows display scaling |
| **PU-10-R4** | Optional `ExtendViewport` design resolution for window resize |
| **Layout** | Audit fixed `.width(N)` on league/inbox tables at 200% |

---

## 6. Related docs

| Doc | Role |
|-----|------|
| [ANALYSIS_UI_SCALING_HIDPI_V1.md](ANALYSIS_UI_SCALING_HIDPI_V1.md) | Original gap analysis |
| [ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md](ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md) | Settings integration |
| [ANALYSIS_UI_SCALE_VIEWPORT_CLIPPING_V1.md](ANALYSIS_UI_SCALE_VIEWPORT_CLIPPING_V1.md) | Clipping + R2/R3 options |
| [ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md](ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md) | Browser zoom comparison |
| [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) | Engine UI architecture §2 |

---

## 7. Reset UI scale manually

Edit `%USERPROFILE%\.prefs\Futtuboru Preferences`:

```properties
settings.ui_scale=1.0
```

Or use Settings in-game. No savegame field is involved.
