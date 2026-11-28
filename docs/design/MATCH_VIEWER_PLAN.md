# Futtoboru match viewer: translation plan from the DarkBlade graphics engine

_Lane AA, 2026-11-27. Draft for producer review. Written to be **public-safe**: no secrets, no machine paths, no
private measurements; DarkBlade class names are allowed by **D23** (reuse or re-implementation of DarkBlade code in
Futtoboru approved). The PM moves a sanitised copy into the public repo (`docs/design/`) when accepted._

Target: E-21 of `docs/design/MATCH_ENGINE_ECS.md` — the viewer for the headless 30 Hz `MatchWorld`
(`match.ecs`), drawn as a **top-down / three-quarter pixel-art** football match of 1888-89: pitch, players, ball with
height and shadow, officials, crowd sections, weather (rain, mud, fog, snow, wind), winter dusk light, chimney smoke
around the ground, dust and mud particles, goal moments, camera follow/zoom, replays.

---

## 1. What Futtoboru already has (and where it differs from DarkBlade)

| Futtoboru (`match.ecs`) | DarkBlade equivalent | Gap to close |
|---|---|---|
| `SimSystem` + `DrawPass` markers; `MatchWorld` refuses a system that is both, and unique priorities | `DrawPass` marker + processing flip in `UniverseView` | Ours is stricter (draw passes never run in `engine.update`; `MatchWorld.draw(alpha)` calls them). Keep ours. |
| `DrawPass.draw(double alpha)` | `EntitySystem.update(frameDelta)` | **We pass no frame delta.** Particles, weather, flicker, camera springs need real frame time. Add it (2.1). |
| `SystemPriority` (DRAW_* 1000..1070) | `DrawOrder` (FRAME..POST, gaps of 50) | Too few slots and gaps of 10; extend (2.2). |
| `FixedStepClock` (cap, speed, alpha) | `SimulationClock` (cap + per-frame time budget) | Add the time budget so a slow machine slows the match, not the frame rate. |
| `StateHash` + `setDrawGuard` (hash around every pass) | `StateHash` + "on vs off" tests | Ours is better (per-pass guard). Keep. |
| 30 Hz sim, ball 60 Hz substeps | 60 Hz sim = display | **DarkBlade never interpolates.** We must (E-21a). |
| Code-generated art (`tools/art/ground_art.py`, `ui/GroundArt`) | Sprite sheet + `ProceduralArt` + runtime atlas | Keep D21 (own, code-generated art); take DarkBlade's atlas machinery only. |
| No viewer yet; old `MatchEngineScreen`/Box2D to delete | `UniverseView`, `GraphicsSystems.install` | Build `match.ecs.view` with one installer. |

## 2. Viewer foundations (things to add before any module)

### 2.1 `ViewFrame` context (re-implement; small)
A per-frame value object the viewer fills before `MatchWorld.draw(alpha)`: `alpha`, `frameSeconds` (real),
`viewSeconds` (accumulated, pausable), `matchSpeed`, camera, batch, atlas, `ViewSky`, quality. Every draw pass reads
it instead of statics. Cosmetic random: one `Random` per viewer, reseedable in tests (DarkBlade `RNG.cosmetic()`
pattern), never the match fork.

### 2.2 Priority table for draw passes
Extend `SystemPriority` with gaps (DarkBlade `DrawOrder` shape): `FRAME 1000` (interpolation, camera, sky),
`EVENTS 1010`, `LIGHTS 1015`, `PITCH 1100`, `MARKS 1120` (mud, divots, footprints), `SHADOWS 1150`, `ACTORS 1200`
(players, officials, ball, depth-sorted together), `OVERHEAD 1250` (ball in the air above heads), `LIGHT 1300`,
`PARTICLES 1350`, `CROWD_FRONT 1400`, `ATMOSPHERE 1500`, `CAMERA_FX 1800`, `DEBUG 1850`, `POST 1900`. The crowd at
the far touchline draws before ACTORS (behind), near touchline after (in front) in a three-quarter view.

### 2.3 Interpolation pass (new; DarkBlade has none)
`InterpolationPass` at FRAME: for every entity with a sim transform keep `prev` and `curr` (copied at the end of each
tick by a view-side listener or by comparing tick counts), and write a **view-only** `ViewTransform` =
lerp(prev, curr, alpha) in floats. Every other pass reads `ViewTransform` only. Ball height `z` interpolated the same
way. The draw guard proves no sim component is touched.

### 2.4 Quality knobs (port `GraphicsQuality` + `TickGate` + `VfxSwitches`, adapt)
`match_view_quality.json` (low/medium/high/ultra), modules: particles, crowd, weather, fog, light, shadows, marks,
smoke, camera_fx. Software-renderer detection drops to low. Startup benchmark (DarkBlade's designed GFX-61) later.

---

## 3. Module-by-module decision

Legend — **Port as-is**: copy the class, rename package, minimal edits. **Port + adapt**: copy and change its inputs
or data. **Re-implement**: take the idea, write ours. **Skip**: not needed for a football match.

| # | DarkBlade module (classes) | Football use | Decision | Notes |
|---|---|---|---|---|
| 1 | `DrawPass` / `UniverseView` processing flip | — | **Skip** (ours is stricter) | Keep `MatchWorld.addDrawPass/draw`. |
| 2 | `GraphicsSystems.install` + `GraphicsContext` | `MatchViewer.installDrawPasses(world, ViewFrame)` | **Re-implement** (tiny) | One registration point; one line per module. |
| 3 | `GraphicsQuality`, `TickGate`, `VfxSwitches` | knobs per module, live switches, accessibility | **Port + adapt** | Rename modules, English keys, our JSON path; drop mod lookup or point it at our data pack. |
| 4 | `DepthSort` | y-sort players, officials, ball, goal frames, crowd front rows | **Port as-is** | Pure Java, zero allocation. Depth key: ground y, with the ball's y at its shadow point. |
| 5 | `ActiveZones`, `ViewCulling`, `FrameCulling`, `SpriteZones` | culling crowd rows and marks outside view | **Port `ViewCulling` as-is; skip `SpriteZones`** | ~30 actors never need zone buckets; the crowd arrays need a simple view-rect cull. |
| 6 | `RuntimeAtlas`, `ShelfPacker`, `ProceduralArt.adopt` | pack code-generated kits, players, ball, fx pieces into one page: few binds | **Port as-is** (+ adopt at load) | Lesson: adopt the atlas before any `findRegions` call (it returns copies). Per-kit colour variants packed at match start. |
| 7 | `DrawCallDiagnostic` | find texture interleaving | **Port as-is** (debug flag) | |
| 8 | `Particles`, `ParticleSystem`, `EmitterSpec`, `ParticleOwner`, `EmitterComponent` | mud splash on bounce/slide, dust on dry/frozen ground, breath on cold days, rain splashes, chimney smoke, goal "hat toss"/paper | **Port + adapt** | Owners become GAMEPLAY (ball/tackle feedback, reserved), SMOKE, WEATHER, AMBIENT. Specs from our own JSON. Emitters: ball, players, chimneys (view-only data). |
| 9 | `VfxEvents`, `VfxEvent`, `VfxEventSystem`, `VfxCatalog` + `vfx.json` | match events (KICK, BOUNCE, HEADER, TACKLE, GOAL, POST/BAR, APPEAL, OUT) to visuals | **Port + adapt** | Our sim already emits `MatchEvent`s (E-17). Add a view-side event queue fed per tick; a `match_vfx.json` maps event type + surface to emitters, camera kicks and crowd reactions. Keep "unknown id is an error" test. |
| 10 | `ShadowRenderSystem` | player blobs; **ball shadow that shrinks/fades with height** | **Port + adapt** | Kinds PLAYER, OFFICIAL, BALL, POST. Ball: shadow at (x, y), sprite drawn at y + z·k (three-quarter lift), shadow scale/alpha by z. Violet 50 % shadow style fits our palette. |
| 11 | `LightMapSystem`, `SceneTint`, `SkyColor`, `Ambient` | winter afternoon kick-offs fading to dusk; lamps on the grandstand or the pavilion | **Port `SkyColor` grade + `SceneTint` (adapt); port `LightMapSystem` later** | 1888 grounds had no floodlights: the main need is the ambient grade (match minute -> sun). The FBO only when there are lights in view (lamps, pipe glow, gas lamps outside the ground) — its "no light, no FBO" shortcut makes it free otherwise. |
| 12 | `LightningSystem` | storms | **Skip for now** | Matches were rarely played through lightning; keep as idea. |
| 13 | `Sky`, `SkySystem`, `WeatherSource`, `SeedWeather`, `ClimateWeather` | the match's weather as the viewer sees it | **Port + adapt `Sky`/`SkySystem`; replace sources** | The source must be the **match input + sim state** (rain schedule from the weather fork, `PitchConditions.wetness()`, wind from input), never an independent seed: the viewer must agree with the physics. Levels fade over seconds. |
| 14 | `PrecipitationSystem` | rain, sleet, snow, overcast grey | **Port + adapt** | Density from sim wetness/rain schedule; snow lying on the pitch is a pitch-bake variant (FROZEN surface). |
| 15 | `FogSystem` | fog matches (historically abandoned or played blind) | **Port + adapt** | Remove doors/crypts/horror; add "fog never hides the ball and its carrier": clear radius around ball. Visibility could later be a sim input (perception noise), never the reverse. |
| 16 | `CloudShadowSystem` | cloud shadows sweeping the pitch on bright days | **Port as-is** (input from `ViewSky`) | Hash field, zero state, very cheap. |
| 17 | `WindSystem`, `GustSystem` | flags, smoke lean, rain lean, paper/leaves; wind also affects the ball in sim | **Port + adapt** | Wind vector comes from match input (shared with the sim); gusts stay cosmetic unless the sim models them (then from the sim). |
| 18 | `HearthSystem` chimney part | factory and terrace chimneys behind the stands (as in our ground art) | **Re-implement** (simple) | View-only list of chimney points per ground (from the ground descriptor), smoke emitters, capped nearest-first. |
| 19 | `FootDustSystem`, `GroundMarks`, `GroundMarkSystem`, `FootprintSystem`, `PuddleSystem` | dust under sprinting feet on dry/frozen ground; mud splashes; **pitch wear: churned goalmouths and centre, slide marks, footprints in snow, puddles** | **Port + adapt** | Surface from `PitchConditions.surfaceAt`. Long-lived wear should go to a **wear texture** (one pixel per pitch cell, updated in place, like DarkBlade's `FieldOverlay` idea) rather than thousands of marks. |
| 20 | `BirdSystem`, `BirdFlockComponent` | crowd **as arrays**; also pigeons/gulls over the ground | **Re-implement crowd from the pattern; optionally port birds** | Crowd: one entity per section (sim, E-18) holding mood/density; spectators are float arrays in a `CrowdPass` (x, y, kind, phase, colour) with cheap animation: sway, hats up on goals, surge on encroachment. |
| 21 | `CameraZoom` | pixel-perfect zoom levels | **Port as-is** | |
| 22 | `CameraShake`, `CombatFeel` | goal/post/bar thump, heavy charge | **Port + adapt** | Make it an instance (no statics), cosmetic random from `ViewFrame`, accessibility scale knob. |
| 23 | Camera director (none in DarkBlade) | follow the ball with lead, zoom out on long balls, frame the goalmouth on shots, cut to goal celebration | **Re-implement (new)** | Critically-damped spring on the interpolated ball + play centroid; dead zone; look-ahead along ball velocity; zoom from ball speed and spread of players; snaps to whole-pixel zoom at rest. |
| 24 | Replays (none in DarkBlade) | instant goal replays; full replay from seed | **Re-implement (new)** | (a) `ReplayBuffer`: a view-side ring of the last ~20 s of interpolated transforms (≈30 floats × 30 Hz), filled every tick; a goal replays from it at slow motion without touching the sim. (b) Full replay = re-simulate from seed headless to the wanted tick (the match runs in well under a second), then view; `finalStateHash` verifies. |
| 25 | `LookResolver`, `PeopleArt`, `VillagerLook`, `ClothesLayer` | players in era kits (shirt colour, hoops, caps, knickerbockers), officials | **Re-implement** with our generator | Pattern worth taking: a look is a name/descriptor on the entity resolved to a region once when it enters. Our kits come from `kit/<season>.json` + a Python/Java generator. |
| 26 | `DarknessVeilSystem` | — | **Skip** | |
| 27 | `MarksRenderSystem`, `AuraSystem`, `Speech*` | possession marker, selected player ring, appeal "hand up" icon, umpire flag | **Re-implement** (small) | Debug/HUD overlays in a MARKS pass. |
| 28 | `proto/mode7` (3D) | broadcast-style tilted view, later | **Skip for E-21; idea for later** | Its floor bake + billboards fit a "grandstand camera" mode; a pitch is small, so the full-floor bake is fine. |
| 29 | `FrameProfile`, `SystemSamples`, `StackSampler`, `PerfRecorder`, `PerfJson` | viewer frame/sim split, per-pass cost | **Port + adapt** | Our sim benchmark (E-19) already plans per-system timing; for the viewer, `FrameProfile` (off = a few boolean checks) plus GL counters. |
| 30 | `NoGl`, `MockGl`, `ArtGuard` | GL-free tests for every draw pass; sim never asks for art | **Port as-is** (test scope) | `ArtGuard` complements our sim guard test (no `gdx.graphics` imports in `match.ecs`). |
| 31 | `tools/gfx/compare_shots.py` | screenshot regression of fixed viewer scenes | **Port + adapt** | Our art tooling is stdlib-only; rewrite its PNG reading without PIL (we already write PNGs that way in `ground_art.py`). Keep mandatory A/A. |
| 32 | `tools/perf.py` | interleaved A/B with bootstrap CIs | **Port + adapt later** | Valuable for E-19 (sim) and the viewer; Maven/JUnit entry points instead of Gradle. |

### Pitch rendering (new, combines several ideas)
Bake the pitch once per match into an atlas-sized texture: grass base per season (palette from `ground_art.py`),
mowing stripes (if the era allows), lines of the era (`EraMarking`), goal frames as sprites (depth-sorted), slope and
surroundings from the ground descriptor. During the match a low-resolution **wear/wetness overlay** texture (one
texel per 0.5-1 m) darkens with play in the goalmouths and centre, blends to mud with wetness, whitens with frost/snow.
Updated a few cells per tick, drawn as one quad: constant cost.

---

## 4. Slices (PR-sized), in order

| Slice | Content | Ports | Effort | Depends |
|---|---|---|---|---|
| **E-21a** | `match.ecs.view` scaffold: `ViewFrame`, extended draw priorities, `MatchViewer.installDrawPasses`, `InterpolationPass` + `ViewTransform`, time-budgeted clock, pixel-perfect camera, quality JSON + switches, `NoGl`/`ArtGuard` test helpers, draw-guard test with all passes on | `GraphicsQuality`, `TickGate`, `VfxSwitches`, `CameraZoom`, `NoGl`, `ArtGuard` | M (3-5 days) | E-10..E-12 |
| **E-21b** | Pitch bake + era markings + goal frames; players and officials as generated sprites; `RuntimeAtlas` page; `DepthSort` for actors | `RuntimeAtlas`, `ShelfPacker`, `DepthSort`, `DrawCallDiagnostic` | M | E-21a, E-13 |
| **E-21c** | Ball with height (three-quarter lift), contact shadows for all actors, ball-over-heads ordering | `ShadowRenderSystem` (adapt) | S (1-2 days) | E-21b, E-11 |
| **E-21d** | View event queue from `MatchEvent`s + `match_vfx.json` + particle pool: dust, mud splash, ball skid, header puff | `Particles`, `ParticleSystem`, `EmitterSpec`, `ParticleOwner`, `VfxEvents`/`VfxEventSystem`/`VfxCatalog` (adapt) | M | E-21c, E-17 |
| **E-21e** | Camera director: follow with lead, dynamic zoom, shot framing, goal shake (accessible) | `CameraShake` (adapt) + new | S-M | E-21a |
| **E-21f** | Crowd sections as arrays: rows along ropes/stands, sway, goal reaction, surge on encroachment, cull by view | birds pattern (re-implement) | M | E-18 |
| **E-21g** | Weather: `ViewSky` from match input and sim wetness; rain/sleet/snow; overcast grey; wind lean; fog with clear radius around the ball; cloud shadows | `Sky`/`SkySystem`, `PrecipitationSystem`, `FogSystem`, `CloudShadowSystem`, `WindSystem`, `GustSystem` (adapt) | M-L | E-21d |
| **E-21h** | Pitch wear/wetness overlay; footprints in snow; puddles; slide marks | `GroundMarks`, `FootprintSystem`, `PuddleSystem`, `FootDustSystem` (adapt) | M | E-21g |
| **E-21i** | Light: dusk grade by match clock and date; chimney smoke from the ground descriptor; optional light map for lamps | `SkyColor`, `SceneTint` (adapt), `LightMapSystem` (later), chimney smoke (new) | S-M | E-21g |
| **E-21j** | Goal moment + replays: `ReplayBuffer` instant replays (slow motion, camera re-framing), full replay from seed with hash check | new | M | E-21e, E-22 |
| **E-21k** | Viewer perf: `FrameProfile`, GL counters, viewer benchmark scene, draw-call ceiling test; startup quality pick later | `FrameProfile` (adapt) | S | E-21b |
| **E-21l** | Screenshot regression: fixed seeded scenes (dry noon, rain, fog, snow dusk), stdlib pixel diff with A/A | `compare_shots.py` (re-write stdlib) | S | E-21b |
| E-21m | Delete `MatchEngineScreen`, debug screen, Box2D and gdx-ai dependencies | — | S | E-21a..d |

Effort: S ≈ 1-2 days, M ≈ 3-5 days, L ≈ 1-2 weeks of one developer/agent lane. The first visible "match on screen"
is E-21a + b + c (~2 weeks); "looks like a period match in weather" adds d, e, f, g (~3 more weeks).

## 5. Rules we adopt (from DarkBlade, our words)
1. A draw pass reads sim components and writes only view state; the per-pass draw guard is on in every viewer test.
2. Every module has a switch; with all modules off the viewer draws a plain pitch, actors and ball.
3. Visuals are data (`match_vfx.json`); an unknown event id fails a test.
4. Thousands of small things (spectators, particles, marks) are arrays in one pass; only owners are entities.
5. No allocation per frame in passes (pre-sized arrays, reused colours, primitive collections); `ComponentMapper`
   only; static families.
6. Cosmetic randomness only from the viewer's own source; deterministic flicker from time and position.
7. Adopt atlases at load, before any region lookup.
8. Low quality is designed: each effect has a cheap form that still reads.
9. Measure on ratios, A/B in the same run, A/A first for screenshots.

## 6. Sharing code between the two games: options

| Option | What | Pros | Cons |
|---|---|---|---|
| **A. Copy-port into Futtoboru** (recommended now) | Copy the named classes into `match.ecs.view` / `ui.gfx`, rename packages, adapt; note the origin in each class header | Fastest; no build coupling; Futtoboru stays self-contained (Maven, libGDX 1.12, Java 17); D23 allows it | Two copies diverge; fixes don't flow back automatically |
| B. Shared public library (e.g. a small "pixel-gfx" module: quality knobs, tick gate, depth sort, runtime atlas, particle pool, culling, sky/weather passes, test helpers) | New public repo or a module in Futtoboru, published to a Maven repo (JitPack/GitHub Packages); DarkBlade depends on it too | One source of truth; both games improve it | Must strip every DarkBlade type (`World`, `Mapper`, `SpriteManager`, components) behind interfaces; two build systems (Gradle and Maven) and two libGDX versions to support; DarkBlade would depend on a public artifact; release/versioning overhead |
| C. Shared private library | Same as B, private | Keeps DarkBlade private | **Not workable**: the public Futtoboru build could not resolve a private artifact (public contributors and CI break) |
| D. Re-implement from this plan | Write ours from the patterns | Cleanest licensing; fits our double-precision sim and alpha-driven passes | Slowest; re-learns pitfalls |

**Licensing implication (for the producer to decide):**
- Futtoboru's repo is public under **GPL-3.0**. Any DarkBlade code copied in (A) is published under GPL-3.0 from that
  moment; anyone may reuse those classes under GPL terms. DarkBlade itself stays proprietary: a copyright holder is not
  bound by the licence of a copy he publishes, so DarkBlade's own copy is unaffected.
- That only holds for code whose authors agree. The producer confirmed clearance for all DarkBlade code (D24); each
  ported class still carries an "adapted from DarkBlade" origin line.
- If option B is chosen, license the shared library **permissively (Apache-2.0 or MIT)**, not GPL: DarkBlade
  (proprietary) can then use it and absorb outside contributions, and Futtoboru (GPL-3.0) can depend on it (Apache-2.0
  is GPL-3-compatible). A GPL shared library would stop DarkBlade from taking outside contributions back.
- No DarkBlade **assets** move (art and sound have their own licences). Futtoboru keeps its own code-generated art (D21). Fonts already come from our own skin.
- Secrets, LAN addresses, internal governance/business notes are never copied (headers of ported classes are reviewed
  for item ids and internal doc references; keep a neutral "adapted from DarkBlade" line).

**Recommendation:** A now (port the ~12 classes named in section 3 as needed by each slice), with an origin line per
class; revisit B after E-21g, when we know which classes stayed close to the originals (likely: `DepthSort`,
`TickGate`, quality knobs, `RuntimeAtlas`/`ShelfPacker`, `Particles`, `ViewCulling`, `NoGl`).

## 7. Producer answers (D22-D24, 2026-11-28)
1. Sharing model: **A, copy-port per class** with an origin line.
2. Authorship: **clearance for all DarkBlade code** confirmed by the producer.
3. View angle and 4. pixel scale: chosen from the art prototype checklist (pending).
5. Weather authority: viewer sky comes only from match input + sim state (plan default).
6. Fog: cosmetic in v0.6 unless decided otherwise.
7. Replays: **~20 s view-side buffer + full re-simulation from seed**, checked against the saved hash.
8. Quality auto-pick: manual quality + software-renderer detection first; benchmark later.
9. **Keep 30 Hz simulation with interpolation** in the viewer.
