# Era events: data proposal (for BOOK3PRO / producer approval)

_Lane H-03, 2026-11-27. Status: **proposal**. Nothing in `validate.py`, `ids.json`, packs or Java is changed.
Research behind it: `docs/data/history/ERA_TIMELINE.md` and `BALL_AND_KIT_PHYSICS.md`.
Decisions served: D7 (era rules), D14 (all data in JSON, strict validator), D11 (stable ids), D18 (all text via
language files), D19-11 (each season's sourced rule), D20 (era physics and kit as data, announced by authorities)._

## 1. Idea
One JSON file of **dated era events**. Each event has (a) when it is **announced** and when it **takes effect**,
(b) **effects** as engine/rules parameters, (c) a **message** (inbox) whose text lives in the language files.
The game keeps a resolved **era profile** (all parameters) per date: start from a baseline (1888-89) and apply
every event whose `effective` date has passed, in date order. The match engine reads only the profile (today's
`RulesProfile.season1888()` in MATCH_ENGINE_V2 §3 becomes `EraProfile.at(date)`), never the events themselves.

Real history drives the events, like the real calendar drives the founding of the League. That is consistent with
D17: era events change the **rules and equipment**, never the result of a match the player plays.

## 2. Where it lives
```
gamedata/core/era/
  baseline.json      the 1888-89 profile: every parameter with its default (one object, see §4)
  events.json        the dated events (this proposal)
```
Both listed in `pack.json` `files`. Mods add events with `author.slug` ids, same rules as other packs.
Events are not runtime objects that saves point at by number, so **no `ids.json` entry is needed** (saves store the
ids of events already announced/applied as strings). Open for BOOK3PRO: confirm or give them numbers anyway.

## 3. Event schema (JSON Schema draft 2020-12, informal where obvious)
```json
{
  "$id": "futtoboru/era-events.schema.json",
  "type": "array",
  "items": {
    "type": "object",
    "additionalProperties": false,
    "required": ["id", "kind", "authority", "announced", "effective", "effects", "message", "confidence", "sources"],
    "properties": {
      "id":         { "type": "string", "pattern": "^[a-z0-9]+(-[a-z0-9]+)*$", "description": "stable slug, never changed (D11)" },
      "kind":       { "enum": ["rule", "equipment", "ball", "authority"] },
      "authority":  { "enum": ["ifab", "fa", "football-league", "press"], "description": "who announces; later an authority data id" },
      "announced":  { "type": "string", "format": "date", "description": "inbox delivery date (e.g. IFAB AGM in June)" },
      "effective":  { "type": "string", "format": "date", "description": "when the effects apply (usually 1 Sept of the next season)" },
      "dateCertainty": { "enum": ["day", "month", "year", "season"], "default": "day", "description": "how exact the real date is; year => game uses a plausible day" },
      "scope": {
        "type": "object", "additionalProperties": false,
        "properties": {
          "countries":    { "type": "array", "items": { "type": "string" }, "description": "country ids; empty = everywhere the authority rules" },
          "competitions": { "type": "array", "items": { "type": "string" }, "description": "competition ids; empty = all" }
        }
      },
      "effects": {
        "type": "array",
        "items": {
          "type": "object", "additionalProperties": false,
          "required": ["param", "op", "value"],
          "properties": {
            "param": { "type": "string", "description": "dotted parameter path that must exist in baseline.json" },
            "op":    { "enum": ["set", "scale", "add"] },
            "value": { "type": ["number", "boolean", "string"] },
            "adoption": {
              "type": "object", "additionalProperties": false,
              "description": "optional: gradual spread (equipment); value reached linearly over N seasons",
              "properties": { "seasons": { "type": "integer", "minimum": 1 }, "from": { "type": "number" } }
            }
          }
        }
      },
      "message": {
        "type": "object", "additionalProperties": false,
        "required": ["key"],
        "properties": {
          "key":       { "type": "string", "description": "language-file key prefix: <key>.title, <key>.body (D18)" },
          "args":      { "type": "array", "items": { "type": "string" }, "description": "values for {0},{1}.. in the text, e.g. dates, numbers" },
          "category":  { "enum": ["AUTHORITY", "LEAGUE", "SYSTEM"], "default": "AUTHORITY" },
          "priority":  { "enum": ["LOW", "NORMAL", "HIGH"], "default": "NORMAL" },
          "mandatory": { "type": "boolean", "default": false, "description": "blocks time until read (big law changes)" },
          "newsOnly":  { "type": "boolean", "default": false, "description": "press items: news feed, not an official circular" }
        }
      },
      "requires":   { "type": "array", "items": { "type": "string" }, "description": "event ids that must already apply (e.g. linesmen needs referee-on-field)" },
      "confidence": { "enum": ["verified", "unverified", "disputed"], "description": "same vocabulary idea as people (D11); disputed = sources conflict" },
      "sources":    { "type": "array", "minItems": 1, "items": { "type": "string", "format": "uri" } },
      "notes":      { "type": "string" }
    }
  }
}
```
Validator rules (to add in `validate.py` by its owner, not here): unknown fields are errors (D14); `param` must
exist in `baseline.json` and the value type must match; `effective >= announced`; ids unique; every `message.key`
has `.title` and `.body` in `BaseBundle_en_uk.properties` (and later the other bundles); `requires` ids exist and
take effect no later.

## 4. Parameters (baseline.json, 1888-89 values)
Grouped by owner. Rules come from MATCH_ENGINE_V2 §1/§3, physics from `BALL_AND_KIT_PHYSICS.md` §3.
```json
{
  "rules":  { "offsideOpponents": 3, "offsideOwnHalf": true, "offsideFromThrowIn": true, "penaltyKick": false,
              "goalNets": false, "keeperHandling": "OWN_HALF", "keeperHandlingPurpose": "DEFENCE",
              "keeperChargeProtected": false, "keeperSteps": 2, "officials": "UMPIRES_AND_REFEREE",
              "refereeActsWithoutAppeal": false, "directFreeKicks": false, "advantage": false,
              "goalFromCorner": false, "goalFromGoalKick": true, "maxSubstitutes": 0, "minutes": 90, "addedTime": false },
  "ball":   { "radiusM": 0.111, "massDryG": 400, "massMinG": 0, "massMaxG": 0, "laced": true,
              "waterUptakeMax": 0.56, "uptakeTauMin": 39, "restitution": 0.80, "wetRestitutionScale": 0.875,
              "laceStrikeShare": 0.125 },
  "kit":    { "bootMassG": 500, "bootWetGain": 0.30, "toecap": true, "studMaxIn": 0.5, "shinPadShare": 0.6,
              "shinPadInjuryScale": 0.6, "shirtWetMassKg": 1.0, "kneesCovered": true, "keeperDistinct": false,
              "numbers": false },
  "league": { "pointsWin": 2, "pointsDraw": 1, "tieBreak": "GOAL_AVERAGE", "maxWagePerWeek": 0,
              "retainAndTransfer": false, "transferFeeCap": 0, "promotion": "NONE", "reElectionBottom": 4 }
}
```
(`massMinG/massMaxG = 0` means "no rule"; `maxWagePerWeek = 0` means no cap.) Values marked D in the physics doc
are calibration inputs.

## 5. Example events (first entries of events.json)
```json
[
  {
    "id": "ifab-1889-ball-weight",
    "kind": "ball", "authority": "ifab",
    "announced": "1889-06-03", "effective": "1889-09-01", "dateCertainty": "year",
    "effects": [ { "param": "ball.massMinG", "op": "set", "value": 369 },
                 { "param": "ball.massMaxG", "op": "set", "value": 425 } ],
    "message": { "key": "era.ifab_1889_ball_weight", "args": ["13", "15"] },
    "confidence": "verified",
    "sources": [ "https://nachspielzeiten.de/law-2-the-ball-en/", "https://en.wikisource.org/wiki/Laws_of_the_Game_(1890)" ],
    "notes": "1890 text says international matches; applied to all matches here (open question)."
  },
  {
    "id": "ifab-1891-penalty-kick",
    "kind": "rule", "authority": "ifab",
    "announced": "1891-06-02", "effective": "1891-09-01",
    "effects": [ { "param": "rules.penaltyKick", "op": "set", "value": true } ],
    "message": { "key": "era.ifab_1891_penalty_kick", "args": ["12"], "priority": "HIGH", "mandatory": true },
    "confidence": "verified",
    "sources": [ "https://en.wikipedia.org/wiki/Penalty_kick_(association_football)" ]
  },
  {
    "id": "ifab-1891-referee-linesmen",
    "kind": "rule", "authority": "ifab",
    "announced": "1891-06-02", "effective": "1891-09-01",
    "effects": [ { "param": "rules.officials", "op": "set", "value": "REFEREE_AND_LINESMEN" },
                 { "param": "rules.refereeActsWithoutAppeal", "op": "set", "value": true } ],
    "message": { "key": "era.ifab_1891_referee_linesmen", "priority": "HIGH", "mandatory": true },
    "confidence": "verified",
    "sources": [ "https://nachspielzeiten.de/law-5-the-referee-en/" ]
  },
  {
    "id": "league-1891-goal-nets",
    "kind": "equipment", "authority": "football-league",
    "announced": "1891-08-01", "effective": "1891-09-01", "dateCertainty": "season",
    "scope": { "competitions": ["football-league"] },
    "effects": [ { "param": "rules.goalNets", "op": "set", "value": true } ],
    "message": { "key": "era.league_1891_goal_nets" },
    "confidence": "verified",
    "sources": [ "https://en.wikipedia.org/wiki/John_Alexander_Brodie" ]
  },
  {
    "id": "press-1874-widdowson-shin-pads",
    "kind": "equipment", "authority": "press",
    "announced": "1874-01-01", "effective": "1874-01-01", "dateCertainty": "year",
    "effects": [ { "param": "kit.shinPadShare", "op": "set", "value": 0.9, "adoption": { "seasons": 15, "from": 0.0 } } ],
    "message": { "key": "era.press_1874_shin_pads", "newsOnly": true },
    "confidence": "verified",
    "sources": [ "https://en.wikipedia.org/wiki/Shin_guard" ],
    "notes": "Before the start date: already folded into baseline (0.6 in 1888-89); kept for history screens."
  },
  {
    "id": "league-1901-maximum-wage",
    "kind": "authority", "authority": "football-league",
    "announced": "1901-04-01", "effective": "1901-09-01", "dateCertainty": "year",
    "effects": [ { "param": "league.maxWagePerWeek", "op": "set", "value": 4 } ],
    "message": { "key": "era.league_1901_maximum_wage", "args": ["£4"], "priority": "HIGH" },
    "confidence": "verified",
    "sources": [ "https://en.wikipedia.org/wiki/Retain_and_transfer_system" ]
  },
  {
    "id": "ifab-1925-offside-two",
    "kind": "rule", "authority": "ifab",
    "announced": "1925-06-01", "effective": "1925-09-01", "dateCertainty": "month",
    "effects": [ { "param": "rules.offsideOpponents", "op": "set", "value": 2 } ],
    "message": { "key": "era.ifab_1925_offside_two", "priority": "HIGH", "mandatory": true },
    "confidence": "verified",
    "sources": [ "https://en.wikipedia.org/wiki/Laws_of_the_Game_(association_football)" ]
  }
]
```

## 6. Message templates (language files, D18)
Keys go in `mods/language/BaseBundle_en_uk.properties` (libGDX `I18NBundle`, `{0}` placeholders), translated
later in the other bundles. Tone: a Victorian circular or newspaper paragraph. Examples:
```properties
# ERA EVENTS (H-03 proposal)
era.ifab_1889_ball_weight.title=The Board fixes the weight of the ball
era.ifab_1889_ball_weight.body=At its annual meeting the International Football Association Board resolved that the ball shall weigh from {0} to {1} ounces at the start of the game. The circumference remains 27 to 28 inches.
era.ifab_1891_penalty_kick.title=New law: the penalty kick
era.ifab_1891_penalty_kick.body=From next season, wilful tripping, holding or handling by a defender within {0} yards of his own goal line shall be punished by a penalty kick, taken from the twelve-yard line with only the goalkeeper to beat.
era.ifab_1891_referee_linesmen.title=The referee takes the field
era.ifab_1891_referee_linesmen.body=The Board has resolved that the referee shall control the game from the field of play and decide all disputed points without waiting for an appeal. The two umpires are replaced by linesmen, who shall signal when the ball is out of play.
era.league_1891_goal_nets.title=Nets to be fitted to the goals
era.league_1891_goal_nets.body=League clubs are to fit Mr Brodie's goal nets this season, so that there may be no more dispute whether the ball passed between the posts.
era.press_1874_shin_pads.title=A Forest man's novelty
era.press_1874_shin_pads.body=Mr S. W. Widdowson of Nottingham Forest has taken the field with a pair of cut-down cricket pads strapped outside his stockings. The spectators were much amused.
era.league_1901_maximum_wage.title=League sets a maximum wage
era.league_1901_maximum_wage.body=The Football League has resolved that no club shall pay a player more than {0} a week. Existing agreements above this sum must be revised.
era.ifab_1925_offside_two.title=The offside law is changed
era.ifab_1925_offside_two.body=A player will now be on side if two opponents, instead of three, are nearer their own goal line when the ball is played.
```
Wiring (for the code owner, not done here): an `EraEventScheduler` reads `events.json` at new game, and for every
event with `announced` after the start date calls `MessageManager.scheduleMessage(...)` with
`category = AUTHORITY` (the enum already exists and is documented "Rule changes" but is unused today),
`messageType = "ERA_EVENT"`, `scheduledDate = announced`, `actionData = event id`, title/body from the bundle,
`isMandatory` from the event. On `effective`, the scheduler updates the saved `EraProfile`. Events dated before the
start date are folded into the baseline and shown on a history screen (U-07), not sent.
`Authority` today is code-only (`ScriptsManager`); the `authority` enum above can later become an authority data id.

## 7. Decisions asked (BOOK3PRO / producer)
1. Approve `gamedata/core/era/{baseline,events}.json` and the schema (strict, D14).
2. No `ids.json` numbers for events (saves keep string ids)? Or allocate like other records?
3. Mandatory messages only for the two giant law changes (1891 package, 1925 offside), or for every Laws change?
4. 1889 ball weight: apply to all matches (proposal) although the text says internationals?
5. `adoption` curves for gradual equipment (shin pads, shorter knickers after 1904, lighter boots): keep, or model
   per player later?
6. Should the player's club be able to deviate (e.g. keep heavy boots) or is kit always the era profile?
