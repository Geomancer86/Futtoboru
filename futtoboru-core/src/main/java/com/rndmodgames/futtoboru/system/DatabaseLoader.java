package com.rndmodgames.futtoboru.system;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.NationalityModifier;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.RegionModifier;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.data.BirthDates;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.DataReport;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.IdAllocator;
import com.rndmodgames.futtoboru.system.data.LanguageData;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.futtoboru.system.generators.LanguageGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerContractGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerProfessionAssigner;
import com.rndmodgames.futtoboru.system.loaders.ScriptsLoader;

/**
 * Database Loader v2 (P2-4)
 *
 *  - Builds every game object from the JSON packs read by {@link GameData} (countries, continents, professions,
 *    occupations, names, competitions and editions, clubs, grounds, people, seasons, squads, modifiers)
 *  - Keeps the data in memory to be accessed later, by runtime id or by data id
 *
 * Runtime ids of data objects come from the data (below {@link IdAllocator#GAME_START}); objects made during play
 * get theirs from the game's allocator in the {@link SaveGame}.
 *
 * @author Geomancer86
 */
public class DatabaseLoader {

    //
    private List<Season> seasons = new ArrayList<>();
    private List<Authority> authorities = new ArrayList<>();
    private List<Competition> competitions = new ArrayList<>();
    private List<Continent> continents = new ArrayList<>();
    private List<Country> countries = new ArrayList<>();
    private List<Club> clubs = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();

    //
    private HashMap<Long, Continent> continentsById = new HashMap<>();
    private HashMap<Long, Country> countriesById = new HashMap<>();
    private HashMap<Long, Club> clubsById = new HashMap<>();
    private HashMap<Long, Profession> professionsById = new HashMap<>();

    private HashMap<Long, List<Country>> countriesByContinent = new HashMap<>();
    private HashMap<Long, List<League>> leaguesByCountry = new HashMap<>();
    private HashMap<Long, List<Club>> clubsByLeague = new HashMap<>();
    private HashMap<Long, List<Club>> clubsByCountry = new HashMap<>();

    // by data id (canonical objects)
    private HashMap<String, Country> countriesByDataId = new HashMap<>();
    private HashMap<String, Club> clubsByDataId = new HashMap<>();
    private HashMap<String, Person> personsByDataId = new HashMap<>();
    private HashMap<String, Profession> professionsByDataId = new HashMap<>();
    private HashMap<String, Competition> competitionsByDataId = new HashMap<>();
    private HashMap<String, Season> seasonsByDataId = new HashMap<>();
    private HashMap<String, Continent> continentsByDataId = new HashMap<>();
    private HashMap<String, PlayerProfession> playerProfessionsByDataId = new HashMap<>();
    /** P2-14: every ground by data id (a club's season ground object, else one built from grounds.json) */
    private HashMap<String, Stadium> groundsByDataId = new HashMap<>();

    //
    private List<Profession> professions = new ArrayList<>();
    private List<Profession> selectableProfessions = new ArrayList<>();

    // occupations of amateur players (PlayerProfession), modifiers, generator names
    private Map<Long, PlayerProfession> playerProfessions = new LinkedHashMap<>();
    private Map<Long, NationalityModifier> nationalityModifiers = new HashMap<>();
    private Map<String, RegionModifier> regionModifiers = new LinkedHashMap<>(); // key: REGION_countryId
    private Map<Long, List<String>> firstNamesByCountry = new HashMap<>();
    private Map<Long, List<String>> lastNamesByCountry = new HashMap<>();

    // number of squad places that are runtime copies of a person already in another squad
    private int personCopies;

    // G-04: the wage bands of each data season, by the year it starts (1888 = 1888-89)
    /** G-12a: each data season's club economies ({@code economy.json}) and League clubs, by its start year */
    private java.util.TreeMap<Integer, List<com.rndmodgames.futtoboru.data.ClubEconomy>> economies =
            new java.util.TreeMap<>();
    private java.util.TreeMap<Integer, Set<String>> seasonLeagueClubs = new java.util.TreeMap<>();
    private java.util.TreeMap<Integer, List<com.rndmodgames.futtoboru.data.ContractBand>> contractBands =
            new java.util.TreeMap<>();

    // keep track of main/top authority
    private Authority mainAuthority;

    // the data the database was built from
    private GameData gameData;
    private LanguageData languageData;
    private RecruitmentData recruitmentData;

    /**
     * The random source this database is generated from (P2-0). {@code Futtoboru.create()} injects
     * {@code game.random().fork("database")} before the first {@link #getInstance()}; without a game (tests, tools)
     * it is a freshly seeded source. Every player draws from its own fork ("player:" + id), so the data of one
     * player does not depend on load order.
     */
    private RandomSource random = RandomSource.seeded(RandomSource.newSeed());

    /** True once built (what the old singleton's "instance != null" meant); {@link #resetDatabaseLoaderInstance()} clears it. */
    private boolean loaded;

    /** True while {@link #reload} runs: the generators it calls look the database up and get it as it is so far. */
    private boolean loading;

    /**
     * RF-01: the game's database. The only static field of the class: the one place the engine, screens and tools
     * look the database up ({@link #getInstance()}); all data is instance state. It is created once and rebuilt in
     * place ({@link #rebuild()}, {@link #load}), so a reference to it never goes stale.
     */
    private static DatabaseLoader current;

    /** The database object, built or not (never loads). */
    private static synchronized DatabaseLoader holder() {
        if (current == null) {
            current = new DatabaseLoader();
        }
        return current;
    }

    /** The source the next {@link #getInstance()} load uses (and the fallback when code runs without a game). */
    public static synchronized RandomSource getRandom() {
        return holder().random;
    }

    /** Sets the source for the next load; call before {@link #getInstance()} (or after {@link #resetDatabaseLoaderInstance()}). */
    public static synchronized void setRandom(RandomSource source) {
        if (source == null) {
            throw new IllegalArgumentException("random source is required");
        }
        holder().random = source;
    }

    private DatabaseLoader() {

    }

    /**
     * The game's database, built from the default game data on first use.
     *
     * Synchronized and published only after loading: the headless create() thread and a test thread may both
     * ask for the database, and neither may see it half-loaded.
     */
    public static synchronized DatabaseLoader getInstance() {
        DatabaseLoader db = holder();
        if (!db.loaded && !db.loading) {
            db.reload(GameData.loadDefault(), db.random);
            db.loaded = true;
        }
        return db;
    }

    /**
     * Rebuilds every object of the database from the game data already read (fresh, unplayed objects), e.g. before
     * a saved game is loaded so nothing of an earlier game in this session is left in the lookups.
     */
    public static synchronized void rebuild() {
        DatabaseLoader db = holder();
        if (!db.loaded || db.gameData == null) {
            getInstance();
            return;
        }
        db.reload(db.gameData, db.random);
    }

    /**
     * Builds the whole database from {@code data}, drawing from {@code rng} (tests and tools that want a given seed).
     */
    public static synchronized void load(GameData data, RandomSource rng) {
        DatabaseLoader db = holder();
        db.reload(data, rng);
        db.loaded = true;
    }

    /**
     * After a saved game is loaded (P2-5): the lookups the screens and engine use ({@link #getClubById},
     * {@link #getClubByDataId}, {@link #getClubs}, clubs by country, seasons' clubs, people by data id, competitions,
     * the main authority) return the game's objects, so there is one object per club, person and competition.
     * Data objects the save doesn't hold (clubs of countries not played) stay as they are.
     */
    public void bindGame(List<Club> gameClubs, List<Person> gamePersons,
            List<Competition> gameCompetitions, Authority gameAuthority) {
        synchronized (DatabaseLoader.class) {
            for (Club club : gameClubs) {
                Club byId = clubsById.get(club.getId());
                final Club fresh = byId == null && club.getDataId() != null
                        ? clubsByDataId.get(canonical(GameData.CLUB, club.getDataId())) : byId;
                if (fresh == club) {
                    continue;
                }
                if (fresh != null) {
                    clubsById.remove(fresh.getId());
                    replace(clubs, fresh, club);
                    for (List<Club> byCountry : clubsByCountry.values()) {
                        replace(byCountry, fresh, club);
                    }
                    for (Season season : seasons) {
                        replace(season.getClubs(), fresh, club);
                    }
                    clubsByDataId.values().removeIf(c -> c == fresh);
                } else {
                    clubs.add(club);
                    if (club.getCountry() != null) {
                        clubsByCountry.computeIfAbsent(club.getCountry().getId(), k -> new ArrayList<>()).add(club);
                    }
                }
                clubsById.put(club.getId(), club);
                if (club.getDataId() != null) {
                    clubsByDataId.put(canonical(GameData.CLUB, club.getDataId()), club);
                }
            }
            Map<Long, Person> personsById = new HashMap<>();
            for (Person person : gamePersons) {
                personsById.put(person.getId(), person);
            }
            for (int i = 0; i < persons.size(); i++) {
                Person saved = personsById.get(persons.get(i).getId());
                if (saved != null) {
                    persons.set(i, saved);
                }
            }
            for (Map.Entry<String, Person> e : personsByDataId.entrySet()) {
                Person saved = personsById.get(e.getValue().getId());
                if (saved != null) {
                    e.setValue(saved);
                }
            }
            for (Competition competition : gameCompetitions) {
                for (int i = 0; i < competitions.size(); i++) {
                    Competition fresh = competitions.get(i);
                    if (fresh.getId() != null && fresh.getId().equals(competition.getId())) {
                        competitions.set(i, competition);
                        competitionsByDataId.values().removeIf(c -> c == fresh);
                        if (competition.getDataId() != null) {
                            competitionsByDataId.put(canonical(GameData.COMPETITION, competition.getDataId()), competition);
                        }
                    }
                }
            }
            if (gameAuthority != null) {
                for (int i = 0; i < authorities.size(); i++) {
                    if (authorities.get(i).getId() != null && authorities.get(i).getId().equals(gameAuthority.getId())) {
                        authorities.set(i, gameAuthority);
                    }
                }
                mainAuthority = gameAuthority;
            }
    
        }
    }

    private static <T> void replace(List<T> list, T fresh, T saved) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == fresh) {
                list.set(i, saved);
            }
        }
    }

    /**
     * Used between some unit tests to avoid issues with missing data
     */
    public static synchronized void resetDatabaseLoaderInstance() {
        if (current != null) {
            current.loaded = false;
        }
    }

    /**
     * Builds the whole database from game data. Missing month/day of birth dates, generated attributes, contracts
     * and occupations are drawn from forks of {@code rng}.
     */
    private void reload(GameData data, RandomSource rng) {
        loading = true;
        try {
            build(data, rng);
        } finally {
            loading = false;
        }
    }

    private void build(GameData data, RandomSource rng) {

        clear();
        gameData = data;
        languageData = LanguageData.from(data);
        recruitmentData = RecruitmentData.from(data);

        DataReport report = data.getReport();
        for (String error : report.getErrors()) {
            Log.error(Log.DATA_LOADING, "GameData", error);
        }
        for (String warning : report.getWarnings()) {
            Log.warn(Log.DATA_LOADING, "GameData", warning);
        }
        Log.info(Log.DATA_LOADING, "GameData",
                "Read packs " + data.getPacks() + ": " + report);

        // a copy: every build (start, rebuild before a load or a new game) hands out the same derived ids (P2-10)
        IdAllocator dataIds = new IdAllocator(data.getDataIdAllocator().peek());

        loadContinents(data);
        loadCountries(data);
        loadNames(data);
        loadAuthorities(dataIds);
        loadProfessions(data);
        loadOccupations(data);
        loadModifiers(data, dataIds);
        loadCompetitions(data);
        loadPeople(data, rng);
        loadCalendars(data); // P2-14: before the seasons, whose cup scripts run on the calendar's opening day
        loadSeasons(data, dataIds, rng);
        loadGrounds(data);

        if (dataIds.peek() >= IdAllocator.GAME_START) {
            Log.error(Log.DATA_LOADING, "GameData",
                    "Data runtime ids reached " + dataIds.peek() + ", the range of game objects starts at "
                            + IdAllocator.GAME_START);
        }
    }

    private void clear() {
        seasons = new ArrayList<>();
        authorities = new ArrayList<>();
        competitions = new ArrayList<>();
        continents = new ArrayList<>();
        countries = new ArrayList<>();
        clubs = new ArrayList<>();
        persons = new ArrayList<>();
        continentsById = new HashMap<>();
        countriesById = new HashMap<>();
        clubsById = new HashMap<>();
        professionsById = new HashMap<>();
        countriesByContinent = new HashMap<>();
        leaguesByCountry = new HashMap<>();
        clubsByLeague = new HashMap<>();
        clubsByCountry = new HashMap<>();
        countriesByDataId = new HashMap<>();
        clubsByDataId = new HashMap<>();
        personsByDataId = new HashMap<>();
        professionsByDataId = new HashMap<>();
        competitionsByDataId = new HashMap<>();
        seasonsByDataId = new HashMap<>();
        continentsByDataId = new HashMap<>();
        playerProfessionsByDataId = new HashMap<>();
        groundsByDataId = new HashMap<>();
        professions = new ArrayList<>();
        selectableProfessions = new ArrayList<>();
        playerProfessions = new LinkedHashMap<>();
        nationalityModifiers = new HashMap<>();
        regionModifiers = new LinkedHashMap<>();
        firstNamesByCountry = new HashMap<>();
        lastNamesByCountry = new HashMap<>();
        personCopies = 0;
        contractBands = new java.util.TreeMap<>();
        economies = new java.util.TreeMap<>();
        seasonLeagueClubs = new java.util.TreeMap<>();
        mainAuthority = null;
    }

    // ------------------------------------------------------------------------------------------------- builders

    private void loadContinents(GameData data) {
        for (DataRecord r : data.all(GameData.CONTINENT)) {
            Continent continent = new Continent();
            continent.setId(r.runtimeId());
            continent.setDataId(r.id());
            continent.setName(r.str("name"));
            continents.add(continent);
            continentsById.put(continent.getId(), continent);
            continentsByDataId.put(continent.getDataId(), continent);
            countriesByContinent.put(continent.getId(), new ArrayList<>());
        }
        Log.info(Log.DATA_LOADING, "FINISHED LOADING " + continents.size() + " CONTINENTS");
    }

    private void loadCountries(GameData data) {
        for (DataRecord r : data.all(GameData.COUNTRY)) {
            Country country = new Country();
            country.setId(r.runtimeId());
            country.setDataId(r.id());
            country.setCommonName(r.str("name"));
            country.setFormalName(r.str("formalName", ""));
            country.setContinent(continentsById.get(data.get(GameData.CONTINENT, r.ref("continent")).runtimeId()));
            country.setDefaultCountry(r.bool("selectableByDefault", false));
            countries.add(country);
            countriesById.put(country.getId(), country);
            countriesByDataId.put(country.getDataId(), country);
            countriesByContinent.get(country.getContinent().getId()).add(country);
            leaguesByCountry.put(country.getId(), new ArrayList<>());
            clubsByCountry.put(country.getId(), new ArrayList<>());
        }
        Log.info(Log.DATA_LOADING, "FINISHED LOADING " + countries.size() + " COUNTRIES");
    }

    private void loadNames(GameData data) {
        for (DataRecord r : data.records("names", null)) {
            Country country = countriesByDataId.get(r.ref("country"));
            firstNamesByCountry.computeIfAbsent(country.getId(), k -> new ArrayList<>()).addAll(r.strs("firstNames"));
            lastNamesByCountry.computeIfAbsent(country.getId(), k -> new ArrayList<>()).addAll(r.strs("lastNames"));
        }
    }

    /**
     * TODO: authorities are not in the data yet; the IFAB is the only (hard-coded) one
     */
    private void loadAuthorities(IdAllocator dataIds) {
        Authority ifab = new Authority();
        ifab.setId(dataIds.nextId());
        ifab.setName("International Football Association Board");
        ifab.setShortName("IFAB");
        ifab.setFoundingDate(LocalDateTime.of(1886, Month.JUNE, 2, 19, 30, 00)); // 2 June 1886
        ifab.setCountry(getCountryByDataId("england"));
        ifab.setLevel(1); // Top Level
        ifab.setParent(null); // No Authority on Top
        authorities.add(ifab);
        mainAuthority = ifab;
    }

    private void loadProfessions(GameData data) {
        for (DataRecord r : data.all(GameData.PROFESSION)) {
            Profession profession = new Profession();
            profession.setId(r.runtimeId());
            profession.setDataId(r.id());
            profession.setName(r.str("name"));
            profession.setPlayerSelectable(r.bool("playerSelectable", false));
            professions.add(profession);
            professionsById.put(profession.getId(), profession);
            professionsByDataId.put(profession.getDataId(), profession);
        }
        Log.info(Log.DATA_LOADING, "FINISHED LOADING " + professions.size() + " PROFESSIONS");
    }

    private void loadOccupations(GameData data) {
        for (DataRecord r : data.all(GameData.OCCUPATION)) {
            PlayerProfession occupation = new PlayerProfession();
            occupation.setId(r.runtimeId());
            occupation.setDataId(r.id());
            occupation.setName(r.str("name"));
            occupation.setCategory(r.str("category"));
            applyAttributes(occupation, r.attrs("bonuses"), "Bonus");
            occupation.setHistoricalNotes(r.str("notes"));
            playerProfessions.put(occupation.getId(), occupation);
            playerProfessionsByDataId.put(occupation.getDataId(), occupation);
        }
        Log.info(Log.DATA_LOADING, "Loaded " + playerProfessions.size() + " player professions");
    }

    private void loadModifiers(GameData data, IdAllocator dataIds) {
        // TODO: modifiers belong to a season; with one season they are global, as before
        for (DataRecord r : data.records("nationality_modifier", null)) {
            Country country = countriesByDataId.get(r.ref("country"));
            NationalityModifier modifier = new NationalityModifier();
            modifier.setId(dataIds.nextId());
            modifier.setDataId(country.getDataId());
            modifier.setCountryId(country.getId());
            modifier.setCountryName(country.getCommonName());
            applyAttributes(modifier, r.attrs("modifiers"), "Modifier");
            modifier.setHistoricalNotes(r.str("notes"));
            if (r.has("footballingStrength")) {
                modifier.setFootbalingStrength(r.decimal("footballingStrength").intValue());
            }
            nationalityModifiers.put(modifier.getCountryId(), modifier);
        }
        for (DataRecord r : data.all(GameData.REGION)) {
            Country country = countriesByDataId.get(r.ref("country"));
            RegionModifier modifier = new RegionModifier();
            modifier.setId(r.runtimeId());
            modifier.setDataId(r.id());
            modifier.setRegionName(r.str("name").toUpperCase(Locale.ROOT));
            modifier.setCountryId(country.getId());
            applyAttributes(modifier, r.attrs("modifiers"), "Modifier");
            if (!r.strs("places").isEmpty()) {
                modifier.setApplicableCities(String.join(",", r.strs("places")));
            }
            modifier.setHistoricalNotes(r.str("notes"));
            regionModifiers.put(regionKey(modifier.getRegionName(), modifier.getCountryId()), modifier);
        }
        Log.info(Log.DATA_LOADING, "Loaded " + nationalityModifiers.size()
                + " nationality modifiers and " + regionModifiers.size() + " region modifiers");
    }

    private void loadCompetitions(GameData data) {
        for (DataRecord r : data.all(GameData.COMPETITION)) {
            Competition competition = new Competition();
            competition.setId(r.runtimeId());
            competition.setDataId(r.id());
            competition.setName(r.str("name"));
            competition.setFullname(r.str("longName", r.str("name")));
            competition.setUrlSource(r.first("sources"));
            competition.setCompetitionType(r.str("type"));
            competitions.add(competition);
            competitionsByDataId.put(competition.getDataId(), competition);
        }
        Log.info(Log.DATA_LOADING, "Finished loading " + competitions.size() + " Competitions.");
    }

    /** The canonical Person of every record in people.json (birth dates completed from {@code rng.fork("born:" + id)}). */
    private void loadPeople(GameData data, RandomSource rng) {
        for (DataRecord r : data.all(GameData.PERSON)) {
            Person person = new Person();
            person.setId(r.runtimeId());
            person.setDataId(r.id());
            person.setName(r.str("firstName"));
            person.setLastname(r.str("lastName"));
            person.setCountry(countriesByDataId.get(r.ref("country")));
            person.setBirthDate(BirthDates.complete(r.str("born"), rng.fork("born:" + r.id()).asRandom()));
            // R-11a: languages on the person's own fork (never the player's stream: attributes are unchanged)
            LanguageGenerator.assign(person, languageData, data, rng);
            persons.add(person);
            personsByDataId.put(person.getDataId(), person);
        }
    }

    /**
     * P2-14 (D30 A9): each season's {@code calendar} rules (engine/temporal/CalendarRules) and its cups' neutral
     * grounds (engine/cup/NeutralVenues), by the season's start year. A season without them keeps the latest
     * earlier season's; none at all = the code defaults.
     */
    private void loadCalendars(GameData data) {
        Map<Integer, com.rndmodgames.futtoboru.engine.temporal.CalendarRules> calendars = new HashMap<>();
        Map<String, java.util.NavigableMap<Integer, com.rndmodgames.futtoboru.engine.cup.NeutralVenues.Rules>> neutral =
                new HashMap<>();
        for (DataRecord r : data.all(GameData.SEASON)) {
            int startYear = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.startYearOf(
                    LocalDateTime.parse(r.str("start")).toLocalDate());
            if (r.raw("calendar") != null) {
                calendars.put(startYear, com.rndmodgames.futtoboru.engine.temporal.CalendarRules.read(r.raw("calendar"),
                        i -> first(r.refs("calendar.cups[" + i + "].competition"))));
            }
            for (DataRecord e : r.children()) {
                if (e.raw("neutralVenues") != null && e.ref("competition") != null) {
                    neutral.computeIfAbsent(e.ref("competition"), k -> new java.util.TreeMap<>()).put(startYear,
                            com.rndmodgames.futtoboru.engine.cup.NeutralVenues.read(e.raw("neutralVenues"),
                                    i -> e.refs("neutralVenues.rounds[" + i + "].grounds")));
                }
            }
        }
        com.rndmodgames.futtoboru.engine.temporal.CalendarRules.install(calendars);
        com.rndmodgames.futtoboru.engine.cup.NeutralVenues.install(neutral);
        Log.info(Log.DATA_LOADING, "Season calendars: " + calendars.keySet() + "; neutral grounds: " + neutral.keySet());
    }

    private String first(List<String> ids) {
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * P2-14: every ground by data id, for a match's venue. A club's season ground is that club's object; a ground
     * no club plays at (the neutral cup grounds) gets one built from grounds.json with its data number.
     */
    private void loadGrounds(GameData data) {
        for (Club club : clubs) {
            if (club.getStadium() != null && club.getStadium().getDataId() != null) {
                groundsByDataId.putIfAbsent(club.getStadium().getDataId(), club.getStadium());
            }
        }
        for (DataRecord g : data.all(GameData.GROUND)) {
            if (groundsByDataId.containsKey(g.id())) {
                continue;
            }
            Stadium stadium = new Stadium();
            stadium.setId(g.runtimeId());
            stadium.setDataId(g.id());
            stadium.setName(g.str("name"));
            stadium.setBuiltYear(g.integer("opened"));
            stadium.setCapacity(g.integer("capacity"));
            stadium.setValue(g.decimal("value"));
            stadium.setDescription(g.str("description"));
            stadium.setUrlSource(g.first("sources"));
            stadium.setLandValue(g.decimal("landValue"));
            stadium.setOwned(g.bool("ownedByClub", false));
            stadium.setAnnualRent(g.decimal("annualRent"));
            groundsByDataId.put(g.id(), stadium);
        }
    }

    /** @return a ground by data id (aliases followed), or null (P2-14: a match's neutral venue) */
    public Stadium getGroundByDataId(String dataId) {
        return dataId == null ? null : groundsByDataId.get(canonical(GameData.GROUND, dataId));
    }

    private void loadSeasons(GameData data, IdAllocator dataIds, RandomSource rng) {
        for (DataRecord r : data.all(GameData.SEASON)) {
            Season season = new Season();
            season.setId(r.runtimeId());
            season.setDataId(r.id());
            season.setName(r.str("name"));
            season.setDescription(r.str("description"));
            season.setUrlSource(r.first("sources"));
            season.setStartDate(LocalDateTime.parse(r.str("start")));
            season.setCountries(new ArrayList<>());
            season.setLeagues(new ArrayList<>());
            season.setClubs(new ArrayList<>());
            for (String countryId : r.refs("countries")) {
                season.getCountries().add(countriesByDataId.get(countryId));
            }
            seasons.add(season);
            seasonsByDataId.put(season.getDataId(), season);

            Log.info(Log.DATA_LOADING, "LOADING SEASON " + season.getName());

            // G-04: the season's wage bands and its league clubs, before the squads' contracts are made
            int startYear = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(season.getStartDate())
                    .getStartYear();
            List<com.rndmodgames.futtoboru.data.ContractBand> bands = loadContractBands(data, season.getDataId());
            if (!bands.isEmpty()) {
                contractBands.put(startYear, bands);
            }
            Set<String> leagueClubs = new HashSet<>();
            for (DataRecord e : r.children()) {
                Competition competition = competitionsByDataId.get(e.ref("competition"));
                if (competition != null && Competition.COMPETITION_LEAGUE.equals(competition.getCompetitionType())) {
                    leagueClubs.addAll(e.refs("participants"));
                }
            }

            economies.put(startYear, loadEconomies(data, season.getDataId()));
            seasonLeagueClubs.put(startYear, leagueClubs);

            loadSquads(data, season, dataIds, rng, startYear, leagueClubs);

            // Editions of this season (participants by runtime club id)
            Map<CompetitionEdition, Competition> editions = new LinkedHashMap<>();
            for (DataRecord e : r.children()) {
                Competition competition = competitionsByDataId.get(e.ref("competition"));
                CompetitionEdition edition = new CompetitionEdition();
                edition.setId(e.runtimeId());
                edition.setDataId(e.id());
                edition.setName(e.str("name"));
                edition.setUrlSource(e.first("sources"));
                for (String clubId : e.refs("participants")) {
                    Club club = clubsByDataId.get(clubId);
                    if (club == null) {
                        Log.error(Log.DATA_LOADING, "GameData", e.where()
                                + ": club `" + clubId + "` has no squad in season " + season.getDataId() + ", left out");
                        continue;
                    }
                    edition.getParticipantClubsIds().add(club.getId());
                }
                competition.getEditions().add(edition);
                editions.put(edition, competition);
            }

            // Season scripts (league and cup creation) from the editions
            ScriptsLoader.load(season, editions);
        }
        Log.info(Log.DATA_LOADING, "FINISHED LOADING " + seasons.size() + " SEASONS");
    }

    /**
     * Clubs of a season with their ground, balance and players (squads.json).
     *
     * A person listed in several squads of the season (legacy copied rosters, see docs/data/REVIEW_1888-89.md)
     * keeps today's gameplay: the first squad gets the canonical Person, later squads a runtime copy with the same
     * data id. Lookups by data id return the canonical one.
     */
    /** G-04: the wage bands of a season's {@code contracts.json} (money read in shillings, kept in pounds). */
    private List<com.rndmodgames.futtoboru.data.ContractBand> loadContractBands(GameData data, String season) {
        List<com.rndmodgames.futtoboru.data.ContractBand> bands = new ArrayList<>();
        for (DataRecord r : data.records("contract_band", season)) {
            com.rndmodgames.futtoboru.data.ContractBand b = new com.rndmodgames.futtoboru.data.ContractBand();
            b.setStanding(r.str("standing"));
            b.setClubs(new ArrayList<>(r.refs("clubs")));
            b.setProfessionalShare(r.decimal("professionalShare").doubleValue());
            b.setSeasonWageMin(com.rndmodgames.futtoboru.data.ContractBand.pounds(r.decimal("seasonWageMin")));
            b.setSeasonWageMax(com.rndmodgames.futtoboru.data.ContractBand.pounds(r.decimal("seasonWageMax")));
            b.setSummerWageMin(com.rndmodgames.futtoboru.data.ContractBand.pounds(r.decimal("summerWageMin")));
            b.setSummerWageMax(com.rndmodgames.futtoboru.data.ContractBand.pounds(r.decimal("summerWageMax")));
            b.setSummerPayShare(r.decimal("summerPayShare").doubleValue());
            b.setAmateurExpenses(com.rndmodgames.futtoboru.data.ContractBand.pounds(r.decimal("amateurExpenses")));
            b.setMinimumSquad(r.integer("minimumSquad"));
            b.setSquadSize(r.integer("squadSize"));
            b.setConfidence(r.str("confidence"));
            bands.add(b);
        }
        return bands;
    }

    /** G-12a: the club economies of a season's {@code economy.json} (admission in pence, money in pounds). */
    private List<com.rndmodgames.futtoboru.data.ClubEconomy> loadEconomies(GameData data, String season) {
        List<com.rndmodgames.futtoboru.data.ClubEconomy> out = new ArrayList<>();
        for (DataRecord r : data.records("economy", season)) {
            com.rndmodgames.futtoboru.data.ClubEconomy e = new com.rndmodgames.futtoboru.data.ClubEconomy();
            e.setStanding(r.str("standing"));
            e.setStartingBalance(r.decimal("startingBalance"));
            e.setLeagueAdmissionPence(r.decimal("leagueAdmissionPence").doubleValue());
            e.setCupAdmissionPence(r.decimal("cupAdmissionPence").doubleValue());
            e.setFriendlyAdmissionPence(r.decimal("friendlyAdmissionPence").doubleValue());
            e.setFriendlyCrowd(r.decimal("friendlyCrowd").doubleValue());
            e.setFriendlyCrowdMax(r.integer("friendlyCrowdMax"));
            e.setSubscriptions(r.decimal("subscriptions"));
            e.setOverheads(r.decimal("overheads").doubleValue());
            e.setFriendlyChance(r.decimal("friendlyChance").doubleValue());
            e.setConfidence(r.str("confidence"));
            out.add(e);
        }
        return out;
    }

    private void loadSquads(GameData data, Season season, IdAllocator dataIds, RandomSource rng, int startYear,
            Set<String> leagueClubs) {
        Set<String> listed = new HashSet<>();
        Set<String> usedGrounds = new HashSet<>();
        int copies = 0;

        for (DataRecord sq : data.records("squad", season.getDataId())) {
            DataRecord clubRecord = data.get(GameData.CLUB, sq.ref("club"));
            if (clubsByDataId.containsKey(clubRecord.id())) {
                Log.error(Log.DATA_LOADING, "GameData", sq.where()
                        + ": club `" + clubRecord.id() + "` has a second squad this season, skipped");
                continue;
            }
            Club club = new Club();
            club.setId(clubRecord.runtimeId());
            club.setDataId(clubRecord.id());
            club.setName(clubRecord.str("name"));
            club.setFullName(clubRecord.str("fullName", clubRecord.str("name")));
            club.setUrlSource(clubRecord.first("sources"));
            club.setYear(clubRecord.integer("founded"));
            club.setCountry(countriesByDataId.get(clubRecord.ref("country")));
            // G-12a: a squad's own balance, else its standing's starting balance (economy.json)
            club.setClubBalance(sq.has("balance") ? sq.decimal("balance")
                    : startingBalance(startYear, club.getDataId(), leagueClubs.contains(club.getDataId())));

            // Ground
            if (sq.ref("ground") != null) {
                DataRecord g = data.get(GameData.GROUND, sq.ref("ground"));
                Stadium stadium = new Stadium();
                // a ground shared by two clubs: the second club gets its own copy (same data id)
                stadium.setId(usedGrounds.add(g.id()) ? g.runtimeId() : dataIds.nextId());
                stadium.setDataId(g.id());
                stadium.setName(g.str("name"));
                stadium.setBuiltYear(g.integer("opened"));
                stadium.setCapacity(g.integer("capacity"));
                stadium.setValue(g.decimal("value"));
                stadium.setDescription(g.str("description"));
                stadium.setUrlSource(g.first("sources"));
                stadium.setLandValue(g.decimal("landValue"));
                stadium.setOwned(g.bool("ownedByClub", false));
                stadium.setAnnualRent(g.decimal("annualRent"));
                stadium.setClubOwnerId(club.getId());
                club.setStadium(stadium);
            }

            // Players (E-01: this season's position and any sourced dribbling come from the squad entry)
            Map<String, Position> positions = data.squadPositions(sq);
            Map<String, Integer> dribbling = data.squadDribbling(sq);
            com.rndmodgames.futtoboru.data.ContractBand band = com.rndmodgames.futtoboru.data.ContractBand.forClub(
                    getContractBands(startYear), club.getDataId(), leagueClubs.contains(club.getDataId()));
            for (String personId : sq.refs("players")) {
                Person person = personsByDataId.get(personId);
                if (!listed.add(personId)) {
                    person = copyOf(person, dataIds.nextId());
                    copies++;
                }
                Player player = createPlayer(person, club, season, dataIds, rng, startYear, band);
                player.setPosition(positions.get(personId));
                if (dribbling.containsKey(personId)) {
                    player.setDribbling(dribbling.get(personId).floatValue());
                }
                club.getPlayers().add(player);
            }
            // E-01b: a squad player without a positions entry (a validator warning) gets the slot the squad is
            // shortest of, so every squad can field a 2-3-5
            int filled = com.rndmodgames.futtoboru.system.generators.PositionAssigner.fillMissing(club.getPlayers());
            if (filled > 0) {
                Log.warn(Log.DATA_LOADING, "GameData", "season " + season.getDataId() + ": " + filled
                        + " players of " + club.getDataId() + " have no position in the data; assigned a 2-3-5 slot");
            }

            season.getClubs().add(club);
            clubs.add(club);
            clubsById.put(club.getId(), club);
            clubsByDataId.put(club.getDataId(), club);
            clubsByCountry.get(club.getCountry().getId()).add(club);
        }

        personCopies += copies;
        if (copies > 0) {
            Log.warn(Log.DATA_LOADING, "GameData", "season "
                    + season.getDataId() + ": " + copies + " squad places are people already listed in another squad;"
                    + " they play as separate copies until the data review (docs/data/REVIEW_1888-89.md)");
        }
        Log.info(Log.DATA_LOADING, "FINISHED LOADING " + season.getClubs().size() + " SEASON CLUBS");
    }

    private Person copyOf(Person canonical, long runtimeId) {
        Person copy = new Person();
        copy.setId(runtimeId);
        copy.setDataId(canonical.getDataId());
        copy.setName(canonical.getName());
        copy.setLastname(canonical.getLastname());
        copy.setCountry(canonical.getCountry());
        copy.setBirthDate(canonical.getBirthDate());
        copy.setLanguages(new ArrayList<>(canonical.getLanguages()));
        copy.setHomeLanguage(canonical.getHomeLanguage());
        copy.setBirthArea(canonical.getBirthArea());
        copy.setParentsOrigin(canonical.getParentsOrigin());
        return copy;
    }

    /**
     * A loaded player: attributes, his engagement for the season (G-04: one season to 30 April, terms from the club's
     * wage band) and an occupation.
     */
    private Player createPlayer(Person person, Club club, Season season, IdAllocator dataIds, RandomSource rng,
            int startYear, com.rndmodgames.futtoboru.data.ContractBand band) {
        Player player = new Player();

        // one stream per player (runtime ids of data are stable), so load order does not matter
        RandomSource playerRng = rng.fork("player:" + person.getId());

        // players and persons share ids (a Player is the football role of a Person)
        player.setId(person.getId());
        player.setPerson(person);

        // Note: the attribute generator uses the person's birth date + 20 years as the reference date
        LocalDateTime estimatedDate = person.getBirthDate().plusYears(20);
        new PlayerAttributeGenerator().generatePlayerAttributes(player, person, estimatedDate, playerRng.fork("attributes"));

        // The season's engagement (ids from the data range: they are made at load)
        LocalDateTime seasonStartDate = season.getStartDate() != null ? season.getStartDate() : estimatedDate;
        PlayerContract contract = PlayerContractGenerator.generateSeasonContract(player, club, seasonStartDate,
                startYear, band, dataIds, playerRng.fork("contract"));
        if (contract != null) {
            club.addPlayerContract(contract);
        }

        // Professional players won't get occupations (full-time football)
        PlayerProfessionAssigner.assignProfession(player, club, contract != null ? contract.getContractType() : null,
                playerRng.fork("profession"));

        if (person.getCurrentClubId() == null) {
            person.setCurrentClubId(club.getId());
        }
        return player;
    }

    /** Sets {@code set<Attribute><suffix>(Integer)} for each attribute (e.g. setStrengthBonus). */
    private void applyAttributes(Object target, Map<String, Integer> attrs, String suffix) {
        for (Map.Entry<String, Integer> a : attrs.entrySet()) {
            String name = "set" + Character.toUpperCase(a.getKey().charAt(0)) + a.getKey().substring(1) + suffix;
            try {
                Method m = target.getClass().getMethod(name, Integer.class);
                m.invoke(target, a.getValue());
            } catch (ReflectiveOperationException e) {
                Log.error(Log.DATA_LOADING, "GameData",
                        "no " + name + " on " + target.getClass().getSimpleName());
            }
        }
    }

    private String regionKey(String regionName, Long countryId) {
        return (regionName == null ? "" : regionName.toUpperCase(Locale.ROOT)) + "_" + countryId;
    }

    // ------------------------------------------------------------------------------------------------- getters

    /** @return the game data the database was built from (with its error/warning report) */
    public GameData getGameData() {
        return gameData;
    }

    /** R-11a: the languages, rate areas and rates of the data (null before the first load) */
    public LanguageData getLanguageData() {
        return languageData;
    }

    /** R-02: the registration rules, wages, origins and origin clubs of the data (null before the first load) */
    public RecruitmentData getRecruitmentData() {
        return recruitmentData;
    }

    /** @return how many squad places are runtime copies of a person already listed in another squad */
    public int getPersonCopies() {
        return personCopies;
    }

    /**
     * G-04: the wage bands for the season starting in {@code startYear}: those of the latest data season starting
     * on or before it (1889-90 plays on 1888-89's until the data has its own), else the earliest data season's, else
     * the built-in bands.
     */
    /** G-12a: the starting balance of a club of the season starting in {@code startYear}, by its standing */
    private BigDecimal startingBalance(int startYear, String clubDataId, boolean leagueClub) {
        String standing = com.rndmodgames.futtoboru.data.ContractBand.forClub(getContractBands(startYear), clubDataId,
                leagueClub).getStanding();
        return com.rndmodgames.futtoboru.data.ClubEconomy.forStanding(getEconomies(startYear), standing)
                .getStartingBalance();
    }

    /** G-12a: the club economies of the latest data season starting on or before {@code startYear} (may be empty) */
    public List<com.rndmodgames.futtoboru.data.ClubEconomy> getEconomies(int startYear) {
        synchronized (DatabaseLoader.class) {
            Map.Entry<Integer, List<com.rndmodgames.futtoboru.data.ClubEconomy>> e = economies.floorEntry(startYear);
            if (e == null) {
                e = economies.firstEntry();
            }
            return e != null ? e.getValue() : java.util.Collections.emptyList();
        }
    }

    /** G-12a: the League clubs (data ids) of the latest data season starting on or before {@code startYear} */
    public Set<String> getSeasonLeagueClubs(int startYear) {
        synchronized (DatabaseLoader.class) {
            Map.Entry<Integer, Set<String>> e = seasonLeagueClubs.floorEntry(startYear);
            if (e == null) {
                e = seasonLeagueClubs.firstEntry();
            }
            return e != null ? e.getValue() : java.util.Collections.emptySet();
        }
    }

    public List<com.rndmodgames.futtoboru.data.ContractBand> getContractBands(int startYear) {
        synchronized (DatabaseLoader.class) {
            Map.Entry<Integer, List<com.rndmodgames.futtoboru.data.ContractBand>> e = contractBands.floorEntry(startYear);
            if (e == null) {
                e = contractBands.firstEntry();
            }
            return e != null ? e.getValue() : com.rndmodgames.futtoboru.data.ContractBand.defaults();
        }
    }

    /**
     * @return the complete list of Seasons
     */
    public List<Season> getSeasons() {

        //
        return seasons;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    /**
     * @return the complete list of Continents
     */
    public List<Continent> getContinents(){

        //
        return continents;
    }

    /** @return every country, in data order */
    public List<Country> getCountries() {
        return countries;
    }

    /** @return every club that has a squad, in data order */
    public List<Club> getClubs() {
        return clubs;
    }

    /**
     * @return the complete list of Professions
     */
    public List<Profession> getProfessions() {
        return professions;
    }

    /** @return the canonical loaded people (one per record of people.json) */
    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    /**
     * @return the List of selectable or not Player Professions
     */
    public List<Profession> getSelectableProfessions() {

        //
        if (selectableProfessions.isEmpty()) {

            for (Profession profession : professions) {

                if (profession.getPlayerSelectable()) {

                    selectableProfessions.add(profession);
                }
            }

        }

        return selectableProfessions;
    }

    public List<Competition> getCompetitionsByAuthority(Authority authority){

        return null;
    }

    public List<Competition> getCompetitionsByCountry(Country country) {

        return null;
    }

    /**
     * Returns a Country by runtime ID or null if doesn't exist
     */
    public Country getCountryById(Long id) {
        return id == null ? null : countriesById.get(id);
    }

    /** @return a Continent by data id (aliases followed) or null */
    public Continent getContinentByDataId(String dataId) {
        return continentsByDataId.get(canonical(GameData.CONTINENT, dataId));
    }

    /** @return an occupation of amateur players by data id (aliases followed) or null */
    public PlayerProfession getPlayerProfessionByDataId(String dataId) {
        return playerProfessionsByDataId.get(canonical(GameData.OCCUPATION, dataId));
    }

    /** @return a Country by data id (e.g. {@code england}, aliases followed) or null */
    public Country getCountryByDataId(String dataId) {
        return countriesByDataId.get(canonical(GameData.COUNTRY, dataId));
    }

    /**
     * @param A Continent
     * @return the list of available Countries by Continent
     */
    public List<Country> getCountriesByContinent(Continent continent){

        //
        return countriesByContinent.get(continent.getId());
    }

    /**
     * @param A Country
     * @return the list of available Leagues by Continent
     */
    public List<League> getLeaguesByCountry(Country country){

        //
        return leaguesByCountry.get(country.getId());
    }

    /**
     * @param a League
     * @return the list of available Clubs by League
     */
    public List<Club> getClubsByLeague(League league) {

        return clubsByLeague.get(league.getId());
    }

    public List<Club> getClubsByCountry(Country country){

        return clubsByCountry.get(country.getId());
    }

    /**
     * @param id
     * @return a Club by runtime ID or null
     */
    public Club getClubById(Long id) {
        return id == null ? null : clubsById.get(id);
    }

    /** @return a Club by data id (aliases followed) or null */
    public Club getClubByDataId(String dataId) {
        return clubsByDataId.get(canonical(GameData.CLUB, dataId));
    }

    /** @return the canonical Person with this data id (aliases followed) or null */
    public Person getPersonByDataId(String dataId) {
        return personsByDataId.get(canonical(GameData.PERSON, dataId));
    }

    /** @return a Competition by data id (aliases followed) or null */
    public Competition getCompetitionByDataId(String dataId) {
        return competitionsByDataId.get(canonical(GameData.COMPETITION, dataId));
    }

    /** @return a Season by data id (aliases followed) or null */
    public Season getSeasonByDataId(String dataId) {
        return seasonsByDataId.get(canonical(GameData.SEASON, dataId));
    }

    /**
     * @param id
     * @return a Profession by runtime ID or null
     */
    public Profession getProfessionById(Long id) {
        return id == null ? null : professionsById.get(id);
    }

    /** @return a Profession by data id (e.g. {@link Profession#MANAGER}) or null */
    public Profession getProfessionByDataId(String dataId) {
        return professionsByDataId.get(canonical(GameData.PROFESSION, dataId));
    }

    private String canonical(String kind, String dataId) {
        if (dataId == null) {
            return null;
        }
        String c = gameData != null ? gameData.resolve(kind, dataId) : null;
        return c != null ? c : dataId.toLowerCase(Locale.ROOT);
    }

    /** @return the occupations of amateur players by runtime id (data order) */
    public Map<Long, PlayerProfession> getAllPlayerProfessions() {
        return Collections.unmodifiableMap(playerProfessions);
    }

    /** @return the nationality modifier for a country runtime id, or null */
    public NationalityModifier getNationalityModifier(Long countryId) {
        return nationalityModifiers.get(countryId);
    }

    /** @return the region modifier for a region name (any case) in a country, or null */
    public RegionModifier getRegionModifier(String regionName, Long countryId) {
        return regionModifiers.get(regionKey(regionName, countryId));
    }

    /** @return the first names the generator uses for a country (empty when there are none) */
    public List<String> getFirstNames(Country country) {
        List<String> names = country == null ? null : firstNamesByCountry.get(country.getId());
        return names == null ? Collections.emptyList() : names;
    }

    /** @return the last names the generator uses for a country (empty when there are none) */
    public List<String> getLastNames(Country country) {
        List<String> names = country == null ? null : lastNamesByCountry.get(country.getId());
        return names == null ? Collections.emptyList() : names;
    }

    /**
     * Add a Club
     */
    public void addClub(Club club) {

        //
        if (!clubsById.containsKey(club.getId())) {
            clubs.add(club);
            clubsById.put(club.getId(), club);
        }
    }

    public HashMap<Long, List<Club>> getClubsByCountry() {
        return clubsByCountry;
    }

    public void setClubsByCountry(HashMap<Long, List<Club>> clubsByCountry) {
        this.clubsByCountry = clubsByCountry;
    }

    public List<Competition> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
    }

    public Authority getMainAuthority() {
        return mainAuthority;
    }

    public void setMainAuthority(Authority mainAuthority) {
        this.mainAuthority = mainAuthority;
    }
}
