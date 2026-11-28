# Recruitment, trials, hidden ratings and signing (R-01 design, BOOK3PRO, 2026-11-28)

Producer's ask: get new players (young, old, imported) the way clubs did then; have the candidates come and **show
them playing, running, shooting, dribbling at our ground in the match engine**; ratings hidden behind what our people
can judge; the public watching if that is historical; then take men on, with or without a contract.
Facts and confidence: [`docs/data/history/RECRUITMENT_1888-1900.md`](../data/history/RECRUITMENT_1888-1900.md)
(`R§n.m` below = its finding numbers). Rules: D7 (era-accurate), D17 (no real future scripted), D20/D22 (ECS engine),
D26 (art), DATA_MODEL (ids, sources). This doc is a design: no code yet.

## 1. What history gives us (the gameplay hooks)
- **No academies.** Young men come from church, Sunday-school, works and local "junior" clubs (R§1.7, §5.1). Schools
  football starts later (Sheffield 1889, London 1892, ESFA 1904: R§5.2). The youngest sourced debut in 1888-89 is 17
  (R§5.3).
- **The Scottish market** is the big one: men of about 21 from Ayrshire, Dunbartonshire and Edinburgh (R§1.3). It runs
  through secretaries' trips, agents, adverts in Scottish papers, and jobs found in England (R§1.2-1.5). It is risky:
  the SFA blacklists professionals until 1893 (R§3).
- **Poaching is open warfare.** "Evertonians visited Preston in disguise" with "a magnificent offer"; the press called
  it "the kidnapping business" (R§1.6). A man registered with one club needs the FA Council's consent to sign a second
  registration in the season (R§3, 1888).
- **Trials are public events.** August practice matches decided who was "retained" (R§2.1); 4,000 watched a Wednesday
  practice in 1888 (R§2.2); a 1889 First Team v Reserves trial took a £19 gate, and the papers judged the trialists in
  print (R§2.3).
- **Judges:** the secretary(-manager) recruits; the **committee** picks the team weekly; the **trainer** knows fitness
  (a former sprinter or pedestrian; rubs and examines the men); the **captain** teaches on the field (R§6). A named
  "scout" is not sourced before 1900; agents do the job (R§6.5).
- **Contracts are for one season.** About 45s a week in the season and 20s in summer for a good 1888 man; stars get a
  business set up; jobs are found at mills, pubs and works; amateurs take no wage (R§4, §1.8). Retain and transfer
  starts in 1893-94 and fees follow (£100 Groves 1893). The £4 maximum wage comes in 1901 (R§3).
- **Training** is athletics, not ball work: walks of 8-10 miles, sprints, skipping, rub-downs, special weeks at resorts
  before cup ties (R§6.6).

## 2. Analysis: fun, fair, beyond FM
- **What FM does:** attributes are masked by scouting knowledge (a % per nation or region, plus a scout's judging
  ability and potential); scout reports with ranges and stars; trials are a short contract with the player in the squad,
  and the player never sees the trial.
- **Where we go further, staying era-true:** (1) the trial is a **scene**: the candidates actually run, shoot and play
  in our ECS engine at our ground, and what each observer *saw* sets his judgement. (2) Judgement belongs to
  **people**: each observer has his own perceived range and his own bias (the committee man who prefers a local lad, the
  trainer who rates wind and legs), not a club-wide %. (3) The **public and the press** watch and judge too: a gate for
  a public trial, a newspaper paragraph that is another (biased) opinion. (4) **Poaching** and registration rules make
  signing a story, not a button.
- **Fair:** the truth is fixed when the candidate is generated; perception is deterministic (fixed error per observer,
  §4), so a reload cannot reroll a man. Ranges contain the truth **most of the time** (about 80%, tunable), so the
  player learns to trust his good judges.
- **Risks:** (a) **time cost**: watching every trial is too slow. Answer: scenes are short (a 2 × 20 min trial, drills of
  1-3 min at speed ×4), always skippable, and **skipping loses nothing** (§5.4). (b) **Pacing**: candidates cluster in
  the close season and August, which is historical; in-season, letters arrive at a low rate. (c) **Determinism**: the
  scene sim is seeded and headless-equal (D20 rule 4); the viewer never changes an outcome. (d) **Engine dependency**:
  trials need the ECS engine (v0.6); §8 has an interim option (Q4). (e) **History vs fun**: the 1885-89 six-mile
  residence rule (R§3) is real but fiddly; we model it simply (a "takes up residence" step) and drop it in 1889.

## 3. Candidate sources and pools
### 3.1 Source kinds (by era; `season` = when the kind is available)
| Kind | Who | Ages | Reaches us by | Available | Risk / cost |
|---|---|---|---|---|---|
| `local-junior` | church, Sunday-school, local clubs | 17-24 | letter from the man or his club secretary; committee tip; watching a junior cup tie | always | cheap; raw |
| `works-team` | mill, foundry, colliery, railway sides | 18-28 | committee tip (often an employer on the committee); letter | always | job already held; may refuse to leave the works |
| `scottish-import` | Ayrshire, Dunbartonshire, Glasgow, Edinburgh clubs | 19-26 | advert in a Scottish paper, secretary's trip, agent | 1885-; open after 1893 | travel cost; the SFA blacklists him until 1893; his club may resist; a "job found" is needed |
| `league-poach` | a man at another English club | 20-30 | approach in the close season (before 1893 he is free between seasons) | 1888-1893 | dispute, FA Council consent, retaining fees repaid (R§3); after 1893: retain and transfer, a fee |
| `released` | men not retained by their club in May | 22-32 | newspaper "not retained" lists, letters | always | known form; may be past his best |
| `amateur` | gentlemen, students, professional men (e.g. a doctor) | 20-30 | recommendation, letter | always | no wage, plays when free (cup ties); refuses a professional form |
| `veteran` | 30+ men still playing | 30-34 | letter, captain's tip | always | short horizon; may also serve as trainer later |
| `schoolboy` | a schools-league star | 15-16 | schoolmaster's letter | Sheffield from 1889, London 1892, wider from 1904 | not signable before 17 (game rule, see Q9); "keep an eye on" only |

### 3.2 Pool generation (deterministic)
- **Pools per region and season**, rebuilt in May and topped up monthly: `fork("recruit:pool:" + season + ":" + region)`.
  Regions follow the existing `region_modifiers.json` (Lancashire, Midlands, North East, Scotland west/east, Wales,
  London...).
- **Real people first:** a person in `people.json` with sources who is not in a League squad that season (e.g. a man
  who debuts in 1889-90 and whose origin club is sourced) enters the pool of his region with his sourced facts. Only
  his identity, origin and age come from the data; **his ability is generated** from his tier and position like every
  other ratings seed. He is never scripted to become what he became (D17 spirit).
- **Else generated:** name from `names.json` by region, origin club from a sourced list of period junior and works
  clubs per region (new data, R-02), age band and attribute profile per source kind (§3.1), a new `confidence: generated` value.
  Generated men get **runtime ids** from the `IdAllocator` (no `ids.json` entry). Real men keep their data id.
- **Truth:** `PlayerAttributeGenerator` makes the true attributes (3..23) plus a hidden **ceiling** and **development
  rate**, all on the candidate's own fork `fork("recruit:cand:" + id)`.

### 3.3 How a candidate reaches us
- **Inbox letter** (`MessageManager`, new category `RECRUITMENT`), in period voice: "Sir, I play centre-forward for
  St Mark's ... I should be glad of a trial." Or a secretary writes about his man. Letters can be answered with: invite
  to trial, invite to practice, decline, "will watch him play".
- **Newspaper notes** (later news screen; inbox until then): adverts we place ("WANTED, a first-class centre and right
  wing", R§1.4: costs money, brings Scottish letters a week later), "not retained" lists in May, gossip about other
  clubs' men.
- **Staff recommendations:** a committee man, the trainer or the captain names a man he knows (his network region and
  bias apply).
- **Agents** (Scotland; pre-1893 risky): pay a fee, get N names with the agent's (biased, self-interested) opinion.
- **Going to watch:** spend a day (and a train fare) to watch a junior or Scottish match; this gives exposure (§4)
  without any trial.

## 4. Hidden ratings and perception
### 4.1 Model
- The truth stays on the `Player`; the UI for a non-squad man never shows it.
- **Observer** = a person with judging skills: `judgePhysical`, `judgeTechnical`, `judgeMental`, `judgeKeeper` (3..23),
  `bias` (a small set: `local`, `pace`, `size`, `scots`, `flair`), `network` regions. Squad men and secretaries
  have them too (the captain judges on the field).
- **Knowledge** per (observer, candidate): exposure seconds per attribute group, gained from what the observer
  actually **saw** (scene events, §5) or heard (letters and press give a tiny amount plus the writer's bias).
- **Estimate** for attribute *a*: `est = true + bias(a) + z(a) · σ`, where `z(a)` is drawn **once** from
  `fork("perceive:" + observerId + ":" + candidateId + ":" + a)` and kept, and
  `σ = σ0 · (1 - skill/30) / sqrt(1 + exposure/τ(a))`. More exposure narrows the range around a fixed error, so a
  range never jumps around and a reload changes nothing.
- **Shown range** = `est ± 1.28 σ` (about 80% coverage), clamped to 3..23. Club view = the best judge per group, or a
  weighted consensus (Q6). Ceiling (potential) uses the same model with a large `σ0`, and only the trainer and captain
  narrow it (watching him train over weeks).
- **Error sources:** skill (σ), bias (a shift: the trainer adds +1 to pace for men he likes the look of; a committee
  man adds +1 overall to locals), the writer's interest (an agent inflates his men; the man's own letter inflates
  himself).

### 4.2 Presentation (period language)
- Default view: **words**, not numbers, per group and for the man: "a clever dribbler", "wants pace", "a rare kick",
  "plucky", "slow on the turn", "a fair back, no more", with the observer's name: "Trainer: fast, good wind".
  Optional numeric ranges (`11-15`) behind a toggle (Q5).
- A **trial card** per candidate: who saw him, how much, the words per group, a confidence bar that fills with
  exposure, the press view after a public trial, and the club consensus.
- New labels go through the language files (D18).

## 5. Trials as scenes
### 5.1 Scene types (what a session can contain)
| Scene | Engine content | Reveals most | Length (sim) |
|---|---|---|---|
| `TRIAL_MATCH` | 11 v 11 (or less), mixed sides: **First XI v Reserves** (sourced form, R§2.3) or two mixed sides; normal 1888 laws, referee + umpires from the committee; no table, no cup | everything, weighted by touches and duels | 2 × 20-45 min |
| `PRACTICE_GAME` | small sides, evening, at our ground | technical, teamwork | 20-30 min |
| `SPRINT` | 2-6 men race 100 yd (and 50 yd turn-and-back) | speed, acceleration, (stamina on repeats) | ~15 s each |
| `SHOOTING` | each man shoots N balls from set spots against our keeper | kicking, long shots, keeper skills of the keeper | ~2 min |
| `DRIBBLE_DUEL` | one man with the ball against one or two defenders toward goal (era "backing up" optional) | dribbling, dexterity, courage; defender's tackling | ~1 min |
| `HEADING` / `CROSSES` | balls lofted from the wing | heading, jumping, positioning | ~1 min |
| `ROAD_WALK` / `MILE_RUN` | off-pitch: not drawn, text only (walks of 8-10 miles, R§6.6) | stamina, endurance (trainer only) | — |
| `KEEPER_TRIAL` | shots and charges on a keeper (keeper charge legal in 1888) | keeper skills, courage | ~2 min |

### 5.2 A trial day
- **Booking:** a free day with no fixture (as for friendlies, U-08: at least a day's notice; candidates from afar need
  travel days, and the club pays fares); evening sessions in August, Saturdays only if no match.
- **Where:** our ground, or a hired field (cheaper, no gate). The scene uses that ground's pitch (from `grounds.json`;
  "training field" = a plain pitch with no stands).
- **Who watches:** the staff the player assigns (each observer gains exposure only if present), the **public** (gate
  only at our ground and only for a `TRIAL_MATCH`, and only from 1889 when it is sourced, R§2.3; before that a free
  crowd, Q7), and the **press** at a public trial.
- **Gate:** seeded attendance (`fork("gate:trial:" + id)`), small (a fraction of a league gate, calibrated to
  3,000-5,000 and £19 in 1889); the home club takes it (as P2-12).
- **Cost:** a day of the calendar; fares; an injury risk from the scene's own contact model (rare, as in a match).

### 5.3 Exposure from events
The scene emits the engine's `MatchEvent`s plus drill events (§6). An **ObservationAccumulator** (BOOK3PRO, outside
`match/ecs`) turns them into exposure per (observer, candidate, group): a touch, a sprint, a shot or a duel the
observer could see (present, and the event within his view distance in the scene). This is deterministic from the
event list.

### 5.4 Watch or skip
- **Skipping gives the same knowledge** as watching: exposure comes from who *in the game* attended, not from whether
  the human watched the viewer (determinism rule D20-4; no grind). Watching is for the eye and the fun.
- Skip = run the scene headless and show a **text report** in period style ("Smith ran the hundred in fine style and
  beat Brown by two yards; Jones shot well but wants pace") from the same events.
- The human can still **see** a moment (replay from seed, D24) from the report.

### 5.5 Interface contract for WORKSTATION (no BOOK3PRO edits to `match/ecs/**`)
What gameplay needs from the engine and viewer lanes. Names are proposals; WORKSTATION owns the shape.
1. **Scene entry point** in `match.ecs`: `SceneInput` → `SceneResult`, headless and seeded like `EcsMatchEngine`:
   ```java
   record SceneInput(SceneType type,            // TRIAL_MATCH, PRACTICE_GAME, SPRINT, SHOOTING, DRIBBLE_DUEL, HEADING, KEEPER_TRIAL
                     String forkName,           // "scene:" + sessionId + ":" + index, from game.random()
                     PitchConditions pitch,     // our ground or a plain training field
                     List<SceneParticipant> participants, // AthleteInputs + side (A/B/NONE) + role + Position
                     SceneParams params,        // duration ticks, reps, distances (yd), spots, sides size, laws on/off
                     int spectators) {}         // 0 = none; crowd sections sized from it
   record SceneResult(List<MatchEvent> events, List<DrillMeasure> measures, long finalStateHash, String engineVersion) {}
   record DrillMeasure(long participantId, String kind, double value) {}  // e.g. SPRINT_100YD_SECONDS 11.4, SHOT_ON_TARGET 1
   ```
   `TRIAL_MATCH` / `PRACTICE_GAME` = the match world with n v n, mixed sides and a `RulesProfile` with no table effect
   (laws as the season). Drills = the same world with **scripted intents** (run to a line at full effort; shoot from a
   spot; dribble past a defender to goal; head a lofted ball) instead of the match AI, and a stop condition.
2. **Events:** the existing V2/ECS `MatchEvent` types (touches, shots, saves, duels, charges, injuries), each with actor ids
   and position; plus `DRILL_START`, `DRILL_END`, the `DrillMeasure`s; and `participantId` values equal to our player
   runtime ids.
3. **Determinism:** same `SceneInput` → same `finalStateHash` headless or viewed, on all machines (as the match goldens);
   one golden per scene type.
4. **Viewer:** `MatchViewer` can show a `SceneInput` at a chosen ground: camera presets per scene (side-on V3 for
   matches; a fixed framing on the sprint lanes, the shooting goal or the duel), name labels over trialists (newspaper
   HUD style, D26), a sparse crowd and press table from `spectators`, a skip button and a speed control. A training
   field without stands = pitch P1 with no stands drawn.
5. **Not needed from WORKSTATION:** observers, exposure, perception, gate and money. These are BOOK3PRO, outside
   `match/ecs`.

## 6. Signing and registration
### 6.1 Offer types
| Offer | Fields | Era rules |
|---|---|---|
| **Amateur** (no contract) | expenses per match (fares, "loss of time" if the season allows), plays when free | always; refuses if asked to sign a professional form |
| **Professional, one season** | weekly wage (season), summer wage (or none), length = to 30 April, or to the next August with summer pay (Q8), signing-on present (from 1890s), **job found** (employer kind: mill, works, pub, shop; its own wage), "set up in business" for stars | from 20 Jul 1885 |
| **Retained** (from 1893-94) | the club keeps his registration at the end of the season; another club pays a fee | 1893- |

Wage bands come from data (`gamedata/core/recruitment/wages.json`, per season and per standing, sourced: 20-50s a week
in 1888, R§4.2), never typed into code.

### 6.2 Registration steps (rules data per season, `recruitment/rules.json`)
1. Sign → register with the FA (and county FA) and the League. Approval takes N days (data; the 7-day League rule from
   1890, R§3). He cannot play before approval.
2. **1885-1889 residence:** for a man not born within 6 miles, he must "take up residence" (an action plus a cost; the
   game counts days; an FA Cup tie needs the qualification). Dropped in 1889.
3. **Second registration in a season** (he played for another club this season) → needs FA Council consent: a dated
   decision letter, a chance of refusal, his old club may claim "retaining fees" repaid (£17 10s for Ross, R§3).
4. **Scots before 1893:** the SFA blacklists him; his old club may protest; he may refuse for fear of the ban.
5. **From 1893-94:** retain and transfer: a player on another club's retain list needs that club's consent and a fee.

### 6.3 Refusals, poaching and disputes
- A candidate weighs the offer: wage against his band, the job found, the town (near home, Scottish community), the
  club's standing, and a rival offer. The answer comes by letter in 1-3 days (seeded, as friendlies).
- **Rival clubs** (AI, R-08) make offers for the same man; our own men get approached in the close season
  ("visited in disguise"). A news item, a letter from our man asking for more, or a defection.
- **Disputes:** a man who signs two forms → FA inquiry: suspension, a fine, or the registration awarded to one club.

### 6.4 Data and saves (DATA_MODEL rules)
- **Data (sourced, new files listed in `pack.json`):** `recruitment/rules.json` (per season: residence, registration
  days, retain and transfer, max wage, signing-on cap, SFA ban), `recruitment/wages.json` (bands per season),
  `recruitment/origin_clubs.json` (period junior and works clubs per region; each a stable id **and an `ids.json`
  number via `assign_ids.py`**, with sources). Validator and pytest updated in the same PR.
- **Runtime (save, `saveVersion` 3, with a migration step):** `Candidate` (player runtime id, source kind, region, origin
  club id, how found, status), `Knowledge` (observer id, candidate id, exposure per group, fixed z per attribute;
  biases come from the observer), `TrialSession` (date, ground, scenes, observers, gate, result digest + fork names so
  the scenes replay), `Registration` (club, season, status, approval date, residence days), contract fields added to
  `PlayerContract` (`summerWage`, `jobFound` occupation id, `jobWage`, `expensesPerMatch`, `retained`).
  Generated candidates are runtime objects; unsigned ones are pruned at the season rollover (kept only if
  watched).

## 7. Staff roles
| Role | Era | Judges well | Bias | Gameplay |
|---|---|---|---|---|
| **Secretary(-manager)** (the player, usually) | 1885- | all, middling | his own | recruits, writes letters, travels, signs |
| **Committee men** (3-6) | 1885- | mental, "character"; local knowledge | `local`, some `size` | tips from their network (an employer brings works men); weekly team pick (later); vote on big wages (Q1) |
| **Trainer** | 1885- (ex-sprinter or pedestrian) | physical, fitness, ceiling over weeks of training | `pace` | runs drills; sees men at training daily |
| **Captain** (a player) | always | teamwork, positioning, technical on the field | `flair` | judges from inside a trial match (he plays) |
| **Agent** (Scotland) | 1880s-, open after 1893 | variable, self-interested | inflates his men | paid per name |
| **Scout** (a named role) | not sourced before 1900 (R§6.5) | technical, wide network | per person | unlocks later in the timeline; Q3 |
The existing `scout` and `agent` professions are reused; `trainer`, `secretary` and `committee-member` are new
professions (data ids, `ids.json`).

## 8. Slices (PR-sized)
| Slice | Content | Depends on | Machine |
|---|---|---|---|
| **R-02** | Data and research: `recruitment/rules.json`, `wages.json`, `origin_clubs.json` (sourced), new professions; validator and pytest; fill the research gaps (§7 of the note) | R-01 | BOOK3PRO (or WORKSTATION history lane) |
| **R-03** | Candidate generator + regional pools (real people first, else generated), `Candidate` in saves v3 | R-02 | BOOK3PRO |
| **R-04** | Observers, `Knowledge`, perception math, trial card UI with words (+ toggle for ranges), labels in the bundles | R-03, R-09 | BOOK3PRO |
| **R-05** | Sourcing: inbox letters, adverts, "not retained" lists, staff tips, agents, going to watch a match (exposure without a scene) | R-03, R-04 | BOOK3PRO |
| **R-06a** | **Scene API** in `match.ecs`: `SceneInput`/`SceneResult`, n v n trial match, drill intents and measures, goldens (§5.5) | E-15, E-17 | **WORKSTATION** |
| **R-06b** | Trial sessions: booking, observers present, gate, `ObservationAccumulator`, text report; headless | R-04, R-06a | BOOK3PRO |
| **R-06c** | Viewer: scene camera presets, trialist labels, training field, skip and speed | R-06a, E-21b..e | **WORKSTATION** |
| **R-07** | Signing + registration per season rules, refusals, `PlayerContract` fields, wages into finances, job found | R-02, R-03 | BOOK3PRO |
| **R-08** | AI clubs recruit from the same pools, poaching approaches, disputes, FA consent letters | R-07 | BOOK3PRO |
| **R-09** | Staff roles: trainer, committee, captain with judging skills; hire through the existing job system | R-02 | BOOK3PRO |

The order without the engine: R-02 → R-09 → R-03 → R-04 → R-05 → R-07 → R-08 gives recruitment by letters, watching
matches and signing; R-06 adds the scenes when the engine is ready (v0.6+).

## 9. Open questions for the producer

> **Answered 2026-11-28 (ROADMAP D28):** all eleven as recommended, plus Q12 origin nudges, Q13 foreign-born
> players with family backstories and **per-language speaking / understanding / literacy attributes** feeding
> cohesion, Q14 football-upbringing effects (not a per-country table), Q15 agents research and convert the era
> player sources. Slices gain **R-10** (era player sources research + conversion) and **R-11** (languages and
> literacy: data, attributes, cohesion effect, sourced literacy and migration rates).
1. **Who is the player at the club?** (a) the secretary(-manager), who recruits and signs alone; (b) the secretary,
   and the committee must approve wages above a band and votes on big signings; (c) the committee is only flavour.
   **Recommend (b)**: historical, and it gives the committee a voice.
2. **Real people as candidates?** (a) yes: sourced men not in a squad enter the pools with their real identity and a
   generated ability; (b) generated men only. **Recommend (a)**: it keeps the world historical, and D17 holds because
   their careers are not scripted.
3. **Scouts before 1900?** (a) no "scout" role before it is sourced; agents and staff trips only; (b) allow a scout
   from the start as a gameplay convenience. **Recommend (a)**, with a timeline event when the role is sourced.
4. **Trials before the ECS engine ships (v0.6)?** (a) wait: R-02..R-05 and R-07 ship first, with knowledge from
   letters, tips and watching matches (text); (b) an interim drill formula with text reports, replaced later. **Recommend
   (a)**: no throw-away sim (in the spirit of D22-4).
5. **Ranges in numbers?** (a) words only; (b) words by default plus a numeric-range toggle; (c) numbers. **Recommend (b).**
6. **Club view of a man:** (a) the best judge per group; (b) a weighted consensus (skill × exposure); (c) show each
   observer separately only. **Recommend (b)** in the summary, (c) on the trial card.
7. **Gate at trials before 1889** (no sourced admission charge for 1888, R§2.2): (a) free crowd only until 1889;
   (b) gate from 1888. **Recommend (a)**: follow the source.
8. **Contract end and summer pay:** (a) the season ends 30 April and summer pay is an option per contract (Holt:
   20s); (b) every professional is on summer pay. **Recommend (a).**
9. **Youngest signing age:** (a) 17 (the youngest sourced debut, R§5.3); (b) 16; (c) none. **Recommend (a)**, with
   schoolboys only "noted" until then.
10. **Time cost:** (a) one trial session a day, at most 3 in a week in season; (b) no cap. **Recommend (a)**: it
    keeps August busy and the season about matches.
11. **Poaching our own men:** (a) the AI may approach our men in the close season (pre-1893) with a news item and a
    chance to counter-offer; (b) never. **Recommend (a).**
