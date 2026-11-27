# History data proposal (H-01, for BOOK3PRO / producer approval)

Status: **proposal only** (WORKSTATION, 2026-11-27). Nothing here is in `validate.py`, `ids.json`, the Java loader or
the saves yet. BOOK3PRO leads gameplay (D21); the data side follows `docs/DATA_MODEL.md` (D11, D14, D15) and the
language-file rule of D18. The research it would carry is in `docs/data/history/` (club files, `PLAYERS_1888-89.md`).

## 1. What we want to store
| What | Example | Changes per season? | Language-dependent? |
|---|---|---|---|
| Club origin type | `cricket`, `church`, `works`, `school` | no | no (enum, label via bundle) |
| Club nicknames | "The Invincibles" (later), "The Lilywhites" | slowly (period-dated) | proper noun; translation optional |
| Club rivals | Preston ↔ Blackburn, Villa ↔ WBA | rarely | no |
| Kit colours | white shirts, navy knickerbockers | **yes** (Wolves, Villa, Stoke changed often) | no (structured) |
| Club history text | 2-4 paragraphs + one-line hook | no (per-season text separate) | **yes** |
| Season narrative | "1888-89: the Double, unbeaten" | per season | **yes** |
| Player nicknames | "Long Bob" (WBA goalkeeper Bob Roberts) | slowly | proper noun |
| Player bio | 1-3 sentences | can be extended per season | **yes** |
| Fan-love tier | 0-5, per club | **yes**, and evolves in play | no |
| Sources | URLs | with the fact | no |

## 2. Recommendation on text (D18)
D18 puts every **label** through the language files. Club histories and bios are **content**, multi-paragraph, and
there will be hundreds of them: `.properties` bundles (one line per key, escaped newlines, ISO-8859-1 by default) are a
poor home for them, and inline English in `clubs.json` / `people.json` would make the Spanish translation a data edit.

**Recommended: a per-language text file in the pack, keyed by data id.**
```
gamedata/core/text/en/history.json      English (the fallback for every language)
gamedata/core/text/es/history.json      later, a pure translation job (same keys)
```
```json
{
  "club": {
    "preston-north-end": {
      "hook": "The Invincibles: league and cup, unbeaten.",
      "history": ["paragraph 1", "paragraph 2"],
      "seasons": { "1888-89": "One-paragraph season narrative." },
      "sources": ["https://en.wikipedia.org/wiki/1888%E2%80%9389_Preston_North_End_F.C._season"]
    }
  },
  "person": {
    "john-goodall-1863": {
      "bio": "Centre-forward ...",
      "sources": ["https://en.wikipedia.org/wiki/John_Goodall"]
    }
  }
}
```
- Keys are the stable data ids (D11), so a renamed record is followed through `aliases.json` like any reference.
- `validate.py`: listed in `pack.json` like every file; unknown fields are errors; every key must resolve to a record
  of that kind; `sources` required on each entry; a non-English file may only hold keys that exist in `en` (warning
  for missing translations, error for orphans).
- The structured facts (enums, tiers, colours, rivals) stay in the data records; only prose goes to `text/`.
- Alternative rejected: `history` / `bio` string fields inline in `clubs.json` / `people.json`. Simpler now, but
  breaks D18's "Spanish is a pure translation job" and bloats the files the loader parses for every game.

## 3. Fields for the data records
New fields are optional (old packs and mods stay valid). Types use the validator's existing vocabulary
(`str`, `int`, `strs`, `ref:`, `refs:`, `enum:`, `list:`) plus two new types marked **new**.

### `clubs.json` (permanent identity)
| Field | Type | Meaning |
|---|---|---|
| `origin` | `enum:club_origin` | `church`, `cricket`, `works`, `school`, `old-boys`, `rugby`, `merger`, `other` |
| `nicknames` | `list:nickname` | `{ "name", "from"?: int year, "to"?: int year, "period"?: bool, "sources" }`; `period: true` = sourced as used at the time, else a later name. The game shows only period names in-season. |
| `rivals` | `refs:club` | local / era rivals; symmetric by convention (validator warns if one-sided) |
| `nameHistory` | `list:club_name` | `{ "name", "from", "to"?, "sources" }` for names that differ by season (The Wednesday, Small Heath, Walsall Town Swifts, Linfield Athletic). Closes the open item in `REVIEW_1888-89_FINDINGS.md`. |

### `seasons/<season>/squads.json` (per club, per season)
| Field | Type | Meaning |
|---|---|---|
| `kit` | `list:kit` | `[{ "use": "home"|"change", "shirt": colour, "shirtPattern": enum, "shirtSecond"?: colour, "shorts": colour, "socks": colour, "sources", "confidence" }]`. `colour` = one of a fixed named palette (`white`, `navy`, `claret`, `light-blue`, `old-gold`, ...) mapped to RGB in code, so generated kit art (D20/D21) stays consistent. `shirtPattern`: `plain`, `halves`, `quarters`, `stripes`, `hoops`, `sash`. |
| `fanTiers` | **new** `tiers:person` | `{ "person-id": 0-5 }`, the club's fans' tier for that person **at the start of the season**. Every key must be in `players` (error otherwise). Missing = derived default (see §4). |

Colours go in the season squad, not the club, because kits changed often in this era (several 1888-89 clubs wore
different colours a few years later; see the club files). H-03 (era kit) owns boots/shirt materials; this proposal
only owns colours and patterns; agree one `kit` shape with H-03 before implementing.

### `people.json` (permanent)
| Field | Type | Meaning |
|---|---|---|
| `nicknames` | `list:nickname` | same shape as clubs; e.g. "Ironsides" (Ezra Horton), "Little Demon" (Jimmy Ross) |
| `knownAs` | `str` | display name where it differs ("Archie Hunter" for Archibald) |

The fan tier is **not** on the person: fan love is per club (Nick Ross was a Preston idol and an Everton captain;
Archie Goodall moved Preston → Villa in October 1888). A national reputation could later be a separate per-season
`renown` 0-5 on the squad entry or a season file; not proposed now.

### Not stored
`legacyTier` (the career ceiling in `PLAYERS_1888-89.md`) is a research/design note for balancing, not game data:
the game must earn it in play (D17 spirit: history informs, never scripts).

## 4. Fan-love tiers: scale and gameplay hooks (suggestion for BOOK3PRO)
| Tier | Name | Data criteria (start value) |
|---|---|---|
| 0 | Unknown | name only, no source beyond a line-up |
| 1 | Squad player | sourced appearances, nothing notable |
| 2 | Local favourite | regular starter, several seasons at the club, local-born mainstay, useful goals |
| 3 | Crowd idol | key man: captain, top scorer, a cap while at the club, sourced nickname/popularity |
| 4 | Club hero | central in the club's trophies/greatest era, several caps, named among the period's greats |
| 5 | Legend | national star of the era, still among the club's greatest ever |

Default when `fanTiers` has no entry: 1 for a real person with sources, 0 for `confidence: placeholder`.

How a tier could evolve in play (runtime value in the save, per club-person pair, starting from the data):
- **Up:** a run of goals/clean sheets, scoring in a derby or cup tie, winning the league or the FA Cup (big jump for
  the scorer/captain), being capped, long service (e.g. +1 chance after each full season as a regular, capped at 4
  without a trophy), being made captain.
- **Down / drift:** long absence from the XI, poor form, a move to a rival (fans of the old club drop him; the new
  club starts him at their own lower value), age decline. Tier 5 decays only very slowly ("legends stay legends").
- **Effects (ideas):** attendance and gate bonus when tier 4-5 players play; morale/press reaction to selling or
  dropping a crowd idol; board pressure; news items written from the bio text; a hall of fame per club (tier 5 reached
  in play). Tier should never change match physics directly (keep D17/D20 clean); it acts on the crowd, money and morale.
- Use hysteresis (a points meter per pair, tier = thresholds) so it doesn't flip every week.

## 5. Implementation order (when approved)
1. BOOK3PRO/producer approve or amend §2-§4 (ask in `docs/HANDOFF.md` log).
2. One lane: validator specs + tests (`nickname`, `kit`, `club_name` record specs; `club_origin` enum; `tiers:person`
   type; `text/<lang>/history.json` file kind), `pack.json`, `DATA_MODEL.md` section. No `ids.json` change: no new
   record kinds with runtime numbers (nicknames, kits and text entries are values, not records).
3. Fill the data from `docs/data/history/` (12 league clubs first).
4. Java: `GameData` reads the new optional fields and the text file (UTF-8, `en` fallback); saves store the runtime
   fan meter per club-person pair (a `saveVersion` step).
