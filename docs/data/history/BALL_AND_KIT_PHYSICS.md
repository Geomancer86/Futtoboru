# Ball and kit physics, 1888 onward (numbers for the ECS engine, D20)

_Lane H-03, 2026-11-27. Each number has a source and a confidence: **H** measured / primary, **M** one good
secondary source, **L** weak source, **D** a design estimate derived here (no source; tune in calibration).
Sources: keys as in `ERA_TIMELINE.md`. Dates of each change: `ERA_TIMELINE.md` §1._

## 1. The ball

### 1.1 Construction (1888-89)
- India-rubber bladder (Lindon, c.1862) inside a hand-stitched cowhide leather case; the bladder neck is pushed in
  through a slit that is closed with a **leather lace** (the "tiento" in Spanish usage). No valve. **M** (W-Lindon, W-Ball)
- Before c.1862 a pig's bladder, blown by mouth, which set an uneven ("plum") shape. Irrelevant from 1888 except as
  flavour. **M**
- No inflation pressure in the Laws until **1967** (NSZ-2): pressure, and so stiffness and bounce, varied by ball
  and by who pumped it. **H** for the absence of a rule.
- Laceless valve ball: Argentina 1931 (patent), 1932 (AFA official). In England laceless and coated leather balls
  arrive in the **1950s**. **H/M** (W-Ball, PH-25)

### 1.2 Size and dry mass
| Period | Circumference | Radius | Dry mass at kick-off | Conf. |
|---|---|---|---|---|
| 1872 (or 1883) to now | 27-28 in = 68.6-71.1 cm | 10.9-11.3 cm | - | H |
| 1888-89 | as above | | **no rule**; use 13-15 oz like the next season | D |
| 1889-1937 | as above | | **13-15 oz = 369-425 g** (1890 text: international matches) | H |
| 1937 onward | as above | | **14-16 oz = 397-454 g** | H |
| modern (reference) | 68-70 cm | | 410-450 g | H |

Engine default for 1888-1937: `massDryG = 400`, `radiusM = 0.111`. **D** (mid-range).

### 1.3 Water uptake (the wet ball)
Measured data (all **H**, from PH-25 unless noted):
| Case | Mass gain | Exposure |
|---|---|---|
| Laced, uncoated, hand-stitched leather, full submersion (lab) | **+55.8 %** saturation; ~90 % of saturation reached within 90 min; fastest uptake in the first 15 min | 90 min submerged |
| Laceless, coated leather (1950s type), submersion | 39 % less than the laced ball; not saturated at 90 min | 90 min submerged |
| FA test, uncoated ball, real match on a wet day (FA News 1953) | **+19 %** | 45 min of play |
| FA test, coated ball, same match | +3 % | 45 min |
| Press report 1909 (Sussex Express) | **+47 %** | 90 min (conditions not given) |
| Drying | only 0.7-4.3 % of mass lost in 90 min out of water; leather balls not fully dry after 7 days; repeated wetting left a starting mass up to +31 % next day | |
| "Doubled or tripled" (anecdote) | **not supported** | |

**Model** (fits the three data points; **D**, shape from PH-25):

```
m(t) = m0 * (1 + U * (1 - exp(-E / tau)))
  U    = 0.56          laced uncoated leather (0.34 for a 1950s coated ball, ~0.03 modern)
  tau  = 39 min        time constant at full submersion (90 % of U in 90 min)
  E    = integral of wetness(t) dt   (minutes of "effective submersion")
  wetness per minute: dry 0 · damp/dew 0.05 · drizzle 0.15 · rain or wet pitch 0.36 · heavy rain / waterlogged / mud 0.8
  drying while playing: ignore (0.7-4.3 % per 90 min)
  m0 for the next match: m0_dry * (1 + carry), carry 0..0.31 if the same ball was wet within ~7 days (the home club supplies the ball)
```
Calibration check: wetness 0.36 gives +19 % at 45 min (FA 1953) and +32 % at 90 min; wetness 0.8 gives +47 % at
90 min (1909). Ceiling +56 %.

Ball of 400 g dry:
| Minutes played | Dry | Drizzle (0.15) | Rain (0.36) | Waterlogged (0.8) |
|---|---|---|---|---|
| 0 | 400 g | 400 | 400 | 400 |
| 15 | 400 | 413 | 429 | 459 |
| 30 | 400 | 424 | 454 | 503 |
| 45 | 400 | 436 | 476 | 535 |
| 60 | 400 | 446 | 495 | 559 |
| 90 | 400 | 466 | 526 | 589 |
(computed from the model; ceiling 624 g)

Mud sticking to the case adds mass on top; no measurement found (**D**: up to +5 % on a muddy pitch, optional).

### 1.4 Bounce (coefficient of restitution, e = sqrt(rebound height / drop height))
| Case | e | Conf. | Basis |
|---|---|---|---|
| Modern FIFA ball on steel, 2 m drop | 0.82-0.88 (135-155 cm rebound) | H | FIFA Quality Pro spec (FIFA-Ball) |
| Older FIFA "approved" band on steel | 0.77-0.91 (120-165 cm) | M | FIFA-Ball (older spec, via patent) |
| 1888 leather ball on steel, dry | **0.75-0.85** (default 0.80) | D | no pressure rule; hand-pumped, softer leather |
| same, saturated (+56 %) | default **0.70** (-10 %) | D | heavier ball at the same pressure, wet leather damps |
| Ball on firm natural turf | 0.55-0.71 (0.60-1.00 m rebound from 2 m) | M | FIFA turf vertical rebound band (verify against the FIFA turf handbook) |
| Hard / frozen ground | **0.75-0.80** | D | near the ball-only value |
| Heavy, soft pitch | **0.40-0.50** | D | |
| Mud / standing water | **0.25-0.35**, plus rolling drag x2-x4 | D | ball "holds up" in mud (period reports), unmeasured |

Ground-dependent `e_ground` multiplies with the ball's own `e_ball` scale (wet ball 0.875 of dry). Rolling
friction and skid on wet grass: no historical data; tune in calibration (**D**).

### 1.5 Kicking and heading
- Header loading depends much more on **ball speed** than on ball mass or stiffness (Leeds, Tierney et al. 2020,
  DOI 10.1111/sms.13816). **H**
- Loughborough (Phillips et al. 2025, DOI 10.1177/17543371251397204) found corner-kick **arrival speeds at the
  header comparable** between modern and historic balls, dry and wet (title and abstract; full text not read). **M**
  Consequence for the engine: a wet heavy ball reaches the head at about the same speed, so its **momentum per
  header rises with mass** (+19 to +47 %), and a long kick needs more effort.
- Kick speed (impulse model, **D**): `v_ball = v_foot * M_eff * (1 + e_kick) / (M_eff + m_ball)`, with
  `M_eff` ~1.2 kg effective foot+boot mass (heavier boots raise it slightly). A 526 g wet ball comes off the foot
  about 7 % slower than a 400 g dry one for the same swing.
- Heading injury hooks (**D**): `concussionRisk ∝ m_ball * v_arrival`; lace-strike chance ~1 in 8 headers (lace
  sits on ~1 of 8 surface sectors), and a lace strike adds a small `cut/graze` chance, x2 when wet. Anecdotal basis
  only (LFCH); keep rare.

## 2. Kit
| Item | 1888-89 state | Numbers / effect (engine) | Changes | Conf. |
|---|---|---|---|---|
| Boots | heavy leather, ankle-high, hard (often steel) toecaps, leather bars or studs <= 0.5 in, fastenings flush | ~**500 g per boot dry**; wet "about double" (~1 kg) is repeated but unmeasured -> use +30 % wet (D). Effect: top speed -3 % and fatigue +5 % dry vs a 1930s light boot (D); toecap = toe-punt power +, ball control -; kick injury to opponents + | 1888 stud/bar specs; 1898 transverse bars, boot check; 1910; 1937 specs deleted; 1936 light continental boots (1/3 weight) | M (500 g), L (wet), D (effects) |
| Studs/bars | leather, 0.5 in max | grip on heavy ground +; slip chance on frozen ground high | 1925 replaceable studs (Dassler), 1950s screw-in | M |
| Shin pads | optional; 1874 Widdowson cricket pads outside stockings; smaller and inside stockings by c.1900 | shin/hack injury x0.6 when worn (D); small mass +100-200 g (D) | never compulsory before 1990 | H (history), D (effect) |
| Shirt | thick cotton or wool jersey, long sleeves; lace-up collars common (period photos; not sourced here) | wool takes water: +0.5-1.5 kg in rain (D); fatigue + in rain | keeper colour 1909 (scarlet/royal blue), green 1912 | M / D |
| Knickerbockers | must cover the knee (FA) | flannel/serge; small drag/fatigue + when wet (D) | rule dropped 1904 | M |
| Caps | no longer the team marker by 1888; honour caps from 1886 | flavour only; no protection value | - | M |
| Numbers | none | identification only | 1933 cup final one-off; 1939 League compulsory | H |
| Keeper | same shirt as team until 1909 | referee/umpire confusion chance (D) until 1909 | 1909, 1912 | M |

## 3. Suggested engine parameters (per season, from era events)
```
ball.radiusM 0.111 · ball.massDryG 400 (1889: 369-425 range; 1937: 397-454)
ball.waterUptakeMax 0.56 · ball.uptakeTauMin 39 · ball.restitution 0.80 · ball.wetRestitutionScale 0.875
ball.laced true (until the laceless event) · ball.laceStrikeShare 0.125
kit.bootMassG 500 · kit.bootWetGain 0.30 · kit.toecap true · kit.studMaxIn 0.5
kit.shinPadShare 0.6 (share of players wearing; rises over seasons) · kit.shinPadInjuryScale 0.6
kit.shirtWetMassKg 1.0 · kit.kneesCovered true (until 1904) · kit.keeperDistinct false (until 1909)
pitch.restitution {hard 0.78, firm 0.63, heavy 0.45, mud 0.30} · pitch.rollDrag {firm 1, heavy 2, mud 3.5}
```
All **D** except the masses, radius and uptake ceiling. They are inputs to the ECS design (`ws/E-design-ecs`) and
to calibration (E-05), not facts.

## Sources (beyond `ERA_TIMELINE.md`)
- [PH-25] Phillips et al. 2025, Sports Engineering 28:40 (open access): https://doi.org/10.1007/s12283-025-00521-8
- Phillips et al. 2025, arrival velocities of headers, modern vs historic balls: https://doi.org/10.1177/17543371251397204
- Tierney et al. 2020 (Leeds), ball speed vs mass in heading: https://biologicalsciences.leeds.ac.uk/news/article/276/faster-footballs-result-in-harder-headers (DOI 10.1111/sms.13816)
- [FIFA-Ball] FIFA Quality Programme for footballs, testing manual 2018: https://digitalhub.fifa.com/m/4e93a32cd03542cf/original/testing-manual-fifa-quality-programme-for-footballs-2018.pdf
- FIFA Quality Programme for football turf (vertical ball rebound band): https://inside.fifa.com/innovation/standards/football-turf
- [W-Boot] https://en.wikipedia.org/wiki/Football_boot · [SATRA] https://satra.com/bulletin/article.php?id=2311
- Medibank, evolution of the football boot (500 g dry, ~1 kg wet claim): https://www.medibank.com.au/livebetter/evolution-of-the-football-boot
- [LFCH] https://lfchistory.net/articles/4667
