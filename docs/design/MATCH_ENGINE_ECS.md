# Match engine ECS: physics design (D20)

_Started 2026-11-27 (WORKSTATION, `ws/E-design-ecs`). Decisions D5, D6, D7, D17, D19, **D20**; Phase 4 in
`docs/ROADMAP.md`._ Status: **draft for producer review**; open questions in section 9. Spike:
`match/ecs/ball/BallPhysicsTest` and `match/ecs/MatchWorldTest` (E-10/E-11; the spike was removed).

Goal: one headless, seeded, fixed-step, testable Ashley simulation in which players, referee, umpires, ball and
crowd are entities, and the 1888-89 match (scoreline, every event, minute ticker, period report) is **read off the
physics**, not rolled from dice.

## 0. What D20 changes in `MATCH_ENGINE_V2.md`

| V2 section | Status |
|---|---|
| §1 Laws in 1888-89 (with sources) | **Kept as is.** It is the rules spec of section 4 below. |
| §2 Possession/duel model | **Superseded** by sections 2-4 (physics + agents). Its unit/attribute tables survive as inputs to the AI's skill noise (4.1). |
| §3 Data contracts (`MatchInput`, `TeamSheet`, `RulesProfile`, `MatchEvent`, `MatchResult`, `MatchEngine`) | **Kept.** The ECS engine is one implementation of `MatchEngine.play(input, rng)`. Additions in 2.4 and 5. |
| §4 Where it plugs in (`MatchSimulator` → `MatchResultApplier` → `MatchResultRecorder`) | **Kept** (section 5). |
| §5 Report examples, §7 rules invariants and determinism tests | **Kept**; physics adds its own tests (section 7). |
| §6 Calibration | **Kept**, extended in section 7. |
| §8 Slicing E-01..E-06 | E-01, E-04, E-05 stay; **E-02/E-03 become E-10..E-21** (section 8). |
| §9 answers (D19) | All still apply; how each one is realised physically is in sections 3-5. |

---

## 1. What we reuse

### 1.1 From the DarkBlade engine (patterns only, described in our words)
DarkBlade (named in D11/D12/D20) is another libGDX/Ashley project; its graphics design and performance review
taught these lessons. We take the ideas, not code or details.

| Pattern | What it means for Futtoboru |
|---|---|
| **Two kinds of systems, marked by type** | Every Ashley system is either a *simulation* system (fixed step, writes game state, counted by the hash) or a *draw pass* (implements a marker interface, runs once per frame, never writes simulation components). "Is this a drawing system?" is `instanceof DrawPass`, never a hand-kept list (a hand list once let a draw pass run inside the sim loop N times per frame). |
| **Drawing never writes game state; a state hash proves it** | A `MatchStateHash` over sorted simulation state (exact double bits). Test: the same match run headless and with every draw pass installed gives the same hash at every tick. Also a "describe" mode that prints the hash inputs line by line so a divergence can be diffed. |
| **Cosmetic randomness is separate** | Draw passes use their own unseeded/cosmetic random (crowd flicker, dust); the match fork is only for the simulation. A particle must never draw from the match dice. |
| **Fixed-step clock with a cap** | Simulation steps are a fixed size; game speed multiplies steps, never the step size; leftover frame time carries over; a frame has a step cap and a time budget (the viewer slows instead of stuttering). Headless runs ignore the clock and just loop. |
| **One registration point** | `MatchSystems.installSimulation(engine, ctx)` and `MatchViewer.installDrawPasses(engine, gfx)`; parallel lanes each add a line there, not in a screen class. Explicit priorities from one constants table, not registration order. |
| **Instant things are events** | Sim systems publish typed events on a signal (goal, kick, bounce, appeal); draw passes and the ticker listen. The sim never creates visual entities. |
| **Thousands of tiny things are arrays, not entities** | Crowd spectators, particles, footprints in mud: arrays inside one system; only their owners (crowd *sections*) are entities. Entity-plus-physics-body per unit was measured several times slower and heavier than plain arrays. |
| **Effects as data** | A `match_vfx.json` (later) maps event types to visuals (mud splash on bounce, dust on a frozen pitch, flag waving on appeal). Code-generated art, safe for this public repo (D21). |
| **Performance knobs are data, measured by tests** | Tick rates, think intervals, caps live in JSON; every knob has a metric (`nsPerTick`, `msPerMatch`, allocations per tick) in a benchmark. Zero allocation per tick is a goal (per-frame garbage shows up as GC pauses). |
| **GL-free tests** | Draw passes are tested with a no-op GL stub and the headless backend; sim systems need no Gdx at all. |
| **Lessons on Box2D** | Per-body JNI reads every step become a large cost as body counts grow. We have only ~25 bodies, so the decisive reason to drop it is another one (3.1: cross-machine bits). |

### 1.2 From our prototype (`match/engine/**`, `MatchEngineScreen`, `MatchEngineDebugScreen`)
| Keep | Drop / fix |
|---|---|
| Ashley `PooledEngine` + `Poolable` components; mapper style | `PhysicsSystem`: **static** accumulator shared by all instances, steps driven by the frame delta, float. Replaced by a fixed-step sim loop. |
| Component split idea: transform / player / ball / goal-intent | Box2D bodies for players and ball (2D only, native, see 3.1). |
| `GoalComponent` intent types (chase ball, mark, support, formation slot) → seed of the AI intents (4.1) | `CollisionAvoidanceSystem` (lazy `BlendedSteering` per entity, unfinished raycast avoidance), `LocationAdapter` (gdx-ai float adapters). Own small double steering instead. |
| `RenderingSystem`, field drawing, PPM camera → become draw passes of the viewer (E-21) | `new Vector2()` per entity per tick (garbage). |
| `MatchEngineDebugScreen` options (weather, pitch, crowd, condition) → the viewer's debug panel feeding `Conditions` | `MatchConfiguration` strings → typed `Conditions` (ground, weather, temperature) |
| `MatchEngineVersion` idea → an engine version **saved with each match** | "DarkBlade-v1" architecture label; build-info coupling. |

`RandomSource` (P2-0) and `MatchResultRecorder` (P2-2) are used unchanged.

---

## 2. Architecture

```
MatchInput (V2 §3) + rng = game.random().fork("match:" + matchKey)
  └─ MatchWorld (one Ashley Engine per match, no Gdx, no statics)
       simulation systems, fixed order, one engine.update() = one tick:
         10 InputCommandSystem     (scheduled tactical commands; empty in v0.6)
         20 PerceptionSystem       (who sees what, with noise)          ┐ AI runs at think rate,
         30 DecisionSystem         (intents per player, utility scores) ┘ staggered by player index
         40 OfficialsSystem        (umpires/referee move, watch, judge appeals)
         50 LocomotionSystem       (bodies: accel/turn/speed limits, stamina)
         60 ActionSystem           (kicks, headers, touches, charges, keeper handling → impulses)
         70 BallSystem             (2.5D flight/bounce/roll, substeps, wet mass)
         80 ContactSystem          (body-body, body-ball, posts/bar, swept)
         90 LawsSystem             (out of play, goal / disputed, offside snapshot, fouls, restarts)
        100 CrowdSystem            (sections: mood, encroachment, noise)
        110 EventDetectionSystem   (physics transitions → MatchEvent stream)
        120 ClockSystem            (match minute, half-time, full time)
       draw passes (viewer only): interpolation, pitch, players, ball+shadow, officials, crowd, vfx, debug overlay
  └─ MatchResult (events, stats, sides at full time, seed, engine version, final state hash)
```

- **Package** `com.rndmodgames.futtoboru.match.ecs` (sim, no `com.badlogic.gdx.graphics` imports, enforced by a
  guard test like `LoggingStyleGuardTest`) and `match.ecs.view` (draw passes). The old `match.engine` is deleted at
  E-21.
- **Fixed step.** Recommended **30 Hz** for bodies, contacts and laws, **ball integrated in 2 substeps (60 Hz)**,
  AI thinking at **10 Hz** (each player every 3rd tick, offset by index so the cost is flat). A 90-minute match is
  162,000 ticks. 60 Hz everywhere is the fallback if 30 Hz feels coarse in the viewer (cost x2, section 6).
- **Headless first.** `MatchEngine.play` loops ticks with no clock. The viewer (E-21) runs the *same* `MatchWorld`
  with a fixed-step clock and interpolates between the last two sim states for drawing.

### 2.1 Determinism strategy
1. **Seeded only.** One fork per match (`match:<key>`, already used by `MatchSimulator`); sub-forks per purpose
   (`fork("weather")`, `fork("ai:" + playerId)`, `fork("officials")`) so adding a draw in one system does not shift
   another's sequence. No `Math.random`, no `new Random`, no `System.nanoTime`/wall clock in sim code (guard test).
   No JDK generator anywhere in game code (`JdkRandomnessGuardTest`): `RandomSource` is our own SplitMix64 with a
   Marsaglia-polar Gaussian (`StrictMath.log`), because `SplittableRandom.nextGaussian` differs between JDK releases.
2. **Arithmetic.** Java 17 floating point is strict everywhere (JEP 306), so `+ - * /` and `Math.sqrt` give the same
   bits on every JVM. **Transcendentals use `StrictMath`** (`exp`, `sin`, `cos`, `atan2`, `pow`): `Math.*` may use
   CPU intrinsics that differ between x64 and arm64 (BOOK3PRO vs WORKSTATION vs CI). Sim state is `double`; floats
   only in draw passes. No `Math.fma`, no parallel streams in sim code.
3. **Order.** Entities are iterated in creation order (Ashley `ImmutableArray`), created in a fixed order (home
   1..11, away 1..11, ball, officials, crowd sections). No `HashMap`/`HashSet` iteration in sim code
   (`LinkedHashMap` or arrays). Ties broken by entity index.
4. **No hidden inputs.** Everything the sim reads is in `MatchInput` (+ `EngineParams` + physics params + kit by
   season). The viewer's speed, camera and whether anyone watches never reach the sim.
5. **Tests.** Per-tick `MatchStateHash`; same input → same hash sequence; **golden hash** pinned in a test that
   runs on both machines and CI (`BallPhysicsTest.GOLDEN`, `MatchWorldTest.GOLDEN_PLAY_DRY/RAIN`; equal on Windows and Linux, Jenkins #88); headless vs
   with-draw-passes identical; shuffled match order in a day identical.

### 2.2 Contracts added to V2 §3
```java
record PhysicsParams(BallParams ball, Map<Surface, SurfaceParams> surfaces, BodyParams body, double tickHz,
        int ballSubsteps, int thinkEveryTicks) {}
record BallParams(double dryMassKg, double radiusM, double dragCoefficient, double magnusCoefficient,
        double maxWaterShare, double soakTauSeconds, double wetRestitutionLoss, double laceCutRiskPerHeader) {}
record SurfaceParams(double restitution, double bounceKeep, double rolling, double grip, double fatigueFactor) {}
record KitProfile(double bootMassKg, double studGrip, boolean caps, boolean shinPads, double kitWaterUptake) {}
// RulesProfile (V2) gains: offsideFromCorner=false, offsideFromGoalKick=false, umpireDeployment, refereeOnField=false
// Conditions (V2) gains: wetness 0..1 over time (rain schedule from the weather fork), pitch size per ground
```
All of them are data (D14): `gamedata/core/engine/physics.json`, `kit/<season>.json`, from lane T's research.

---

## 3. Physics model (2.5D)

### 3.1 Backend: custom pure Java, not Box2D (recommendation, Q1)
- **Bits across machines.** Box2D in libGDX is native C++ behind JNI, compiled separately per platform; its float
  results are not guaranteed identical on Windows x64, macOS arm64 and Linux CI. Our saves and replays rely on
  "same seed, same match" on both machines (D8). Pure Java doubles + `StrictMath` are. The spike pins one golden
  hash; BOOK3PRO running the same test is the proof.
- **The ball needs height.** Box2D is 2D; lofted passes, crosses, headers, bar/over decisions need `z` anyway.
- **Scale is tiny.** 22 players + ball + 3 officials: circle-circle contacts and a handful of static shapes
  (posts, bar, corner flags) are a few dozen lines, with swept tests for the fast ball.
- **No natives in tests**; no world-step side effects.
- Spike result (WORKSTATION, JIT warm): ball + 22 naive seekers with O(n²) separation through Ashley mappers:
  **~4.1 µs per tick**, i.e. 1.3 s per 90 minutes at 60 Hz, 0.65 s at 30 Hz, before any optimisation.

### 3.2 Ball
State: position (x, y, z), velocity, spin (vertical axis for curl; topspin later), water (kg), contact state
(air / bouncing / rolling / held).
- **Flight:** gravity; quadratic drag `½ρC_dA|v|v / m`; Magnus `k·(ω×v)/m` (curl on crosses and corners); drag and
  Magnus divided by the *current* mass, so a heavy ball slows less in the air but starts slower.
- **Kick = impulse.** A boot delivers an impulse J (N·s) set by the kicker's power, boot mass and run-up; launch
  speed is `J / m`. A soaked ball (heavier) leaves the boot slower and carries shorter: the era effect falls out of
  the physics (spike: the same kick carries < 75 % on a fully soaked ball, placeholder numbers).
- **Bounce:** restitution and horizontal keep per surface, reduced as the ball soaks; low vertical speed → roll.
- **Roll:** rolling resistance per surface (mud stops it dead, frozen ground runs on); puddles later as patches.
- **Wet mass over the match:** first-order soak toward `maxWaterShare · dryMass · wetness(t)`, time constant
  `soakTau`; wetness follows the seeded rain schedule and the pitch state; uptake also per touch on a wet pitch
  (hook). Mass at half-time/full-time is reported to the report writer ("the leather grew heavy").
- **Numbers:** placeholders in the spike; real values (1888 ball circumference/weight in the Laws, leather and
  bladder, lacing, water uptake, bounce on period pitches) come from lane T's
  `docs/data/history/BALL_AND_KIT_PHYSICS.md` into `physics.json`. The doc defines the parameter names above;
  lane T supplies values, sources and confidence.
- **Laces (tiento):** each header rolls `laceCutRiskPerHeader · f(ball speed, mass, caps)`; a cut is a minor injury
  event (KNOCK) and a report line. Caps reduce it (kit by season).

### 3.3 Player bodies
- Circle (radius ~0.35 m), mass from data when sourced, else a default per position (Q8).
- **Locomotion:** max speed, acceleration, deceleration from attributes (speed, acceleration, strength); **turn rate
  falls with speed** (a sprinting forward cannot cut back); grip per surface × boot studs (slips on frozen ground,
  heavy legs in mud); heavier boots: +kick impulse, −acceleration.
- **Stamina:** energy drains with speed² and surface fatigue factor, recovers when walking; caps max speed and
  decision quality; half-time recovery. Carries into `condition` after the match (E-01 data).
- **Contacts:** circle-circle impulses with mass; a **shoulder charge** is an action: impulse vs balance
  (strength, momentum) → stumble or knock-down for N ticks; charging from behind is a foul (LawsSystem judges from
  the contact normal vs the victim's facing). **Charging the keeper** is legal in 1888-89 (`RulesProfile.
  keeperChargeProtected = false`): a keeper holding the ball can be bundled, and keeper + ball crossing the line
  between the posts is a goal.
- **Injuries:** contact impulse above a threshold × fragility (age, condition, shin pads) → KNOCK / PASSENGER / OFF
  (D19-3). A passenger plays with reduced limits; OFF leaves the field (no substitutes, D7).

### 3.4 Pitch, goals, lines
- Pitch length × width **per ground** (1888 Laws allowed 100-200 × 50-100 yd; grounds research lane H-02 supplies
  the actual sizes, default 110 × 70 yd). Goal 8 yd × 8 ft, crossbar compulsory, **no nets** (V2 §1).
- Line crossings are detected with swept segments (ball centre path within the substep) against touchlines, goal
  lines, posts and bar, so a 30 m/s shot cannot tunnel.
- Close calls at the posts/bar (within a tunable margin) are flagged for a **disputed-goal appeal** (4.3).

---

## 4. Agents

### 4.1 Players
- **Perception (10 Hz):** each player keeps a belief of positions/velocities of others and the ball, refreshed for
  what is inside his view cone, with position noise growing with distance and shrinking with the `perception`
  attribute. Decisions use beliefs, not truth (so offside lines are misjudged, passes go to where a man *was*).
- **Shape anchors:** each player has a slot in the club's shape (2-3-5, 2-2-6, 1-2-7, ... from data, D19-6), shifted
  by ball position and phase (attack/defence); anchors are where he drifts when not involved.
- **Intents (utility scoring):** with the ball: dribble, short pass, long/lofted pass, cross, shot, clearance,
  shield; without: hold slot, press/chase, mark, **back up** the dribbler (the 1880s "backing up"), make a run, charge
  the keeper, appeal. Scores mix position, pressure, skill and **club style** weights: dribbling clubs score runs and
  backing up high, combination clubs score short passes and triangles high (D19-6), using the new `dribbling`
  attribute (D19-7, BOOK3PRO E-01).
- **Execution:** intents turn into physical actions in `ActionSystem` (a kick impulse with direction/power noise
  from skill, pressure, ball wetness and body balance; first touch as a damped impulse; header only when the ball is
  in the jump window). Outcomes are never decided by a dice roll on the result, only by noise on the inputs.
- **Goalkeeper:** positioning on the shot angle, rush out, catch vs punch (wet ball → more punches), handling only
  in his own half (`RulesProfile.keeperHandling = OWN_HALF`), two-step carrying limit.
- **Playing short:** when a player leaves, the shape is rebuilt from data rules (a forward drops to fill the backs;
  keeper hurt → best handler in goal), as in V2 §2.7.

### 4.2 Officials (D7, D19-2)
- **Entities:** two umpires (one supplied by each club) on the field and a referee **off the field** at the halfway
  line (until 1891). Umpire deployment is a parameter (`EACH_HALF` / `FLANK` / `FOLLOW`, Q7); they move with
  simple steering to keep a view of play.
- **Offside, from actual positions:** at the instant a team-mate plays the ball, `LawsSystem` snapshots positions
  and computes the truth with the **3-player rule** (offside unless three opponents, keeper included, are level or
  nearer their goal line; both halves; never from a goal kick or corner; yes from a throw-in). The offside forward
  becomes "in an offside position"; it only matters if he then plays or interferes (simplest era-safe reading:
  takes part in play).
- **Appeals:** defenders appeal with a probability from what *they* perceived. Each umpire judges from his own
  perception of the snapshot (noise from his distance and how far off the line he stands) **plus partisanship**
  toward his club; agree → decision; disagree → the referee judges from his touchline view. Same path for fouls,
  handball, charges from behind and disputed goals (ball over/under the bar, inside/outside the post).
- **Sendings-off:** very rare, only after violent contacts judged by the officials (D19-4).

### 4.3 Crowd
- **Sections, not people:** ~50-150 crowd sections along the touchlines/ends (entities), each with density (from
  attendance and ground layout), partisanship (home share) and mood; individual spectators are arrays drawn only
  by the viewer.
- **Ambient:** mood rises/falls with events near the section; noise feeds a small confidence modifier for the
  players near it (home advantage partly emerges here; calibrated with `homeBias`).
- **Encroachment:** with ropes, not barriers, dense sections push in on big moments: the usable width shrinks, throw
  -in run-ups are blocked, a ball into the crowd comes back at once or is delayed (era colour, rare "crowd
  interference" event); a pitch invasion at the end of big cup ties.
- Attendance itself stays in the gate model (`fork("gate:" + key)`), outside the engine.

---

## 5. Outputs

### 5.1 Events from physics
`EventDetectionSystem` watches state transitions and emits V2 `MatchEvent`s with tick, minute, actors and position:
| Event | Detected when |
|---|---|
| KICK / PASS / CROSS / SHOT | a kick action; SHOT if its predicted path meets the goal mouth (± margin) |
| PASS_COMPLETED / TURNOVER | next controlled touch by a team-mate / opponent |
| SAVE | keeper contact with a ball on a SHOT path |
| GOAL / DISPUTED_GOAL | ball fully over the goal line between posts under the bar (swept); close call → appeal |
| POST / BAR | ball contact with post/bar shapes |
| OUT → THROW_IN / CORNER / GOAL_KICK | line crossing + last touch |
| OFFSIDE_POSITION, APPEAL (+ decisions of both umpires and the referee) | LawsSystem (4.2) |
| FOUL / CHARGE / KEEPER_CHARGED | contact judgement |
| INJURY / PLAYER_LEFT / PLAYER_RETURNED / POSITION_CHANGE | contact impulse, fatigue, shape rebuild |
| LACE_CUT, BALL_HEAVY (half-time, full-time mass), CROWD_INTERFERENCE | ball and crowd systems |
Each event has a level (**key / extended / full**, D19-9): goals, disputed goals, big chances, injuries, sendings-off
are key; shots, saves, appeals, corners extended; passes, throw-ins, possession changes full.

### 5.2 Into the game
`MatchEngine.play` returns `MatchResult` (V2 §3) + `engineVersion` + `finalStateHash`. Then, unchanged from V2 §4:
`MatchResultApplier` sets goals and scorer ids from GOAL events; `MatchResultRecorder.record(match)` (P2-2) does
club stats, points, cup advance, lists and messages. The recorder never sees the ECS. The ticker (E-04) reveals
the event list by minute; the report writer turns events into period prose.

### 5.3 Saves and replay
- Saved per match: input digest, match fork name + game seed, `engineVersion`, params hash, **all events in a
  compact binary/JSON form** (D19-9), `finalStateHash`.
- **Replay = re-run from seed** with the same engine version: the viewer replays the full 2D match by re-simulating
  and checks `finalStateHash`. If the engine version differs (tuning since the save), the saved events are shown
  (ticker/report), never a re-simulation (Q5).

---

## 6. Performance

- **Budget (proposal):** ≤ 1 s CPU per match headless on a mid laptop core; a 1888-89 matchday (6 league games) ≤
  2 s wall on 4 cores; later seasons with two divisions and the cup ≤ 4 s. The Continue button shows progress.
- **Where we are:** the spike's 4.1 µs/tick (ball + 22 seekers) is 0.65 s per match at 30 Hz before AI. Perception
  and decisions at 10 Hz staggered should add 2-4x → **~1.5-2.5 s per match single core**: over budget, so E-19
  plans: hot loops over plain double arrays snapshotted once per tick (no mapper lookups per pair), spatial grid
  for neighbour queries, zero allocation per tick, and **matches of a day on a thread pool** (one `MatchWorld` per
  thread; results depend only on the match fork, so order and thread count do not matter).
- **LOD (Q3), recommendation: the same full physics for every match.** One truth: a result never depends on whom
  the player manages or watches; calibration is done once; replays work for any match. If the budget is missed
  after E-19, the fallback is a coarser *tick profile per competition chosen by data* (e.g. 15 Hz for leagues the
  player is not in), calibrated separately, never switched by viewing.
- **Profiling plan:** a `MatchEngineBenchmark` (Maven profile `-Pbench`, not in `verify`) prints ns/tick, ms/match,
  per-system share (timing decorators around each system's update; order unchanged), allocated bytes per tick
  (`ThreadMXBean`) and heap; a smoke test in `verify` fails only on a 10x regression (generous ceiling, not flaky).
  Measure on both machines; decide on ratios, not absolutes (background load on a machine easily skews absolute timings).

---

## 7. Calibration and tests

**Targets (D17, test code only):** from `docs/data/results_1888-89.csv`: 132 matches, **4.45 goals/game**
(home 2.67, away 1.77), **home win 59 %, draw 17 %, away win 24 %**, goals histogram and margins, table spread.
Physical sanity targets from lane T and biomechanics: kick and throw distances with a dry/wet leather ball, sprint
speeds, shots per match where period reports give them (low weight).

**Method:** E-20 harness plays the 1888-89 fixtures with the season squads, N seeds (pinned, so deterministic),
compares distributions; tunes a few macro knobs (shot noise, keeper reach, decision aggression, pass error,
home crowd effect, umpire accuracy) by coordinate search; commits tuned JSON. Acceptance bands from V2 §6 (goals
±0.25, home-win ±5 pp, draw ±4 pp, histogram TV distance < 0.08). The club strength factor (D19-1) shapes inputs,
never outcomes.

**Test tiers:**
1. **Physics, analytic:** no-drag flight matches the closed form; terminal speed under drag; bounce height ratio =
   e²; soak curve hits `1 − e^{-1}` at τ; rolling distance `v²/(2μg)`; swept goal-line tests at high speed.
2. **Determinism:** golden hash on all machines; headless vs viewer identical; per-tick hash sequence equal;
   shuffled day order equal.
3. **Laws:** constructed snapshots for offside (3 opponents, both halves, throw-in yes, corner/goal kick no);
   keeper handling outside own half never happens; no penalties or substitutes ever (property tests over 1,000
   seeds, V2 §7); goals == GOAL events; scorer on the field.
4. **Behaviour (statistical, fixed seeds):** stronger XI wins more, monotonic; wet/muddy day → fewer long passes
   completed and fewer goals from distance; dribbling vs combination styles differ in passes per possession;
   partisanship 0 → no side bias in appeals.
5. **Calibration** profile (E-20).

---

## 8. Slicing (PR-sized)

| Step | Content | Depends on | Touches BOOK3PRO areas? |
|---|---|---|---|
| **E-10** | `match/ecs` skeleton: `MatchWorld`, fixed-step loop, `SimSystem`/`DrawPass` markers, priorities table, `installSimulation`, `MatchStateHash` + describe, guard test (no gdx graphics / Random / wall clock in sim), golden-hash test; promote the spike | – | no |
| **E-11** | `BallSystem`: 2.5D flight, substeps, drag, Magnus, bounce/roll per surface, wet-mass soak, lace hook; params as Java defaults | E-10; numbers from lane T (placeholders until then) | no |
| **E-12** | Pitch/goal geometry per ground, swept line crossings, out-of-play + restart types, disputed-goal margin | E-10 | no |
| **E-13** | Player bodies: locomotion limits, turn-vs-speed, stamina, contacts, charges, knock-downs, injury impulses, kit params | E-10; stub ratings | no |
| **E-14** | Actions: kick/pass/cross/shot impulses with noise, first touch, dribble touches, headers, keeper catch/punch/handling in own half | E-11, E-13 | no |
| **E-15** | AI v1: perception, shape anchors (2-3-5 + era shapes), utility intents, club style weights | E-14; **E-01** `Position` + `dribbling` (BOOK3PRO) for real data, stubs before | reads only |
| **E-16** | Officials: umpires + referee entities, 3-player offside from snapshots, appeals with perception + partisanship, fouls, disputed goals, rare sendings-off | E-12, E-13 | no |
| **E-17** | Events + `MatchResult`: detection, levels, stats; `EcsMatchEngine implements MatchEngine` behind a flag | E-14, E-16; V2 contracts (E-02a) | `MatchSimulator` hook-up: coordinate (gameplay lead BOOK3PRO, D21) |
| **E-18** | Crowd sections: density, mood, encroachment, noise effect | E-12, E-17 | no |
| **E-19** | Performance: arrays snapshot, spatial grid, zero-alloc, parallel day, benchmark profile | E-15..E-17 | no |
| **E-20** | Calibration harness + tuned `physics.json` / `match_engine.json` (= roadmap E-05) | E-19, E-00 data | data pack + validator: ask BOOK3PRO |
| **E-21** | Viewer: `match.ecs.view` draw passes with interpolation; replaces `MatchEngineScreen`/debug screen; drop Box2D/gdx-ai deps | E-17 | no |
| **E-22** | Saves: events + seed + engine version + final hash; replay verify (with E-04, U-06) | E-17, saves v1 | **yes** (saves: BOOK3PRO) |

Roadmap mapping: E-02 → E-10..E-15 + E-17; E-03 → E-12, E-14, E-16; E-04 ticker/report consumes E-17; E-05 = E-20;
E-06 = switch `MatchSimulator` to the ECS engine after E-20 passes.

**Can start now on WORKSTATION without touching BOOK3PRO areas:** E-10, E-11, E-12, E-13, E-14, E-16, and the
E-19 benchmark (all in new `match/ecs` packages, test data in code). Wait for: E-01 data (E-15 real ratings),
`MatchSimulator` hook-up (E-17, coordinate), data-pack JSON + validator (E-20), saves (E-22).

---

## 9. Open questions for the producer

1. **Physics backend:** a) Box2D for bodies + custom ball; b) **custom pure-Java 2.5D for everything**; c) a 3D
   physics library. _Recommend b_ (identical bits on Windows/macOS/CI, ball height, no natives; spike proves it).
2. **Tick rates:** a) 60 Hz everything; b) **30 Hz bodies/laws, ball 60 Hz substeps, AI 10 Hz**; c) 20 Hz.
   _Recommend b_; a if the viewer looks coarse.
3. **LOD:** a) **same full physics in every match**; b) full only for the player's match, statistical elsewhere;
   c) full for the player's league, coarse tick profile for others. _Recommend a_ (c only as a data fallback).
4. **v0.6 delivery:** a) ECS engine must be ready for v0.6; b) **v0.6 ships the ticker/report/saves on a thin
   baseline behind `MatchEngine` (E-02a) while the ECS matures; swap when E-20 calibration passes.** _Recommend b_
   (keeps BOOK3PRO's gameplay/UI work unblocked and D20 intact).
5. **Old saves after re-tuning:** a) **show saved events, re-simulate only on the same engine version**; b) store
   only the seed and always re-simulate. _Recommend a._
6. **Crowd fidelity:** a) ambient only; b) **sections with mood, encroachment and a small player-confidence effect**;
   c) individual spectator entities. _Recommend b._
7. **Umpire deployment:** a) each umpire in one half; b) each on a touchline following play; c) **a parameter, default
   from lane T's research**. _Recommend c._
8. **Player physique:** a) defaults by position; b) **sourced height/weight in the people data where known, defaults
   otherwise** (schema proposal first, BOOK3PRO validator). _Recommend b later, a for E-13._
9. **Player's own match:** a) **computed headless at kick-off, revealed by the ticker** (in-match changes later as
   logged commands); b) live simulation with tactical changes from v0.6. _Recommend a._
10. **Viewer look:** a) **top-down 2D with code-generated sprites** (public-repo safe, D21); b) isometric/3D later.
    _Recommend a._
