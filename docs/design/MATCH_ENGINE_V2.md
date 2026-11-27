# Match engine v2: design (living doc)

_Started 2026-11-27 (WORKSTATION, lane E). Phase 4 items E-01..E-06 in `docs/ROADMAP.md`; decisions D5, D6, D7, D10._
Status: **draft for producer review**. The open questions are in section 9. Nothing here is built yet.

Goal: one headless, seeded, testable engine that turns two 1888 elevens into a scoreline, a list of key
events and a short report in period style, with the Laws as they stood in 1888-89 (D7).
The 2D ECS prototype (`futtoboru/match/engine/**`) stays a future replay viewer (D5, E-06).

---

## 1. The Laws in 1888-89 (with sources)

Laws in force: the FA laws as revised by the International Football Association Board (founded 1886), for the
1888-89 season. The engine models everything marked **M**, abstracts **A**, or ignores **I**.

| Topic | Rule in 1888-89 | Sim | Source |
|---|---|---|---|
| Offside | Offside unless **3 opponents** (keeper included) are between the player and their goal line when the ball is played by a team-mate. **Applies in both halves** (the own-half exemption came in 1907). | M | [W-Off] |
| Offside from goal kick | **No** offside (since 1863). | M | [W-Off], [W-GK] |
| Offside from corner | **No** offside (agreed Dec 1882, in the laws from 1883). | M | [W-Off], [W-Cor] |
| Offside from throw-in | **Yes, a player could be offside** (1878 to 1920). | M | [W-Off] |
| Sanction for offside / fouls / handball | **Indirect** free kick where it happened. Every free kick was indirect (direct free kicks came in 1903). | M | [W-LotG], [NSZ] |
| Officials | **Two umpires, one supplied by each club**, give decisions **on appeal** by the players; a **referee** (introduced 1881) off the field keeps time and settles it when the umpires disagree. In 1891 the referee moved onto the field and umpires became linesmen. | M | [W-Ref], [W-LotG] |
| Sending off | Allowed for illegal/violent play since 1877; the player is **not replaced**. Very rare. No cards (1970). | A (rare) | [NSZ] |
| Penalty kick | **None.** Adopted by IFAB 2 June 1891; first League penalty 14 Sept 1891. | M (never happens) | [W-Pen] |
| Goal nets | **None.** Brodie's patent filed 1889, first official use Jan 1891, League-wide from autumn 1891. Close calls ("over the bar or under? inside the post?") were argued with the umpires. | A (disputed goals) | [Brodie] |
| Crossbar | **Compulsory** since 1882/83 (tape no longer allowed). Goal 8 yd x 8 ft. | M | [W-LotG] |
| Goalkeeper handling | May handle "for the protection of his goal" **in his own half only** (FA 1882, confirmed IFAB 1887); no carrying beyond two steps (1883). Handling for any purpose from 1901; penalty-area limit 1912. | M | [W-LotG], [NSZ] |
| Charging the keeper | **No special protection**: the keeper could be fairly charged like any player, ball or no ball. Protection ("only when playing the ball or obstructing") came in **1892 (IFAB) or 1894 (FA)**; sources disagree, both after 1888-89. Forwards bundling the keeper (and the ball) through the posts was a real tactic. | M | [NSZ], [Spart] |
| Charging in general | Fair shoulder charge legal; charging from behind illegal (1869) unless the opponent was wilfully obstructing with his back turned (1888 wording). Hacking, tripping, holding, pushing, jumping at an opponent: free kick. | A | [NSZ] |
| Handball (outfield) | Any handling: indirect free kick. A deliberate handball stopping a certain goal near the goal could be awarded as a goal (1882 rule, "near the goal" = 2 yd). | A (very rare) | [NSZ] |
| Throw-in | Against the side that put it out (1873); **two hands from above the head** (1883); any direction. | A | [W-LotG] |
| Corner kick | From within 1 yd of the corner flag; **no goal direct** from a corner (1875-1924). | M | [W-Cor] |
| Goal kick | Within 6 yd of the nearest post; no offside. | A | [W-GK] |
| Kick-off / ends | Ends changed **at half-time only** (since 1875). No goal direct from kick-off. | M | [W-LotG] |
| Duration | **90 minutes by custom** (London-Sheffield 1866 onward; League matches were 90). The Laws only fixed 90 minutes and 11 players in **1897**. No added time concept; the referee kept time. | M (90 min) | [W-LotG] |
| Substitutes | **None** (League allowed one injured-player sub only in 1965). An injured or sent-off player is not replaced: the side plays short. | M | [W-LotG], CLAUDE.md |
| Draws in the FA Cup | Replay (existing cup code). Extra time was possible by agreement; not modelled in v1. | I | (uncertain) |
| Points | 2 for a win, 1 for a draw, goal average tie-break (decided late Nov 1888). Not an engine concern. | - | [W-1888] |

**How they played (for the model, not laws).** The standard shape was **2-3-5**: goalkeeper, two full-backs
(right/left back), three half-backs (the centre-half an attacking pivot, not a stopper), five forwards (outside
right, inside right, centre forward, inside left, outside left). English football of the 1870s was dribbling
"kick and rush" with backing-up; the Scottish **combination (short passing) game** (Queen's Park) spread south
with Scottish professionals. Preston North End under William Sudell built their 1888-89 "Invincibles" (P22 W18
D4 L0, 74-15, also the FA Cup without conceding) on Scottish players and the passing game. [NFM], [W-Inv],
[Nutmeg]; Jonathan Wilson, _Inverting the Pyramid_ (2008), ch. 1-2 (book, no link).

**Uncertain / flagged**
- Keeper-charge protection: 1892 (IFAB, per [NSZ]) vs 1894 (FA, per [Spart]). Either way not in 1888-89.
- How umpires split the work (each in one half? both everywhere?) and how often decisions went the umpire's own
  club's way: anecdotal only. Modelled as tunable parameters, not facts.
- Whether the appeal ("How's that?") was required for every decision or only offside/fouls: model it for offside,
  fouls, handball and disputed goals.
- Free-kick distance for opponents (6 yd until 1913?) and whether a goal could be scored direct from a goal kick
  before 1890: irrelevant for the sim, not verified.
- The archive drafts (`docs/archive/match-engine/**`, `docs/archive/2025-12-ai/ANALYSIS_MATCH_*`) use a
  2-player offside and penalty kicks; **do not reuse their rule tables**. Their general ideas (event list,
  commentary templates) are fine.

---

## 2. Simulation model

Abstraction level: **possession phases**, not ball physics. A match is a sequence of possessions; each
possession moves the ball through zones by contested **duels** until it ends in a shot, a turnover, the ball going
out, a free kick or an offside. Time advances per step; 90 minutes yields roughly 120-180 possessions
(calibrated, E-05).

### 2.1 Units and ratings
Attributes are 3-23 floats (3d6 + modifiers, `PlayerAttributeGenerator`); normalise `r = (x - 3) / 20`.
The XI is grouped into units by `Position` (E-01):

| Unit | Positions | Main attributes (weights tunable, JSON) |
|---|---|---|
| Goal | GK | shotStopping, areaPositioning, rushingOut, handToHand, punching, courage |
| Backs | RB, LB | tackling, marking, positioning, strength, kicking (long clearances), heading |
| Half-backs | RH, CH, LH | tackling, passing, stamina, teamwork, positioning |
| Forwards | OR, IR, CF, IL, OL | dribble = dexterity+acceleration+speed+trickShots, passing, oneTwos, kicking, heading, perception |

There is no `dribbling` attribute; v1 composes it (question Q7). A player out of his position gets a penalty
(e.g. x0.8 for an outfielder in goal). Each player also carries `condition` (starts at 1.0, drops with
stamina/endurance, ground and minutes) and `fit` (false when injured off).

Team **style** (Q6): `DRIBBLING` weights the dribble composite and individual runs; `COMBINATION` weights
passing/oneTwos/teamwork and gives more, shorter steps with lower turnover risk.

### 2.2 Possession loop
```
zones: OWN_HALF -> MIDFIELD -> ATTACK -> SHOT
start of possession: kick-off | throw-in | goal kick | corner | free kick | turnover | keeper's clearance
each step (advances the clock 10-40 s):
  duel  = attacking unit for the zone vs defending unit for the zone
  p(win) = logistic(k * (att - def) + homeBias + styleMod)
  outcomes: PROGRESS | TURNOVER | OUT (throw-in / corner / goal kick) | FOUL (indirect FK) | OFFSIDE check | INJURY roll
ATTACK zone: the move ends in a chance (quality q) or a scrimmage (goalmouth melee, period-typical)
SHOT:  attacker finish vs keeper -> GOAL | SAVED | WIDE/OVER | POST/BAR | KEEPER_CHARGED
```
- Zone duels: OWN_HALF uses own half-backs vs opposing forwards (forwards pressing), MIDFIELD half-backs vs
  half-backs, ATTACK forwards vs backs + centre-half.
- Corners: cross into a scrimmage (heading/strength vs keeper punching/handToHand); no direct goal.
- Free kicks: all indirect; a free kick in the attack zone starts a move with a set-piece bonus (freeKicks).

### 2.3 Offside (kills attacks)
With 3 required opponents and 2 backs + keeper, a forward is offside as soon as he is beyond one of the backs
when the ball is played. When a **forward pass** moves the ball into ATTACK (or a through ball inside it), roll
`p(offside position)` from attacker perception/positioning and the pass type vs the backs' positioning and their
stepping-up tendency (`backs.offsideTrap`, low in 1888). If in an offside position **and the defending side
appeals** (2.4) and it is given: indirect free kick to the defence, the attack ends. Never from a goal kick or a
corner; possible from a throw-in. Unappealed or wrongly refused offsides let the attack continue (a period
grievance the report can mention).

### 2.4 Umpires and appeals
Each incident (offside position, foul, handball, disputed goal) creates an **appeal** with probability
`pAppeal` (higher for blatant cases). Two umpires decide: each says "yes" with `p = truth ? accuracy : (1 - accuracy)`,
shifted by `partisanship` toward his own club. Agree: decision stands. Disagree: the **referee** decides
(neutral, higher accuracy). The event records both umpires and the referee, so the report can say
"the umpires differed and the referee gave the point against North End". Parameters live in JSON (Q2).

### 2.5 Goalkeeper
Handles only in his own half (structural: keeper actions only in OWN_HALF and SHOT). Shot outcome uses keeper
vs finisher; on scrimmages and high balls forwards may **charge the keeper**: keeper handToHand/strength/courage
vs attacker strength; a lost charge can put the ball (and keeper) through the posts = goal, raise an appeal
(dispute) or injure either player. No penalty kicks exist anywhere in the code path.

### 2.6 Disputed goals (no nets)
A small share of POST/BAR or "just inside" shots become `DISPUTED`: the attacking side claims a goal, the
umpires decide (2.4). Off by default until Q5 is answered.

### 2.7 Injuries, playing short, fatigue
- Injury hazard per duel, higher on charges and on hard grounds; lower with strength/courage.
- Severity: `KNOCK` (plays on, condition drop), `PASSENGER` (stays on, rating x0.4, often moved to the wing,
  as period reports describe), `OFF` (leaves the field, may return later in the half) (Q3).
- **No substitutes**: an `OFF` player leaves the side at 10 (or fewer). Units are rebuilt: the team shifts one
  forward back to fill a back or half-back gap (rule: never leave the goal empty; keeper hurt = best-handling
  outfielder goes in goal). Unit ratings scale with fit players.
- Fatigue: condition decays by stamina/endurance and ground; second-half ratings use condition.
- Sendings-off: rare event, same "plays short" path (Q4).

### 2.8 Home advantage and conditions
`homeBias` (one calibrated number) plus umpire partisanship (both sides have an umpire, so the net effect is
small but the home side's umpire is the home club's man). Conditions (Q8): ground `HARD | FIRM | HEAVY | FROZEN`
and weather `FINE | WIND | RAIN | SNOW | FOG` scale dribble vs passing, injury hazard and fatigue. Data has no
weather today (only the debug `MatchConfiguration` strings); v1 can seed it from month and ground.

---

## 3. Data contracts (package `com.rndmodgames.futtoboru.engine.match`, no Gdx imports)

```java
// P2-0: the one seeded, injected random source. The engine only sees this interface.
public interface RandomSource {
    double nextDouble();            // [0,1)
    int nextInt(int bound);         // [0,bound)
    long nextLong();
    RandomSource fork(String stream); // independent, deterministic child stream (SplittableRandom inside)
}
// Per match: rng = game.random().fork("match:" + matchRuntimeId). A match's result then depends only on
// (game seed, match id, input), never on the order in which the day's matches are simulated.

public enum Position { GK, RB, LB, RH, CH, LH, OR, IR, CF, IL, OL }
public enum Side { HOME, AWAY }
public enum Style { DRIBBLING, COMBINATION }

public record PlayerRatings(Long playerId, Long personId, String name, Map<String, Float> attrs, float condition) {}
public record Selection(Position position, PlayerRatings player) {}
public record TeamSheet(Long clubId, String clubName, List<Selection> eleven, Long captainId, Style style) {}
public record Conditions(Ground ground, Weather weather) {}                // nullable fields -> defaults
public record RulesProfile(int offsideOpponents, boolean offsideOwnHalf, boolean offsideFromThrowIn,
        boolean penaltyKick, boolean goalNets, KeeperHandling keeperHandling, boolean keeperChargeProtected,
        Officials officials, int maxSubstitutes, int minutes) {
    public static RulesProfile season1888() { return new RulesProfile(3, true, true, false, false,
            KeeperHandling.OWN_HALF, false, Officials.UMPIRES_AND_REFEREE, 0, 90); }
}
public record MatchInput(Long matchId, int competitionType, LocalDate date, TeamSheet home, TeamSheet away,
        Conditions conditions, RulesProfile rules, EngineParams params) {}

public sealed interface MatchEvent permits ... { int minute(); Side side(); EventType type(); }
// EventType: KICK_OFF, HALF_TIME, FULL_TIME, GOAL, CHANCE, OFFSIDE, APPEAL, FREE_KICK, CORNER,
//            INJURY, PLAYER_LEFT, PLAYER_RETURNED, POSITION_CHANGE, KEEPER_CHARGED, DISPUTED_GOAL, SENT_OFF
// GOAL(minute, side, scorerId, how: SHOT|HEADER|SCRIMMAGE|KEEPER_CHARGED_THROUGH|OWN_GOAL, assistId?)
// APPEAL(minute, side, kind: OFFSIDE|FOUL|HANDBALL|GOAL, homeUmpire, awayUmpire, referee?, given)
// key events (D6): GOAL, CHANCE(big), INJURY, PLAYER_LEFT, SENT_OFF; the rest feed stats + report.

public record MatchStats(int[] shots, int[] onTarget, int[] corners, int[] offsides, int[] appealsWon, int[] possessionShare) {}
public record MatchResult(int homeGoals, int awayGoals, List<MatchEvent> events, MatchStats stats,
        List<Selection> homeAtFullTime, List<Selection> awayAtFullTime, long seedUsed) {}

public interface MatchEngine { MatchResult play(MatchInput input, RandomSource rng); }
public interface MatchReportWriter { String write(MatchInput input, MatchResult result, RandomSource rng); }
```
- `EngineParams`: all weights, logistic `k`, base rates, home bias, umpire accuracy/partisanship, injury rates.
  Loaded from JSON (D14), e.g. `gamedata/core/engine/match_engine.json`, validated like the other packs.
- Records are immutable; `MatchResult` must be `equals`-comparable for determinism tests.
- `Map<String,Float>` attrs keeps the engine independent of `Player`'s 30 getters; the factory copies them.

---

## 4. Where it plugs in

Today `MatchSimulator.simulateMatch(Match)` does everything: strength from the **whole squad**, uniform random
score capped at 5, scorers picked uniformly from the squad (keeper included), attendance, club stats, points
(falls back to 3/1/0 rules), cup advancement. It owns `new Random()` (unseeded). It is called from two places in
`FuttoboruGameEngine` (`getMatchResult()` ~l.257 and `simulateMatchesForDate()` ~l.773/890), which also duplicate
the "move to played list + result message" code; only the second checks the draw blocker (P2-2).

Target after P2-2 and E-06:
```
FuttoboruGameEngine.simulateMatchesForDate  (the single result path, P2-2)
  -> MatchSimulator.simulateMatch(Match)          // public API unchanged: boolean, mutates Match
       input  = MatchInputFactory.build(match, game)          // XI picker (E-01), ratings, conditions, rules
       result = engine.play(input, rng.fork("match:" + id))   // pure, headless
       MatchResultApplier.apply(match, result)                // goals, scorer person ids (from GOAL events), events/report
       attendance = AttendanceModel(rng.fork("gate:" + id))   // moved out of the engine
  -> MatchResultRecorder.record(match)            // P2-2: club stats, points from league rules, cup advance, lists, message
```
- Keep the `MatchSimulator(Futtoboru)` constructor; add `MatchSimulator(Futtoboru, RandomSource, MatchEngine)`
  for tests and for P2-0. The old one delegates with `game.random()`.
- `Match` gets the key events and report (field or separate store keyed by match id; save format is P2-5, Q9).
  `homeScorerIds/awayScorerIds` stay filled for the awards code.
- Injuries that outlast the match (days out) are written back by the recorder, not the engine (Phase 4 scope:
  store `injuredUntil` only if Q3 says so).
- The 2D prototype later replays `MatchResult.events` (a viewer, D5); no engine code depends on it.

---

## 5. Report examples (period style, generated from events)

Style: the Saturday-evening football specials and _Athletic News_ match reports: past tense, club names in full,
"the Invincibles", "the visitors", "the leather", "a scrimmage in the goalmouth", "the custodian", "appealed for
offside", "the umpires differing, the referee...". Templates with variants per event, chosen by the `report` fork.

> **PRESTON NORTH END v. STOKE.** Played at Deepdale on Saturday before some 4,000 spectators, the ground heavy
> after the week's rain. North End won the toss and played with the wind. After twelve minutes Ross put the ball
> to Goodall, who beat Rowley with a fine shot. A long spell of pressure followed; Dewhurst struck the bar, and an
> appeal for offside against Ross was allowed by the referee, the umpires differing. Stoke rallied before the
> interval, but Holmes and Howarth defended stoutly. Half-time: North End 1, Stoke 0. On resuming, Gordon added a
> second from a scrimmage in the goalmouth, and Goodall a third. Stoke played the last twenty minutes with ten men,
> one of their backs being carried off. Result: North End 3 goals, Stoke none.

> **NOTTS COUNTY v. BLACKBURN ROVERS.** A fast game at Trent Bridge. The Rovers' forwards, dribbling in the old
> style, were often pulled up for offside, the Notts umpire being much in demand. From a corner the ball was
> rushed through, the Notts custodian being charged over the line with it; the home side appealed, but the goal
> was allowed. Notts equalised before half-time... Result: Notts County 2 goals, Blackburn Rovers 2.

(Names are illustrative; the writer only uses players in the XI and on the field at that minute.)

---

## 6. Calibration plan (E-05)

Targets from the real season. Another lane (E-00, `ws/E-00-1888-results`) is collecting every 1888-89 League
result into `docs/data/results_1888-89.csv` (later `seasons/1888-89/results.json`); calibration reads that file.
Known headline from [W-1888]: **132 matches, 586 goals, 4.44 per game** (the club table sums to 587, so check the
CSV). Compute from the CSV, do not hard-code:
- goals per game (mean and variance), home goals vs away goals per game;
- home win / draw / away win shares;
- total-goals histogram (0..10+) and winning-margin histogram; share of games with 5+ goals by one side;
- spread of the final table (best GF/GA 74-15, worst 26-51 / 40-73).

Method: a test-scope harness (`MatchEngineCalibration`, run by a Maven profile, not in normal `verify`) plays
the 1888-89 fixture list with the season's squads, N = 200 seeds, and prints target vs simulated. Tune
`EngineParams` by a small coordinate search; commit the tuned JSON. Acceptance bands (proposal): goals/game
±0.25, home-win share ±5 pp, draw share ±4 pp, goals histogram total-variation distance < 0.08.

Caveat: player attributes are random 3d6, not historical, so the **league aggregate** can be matched but the
**table order** (Preston top, Stoke bottom) cannot without club strength data (Q1). The current `MAX_GOALS = 5`
cap goes; real 1888-89 had heavier scores (verify in the CSV).

---

## 7. Test plan

Unit (JUnit, headless, no Gdx, fast):
- **Determinism**: same input + seed => identical `MatchResult` (equals, including event list); different seeds
  differ. Season-level: two runs of the 1888-89 season with one game seed give identical tables (with P2-3).
- **Order independence**: simulating a day's matches in shuffled order gives the same per-match results.
- **Rules invariants** (property tests over 1,000 seeds): never a penalty or substitution event; a side never
  has more than 11 or regains a player who left (unless `PLAYER_RETURNED`); no offside from goal kick or corner;
  offside possible from throw-ins; keeper events only in own half; no direct goal from corner or free kick.
- **Consistency**: goals == number of GOAL events per side; scorer is on the field at that minute; minutes are
  non-decreasing within 0..90; half-time once; stats match events.
- **Appeals**: with accuracy 1.0 and partisanship 0 every appealed true offside is given; with appeal rate 0
  nothing is given.

Statistical (N = 10,000 sims, fixed seed, so they are deterministic and not flaky):
- equal teams on neutral settings: home and away win shares within 1 pp of each other once `homeBias = 0`;
- a stronger XI wins more, monotonically in the rating gap;
- home advantage positive; playing short lowers goals scored;
- mean goals inside the calibration band (after E-05).
Performance: one match < 5 ms; a full season (132 + cup) < 1 s.

---

## 8. Slicing (PR-sized)

| Step | Content | Main files | Depends on |
|---|---|---|---|
| E-01a | `Position` enum; a position per player in season squads (data + loader); best-guess positions for 1888-89 from sources, `confidence` per D11 | `gamedata/core/seasons/1888-89/squads.json`, `tools/data/*`, `data/Player.java` (or squad entry), validator | P2-4 slice 2 (loader, BOOK3PRO lock) |
| E-01b | `XiPicker`: auto 2-3-5 from fit players, out-of-position penalty, `TeamSheet` build; manager override later (Tactics screen) | `engine/match/XiPicker.java`, `MatchInputFactory.java`, tests | E-01a |
| E-02a | Contracts (section 3), `MatchEngine` interface, `EngineParams` JSON + validator, a **baseline engine** reproducing today's behaviour behind the interface; `MatchSimulator` delegates | `engine/match/**`, `MatchSimulator.java`, `gamedata/core/engine/match_engine.json` | P2-0 (RandomSource), P2-2 (single path) |
| E-02b | Possession/duel model, chances, keeper, scorers by position; determinism + invariant tests | `engine/match/PossessionEngine.java`, tests | E-01b, E-02a |
| E-03a | Offside (3-player, throw-in yes, corner/goal kick no) + umpire/referee appeals | `engine/match/rules/Offside.java`, `Officials.java` | E-02b |
| E-03b | Keeper handling + charging, injuries, playing short, fatigue, (sendings-off, disputed goals per Q4/Q5) | `engine/match/rules/**` | E-03a |
| E-04a | `MatchEvent` storage on `Match` + report writer (templates in JSON/i18n bundle) | `data/Match.java`, `engine/match/report/**`, save format | E-02b; P2-5 (saves v1) |
| E-04b | Match result screen shows key events + report | `tables/match/result/MatchResultScreenTable.java` | E-04a; Phase 3 UI conventions (U-01) |
| E-05 | Load results CSV as test data, calibration harness + profile, tuned params | `src/test/.../MatchEngineCalibration.java`, `docs/data/results_1888-89.csv` | E-00, E-03b, P2-3 |
| E-06 | Remove old scoring from `MatchSimulator`, attendance to `AttendanceModel`, delete dead code; document the viewer hook for `match/engine/**` | `MatchSimulator.java`, `match/engine/**` (comments only) | E-05 |

Critical path: P2-0 + P2-2 -> E-02a; P2-4 slice 2 -> E-01a -> E-01b -> E-02b -> E-03a/b -> E-05 -> E-06.
E-02a can start right after P2-0/P2-2 with positions defaulted from squad order.

---

## 9. Open questions for the producer

1. **Team strength source** (decides whether the table can look like 1888-89):
   a) attributes as generated (3d6), accept any table; b) a per-club strength factor seeded from the real 1888-89
   table; c) hand-set attributes for real players from sources (slow, best). _Suggest b now, c later._
2. **Umpire partisanship**: a) none; b) small home bias only; c) each umpire leans to his own club, referee
   settles disagreements. _Suggest c._
3. **Injuries**: a) injured player always leaves for good; b) knock / passenger / leaves, may return; c) no
   injuries in v1. Days out after the match: in v1 or later? _Suggest b, days out later._
4. **Sendings-off**: a) none; b) very rare event. _Suggest b._
5. **Disputed goals (no nets)**: a) leave out; b) rare umpire-decided event. _Suggest b._
6. **Tactics in v1**: a) everyone 2-3-5, no styles; b) per-club style dribbling/combination (AI picks, player can
   set); c) also other shapes (2-2-6, 1-2-7). _Suggest b._
7. **Dribbling attribute**: a) compose from existing attributes; b) add a `dribbling` attribute to the data
   (touches P2-4 data). _Suggest a._
8. **Weather/ground**: a) ignore in v1; b) seeded from month and ground; c) researched historical weather.
   _Suggest b._
9. **Event storage in saves**: a) all events; b) key events + report only (D6); c) nothing, regenerate from seed.
   _Suggest b._
10. **Match screen**: a) final report only; b) minute ticker that reveals key events. _Suggest a in v1._
11. **Cup draws**: a) replay only (current); b) extra time by agreement, then replay. _Suggest a._

---

## Sources
- [W-Off] Offside (association football), history: https://en.wikipedia.org/wiki/Offside_(association_football)
- [W-LotG] Laws of the Game, history section: https://en.wikipedia.org/wiki/Laws_of_the_Game_(association_football)
- [W-Ref] Referee (association football), history: https://en.wikipedia.org/wiki/Referee_(association_football)
- [W-Pen] Penalty kick, history: https://en.wikipedia.org/wiki/Penalty_kick_(association_football)
- [W-Cor] Corner kick, history: https://en.wikipedia.org/wiki/Corner_kick
- [W-GK] Goal kick, history: https://en.wikipedia.org/wiki/Goal_kick
- [W-1888] 1888-89 Football League: https://en.wikipedia.org/wiki/1888%E2%80%9389_Football_League
- [W-Inv] The Invincibles (English football): https://en.wikipedia.org/wiki/The_Invincibles_(English_football)
- [NSZ] Law 12 history table (fouls, charging, keeper handling): https://nachspielzeiten.de/law-12-fouls-and-misconduct-en
- [Spart] Spartacus Educational, goalkeeper (1894 FA charging law): https://spartacus-educational.com/Fgoalkeeper.htm
- [Brodie] John Alexander Brodie (goal nets): https://en.wikipedia.org/wiki/John_Alexander_Brodie and
  https://markmetcalfwfb.blogspot.com/2023/01/goal-nets-were-first-officially-used-on.html
- [NFM] National Football Museum, The Invincibles: https://nationalfootballmuseum.com/stories/12-games-1888
- [Nutmeg] Nutmeg, the Scottish professors: https://www.nutmegmagazine.co.uk/issue-4/the-scottish-professors-and-their-role-in-footballs-first-invincibles
- Primary texts to check when possible: IFAB historical Laws downloads (https://downloads.theifab.com, e.g. the
  1903-04 edition is online; an 1888-89 edition was not found).
