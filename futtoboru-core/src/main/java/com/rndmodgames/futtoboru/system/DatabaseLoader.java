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
    private static List<Season> seasons = new ArrayList<>();
    private static List<Authority> authorities = new ArrayList<>();
    private static List<Competition> competitions = new ArrayList<>();
    private static List<Continent> continents = new ArrayList<>();
    private static List<Country> countries = new ArrayList<>();
    private static List<Club> clubs = new ArrayList<>();
    private static List<Person> persons = new ArrayList<>();

    //
    private static HashMap<Long, Continent> continentsById = new HashMap<>();
    private static HashMap<Long, Country> countriesById = new HashMap<>();
    private static HashMap<Long, Club> clubsById = new HashMap<>();
    private static HashMap<Long, Profession> professionsById = new HashMap<>();

    private static HashMap<Long, List<Country>> countriesByContinent = new HashMap<>();
    private static HashMap<Long, List<League>> leaguesByCountry = new HashMap<>();
    private static HashMap<Long, List<Club>> clubsByLeague = new HashMap<>();
    private static HashMap<Long, List<Club>> clubsByCountry = new HashMap<>();

    // by data id (canonical objects)
    private static HashMap<String, Country> countriesByDataId = new HashMap<>();
    private static HashMap<String, Club> clubsByDataId = new HashMap<>();
    private static HashMap<String, Person> personsByDataId = new HashMap<>();
    private static HashMap<String, Profession> professionsByDataId = new HashMap<>();
    private static HashMap<String, Competition> competitionsByDataId = new HashMap<>();
    private static HashMap<String, Season> seasonsByDataId = new HashMap<>();
    private static HashMap<String, Continent> continentsByDataId = new HashMap<>();
    private static HashMap<String, PlayerProfession> playerProfessionsByDataId = new HashMap<>();
    /** P2-14: every ground by data id (a club's season ground object, else one built from grounds.json) */
    private static HashMap<String, Stadium> groundsByDataId = new HashMap<>();

    //
    private static List<Profession> professions = new ArrayList<>();
    private static List<Profession> selectableProfessions = new ArrayList<>();

    // occupations of amateur players (PlayerProfession), modifiers, generator names
    private static Map<Long, PlayerProfession> playerProfessions = new LinkedHashMap<>();
    private static Map<Long, NationalityModifier> nationalityModifiers = new HashMap<>();
    private static Map<String, RegionModifier> regionModifiers = new LinkedHashMap<>(); // key: REGION_countryId
    private static Map<Long, List<String>> firstNamesByCountry = new HashMap<>();
    private static Map<Long, List<String>> lastNamesByCountry = new HashMap<>();

    // number of squad places that are runtime copies of a person already in another squad
    private static int personCopies;

    // keep track of main/top authority
    private static Authority mainAuthority;

    // the data the database was built from
    private static GameData gameData;

    // singleton
    private static DatabaseLoader instance;

    /**
     * The random source the database is generated from (P2-0). {@code Futtoboru.create()} injects
     * {@code game.random().fork("database")} before the first {@link #getInstance()}; without a game (tests, tools)
     * it is a freshly seeded source. Every player draws from its own fork ("player:" + id), so the data of one
     * player does not depend on load order.
     */
    private static RandomSource random = RandomSource.seeded(RandomSource.newSeed());

    /** The source the next {@link #getInstance()} load uses (and the fallback when code runs without a game). */
    public static synchronized RandomSource getRandom() {
        return random;
    }

    /** Sets the source for the next load; call before {@link #getInstance()} (or after {@link #resetDatabaseLoaderInstance()}). */
    public static synchronized void setRandom(RandomSource source) {
        if (source == null) {
            throw new IllegalArgumentException("random source is required");
        }
        random = source;
    }

    public DatabaseLoader() {

    }

    /**
     * Initialize the Game Data and Scripts in required order
     *
     * Synchronized and published only after loading: the headless create() thread and a test thread may both
     * ask for the database, and neither may see it half-loaded.
     */
    public static synchronized DatabaseLoader getInstance() {

        if (instance == null) {

            DatabaseLoader loaded = new DatabaseLoader();

            load(GameData.loadDefault(), random);

            instance = loaded;
        }

        //
        return instance;
    }

    /**
     * Rebuilds every object of the database from the game data already read (fresh, unplayed objects), e.g. before
     * a saved game is loaded so nothing of an earlier game in this session is left in the lookups.
     */
    public static synchronized void rebuild() {
        if (instance == null || gameData == null) {
            getInstance();
            return;
        }
        load(gameData, random);
    }

    /**
     * After a saved game is loaded (P2-5): the lookups the screens and engine use ({@link #getClubById},
     * {@link #getClubByDataId}, {@link #getClubs}, clubs by country, seasons' clubs, people by data id, competitions,
     * the main authority) return the game's objects, so there is one object per club, person and competition.
     * Data objects the save doesn't hold (clubs of countries not played) stay as they are.
     */
    public static synchronized void bindGame(List<Club> gameClubs, List<Person> gamePersons,
            List<Competition> gameCompetitions, Authority gameAuthority) {
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

        instance = null;
    }

    /**
     * Builds the whole database from game data. Missing month/day of birth dates, generated attributes, contracts
     * and occupations are drawn from forks of {@code rng}.
     */
    public static synchronized void load(GameData data, RandomSource rng) {

        clear();
        gameData = data;

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

    private static void clear() {
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
        mainAuthority = null;
    }

    // ------------------------------------------------------------------------------------------------- builders

    private static void loadContinents(GameData data) {
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

    private static void loadCountries(GameData data) {
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

    private static void loadNames(GameData data) {
        for (DataRecord r : data.records("names", null)) {
            Country country = countriesByDataId.get(r.ref("country"));
            firstNamesByCountry.computeIfAbsent(country.getId(), k -> new ArrayList<>()).addAll(r.strs("firstNames"));
            lastNamesByCountry.computeIfAbsent(country.getId(), k -> new ArrayList<>()).addAll(r.strs("lastNames"));
        }
    }

    /**
     * TODO: authorities are not in the data yet; the IFAB is the only (hard-coded) one
     */
    private static void loadAuthorities(IdAllocator dataIds) {
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

    private static void loadProfessions(GameData data) {
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

    private static void loadOccupations(GameData data) {
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

    private static void loadModifiers(GameData data, IdAllocator dataIds) {
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

    private static void loadCompetitions(GameData data) {
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
    private static void loadPeople(GameData data, RandomSource rng) {
        for (DataRecord r : data.all(GameData.PERSON)) {
            Person person = new Person();
            person.setId(r.runtimeId());
            person.setDataId(r.id());
            person.setName(r.str("firstName"));
            person.setLastname(r.str("lastName"));
            person.setCountry(countriesByDataId.get(r.ref("country")));
            person.setBirthDate(BirthDates.complete(r.str("born"), rng.fork("born:" + r.id()).asRandom()));
            persons.add(person);
            personsByDataId.put(person.getDataId(), person);
        }
    }

    /**
     * P2-14 (D30 A9): each season's {@code calendar} rules (engine/temporal/CalendarRules) and its cups' neutral
     * grounds (engine/cup/NeutralVenues), by the season's start year. A season without them keeps the latest
     * earlier season's; none at all = the code defaults.
     */
    private static void loadCalendars(GameData data) {
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

    private static String first(List<String> ids) {
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * P2-14: every ground by data id, for a match's venue. A club's season ground is that club's object; a ground
     * no club plays at (the neutral cup grounds) gets one built from grounds.json with its data number.
     */
    private static void loadGrounds(GameData data) {
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
    public static Stadium getGroundByDataId(String dataId) {
        return dataId == null ? null : groundsByDataId.get(canonical(GameData.GROUND, dataId));
    }

    private static void loadSeasons(GameData data, IdAllocator dataIds, RandomSource rng) {
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

            loadSquads(data, season, dataIds, rng);

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
    private static void loadSquads(GameData data, Season season, IdAllocator dataIds, RandomSource rng) {
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
            club.setClubBalance(sq.has("balance") ? sq.decimal("balance") : BigDecimal.ZERO);

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
            for (String personId : sq.refs("players")) {
                Person person = personsByDataId.get(personId);
                if (!listed.add(personId)) {
                    person = copyOf(person, dataIds.nextId());
                    copies++;
                }
                Player player = createPlayer(person, club, season, dataIds, rng);
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

    private static Person copyOf(Person canonical, long runtimeId) {
        Person copy = new Person();
        copy.setId(runtimeId);
        copy.setDataId(canonical.getDataId());
        copy.setName(canonical.getName());
        copy.setLastname(canonical.getLastname());
        copy.setCountry(canonical.getCountry());
        copy.setBirthDate(canonical.getBirthDate());
        return copy;
    }

    /** A loaded player: attributes, a random contract and an occupation, as the legacy PlayersLoader did. */
    private static Player createPlayer(Person person, Club club, Season season, IdAllocator dataIds, RandomSource rng) {
        Player player = new Player();

        // one stream per player (runtime ids of data are stable), so load order does not matter
        RandomSource playerRng = rng.fork("player:" + person.getId());

        // players and persons share ids (a Player is the football role of a Person)
        player.setId(person.getId());
        player.setPerson(person);

        // Note: the attribute generator uses the person's birth date + 20 years as the reference date
        LocalDateTime estimatedDate = person.getBirthDate().plusYears(20);
        new PlayerAttributeGenerator().generatePlayerAttributes(player, person, estimatedDate, playerRng.fork("attributes"));

        // For Season 1, contracts are randomly generated (ids from the data range: they are made at load)
        LocalDateTime seasonStartDate = season.getStartDate() != null ? season.getStartDate() : estimatedDate;
        PlayerContract contract = PlayerContractGenerator.generateRandomContract(player, club, seasonStartDate, dataIds,
                playerRng.fork("contract"));
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
    private static void applyAttributes(Object target, Map<String, Integer> attrs, String suffix) {
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

    private static String regionKey(String regionName, Long countryId) {
        return (regionName == null ? "" : regionName.toUpperCase(Locale.ROOT)) + "_" + countryId;
    }

    // ------------------------------------------------------------------------------------------------- getters

    /** @return the game data the database was built from (with its error/warning report) */
    public static GameData getGameData() {
        return gameData;
    }

    /** @return how many squad places are runtime copies of a person already listed in another squad */
    public static int getPersonCopies() {
        return personCopies;
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
    public static List<Country> getCountries() {
        return countries;
    }

    /** @return every club that has a squad, in data order */
    public static List<Club> getClubs() {
        return clubs;
    }

    /**
     * @return the complete list of Professions
     */
    public List<Profession> getProfessions() {
        return professions;
    }

    /** @return the canonical loaded people (one per record of people.json) */
    public static List<Person> getPersons() {
        return persons;
    }

    public static void setPersons(List<Person> persons) {
        DatabaseLoader.persons = persons;
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
    public static Country getCountryById(Long id) {
        return id == null ? null : countriesById.get(id);
    }

    /** @return a Continent by data id (aliases followed) or null */
    public static Continent getContinentByDataId(String dataId) {
        return continentsByDataId.get(canonical(GameData.CONTINENT, dataId));
    }

    /** @return an occupation of amateur players by data id (aliases followed) or null */
    public static PlayerProfession getPlayerProfessionByDataId(String dataId) {
        return playerProfessionsByDataId.get(canonical(GameData.OCCUPATION, dataId));
    }

    /** @return a Country by data id (e.g. {@code england}, aliases followed) or null */
    public static Country getCountryByDataId(String dataId) {
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
    public static Club getClubById(Long id) {
        return id == null ? null : clubsById.get(id);
    }

    /** @return a Club by data id (aliases followed) or null */
    public static Club getClubByDataId(String dataId) {
        return clubsByDataId.get(canonical(GameData.CLUB, dataId));
    }

    /** @return the canonical Person with this data id (aliases followed) or null */
    public static Person getPersonByDataId(String dataId) {
        return personsByDataId.get(canonical(GameData.PERSON, dataId));
    }

    /** @return a Competition by data id (aliases followed) or null */
    public static Competition getCompetitionByDataId(String dataId) {
        return competitionsByDataId.get(canonical(GameData.COMPETITION, dataId));
    }

    /** @return a Season by data id (aliases followed) or null */
    public static Season getSeasonByDataId(String dataId) {
        return seasonsByDataId.get(canonical(GameData.SEASON, dataId));
    }

    /**
     * @param id
     * @return a Profession by runtime ID or null
     */
    public static Profession getProfessionById(Long id) {
        return id == null ? null : professionsById.get(id);
    }

    /** @return a Profession by data id (e.g. {@link Profession#MANAGER}) or null */
    public static Profession getProfessionByDataId(String dataId) {
        return professionsByDataId.get(canonical(GameData.PROFESSION, dataId));
    }

    private static String canonical(String kind, String dataId) {
        if (dataId == null) {
            return null;
        }
        String c = gameData != null ? gameData.resolve(kind, dataId) : null;
        return c != null ? c : dataId.toLowerCase(Locale.ROOT);
    }

    /** @return the occupations of amateur players by runtime id (data order) */
    public static Map<Long, PlayerProfession> getAllPlayerProfessions() {
        return Collections.unmodifiableMap(playerProfessions);
    }

    /** @return the nationality modifier for a country runtime id, or null */
    public static NationalityModifier getNationalityModifier(Long countryId) {
        return nationalityModifiers.get(countryId);
    }

    /** @return the region modifier for a region name (any case) in a country, or null */
    public static RegionModifier getRegionModifier(String regionName, Long countryId) {
        return regionModifiers.get(regionKey(regionName, countryId));
    }

    /** @return the first names the generator uses for a country (empty when there are none) */
    public static List<String> getFirstNames(Country country) {
        List<String> names = country == null ? null : firstNamesByCountry.get(country.getId());
        return names == null ? Collections.emptyList() : names;
    }

    /** @return the last names the generator uses for a country (empty when there are none) */
    public static List<String> getLastNames(Country country) {
        List<String> names = country == null ? null : lastNamesByCountry.get(country.getId());
        return names == null ? Collections.emptyList() : names;
    }

    /**
     * Add a Club
     */
    public static void addClub(Club club) {

        //
        if (!clubsById.containsKey(club.getId())) {
            clubs.add(club);
            clubsById.put(club.getId(), club);
        }
    }

    public static HashMap<Long, List<Club>> getClubsByCountry() {
        return clubsByCountry;
    }

    public static void setClubsByCountry(HashMap<Long, List<Club>> clubsByCountry) {
        DatabaseLoader.clubsByCountry = clubsByCountry;
    }

    public static List<Competition> getCompetitions() {
        return competitions;
    }

    public static void setCompetitions(List<Competition> competitions) {
        DatabaseLoader.competitions = competitions;
    }

    public static Authority getMainAuthority() {
        return mainAuthority;
    }

    public static void setMainAuthority(Authority mainAuthority) {
        DatabaseLoader.mainAuthority = mainAuthority;
    }
}
