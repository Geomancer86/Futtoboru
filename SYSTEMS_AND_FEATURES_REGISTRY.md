# Futtoboru — Systems & Features Registry

**Document type:** Feature inventory  
**Analysis date:** 2025-06-25  
**Branch:** `feature/competitions-finances-seasons`  
**Status legend:** ✅ Complete | ⚠️ Partial | 🔲 Stub | ❌ Missing | 🐛 Broken

---

## 1. Core engine systems

| System | Status | Description | Key files |
|--------|--------|-------------|-----------|
| Game engine / day advance | ⚠️ | 12-step daily pipeline | `engine/FuttoboruGameEngine.java` |
| Script execution | ⚠️ | Only league creation script | `engine/ScriptsManager.java` |
| Authority manager | 🐛 | Intended for cups/fixtures; `currentGame` stale → no-op | `engine/AuthorityManager.java` |
| Match scheduler | ⚠️ | Ticket sales, friendly proposals | `engine/temporal/MatchScheduler.java` |
| Match simulator | ⚠️ | Simple strength-based; overwrites attendance | `engine/simulation/MatchSimulator.java` |
| League fixture generator | ⚠️ | Double RR; ignores `matchFormat` | `engine/temporal/LeagueFixtureGenerator.java` |
| Competition scheduler | 🔲 | `initializeCompetitions()` empty; draw stub | `engine/temporal/CompetitionScheduler.java` |
| League standings | ⚠️ | Calculate/sort works; completion unwired | `engine/simulation/LeagueStandingsManager.java` |
| **Season manager** | ❌ | Does not exist | — |
| **Season transition** | ❌ | Does not exist | — |
| Message / inbox | ✅ | Delivery, mandatory blocks, categories | `engine/messages/MessageManager.java` |
| Expense calculator | ✅ | Weekly historical expenses + wages | `engine/finances/ExpenseCalculator.java` |
| Job manager | ⚠️ | Backend complete; load/init gaps | `engine/jobs/JobManager.java` |
| Club staff manager | ⚠️ | Staff map on clubs | `engine/jobs/ClubStaffManager.java` |
| Player attribute generator | ⚠️ | 3d6 + modifiers; daily changes placeholder | `system/generators/PlayerAttributeGenerator.java` |
| Player contract generator | ✅ | Wages, types, bonuses | `system/generators/PlayerContractGenerator.java` |
| Name / person generator | ✅ | Random names | `system/generators/NameGenerator.java` |
| **Player lifecycle (regen/retire/death)** | ❌ **Not in v1** | Static cohort; `birthDate` only | `data/Person.java`, `PersonGenerator.java` |
| **Automated E2E harness** | ❌ | `GameTestHarness` planned | — |

---

## 2. Data & persistence

| System | Status | Description | Key files |
|--------|--------|-------------|-----------|
| Database loader (static) | ✅ | Countries, seasons, clubs, competitions | `system/DatabaseLoader.java` |
| Seasons loader | ⚠️ | One season; comments on re-election | `system/loaders/SeasonsLoader.java` |
| Scripts loader | ⚠️ | Hardcoded league script; rules null | `system/loaders/ScriptsLoader.java` |
| Players loader | ✅ | Squads, contracts, IDs | `system/loaders/PlayersLoader.java` |
| Stadiums loader | ⚠️ | 12+ stadiums; some clubs missing | `system/loaders/StadiumsLoader.java` |
| Competitions loader | ⚠️ | FA Cup edition loads; no execution | `system/loaders/CompetitionsLoader.java` |
| Nationality/region modifiers | ✅ | Attribute modifiers | `system/loaders/NationalityModifiersLoader.java` |
| Player professions loader | ✅ | 80 professions | `system/loaders/PlayerProfessionsLoader.java` |
| Save game model | ⚠️ | No season identity field | `system/SaveGame.java` |
| Save/load system | ⚠️ | JSON works; no versioning, weak validation | `system/SaveLoadSystem.java` |
| Preferences | ✅ | Locale, resolution, fullscreen | `PreferencesManager.java` |

---

## 3. Domain models

| Model | Status | Notes |
|-------|--------|-------|
| `Club` | ✅ | Matches, finances, players, contracts, staff |
| `Player` | ✅ | Full FM-style attributes |
| `Person` | ✅ | Owner/manager identity |
| `Match` | ✅ | Types: league, friendly, cup |
| `League` | ⚠️ | Rules attached; ID often unset |
| `Competition` / `CompetitionEdition` | ⚠️ | Structure exists; edition never finalized |
| `CompetitionRules` | ✅ | Historical + modern factories |
| `Season` | ⚠️ | Scripts list; only one season populated |
| `PlayerContract` | ✅ | Full contract model |
| `ClubExpenses` / `MatchIncome` | ✅ | Financial tracking |
| `Message` | ✅ | Inbox with mandatory flag |
| `JobOpening` / `JobApplication` / `JobOffer` | ✅ | Unemployed career |
| `PlayerAttributeSnapshot` | ✅ | Weekly tracking |
| `BasicScript` | ⚠️ | Only type 1000 defined |
| `SeasonLeague` | 🔲 | Empty class |

---

## 4. Game screens (pre-game)

| Screen | Status | Notes | File |
|--------|--------|-------|------|
| Splash | ⚠️ | Basic | `screens/SplashScreen.java` |
| Main menu | ⚠️ | Load game partial | `screens/MenuScreen.java` |
| Settings | ⚠️ | Resolution, locale | `screens/SettingsScreen.java` |
| New game setup | ⚠️ | Country selection | `screens/NewGameSetupScreen.java` |
| New manager | ⚠️ | Create person | `screens/NewManagerScreen.java` |
| New game season | ⚠️ | One season only | `screens/NewGameSeasonScreen.java` |
| New game overview | ⚠️ | Initializes save, jobs | `screens/NewGameOverviewScreen.java` |
| Main game shell | ⚠️ | Sidebar + content area | `screens/MainGameScreen.java` |

---

## 5. In-game UI tables (sidebar-routed)

| Screen | Status | Missing / issues | File |
|--------|--------|------------------|------|
| **Home** | 🔲 | Placeholder button only | `tables/main/HomeScreenTable.java` |
| **Inbox** | ✅ | Filters, mandatory routing | `tables/inbox/InboxScreenTable.java` |
| **Personal details** | ✅ | Basic owner info | `tables/person/PersonDetailsScreenTable.java` |
| **Authority** | ⚠️ | Hardcoded season 0 | `tables/authority/AuthorityScreenTable.java` |
| **Squad** | ⚠️ | No fog of war, injuries | `tables/squad/SquadScreenTable.java` |
| **Players list** | ✅ | Sortable, links to detail | `tables/squad/PlayersListTable.java` |
| **Schedule** | ⚠️ | Events model incomplete | `tables/schedule/ScheduleScreenTable.java` |
| **Fixtures** | ⚠️ | Limited date window | `tables/schedule/FixturesTable.java` |
| **Arrange friendly** | ⚠️ | Hardcoded opponents | `tables/schedule/ArrangeFriendlyTable.java` |
| **Match preview** | 🔲 | "Home vs Away" only | `tables/match/preview/MatchPreviewScreenTable.java` |
| **Match result** | ⚠️ | Latest match only | `tables/match/result/MatchResultScreenTable.java` |
| **Match history** | 🐛 | Clicks don't open result | `tables/match/history/MatchHistoryScreenTable.java` |
| **Competitions** | ⚠️ | Leagues yes; cups WIP | `tables/competitions/CompetitionsScreenTable.java` |
| **League detail** | ✅ | Standings, rules, fixtures | `tables/competitions/LeagueDetailScreenTable.java` |
| **League draw** | ✅ | Mandatory step-through | `tables/draw/LeagueDrawScreenTable.java` |
| **Club info** | ✅ | Stadium, balance, staff | `tables/club/ClubInfoScreenTable.java` |
| **Finances** | ⚠️ | Wages shown but season boundary approximate | `tables/finances/FinancesScreenTable.java` |
| **Player detail** | ✅ | Attributes, contract, modifiers | `tables/player/PlayerDetailScreenTable.java` |
| **Club browser** | ✅ | Unemployed path | `tables/jobs/ClubBrowserScreenTable.java` |
| **Club detail** | ⚠️ | Apply flow; debug logging | `tables/club/ClubDetailScreenTable.java` |
| **Job board** | ⚠️ | May fail after load | `tables/jobs/JobBoardScreenTable.java` |
| **My applications** | ⚠️ | Refresh gaps | `tables/jobs/MyApplicationsScreenTable.java` |
| **Job offers** | ⚠️ | Depends on job init | `tables/jobs/JobOfferScreenTable.java` |
| **Negotiation** | ⚠️ | Basic salary/length | `tables/jobs/NegotiationScreenTable.java` |

---

## 6. Orphaned / unwired UI (not in sidebar)

| Screen | Status | File |
|--------|--------|------|
| Training | 🔲 | `tables/main/TrainingScreenTable.java` |
| Tactics | 🔲 | `tables/main/TacticsScreenTable.java` |
| Board | 🔲 | `tables/main/BoardScreenTable.java` |
| Staff | 🔲 | `tables/main/StaffScreenTable.java` |
| Scouting | 🔲 | `tables/main/ScoutingScreenTable.java` |
| Transfers | 🔲 | `tables/main/TransfersScreenTable.java` |
| B-Team | 🔲 | `tables/main/BTeamScreenTable.java` |
| Under-18 | 🔲 | `tables/main/Under18TeamScreenTable.java` |
| Team report | 🔲 | `tables/main/TeamReportScreenTable.java` |
| Squad (duplicate) | 🔲 | `tables/main/SquadScreenTable.java` |
| League table (authority) | 🔲 | Empty class |
| Confederations table | 🔲 | Empty class |

---

## 7. Top menu & navigation

| Component | Status | Notes |
|-----------|--------|-------|
| Continue button | 🐛 | Double-click advances 2 days |
| Save game | ⚠️ | Works; hardcoded filename |
| Load game | 🔲 | "NOT IMPLEMENTED" in top menu |
| Settings (in-game) | 🔲 | Not implemented |
| Quit to menu | 🔲 | Not implemented |
| Match preview routing | ⚠️ | Player club only |
| Mandatory draw block | ✅ | Works |
| Sidebar (employed) | ⚠️ | 11 buttons; missing tactics/training/board/staff |
| Sidebar (unemployed) | ⚠️ | Club browser, job board, applications |
| Screen ID routing | ⚠️ | Magic numbers; LEAGUE_SCREEN unused |

---

## 8. Gameplay features matrix

| Feature | v1 needed? | Status | Blocker |
|---------|----------|--------|---------|
| New game flow | Yes | ⚠️ | One season only |
| Day progression | Yes | ⚠️ | Double-click bug |
| League creation (1888) | Yes | ✅ | — |
| Fixture draw | Yes | ✅ | League ID sometimes null |
| League match simulation | Yes | ⚠️ | Wrong rules |
| League standings | Yes | ⚠️ | Wrong rules |
| **Season completion** | **Yes** | **❌** | Not implemented |
| **Season rollover** | **Yes** | **❌** | Not implemented |
| **Per-season rules** | **Yes** | **❌** | Hardcoded 3-1-0 |
| Mid-season rule change | Optional | ❌ | No script type |
| Re-election (1888) | Optional | ❌ | Comments only |
| FA Cup | Optional | 🔲 | Data only |
| Friendlies | Optional | ⚠️ | Hardcoded clubs |
| Ticket sales | Yes | ⚠️ | Away club bug |
| Weekly expenses | Yes | ✅ | — |
| Player wages | Yes | ✅ | — |
| Player attributes | Yes | ⚠️ | Generated; sim uses subset |
| Contracts | Yes | ✅ | — |
| Save/load | Yes | ⚠️ | Load breaks jobs |
| Unemployed career | Optional | ⚠️ | UI/backend gaps |
| Inbox | Yes | ✅ | — |
| Training | No (v1) | 🔲 | — |
| Tactics | No (v1) | 🔲 | — |
| Transfers | No (v1) | 🔲 | — |

---

## 9. Mod data completeness (season 18)

| Data file | Status | Path |
|-----------|--------|------|
| `seasons.txt` | ⚠️ 1 entry | `mods/seasons.txt` |
| `clubs.txt` | ✅ 35 clubs | `mods/seasons/18/clubs.txt` |
| `club_players/*.txt` | ✅ 12 files | `mods/seasons/18/club_players/` |
| `club_stadiums/*.txt` | ⚠️ 12+ files | `mods/seasons/18/club_stadiums/` |
| `competitions/` | ⚠️ FA Cup edition | `mods/seasons/18/competitions/` |
| `nationality_modifiers.txt` | ✅ | `mods/seasons/18/` |
| `region_modifiers.txt` | ✅ | `mods/seasons/18/` |
| `player_professions.txt` | ✅ | `mods/seasons/18/` |
| `competition_rules.txt` | ❌ | Does not exist |
| Season 19+ data | ❌ | Does not exist |

---

## 10. Test coverage map

**Target state:** [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)

| Area | Test exists? | Planned test |
|------|--------------|--------------|
| DB load | ✅ | `DatabaseLoaderTests` |
| Competitions load/draw | ✅ | `CompetitionsLoaderTests` |
| League script | ✅ | `ScriptManagerTests` |
| Season mod static | ✅ | `SeasonLeaguesAndTeamsTests` |
| Players load | ✅ | `PlayersLoaderTests` |
| Stadiums load | ✅ | `StadiumsLoaderTests` |
| Mandatory gate / draw block | ❌ | `MandatoryGateIT` |
| Match simulation | ❌ | `MatchSimulatorTest` |
| Season completion | ❌ | `LeagueSeasonIT` |
| FA Cup season | ❌ | `FaCupSeasonIT`, `LeagueAndCupIT` |
| Season rollover | ❌ | `SeasonPlaythroughIT` |
| Multi-season soak | ❌ | `MultiSeasonSoakIT`, `LongRunSoakIT` |
| Player lifecycle static | ❌ | `PlayerLifecycleIT` |
| Save/load roundtrip | ❌ | `SaveLoadIT` |
| Rules application | ❌ | `CompetitionRulesLoaderTest` |
| Finances | ❌ | — |

---

## 11. Summary counts

| Status | Engine | UI screens | Gameplay features |
|--------|--------|------------|-------------------|
| ✅ Complete | 6 | 11 | 4 |
| ⚠️ Partial | 14 | 14 | 12 |
| 🔲 Stub | 2 | 12 | 3 |
| ❌ Missing | 2 | 0 | 6 |
| 🐛 Broken | 1 | 2 | 2 |

**For v1.0:** **`GameTestHarness` + season/cup/soak ITs** alongside **Season manager**, **FA Cup execution**, and **per-season rules** — see [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md).
