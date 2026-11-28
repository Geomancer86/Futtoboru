package com.rndmodgames.futtoboru.system.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

/**
 * Game data v2 (P2-4): reads the JSON packs under {@code gamedata/} (decision D14).
 *
 * {@code index.txt} lists the packs in load order, each {@code pack.json} lists its files. The checks are the same
 * as {@code tools/data/validate.py}: an unknown or missing field, a bad id or date, a duplicate id (ignoring case),
 * a reference that doesn't resolve after the aliases, a file listed but missing (or present but not listed, when
 * the folder can be listed) are errors; suspicious data is a warning. A broken record is skipped and reported, it
 * never stops the game.
 *
 * Every record with a data id gets as runtime id its number in the pack's append-only {@code ids.json} registry
 * (P2-5): numbers never change, so saved games keep pointing at the same records when the data is corrected. A
 * renamed record (old id in {@code aliases.json}) inherits the old id's number. A record with no number (a pack
 * without registry, or a record added without running {@code tools/data/assign_ids.py}) gets a temporary one from
 * {@link IdAllocator#DERIVED_START}, with a warning. Objects built at load from the data continue that range.
 */
public class GameData {

    /** Where the packs are read from. */
    public interface Source {

        /** @return the UTF-8 text of a file under the data root, or null when it doesn't exist */
        String read(String path);

        /** @return every {@code .json} file under a folder (paths relative to that folder), or null if unknown */
        List<String> listJson(String folder);

        /** @return the sub-folders of the data root, or null if the root can't be listed (inside a jar) */
        List<String> listFolders();
    }

    // Kinds of data ids (same list as validate.py)
    public static final String CONTINENT = "continent";
    public static final String COUNTRY = "country";
    public static final String PROFESSION = "profession";
    public static final String OCCUPATION = "occupation";
    public static final String COMPETITION = "competition";
    public static final String CLUB = "club";
    public static final String GROUND = "ground";
    public static final String PERSON = "person";
    public static final String REGION = "region";
    public static final String SEASON = "season";
    public static final String EDITION = "edition";
    // R-02 recruitment data, R-11a languages
    public static final String RULE = "rule";
    public static final String WAGE = "wage";
    public static final String ORIGIN = "origin";
    public static final String ORIGIN_CLUB = "origin-club";
    public static final String AREA = "area";
    public static final String LANGUAGE = "language";
    // G-02 injury kinds
    public static final String INJURY = "injury";
    // G-10 fan-love meter rules
    public static final String FAN_RULE = "fan-rule";

    static final List<String> KINDS = Arrays.asList(CONTINENT, COUNTRY, PROFESSION, OCCUPATION, COMPETITION, CLUB,
            GROUND, PERSON, REGION, SEASON, EDITION, RULE, WAGE, ORIGIN, ORIGIN_CLUB, AREA, LANGUAGE,
            INJURY, FAN_RULE);

    /** R-02 / R-11a: the grades that may have no source (validate.py UNSOURCED_OK). */
    static final Set<String> UNSOURCED_OK = new HashSet<>(Arrays.asList("estimate", "placeholder"));
    /** R-02: the value fields a rule of each kind needs. */
    static final Map<String, String[]> RULE_VALUES = new HashMap<>();
    static {
        RULE_VALUES.put("residence", new String[] { "miles", "years" });
        RULE_VALUES.put("registration-wait", new String[] { "days" });
        RULE_VALUES.put("max-wage", new String[] { "pence" });
    }
    /** D28 Q12: an origin's attribute nudge is a whole number in -2..2; literacy moves one level at most. */
    static final int NUDGE_MAX = 2;
    static final int LITERACY_SHIFT_MAX = 1;
    /** R-11a: the rate fields of a language_rate row; readWriteIfSigns has one share per level 1..4. */
    static final String[] RATE_FIELDS = { "speaks", "only", "signsMen", "signsWomen", "parentsBorn",
            "readWriteIfSigns" };
    static final int READ_WRITE_LEVELS = 4;

    static final Pattern ID_RE = Pattern.compile("^([a-z0-9]+(-[a-z0-9]+)*\\.)?[a-z0-9]+(-[a-z0-9]+)*$");
    static final Pattern DATE_RE = Pattern.compile("^\\d{4}(-(0[1-9]|1[0-2])(-(0[1-9]|[12]\\d|3[01]))?)?$");
    static final Pattern DATETIME_RE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");

    static final Set<String> ATTRS = new HashSet<>(Arrays.asList("strength", "endurance", "stamina", "speed",
            "acceleration", "jumping", "dexterity", "concentration", "courage", "determination", "leadership",
            "perception", "positioning", "teamwork"));
    static final Map<String, Set<String>> ENUMS = new HashMap<>();
    static {
        ENUMS.put("confidence", new TreeSet<>(Arrays.asList("verified", "unverified", "placeholder")));
        ENUMS.put("occupation_category", new TreeSet<>(Arrays.asList("TEXTILE", "RAILWAY", "MINING", "CONSTRUCTION",
                "FACTORY", "TRANSPORT", "EDUCATION", "SKILLED_TRADE", "AGRICULTURE", "SERVICE", "OTHER")));
        ENUMS.put("competition_type", new TreeSet<>(Arrays.asList("LEAGUE", "CUP")));
        // E-01: the 2-3-5 slots (data/Position) and where a squad entry's position comes from
        Set<String> positions = new TreeSet<>();
        for (com.rndmodgames.futtoboru.data.Position p : com.rndmodgames.futtoboru.data.Position.values()) {
            positions.add(p.getDataId());
        }
        ENUMS.put("position", positions);
        // H-05: what kind of body a club grew out of (same list as tools/data/validate.py)
        ENUMS.put("club_origin", new TreeSet<>(Arrays.asList("church", "cricket", "works", "school", "old-boys",
                "rugby", "merger", "other")));
        // H-04 sources: docs/data/history/POSITIONS_1888-89.md (same list as tools/data/validate.py)
        ENUMS.put("position_source", new TreeSet<>(Arrays.asList("players-1888-89", "players-1888-89-line",
                "placeholder", "wp-season-1888-89", "wp-season-1888-89-line", "wp-player", "wp-player-line",
                "fa-cup-final-1889")));
        // G-04: the wage band a club's men are engaged on (same list as tools/data/validate.py)
        ENUMS.put("club_standing", new TreeSet<>(Arrays.asList("league", "other", "amateur")));
        // R-02 / R-11a (same lists as tools/data/validate.py)
        ENUMS.put("injury_kind", new TreeSet<>(Arrays.asList("knock", "passenger", "carried-off")));
        // G-10: the fan-love meter's rules (same list as tools/data/validate.py)
        ENUMS.put("fan_rule_kind", new TreeSet<>(Arrays.asList("appearance", "goal", "derby-goal", "cup-tie-won",
                "cup-final-won", "absence", "band", "hysteresis", "legend-damping", "jitter")));
        ENUMS.put("data_confidence", new TreeSet<>(Arrays.asList("high", "medium", "low", "estimate", "placeholder")));
        ENUMS.put("rule_kind", new TreeSet<>(Arrays.asList("registration", "residence", "second-registration-consent",
                "registration-wait", "scottish-professional-ban", "retain-and-transfer", "max-wage")));
        ENUMS.put("rule_body", new TreeSet<>(Arrays.asList("fa", "fa-council", "football-league", "sfa")));
        ENUMS.put("applies_to", new TreeSet<>(Arrays.asList("professional", "amateur", "all")));
        ENUMS.put("wage_type", new TreeSet<>(Arrays.asList("example", "band")));
        ENUMS.put("standing", new TreeSet<>(Arrays.asList("leading", "middling", "small", "any")));
        ENUMS.put("source_kind", new TreeSet<>(Arrays.asList("local-junior", "works-team", "scottish-import",
                "league-poach", "released", "amateur", "veteran", "schoolboy")));
        ENUMS.put("origin_club_kind", new TreeSet<>(Arrays.asList("church", "sunday-school", "works", "junior",
                "school", "scottish")));
        // P2-14: the season calendar and a cup's neutral grounds (same lists as tools/data/validate.py)
        ENUMS.put("weekday", new TreeSet<>(Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday",
                "saturday", "sunday")));
        ENUMS.put("cup_round", new TreeSet<>(Arrays.asList("quarter-final", "semi-final", "final")));
        ENUMS.put("neutral_gate", new TreeSet<>(Arrays.asList("association", "shared", "home")));
        ENUMS.put("later_draws", new TreeSet<>(Arrays.asList("open-draw", "bracket")));
    }

    /** H-05: the fan-love scale of a squad entry. */
    static final int FAN_TIER_MIN = 0;
    static final int FAN_TIER_MAX = 5;
    /** H-05: specs whose records need links as sources (and a forward period). */
    static final Set<String> SOURCED = new HashSet<>(Arrays.asList("nickname", "club_name", "history_club",
            "history_person"));
    static final Pattern LANG_RE = Pattern.compile("^[a-z]{2,3}(-[A-Z]{2})?$");

    /** The attribute scale (3d6 plus modifiers, PlayerAttributeGenerator): a sourced dribbling value is inside. */
    static final int ATTR_MIN = 3;
    static final int ATTR_MAX = 23;

    static final Pattern MONTHDAY_RE = Pattern.compile("^(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
    private static final int[] DAYS_IN_MONTH = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /** @return true for a valid month-day {@code MM-DD} (29 February allowed) */
    static boolean isMonthDay(JsonValue v) {
        if (v == null || !v.isString() || !MONTHDAY_RE.matcher(v.asString()).matches()) {
            return false;
        }
        String s = v.asString();
        return Integer.parseInt(s.substring(3)) <= DAYS_IN_MONTH[Integer.parseInt(s.substring(0, 2)) - 1];
    }

    /** A field of a record spec: its type and whether it is required. */
    static final class Field {
        final String type;
        final boolean required;

        Field(String type, boolean required) {
            this.type = type;
            this.required = required;
        }
    }

    /** Record specs: spec name -> field -> type. {@code "str!"} means a required text. */
    static final Map<String, Map<String, Field>> SPECS = new HashMap<>();

    private static void spec(String name, String... fields) {
        Map<String, Field> m = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            String t = fields[i + 1];
            boolean req = t.endsWith("!");
            m.put(fields[i], new Field(req ? t.substring(0, t.length() - 1) : t, req));
        }
        SPECS.put(name, m);
    }

    static {
        spec("continent", "id", "str!", "name", "str!");
        spec("country", "id", "str!", "name", "str!", "formalName", "str", "continent", "ref:continent!",
                "selectableByDefault", "bool");
        spec("profession", "id", "str!", "name", "str!", "playerSelectable", "bool!");
        spec("occupation", "id", "str!", "name", "str!", "category", "enum:occupation_category!", "bonuses", "attrs!",
                "notes", "str");
        spec("names", "country", "ref:country!", "firstNames", "strs!", "lastNames", "strs!");
        // G-08: the bottom clubs seek re-election at the annual meeting (seasons starting in From..Until)
        spec("competition", "id", "str!", "name", "str!", "longName", "str", "type", "enum:competition_type!",
                "sources", "strs!", "notes", "str", "reElectionPlaces", "int", "reElectionFrom", "int",
                "reElectionUntil", "int", "annualMeetingAfterDays", "int", "annualMeetingConfidence",
                "enum:confidence");
        // H-05: origin, nicknames, rivals, nameHistory (the prose is in text/<lang>/history.json)
        spec("club", "id", "str!", "name", "str!", "fullName", "str", "founded", "int", "country", "ref:country!",
                "sources", "strs!", "notes", "str", "origin", "enum:club_origin", "nicknames", "list:nickname",
                "rivals", "refs:club", "nameHistory", "list:club_name");
        spec("nickname", "name", "str!", "from", "int", "to", "int", "period", "bool", "sources", "strs!");
        spec("club_name", "name", "str!", "from", "int!", "to", "int", "sources", "strs!");
        spec("history_club", "hook", "str!", "history", "strs!", "seasons", "seasontexts", "sources", "strs!");
        spec("history_person", "bio", "str!", "sources", "strs!");
        spec("ground", "id", "str!", "name", "str!", "opened", "int", "capacity", "int", "value", "num",
                "description", "str", "landValue", "num", "ownedByClub", "bool", "annualRent", "num",
                "sources", "strs!", "notes", "str");
        spec("person", "id", "str!", "firstName", "str!", "lastName", "str!", "born", "date!", "country",
                "ref:country!", "confidence", "enum:confidence!", "sources", "strs!", "notes", "str",
                "birthArea", "ref:area", "nicknames", "list:nickname", "knownAs", "str");
        spec("season", "id", "str!", "name", "str!", "description", "str", "start", "datetime!", "sources", "strs!",
                "countries", "refs:country!", "editions", "list:edition!", "calendar", "obj:calendar");
        spec("edition", "id", "str!", "competition", "ref:competition!", "name", "str!", "participants",
                "refs:club!", "sources", "strs!", "notes", "str", "neutralVenues", "obj:neutral_venues");
        // P2-14 (D30 A9): the season calendar (engine/temporal/CalendarRules) and a cup's neutral grounds
        // (engine/cup/NeutralVenues); a date rule needs exactly one anchor (checkRules), as in validate.py
        spec("calendar", "league", "obj:calendar_league", "cups", "list:calendar_cup", "notes", "str");
        spec("calendar_league", "opens", "obj:day_rule!", "closes", "obj:day_rule!", "midweek", "enum:weekday",
                "noMatchOn", "monthdays", "sources", "strs!", "notes", "str");
        spec("calendar_cup", "competition", "ref:competition!", "opens", "obj:day_rule!", "firstDraw", "obj:day_rule!",
                "laterDraws", "enum:later_draws!", "drawDaysAfterRound", "int", "final", "obj:day_rule!",
                "properFrom", "monthday!", "daysBetweenRounds", "int!", "earlyRoundsOn", "enum:weekday",
                "maxEarlyRounds", "int", "sources", "strs!", "notes", "str");
        spec("day_rule", "onOrAfter", "monthday", "onOrBefore", "monthday", "weekday", "enum:weekday!",
                "confidence", "enum:confidence!", "source", "str", "notes", "str");
        spec("neutral_venues", "rounds", "list:neutral_round!", "gate", "enum:neutral_gate!", "gateConfidence",
                "enum:confidence!", "sources", "strs!", "notes", "str");
        spec("neutral_round", "round", "enum:cup_round!", "grounds", "refs:ground!", "confidence", "enum:confidence!",
                "notes", "str");
        // E-01 positions / dribbling: keyed by a person id of `players` (checked in checkSquadRoles)
        spec("squad", "club", "ref:club!", "ground", "ref:ground", "balance", "num", "players", "refs:person!",
                "notes", "str", "positions", "map:squad_position", "dribbling", "map:squad_dribbling", "fanTiers", "tiers:person");
        spec("squad_position", "position", "enum:position!", "source", "enum:position_source!");
        spec("squad_dribbling", "value", "int!", "source", "str!");
        spec("nationality_modifier", "country", "ref:country!", "modifiers", "attrs!", "notes", "str",
                "footballingStrength", "num");
        spec("region", "id", "str!", "name", "str!", "country", "ref:country!", "modifiers", "attrs!",
                "places", "strs", "notes", "str");
        // one real match of a season (E-00b): reference data only (D17), read by the history screens and calibration
        // tests, never to schedule or decide a match; "played or walkover" is checked by validate.py
        spec("result", "edition", "ref:edition!", "round", "str", "date", "date", "home", "ref:club!", "away",
                "ref:club!", "homeGoals", "int", "awayGoals", "int", "walkover", "ref:club", "attendance", "int",
                "venue", "str", "source", "str!", "notes", "str");
        // G-04: one-season engagements of a season per club standing, money in shillings a week (DATA_MODEL.md)
        spec("contract_band", "standing", "enum:club_standing!", "clubs", "refs:club", "professionalShare", "num!",
                "seasonWageMin", "num!", "seasonWageMax", "num!", "summerWageMin", "num!", "summerWageMax", "num!",
                "summerPayShare", "num!", "amateurExpenses", "num!", "minimumSquad", "int!", "squadSize", "int!",
                "confidence", "enum:confidence!", "sources", "strs!", "notes", "str");
        spec("pack", "id", "str!", "name", "str!", "version", "int!", "dataVersion", "int!", "seasons", "strs!",
                "files", "strs!");
        // R-02 (docs/design/RECRUITMENT_AND_TRIALS.md §6): money in pence (240 to the pound)
        spec("rule", "id", "str!", "rule", "enum:rule_kind!", "from", "date!", "until", "date", "body",
                "enum:rule_body!", "appliesTo", "enum:applies_to!", "days", "int", "miles", "int", "years", "int",
                "pence", "int", "confidence", "enum:data_confidence!", "sources", "strs!", "notes", "str");
        spec("wage", "id", "str!", "type", "enum:wage_type!", "from", "date!", "until", "date", "standing",
                "enum:standing!", "club", "ref:club", "person", "ref:person", "weeklyPence", "int",
                "summerWeeklyPence", "int", "minWeeklyPence", "int", "maxWeeklyPence", "int", "confidence",
                "enum:data_confidence!", "sources", "strs!", "notes", "str");
        spec("origin", "id", "str!", "name", "str!", "occupations", "refs:occupation!", "sourceKinds",
                "enums:source_kind!", "nudges", "attrs!", "literacyShift", "int!", "confidence",
                "enum:data_confidence!", "sources", "strs!", "notes", "str");
        spec("origin_club", "id", "str!", "name", "str!", "kind", "enum:origin_club_kind!", "place", "str!",
                "country", "ref:country!", "area", "ref:area", "founded", "int", "club", "ref:club", "origins",
                "refs:origin", "confidence", "enum:confidence!", "sources", "strs!", "notes", "str");
        // R-11a (design §10)
        spec("language", "id", "str!", "name", "str!", "countries", "refs:country!", "sources", "strs!", "notes",
                "str");
        spec("area", "id", "str!", "name", "str!", "country", "ref:country!", "weight", "num!", "places", "strs",
                "confidence", "enum:data_confidence!", "sources", "strs!", "notes", "str");
        // G-02 (D19-3): one record per injury kind
        spec("injury", "id", "str!", "kind", "enum:injury_kind!", "chancePerMatch", "num!", "minDays", "int!",
                "maxDays", "int!", "returnChance", "num", "strengthWhileHurt", "num", "confidence",
                "enum:data_confidence!", "sources", "strs!", "notes", "str");
        // G-10: one record per fan-meter rule
        spec("fan_rule", "id", "str!", "rule", "enum:fan_rule_kind!", "value", "num!", "confidence",
                "enum:data_confidence!", "sources", "strs!", "notes", "str");
        spec("language_rate", "area", "ref:area", "countries", "refs:country", "bornFrom", "int!", "bornUntil",
                "int!", "speaks", "shares:language", "only", "shares:language", "signsMen", "num", "signsWomen",
                "num", "parentsBorn", "shares:country", "readWriteIfSigns", "nums", "confidence",
                "enum:data_confidence!", "sources", "strs!", "notes", "str");
    }

    /** Pack-level file -> {record spec, id kind or null}. */
    static final Map<String, String[]> PACK_FILES = new HashMap<>();
    /** Season file -> {record spec, id kind or null}. */
    static final Map<String, String[]> SEASON_FILES = new HashMap<>();
    static {
        PACK_FILES.put("continents.json", new String[] { "continent", CONTINENT });
        PACK_FILES.put("countries.json", new String[] { "country", COUNTRY });
        PACK_FILES.put("professions.json", new String[] { "profession", PROFESSION });
        PACK_FILES.put("occupations.json", new String[] { "occupation", OCCUPATION });
        PACK_FILES.put("names.json", new String[] { "names", null });
        PACK_FILES.put("competitions.json", new String[] { "competition", COMPETITION });
        PACK_FILES.put("clubs.json", new String[] { "club", CLUB });
        PACK_FILES.put("grounds.json", new String[] { "ground", GROUND });
        PACK_FILES.put("people.json", new String[] { "person", PERSON });
        PACK_FILES.put("recruitment/rules.json", new String[] { "rule", RULE });
        PACK_FILES.put("recruitment/wages.json", new String[] { "wage", WAGE });
        PACK_FILES.put("recruitment/origins.json", new String[] { "origin", ORIGIN });
        PACK_FILES.put("recruitment/origin_clubs.json", new String[] { "origin_club", ORIGIN_CLUB });
        PACK_FILES.put("languages.json", new String[] { "language", LANGUAGE });
        PACK_FILES.put("areas.json", new String[] { "area", AREA });
        PACK_FILES.put("language_rates.json", new String[] { "language_rate", null });
        PACK_FILES.put("injuries.json", new String[] { "injury", INJURY });
        PACK_FILES.put("fans.json", new String[] { "fan_rule", FAN_RULE });
        SEASON_FILES.put("squads.json", new String[] { "squad", null });
        SEASON_FILES.put("nationality_modifiers.json", new String[] { "nationality_modifier", null });
        SEASON_FILES.put("region_modifiers.json", new String[] { "region", REGION });
        SEASON_FILES.put("results.json", new String[] { "result", null });
        SEASON_FILES.put("contracts.json", new String[] { "contract_band", null });
    }

    // ------------------------------------------------------------------------------------------------------------

    private final DataReport report = new DataReport();

    /** Every record read, in load order (live or not). */
    private final List<DataRecord> loaded = new ArrayList<>();
    /** kind -> lower-case id -> live record. */
    private final Map<String, Map<String, DataRecord>> ids = new HashMap<>();
    /** kind -> lower-case id -> where, of every record read (broken ones too), to report duplicates. */
    private final Map<String, Map<String, String>> seenIds = new HashMap<>();
    /** kind -> lower-case old id -> new id. */
    private final Map<String, Map<String, String>> aliases = new HashMap<>();
    /** spec name -> live records in load order (squads, names, modifiers...). */
    private final Map<String, List<DataRecord>> bySpec = new HashMap<>();
    private final List<String> packs = new ArrayList<>();
    /** H-05: text entries read, waiting for their keys to be resolved. */
    private final List<PendingText> pendingTexts = new ArrayList<>();
    /** H-05: language -> kind (club, person) -> canonical id -> text. */
    private final Map<String, Map<String, Map<String, HistoryText>>> texts = new HashMap<>();
    /** Pack id -> {version, dataVersion} from its pack.json, in load order. */
    private final Map<String, int[]> packVersions = new LinkedHashMap<>();
    /** Registry ({@code ids.json}) of each pack that has one. */
    private final Map<String, Registry> registries = new LinkedHashMap<>();
    /** kind -> lower id -> runtime id of every live record (and of the old ids that alias to one). */
    private final Map<String, Map<String, Long>> numbers = new HashMap<>();

    /** Continues after the last data record, for runtime copies made at load (still below GAME_START). */
    private IdAllocator dataIds;

    private GameData() {
        for (String k : KINDS) {
            ids.put(k, new HashMap<>());
            aliases.put(k, new HashMap<>());
        }
    }

    /** Reads the packs shipped in the game ({@code gamedata/} on the classpath). */
    public static GameData loadDefault() {
        return load(new ClasspathSource("gamedata"));
    }

    /** Reads every pack listed in {@code index.txt} of a source. Never throws for bad data. */
    public static GameData load(Source source) {
        GameData data = new GameData();
        data.read(source);
        return data;
    }

    // ---------------------------------------------------------------------------------------------- lookups

    public DataReport getReport() {
        return report;
    }

    /** @return the packs read, in load order */
    public List<String> getPacks() {
        return Collections.unmodifiableList(packs);
    }

    /** @return the live records of a kind (e.g. {@link #CLUB}) in load order */
    public List<DataRecord> all(String kind) {
        List<DataRecord> out = new ArrayList<>();
        for (DataRecord r : loaded) {
            if (r.live && kind.equals(r.kind) && (r.parent == null || r.parent.live)) {
                out.add(r);
            }
        }
        return out;
    }

    /** @return the live records of a spec without ids ({@code squad}, {@code names}...) for a season (or null = all) */
    public List<DataRecord> records(String spec, String season) {
        List<DataRecord> out = new ArrayList<>();
        for (DataRecord r : bySpec.getOrDefault(spec, Collections.emptyList())) {
            if (r.live && (season == null || season.equals(r.season))) {
                out.add(r);
            }
        }
        return out;
    }

    /** @return the record of a kind with this data id (following aliases, ignoring case), or null */
    public DataRecord get(String kind, String id) {
        String canonical = resolve(kind, id);
        return canonical == null ? null : ids.get(kind).get(canonical);
    }

    /**
     * E-01: a squad's positions by canonical person id (keys follow aliases like {@code players} does).
     *
     * @param squad a {@code squad} record
     */
    public Map<String, com.rndmodgames.futtoboru.data.Position> squadPositions(DataRecord squad) {
        Map<String, com.rndmodgames.futtoboru.data.Position> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> e : squadTable(squad, "positions").entrySet()) {
            String person = resolve(PERSON, e.getKey());
            com.rndmodgames.futtoboru.data.Position p = com.rndmodgames.futtoboru.data.Position
                    .fromDataId(e.getValue().getString("position", null));
            if (person != null && p != null) {
                out.put(person, p);
            }
        }
        return out;
    }

    /** E-01: a squad's sourced dribbling values by canonical person id (most players have none: derived). */
    public Map<String, Integer> squadDribbling(DataRecord squad) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> e : squadTable(squad, "dribbling").entrySet()) {
            String person = resolve(PERSON, e.getKey());
            JsonValue v = e.getValue().get("value");
            if (person != null && v != null && v.isLong()) {
                out.put(person, v.asInt());
            }
        }
        return out;
    }

    /**
     * H-05: a squad's fan-love tiers (0-5) at the start of the season, by canonical person id. A player without an
     * entry has the derived default (the proposal: 1 for a sourced person, 0 for a placeholder).
     */
    public Map<String, Integer> squadFanTiers(DataRecord squad) {
        Map<String, Integer> out = new LinkedHashMap<>();
        JsonValue t = squad == null ? null : squad.json.get("fanTiers");
        for (JsonValue x = t == null || !t.isObject() ? null : t.child; x != null; x = x.next) {
            String person = resolve(PERSON, x.name);
            if (person != null && x.isLong()) {
                out.put(person, x.asInt());
            }
        }
        return out;
    }

    /** H-05: the nicknames of a club or a person (all of them; filter on {@link HistoryName#isPeriod()}). */
    public List<HistoryName> nicknames(DataRecord record) {
        return names(record, "nicknames");
    }

    /** H-05: the names a club carried (a name by season), oldest first as in the file. */
    public List<HistoryName> nameHistory(DataRecord club) {
        return names(club, "nameHistory");
    }

    private static List<HistoryName> names(DataRecord record, String field) {
        List<HistoryName> out = new ArrayList<>();
        JsonValue v = record == null ? null : record.json.get(field);
        for (JsonValue x = v == null || !v.isArray() ? null : v.child; x != null; x = x.next) {
            out.add(new HistoryName(x.getString("name", ""), x.getInt("from", 0), x.getInt("to", 0),
                    x.getBoolean("period", false), strings(x.get("sources"))));
        }
        return out;
    }

    /**
     * H-05: the history text of a club or a person ({@link #CLUB} / {@link #PERSON}) in a language, falling back to
     * English; null when there is none.
     */
    public HistoryText historyText(String kind, String id, String lang) {
        String canonical = resolve(kind, id);
        if (canonical == null) {
            return null;
        }
        for (String l : new String[] { lang, "en" }) {
            Map<String, Map<String, HistoryText>> byKind = l == null ? null : texts.get(l);
            Map<String, HistoryText> table = byKind == null ? null : byKind.get(kind);
            HistoryText t = table == null ? null : table.get(canonical);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    /** @return the first live squad of a club (any season, load order), or null */
    public DataRecord squadOf(String clubId) {
        String club = resolve(CLUB, clubId);
        for (DataRecord sq : records("squad", null)) {
            if (club != null && club.equals(sq.ref("club"))) {
                return sq;
            }
        }
        return null;
    }

    private static Map<String, JsonValue> squadTable(DataRecord squad, String field) {
        Map<String, JsonValue> out = new LinkedHashMap<>();
        JsonValue t = squad == null ? null : squad.json.get(field);
        for (JsonValue x = t == null || !t.isObject() ? null : t.child; x != null; x = x.next) {
            if (x.isObject()) {
                out.put(x.name, x);
            }
        }
        return out;
    }

    /** @return the canonical id for a data id (following aliases, ignoring case), or null when unknown */
    public String resolve(String kind, String id) {
        if (id == null || !ids.containsKey(kind)) {
            return null;
        }
        String low = id.toLowerCase();
        for (int depth = 0; depth < 16; depth++) {
            if (ids.get(kind).containsKey(low)) {
                return low;
            }
            String next = aliases.get(kind).get(low);
            if (next == null) {
                return null;
            }
            low = next.toLowerCase();
        }
        return null;
    }

    /** @return pack id -> {version, dataVersion} of every pack read, in load order */
    public Map<String, int[]> getPackVersions() {
        return Collections.unmodifiableMap(packVersions);
    }

    /** @return the highest {@code dataVersion} of the packs read (0 when none) */
    public int getDataVersion() {
        int v = 0;
        for (int[] pv : packVersions.values()) {
            v = Math.max(v, pv[1]);
        }
        return v;
    }

    /** @return the runtime id of a record by data id (aliases followed, ignoring case), or null when unknown */
    public Long numberOf(String kind, String id) {
        if (id == null) {
            return null;
        }
        Map<String, Long> table = numbers.get(kind);
        return table == null ? null : table.get(id.toLowerCase());
    }

    /** @return the allocator for objects built while making the database (from {@link IdAllocator#DERIVED_START}) */
    public IdAllocator getDataIdAllocator() {
        return dataIds;
    }

    // ---------------------------------------------------------------------------------------------- reading

    private void read(Source source) {
        String index = source.read("index.txt");
        if (index == null) {
            report.error("index.txt", "missing (lists the packs to load)");
            dataIds = new IdAllocator(IdAllocator.DATA_START);
            return;
        }
        for (String line : index.split("\\r?\\n")) {
            String l = line.trim();
            if (!l.isEmpty() && !l.startsWith("#")) {
                packs.add(l);
            }
        }
        List<String> onDisk = source.listFolders();
        if (onDisk != null) {
            for (String p : new TreeSet<>(onDisk)) {
                if (!packs.contains(p)) {
                    report.error("index.txt", "pack folder `" + p + "` is not listed");
                }
            }
        }
        for (String pack : packs) {
            readPack(source, pack);
        }
        checkAliases();
        resolveReferences();
        resolveTexts();
        checkLanguageTables();
        warnings();
        assignRuntimeIds();
    }

    private void readPack(Source source, String pack) {
        String manifestWhere = pack + "/pack.json";
        JsonValue manifest = parse(source, pack + "/pack.json", manifestWhere);
        if (manifest == null) {
            return;
        }
        if (!checkRecord(manifest, "pack", manifestWhere, null)) {
            return;
        }
        if (!pack.equals(manifest.getString("id"))) {
            report.error(manifestWhere, "id `" + manifest.getString("id") + "` differs from its folder `" + pack + "`");
        }
        packVersions.put(pack, new int[] { manifest.getInt("version"), manifest.getInt("dataVersion") });
        List<String> listed = new ArrayList<>();
        for (JsonValue f = manifest.get("files").child; f != null; f = f.next) {
            listed.add(f.asString());
        }
        List<String> present = source.listJson(pack);
        if (present != null) {
            Set<String> extra = new TreeSet<>(present);
            extra.remove("pack.json");
            extra.removeAll(listed);
            for (String f : extra) {
                report.error(manifestWhere, "`" + f + "` exists but is not listed in `files` (it would never load)");
            }
        }
        for (String f : listed) {
            String where = pack + "/" + f;
            JsonValue data = parse(source, where, where);
            if (data == null) {
                continue;
            }
            String[] parts = f.split("/");
            if (f.equals("aliases.json")) {
                readAliases(data, where);
                continue;
            }
            if (f.equals("ids.json")) {
                readRegistry(data, where, pack);
                continue;
            }
            if (f.startsWith("era/")) {
                continue; // era events (H-06): read by EraProfile, checked by validate.py
            }
            if (parts.length == 3 && parts[0].equals("text")) {
                readText(data, where, parts[1], parts[2]);
                continue;
            }
            String season = null;
            String[] spec;
            if (parts.length == 3 && parts[0].equals("seasons")) {
                season = parts[1];
                if (parts[2].equals("season.json")) {
                    readSeason(data, where, pack, season);
                    continue;
                }
                spec = SEASON_FILES.get(parts[2]);
                if (spec == null) {
                    report.error(where, "unknown season file");
                    continue;
                }
            } else {
                spec = PACK_FILES.get(f);
                if (spec == null) {
                    report.error(where, "unknown data file");
                    continue;
                }
            }
            if (!data.isArray()) {
                report.error(where, "expected a list of records");
                continue;
            }
            int i = 0;
            for (JsonValue rec = data.child; rec != null; rec = rec.next, i++) {
                String rid = rec.isObject() ? rec.getString("id", null) : null;
                String rw = where + "[" + i + "]" + (rid != null ? " `" + rid + "`" : "");
                DataRecord r = new DataRecord(spec[0], spec[1], rec, rw, pack, season, null);
                addRecord(r);
            }
        }
        for (JsonValue s = manifest.get("seasons").child; s != null; s = s.next) {
            if (source.read(pack + "/seasons/" + s.asString() + "/season.json") == null) {
                report.error(manifestWhere, "season `" + s.asString() + "` has no seasons/" + s.asString() + "/season.json");
            }
        }
    }

    /** A text entry read from {@code text/<lang>/history.json}, waiting for its key to be resolved. */
    private static final class PendingText {
        final String lang;
        final String kind;
        final String key;
        final JsonValue entry;
        final String where;

        PendingText(String lang, String kind, String key, JsonValue entry, String where) {
            this.lang = lang;
            this.kind = kind;
            this.key = key;
            this.entry = entry;
            this.where = where;
        }
    }

    private void readText(JsonValue data, String where, String lang, String name) {
        if (!name.equals("history.json")) {
            report.error(where, "unknown text file (only history.json)");
            return;
        }
        if (!LANG_RE.matcher(lang).matches()) {
            report.error(where, "`" + lang + "` is not a language code (like `en`, `es`, `pt-BR`)");
        }
        if (!data.isObject()) {
            report.error(where, "expected an object of kinds (club, person)");
            return;
        }
        for (JsonValue kind = data.child; kind != null; kind = kind.next) {
            String spec = CLUB.equals(kind.name) ? "history_club" : PERSON.equals(kind.name) ? "history_person" : null;
            if (spec == null) {
                report.error(where, "unknown kind `" + kind.name + "` (allowed: club, person)");
                continue;
            }
            if (!kind.isObject()) {
                report.error(where + "." + kind.name, "expected an object of data id -> entry");
                continue;
            }
            for (JsonValue e = kind.child; e != null; e = e.next) {
                String w = where + "." + kind.name + "." + e.name;
                if (checkRecord(e, spec, w, null)) {
                    pendingTexts.add(new PendingText(lang, kind.name, e.name, e, w));
                }
            }
        }
    }

    private static List<String> strings(JsonValue list) {
        List<String> out = new ArrayList<>();
        for (JsonValue x = list == null ? null : list.child; x != null; x = x.next) {
            out.add(x.asString());
        }
        return out;
    }

    /** Resolves the keys of the text entries after aliases; an unknown key is an error and the entry is dropped. */
    private void resolveTexts() {
        for (PendingText t : pendingTexts) {
            String canonical = resolve(t.kind, t.key);
            if (canonical == null) {
                report.error(t.where, "unknown " + t.kind + " `" + t.key + "`");
                continue;
            }
            Map<String, String> seasons = new LinkedHashMap<>();
            JsonValue st = t.entry.get("seasons");
            for (JsonValue x = st == null ? null : st.child; x != null; x = x.next) {
                String season = resolve(SEASON, x.name);
                if (season == null) {
                    report.error(t.where + ".seasons." + x.name, "unknown season `" + x.name + "`");
                } else {
                    seasons.put(season, x.asString());
                }
            }
            texts.computeIfAbsent(t.lang, k -> new HashMap<>()).computeIfAbsent(t.kind, k -> new HashMap<>()).put(
                    canonical, new HistoryText(t.entry.getString("hook", null), strings(t.entry.get("history")),
                            seasons, t.entry.getString("bio", null), strings(t.entry.get("sources"))));
        }
        Map<String, HistoryText> none = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, HistoryText>>> lang : texts.entrySet()) {
            if ("en".equals(lang.getKey())) {
                continue;
            }
            for (Map.Entry<String, Map<String, HistoryText>> kind : lang.getValue().entrySet()) {
                for (String key : kind.getValue().keySet()) {
                    if (!texts.getOrDefault("en", Collections.emptyMap()).getOrDefault(kind.getKey(), none)
                            .containsKey(key)) {
                        report.error("text/" + lang.getKey() + "/history.json." + kind.getKey(),
                                "`" + key + "` has no entry in text/en (orphan translation)");
                    }
                }
            }
        }
    }

    private void readSeason(JsonValue data, String where, String pack, String season) {
        DataRecord r = new DataRecord("season", SEASON, data, where, pack, season, null);
        addRecord(r);
        if (r.live && !season.equals(data.getString("id", null))) {
            report.error(where, "season id `" + data.getString("id", null) + "` differs from its folder `" + season + "`");
        }
        JsonValue eds = data.get("editions");
        if (r.live && eds != null) {
            int i = 0;
            for (JsonValue ed = eds.child; ed != null; ed = ed.next, i++) {
                DataRecord e = new DataRecord("edition", EDITION, ed, where + ".editions[" + i + "]", pack, season, r);
                r.children.add(e);
                addRecord(e);
            }
        }
    }

    private void readAliases(JsonValue data, String where) {
        if (!data.isObject()) {
            report.error(where, "expected an object of kinds");
            return;
        }
        for (JsonValue kind = data.child; kind != null; kind = kind.next) {
            if (!aliases.containsKey(kind.name)) {
                report.error(where, "unknown kind `" + kind.name + "`");
                continue;
            }
            if (!kind.isObject()) {
                report.error(where + "." + kind.name, "expected an object of old id -> new id");
                continue;
            }
            for (JsonValue a = kind.child; a != null; a = a.next) {
                if (!a.isString()) {
                    report.error(where + "." + kind.name + "." + a.name, "expected an id");
                    continue;
                }
                aliases.get(kind.name).put(a.name.toLowerCase(), a.asString());
            }
        }
    }

    /** Checks a record and registers it (a broken record is reported and left out). */
    private void addRecord(DataRecord r) {
        loaded.add(r);
        boolean ok = checkRecord(r.json, r.spec, r.where, r);
        String rid = r.kind != null && r.json.isObject() ? r.json.getString("id", null) : null;
        if (rid != null) {
            // duplicates are found among every record with an id, broken or not (like validate.py)
            String low = rid.toLowerCase();
            String prev = seenIds.computeIfAbsent(r.kind, k -> new HashMap<>()).putIfAbsent(low, r.where);
            if (prev != null) {
                report.error(r.where, "duplicate " + r.kind + " id `" + rid + "` (also " + prev + ")");
                ok = false;
            }
        }
        if (!ok) {
            r.live = false;
            return;
        }
        if (r.kind != null) {
            ids.get(r.kind).put(r.id().toLowerCase(), r);
        }
        bySpec.computeIfAbsent(r.spec, k -> new ArrayList<>()).add(r);
    }

    private JsonValue parse(Source source, String path, String where) {
        String text = source.read(path);
        if (text == null) {
            report.error(where, "file listed but missing");
            return null;
        }
        try {
            JsonValue v = new JsonReader().parse(text);
            if (v == null) {
                report.error(where, "not valid UTF-8 JSON: empty");
            }
            return v;
        } catch (SerializationException e) {
            report.error(where, "not valid UTF-8 JSON: " + e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------- checks

    /** @return true when the record has no error (its references are collected into {@code rec} when given) */
    private boolean checkRecord(JsonValue v, String specName, String where, DataRecord rec) {
        int before = report.getErrors().size();
        Map<String, Field> spec = SPECS.get(specName);
        if (v == null || !v.isObject()) {
            report.error(where, "expected an object, got " + (v == null ? "nothing" : v.type()));
            return false;
        }
        for (JsonValue f = v.child; f != null; f = f.next) {
            if (!spec.containsKey(f.name)) {
                report.error(where, "unknown field `" + f.name + "` (allowed: " + String.join(", ", spec.keySet()) + ")");
            }
        }
        for (Map.Entry<String, Field> e : spec.entrySet()) {
            JsonValue x = v.get(e.getKey());
            if (x == null || x.isNull()) {
                if (e.getValue().required) {
                    report.error(where, "missing field `" + e.getKey() + "`");
                }
                continue;
            }
            checkValue(x, e.getKey(), e.getValue().type, where + "." + e.getKey(), rec);
        }
        if ("squad".equals(specName)) {
            checkSquadRoles(v, where);
        }
        if ("contract_band".equals(specName)) {
            checkContractBand(v, where);
        }
        if (SOURCED.contains(specName)) {
            checkHistorySourced(v, specName, where);
        }
        checkRecruitmentRecord(specName, v, where);
        checkRules(v, specName, where);
        if (spec.containsKey("id")) {
            JsonValue id = v.get("id");
            if (id != null && id.isString() && !ID_RE.matcher(id.asString()).matches()) {
                report.error(where, "bad id `" + id.asString()
                        + "` (lower-case slug like `preston-north-end`; mods: `author.slug`)");
            }
        }
        return report.getErrors().size() == before;
    }

    private void checkValue(JsonValue v, String field, String type, String where, DataRecord rec) {
        switch (type) {
        case "str":
            expect(v.isString(), v, where, "text");
            break;
        case "int":
            expect(v.isLong(), v, where, "a whole number");
            break;
        case "num":
            expect(v.isNumber(), v, where, "a number");
            break;
        case "bool":
            expect(v.isBoolean(), v, where, "true/false");
            break;
        case "date":
            expect(v.isString() && DATE_RE.matcher(v.asString()).matches(), v, where, "a date yyyy, yyyy-mm or yyyy-mm-dd");
            break;
        case "datetime":
            expect(v.isString() && DATETIME_RE.matcher(v.asString()).matches(), v, where, "a date-time yyyy-mm-ddThh:mm:ss");
            break;
        case "strs":
            expect(isListOfStrings(v), v, where, "a list of texts");
            break;
        case "nums": {
            boolean ok = v.isArray();
            for (JsonValue x = ok ? v.child : null; x != null; x = x.next) {
                ok &= x.isNumber();
            }
            expect(ok, v, where, "a list of numbers");
            break;
        }
        case "monthday":
            expect(isMonthDay(v), v, where, "a month-day MM-DD");
            break;
        case "monthdays": {
            boolean ok = v.isArray();
            for (JsonValue x = ok ? v.child : null; x != null; x = x.next) {
                ok &= isMonthDay(x);
            }
            expect(ok, v, where, "a list of month-days MM-DD");
            break;
        }
        case "attrs":
            if (!v.isObject()) {
                expect(false, v, where, "an object of attribute numbers");
                break;
            }
            for (JsonValue a = v.child; a != null; a = a.next) {
                if (!ATTRS.contains(a.name)) {
                    report.error(where, "unknown attribute `" + a.name + "`");
                } else if (!a.isNumber()) {
                    report.error(where + "." + a.name, "expected a number");
                }
            }
            break;
        default:
            if (type.startsWith("enum:")) {
                Set<String> allowed = ENUMS.get(type.substring(5));
                expect(v.isString() && allowed.contains(v.asString()), v, where, "one of " + allowed);
            } else if (type.startsWith("enums:")) {
                Set<String> allowed = ENUMS.get(type.substring(6));
                if (!isListOfStrings(v)) {
                    expect(false, v, where, "a list of texts");
                    break;
                }
                for (JsonValue x = v.child; x != null; x = x.next) {
                    if (!allowed.contains(x.asString())) {
                        report.error(where, "`" + x.asString() + "` is not one of " + allowed);
                    }
                }
            } else if (type.startsWith("shares:")) {
                // an object id -> share (a rate row's languages or countries): a key that doesn't resolve is
                // reported and left out, like an entry of a list of references
                if (!v.isObject()) {
                    expect(false, v, where, "an object of id -> share");
                    break;
                }
                int i = 0;
                for (JsonValue x = v.child; x != null; x = x.next, i++) {
                    if (!x.isNumber() || x.asDouble() < 0 || x.asDouble() > 1) {
                        report.error(where + "." + x.name, "expected a share between 0 and 1");
                    }
                    if (rec != null) {
                        rec.refs.add(new DataRecord.Ref(field, type.substring(7), x.name, i, where + "." + x.name));
                    }
                }
            } else if (type.startsWith("ref:")) {
                String path = refPath(field, where, rec);
                if (!v.isString()) {
                    expect(false, v, where, "an id");
                } else if (rec != null) {
                    // a reference inside a nested object (P2-14: calendar.cups[0].competition) is kept by its path
                    // and only dropped when it doesn't resolve; a top-level one skips its record
                    rec.refs.add(new DataRecord.Ref(path, type.substring(4), v.asString(), path.equals(field) ? -1 : 0,
                            where));
                }
            } else if (type.startsWith("refs:")) {
                if (!isListOfStrings(v)) {
                    expect(false, v, where, "a list of ids");
                    break;
                }
                String path = refPath(field, where, rec);
                Map<String, Integer> seen = new LinkedHashMap<>();
                int i = 0;
                for (JsonValue x = v.child; x != null; x = x.next, i++) {
                    seen.merge(x.asString(), 1, Integer::sum);
                    if (rec != null) {
                        rec.refs.add(new DataRecord.Ref(path, type.substring(5), x.asString(), i, where + "[" + i + "]"));
                    }
                }
                for (Map.Entry<String, Integer> s : seen.entrySet()) {
                    if (s.getValue() > 1) {
                        report.error(where, "`" + s.getKey() + "` listed " + s.getValue() + " times");
                    }
                }
            } else if (type.startsWith("list:")) {
                if (!v.isArray()) {
                    expect(false, v, where, "a list");
                } else if (!"edition".equals(type.substring(5))) {
                    // small nested records (P2-14: a calendar's cups): each checked against its spec, references
                    // kept on the enclosing record by path; a season's editions are records of their own
                    int i = 0;
                    for (JsonValue x = v.child; x != null; x = x.next, i++) {
                        checkRecord(x, type.substring(5), where + "[" + i + "]", rec);
                    }
                }
            } else if (type.startsWith("obj:")) {
                // one nested object (P2-14: a season's calendar, an edition's neutral grounds)
                checkRecord(v, type.substring(4), where, rec);
            } else if (type.startsWith("tiers:")) {
                // H-05: an object person id -> fan tier 0-5; the keys are checked against the players of the squad
                if (!v.isObject()) {
                    expect(false, v, where, "an object of id -> tier");
                    break;
                }
                for (JsonValue x = v.child; x != null; x = x.next) {
                    if (!x.isLong() || x.asLong() < FAN_TIER_MIN || x.asLong() > FAN_TIER_MAX) {
                        report.error(where + "." + x.name,
                                "tier must be a whole number " + FAN_TIER_MIN + "-" + FAN_TIER_MAX);
                    }
                }
            } else if (type.equals("seasontexts")) {
                if (!v.isObject()) {
                    expect(false, v, where, "an object of season id -> text");
                    break;
                }
                for (JsonValue x = v.child; x != null; x = x.next) {
                    if (!x.isString() || x.asString().trim().isEmpty()) {
                        report.error(where + "." + x.name, "expected a non-empty text");
                    }
                }
            } else if (type.startsWith("map:")) {
                // an object id -> small record (a squad's positions): each value is checked against its spec
                if (!v.isObject()) {
                    expect(false, v, where, "an object of id -> record");
                    break;
                }
                for (JsonValue x = v.child; x != null; x = x.next) {
                    checkRecord(x, type.substring(4), where + "." + x.name, null);
                }
            }
            break;
        }
    }

    /**
     * The key a reference is kept under on its record: the field name at the top level, else the path inside the
     * record ({@code calendar.cups[0].competition}, {@code neutralVenues.rounds[1].grounds}).
     */
    private static String refPath(String field, String where, DataRecord rec) {
        if (rec == null || !where.startsWith(rec.where + ".")) {
            return field;
        }
        return where.substring(rec.where.length() + 1);
    }

    /** P2-14: what a field spec can't say about the calendar and neutral grounds (same checks in validate.py). */
    private void checkRules(JsonValue v, String specName, String where) {
        switch (specName) {
        case "day_rule": {
            int anchors = (v.get("onOrAfter") != null && !v.get("onOrAfter").isNull() ? 1 : 0)
                    + (v.get("onOrBefore") != null && !v.get("onOrBefore").isNull() ? 1 : 0);
            if (anchors != 1) {
                report.error(where, "a date rule needs exactly one of `onOrAfter` / `onOrBefore`");
            }
            break;
        }
        case "calendar_cup": {
            String[] keys = { "daysBetweenRounds", "maxEarlyRounds", "drawDaysAfterRound" };
            int[] lows = { 1, 0, 0 };
            for (int i = 0; i < keys.length; i++) {
                JsonValue x = v.get(keys[i]);
                if (x != null && x.isLong() && (x.asLong() < lows[i] || x.asLong() > 60)) {
                    report.error(where + "." + keys[i], x.asLong() + " is outside " + lows[i] + "-60");
                }
            }
            break;
        }
        case "calendar":
            countTwice(v.get("cups"), "competition", where + ".cups", " calendars");
            break;
        case "neutral_venues": {
            countTwice(v.get("rounds"), "round", where + ".rounds", " times");
            JsonValue rounds = v.get("rounds");
            int i = 0;
            for (JsonValue r = rounds == null || !rounds.isArray() ? null : rounds.child; r != null; r = r.next, i++) {
                JsonValue g = r.isObject() ? r.get("grounds") : null;
                if (g != null && g.isArray() && g.size == 0) {
                    report.error(where + ".rounds[" + i + "].grounds", "a neutral round needs at least one ground");
                }
            }
            break;
        }
        default:
            break;
        }
    }

    /** Reports a value of {@code key} that two entries of a list share ({@code `fa-cup` has 2 calendars}). */
    private void countTwice(JsonValue list, String key, String where, String what) {
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (JsonValue x = list == null || !list.isArray() ? null : list.child; x != null; x = x.next) {
            JsonValue k = x.isObject() ? x.get(key) : null;
            if (k != null && k.isString()) {
                seen.merge(k.asString(), 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> s : seen.entrySet()) {
            if (s.getValue() > 1) {
                report.error(where, "`" + s.getKey() + "` " + (" times".equals(what) ? "listed " + s.getValue() + what
                        : "has " + s.getValue() + what));
            }
        }
    }

    /**
     * E-01: the keys of a squad's {@code positions} / {@code dribbling} are players of that squad; a dribbling
     * value is on the attribute scale and has a source. A player without a position is a warning.
     */
    private void checkSquadRoles(JsonValue sq, String where) {
        Set<String> players = new HashSet<>();
        JsonValue list = sq.get("players");
        for (JsonValue x = list == null || !list.isArray() ? null : list.child; x != null; x = x.next) {
            if (x.isString()) {
                players.add(x.asString());
            }
        }
        for (String field : new String[] { "positions", "dribbling", "fanTiers" }) {
            JsonValue table = sq.get(field);
            if (table == null || !table.isObject()) {
                continue;
            }
            for (JsonValue x = table.child; x != null; x = x.next) {
                if (!players.contains(x.name)) {
                    report.error(where + "." + field, "`" + x.name + "` is not a player of this squad");
                }
                if ("dribbling".equals(field) && x.isObject()) {
                    JsonValue val = x.get("value");
                    JsonValue src = x.get("source");
                    if (val != null && val.isLong() && (val.asLong() < ATTR_MIN || val.asLong() > ATTR_MAX)) {
                        report.error(where + ".dribbling." + x.name,
                                "value " + val.asLong() + " is outside " + ATTR_MIN + "-" + ATTR_MAX);
                    }
                    if (src != null && src.isString() && src.asString().trim().isEmpty()) {
                        report.error(where + ".dribbling." + x.name, "empty source (a sourced value needs its source)");
                    }
                }
            }
        }
        JsonValue positions = sq.get("positions");
        for (String pid : players) {
            if (positions == null || !positions.isObject() || positions.get(pid) == null) {
                report.warn(where + " `" + pid + "`", "no position");
            }
        }
    }

    /**
     * G-04 (same rules as tools/data/validate.py): shares 0-1, money not negative and min &lt;= max, squads that can
     * field eleven; only the {@code amateur} band lists clubs. (One band per standing is checked by validate.py.)
     */
    private void checkContractBand(JsonValue b, String where) {
        for (String f : new String[] { "professionalShare", "summerPayShare" }) {
            JsonValue v = b.get(f);
            if (v != null && v.isNumber() && (v.asDouble() < 0 || v.asDouble() > 1)) {
                report.error(where, "`" + f + "` " + v.asDouble() + " is outside 0-1");
            }
        }
        for (String f : new String[] { "seasonWageMin", "seasonWageMax", "summerWageMin", "summerWageMax",
                "amateurExpenses" }) {
            JsonValue v = b.get(f);
            if (v != null && v.isNumber() && v.asDouble() < 0) {
                report.error(where, "`" + f + "` " + v.asDouble() + " is negative");
            }
        }
        for (String[] range : new String[][] { { "seasonWageMin", "seasonWageMax" }, { "summerWageMin", "summerWageMax" } }) {
            JsonValue lo = b.get(range[0]);
            JsonValue hi = b.get(range[1]);
            if (lo != null && hi != null && lo.isNumber() && hi.isNumber() && lo.asDouble() > hi.asDouble()) {
                report.error(where, "`" + range[0] + "` is above `" + range[1] + "`");
            }
        }
        JsonValue min = b.get("minimumSquad");
        JsonValue size = b.get("squadSize");
        if (min != null && min.isLong() && min.asLong() < 11) {
            report.error(where, "`minimumSquad` " + min.asLong() + " cannot field eleven");
        }
        if (min != null && size != null && min.isLong() && size.isLong() && min.asLong() > size.asLong()) {
            report.error(where, "`minimumSquad` is above `squadSize`");
        }
        JsonValue clubs = b.get("clubs");
        if (clubs != null && clubs.isArray() && clubs.size > 0 && !"amateur".equals(b.getString("standing", null))) {
            report.error(where, "only the `amateur` band lists clubs");
        }
    }

    /** H-05: sources are non-empty http(s) links, names and prose are not empty, a period runs forwards. */
    private void checkHistorySourced(JsonValue v, String specName, String where) {
        JsonValue src = v.get("sources");
        if (src != null && src.isArray()) {
            if (src.size == 0) {
                report.error(where + ".sources", "empty (every history fact and text needs a source)");
            }
            int i = 0;
            for (JsonValue x = src.child; x != null; x = x.next, i++) {
                if (x.isString() && !x.asString().startsWith("http://") && !x.asString().startsWith("https://")) {
                    report.error(where + ".sources[" + i + "]", "`" + x.asString() + "` is not a link");
                }
            }
        }
        for (String f : new String[] { "name", "hook", "bio" }) {
            JsonValue x = v.get(f);
            if (x != null && x.isString() && x.asString().trim().isEmpty()) {
                report.error(where + "." + f, "empty");
            }
        }
        JsonValue h = v.get("history");
        if ("history_club".equals(specName) && h != null && h.isArray()) {
            boolean bad = h.size == 0;
            for (JsonValue x = h.child; x != null; x = x.next) {
                bad |= x.isString() && x.asString().trim().isEmpty();
            }
            if (bad) {
                report.error(where + ".history", "needs at least one non-empty paragraph");
            }
        }
        JsonValue a = v.get("from");
        JsonValue b = v.get("to");
        if (a != null && b != null && a.isLong() && b.isLong() && a.asLong() > b.asLong()) {
            report.error(where, "`from` " + a.asLong() + " is after `to` " + b.asLong());
        }
    }

    /**
     * R-02 / R-11a record checks beyond the field spec (same as validate.py): a source unless {@code estimate} or
     * {@code placeholder}; {@code until} not before {@code from}; a rule's value fields; a wage example's wage and a
     * band's min <= max; small origin nudges; a rate row's target, band, rates, {@code only} within {@code speaks},
     * no English listed, readWriteIfSigns as 4 shares adding up to 1.
     */
    private void checkRecruitmentRecord(String specName, JsonValue v, String where) {
        switch (specName) {
        case "injury":
            checkSourced(v, where);
            checkInjury(v, where);
            return;
        case "fan_rule":
            checkSourced(v, where);
            checkFanRule(v, where);
            return;
        case "rule":
        case "wage":
        case "origin":
        case "area":
        case "language_rate":
            checkSourced(v, where);
            break;
        case "origin_club":
        case "language":
            if (countSources(v) == 0) {
                report.error(where, "no source");
            }
            return;
        default:
            return;
        }
        String from = v.getString("from", null);
        String until = v.getString("until", null);
        if (from != null && until != null && dateKey(until) < dateKey(from)) {
            report.error(where, "`until` " + until + " is before `from` " + from);
        }
        if ("rule".equals(specName)) {
            for (String f : RULE_VALUES.getOrDefault(v.getString("rule", ""), new String[0])) {
                if (!v.has(f)) {
                    report.error(where, "a `" + v.getString("rule", "") + "` rule needs `" + f + "`");
                }
            }
            for (String f : new String[] { "days", "miles", "years", "pence" }) {
                JsonValue x = v.get(f);
                if (x != null && x.isNumber() && x.asDouble() <= 0) {
                    report.error(where + "." + f, "must be above 0");
                }
            }
        } else if ("wage".equals(specName)) {
            if ("example".equals(v.getString("type", null)) && !v.has("weeklyPence")) {
                report.error(where, "an `example` needs `weeklyPence`");
            }
            if ("band".equals(v.getString("type", null))) {
                JsonValue lo = v.get("minWeeklyPence");
                JsonValue hi = v.get("maxWeeklyPence");
                if (lo == null || hi == null) {
                    report.error(where, "a `band` needs `minWeeklyPence` and `maxWeeklyPence`");
                } else if (lo.isNumber() && hi.isNumber() && lo.asLong() > hi.asLong()) {
                    report.error(where, "minWeeklyPence " + lo.asLong() + " is above maxWeeklyPence " + hi.asLong());
                }
            }
            for (String f : new String[] { "weeklyPence", "summerWeeklyPence", "minWeeklyPence", "maxWeeklyPence" }) {
                JsonValue x = v.get(f);
                if (x != null && x.isNumber() && x.asDouble() < 0) {
                    report.error(where + "." + f, "must not be negative");
                }
            }
        } else if ("origin".equals(specName)) {
            JsonValue n = v.get("nudges");
            for (JsonValue x = n == null || !n.isObject() ? null : n.child; x != null; x = x.next) {
                if (x.isNumber() && (x.asDouble() != Math.rint(x.asDouble()) || Math.abs(x.asDouble()) > NUDGE_MAX)) {
                    report.error(where + ".nudges." + x.name,
                            "a nudge is a whole number from -" + NUDGE_MAX + " to " + NUDGE_MAX);
                }
            }
            JsonValue ls = v.get("literacyShift");
            if (ls != null && ls.isNumber() && Math.abs(ls.asLong()) > LITERACY_SHIFT_MAX) {
                report.error(where + ".literacyShift", "from -" + LITERACY_SHIFT_MAX + " to " + LITERACY_SHIFT_MAX);
            }
        } else if ("area".equals(specName)) {
            JsonValue w = v.get("weight");
            if (w != null && w.isNumber() && (w.asDouble() <= 0 || w.asDouble() > 1)) {
                report.error(where + ".weight", "expected a share above 0 and at most 1");
            }
        } else if ("language_rate".equals(specName)) {
            checkRateRow(v, where);
        }
    }

    private void checkRateRow(JsonValue v, String where) {
        JsonValue lo = v.get("bornFrom");
        JsonValue hi = v.get("bornUntil");
        if (lo != null && hi != null && lo.isLong() && hi.isLong() && lo.asLong() > hi.asLong()) {
            report.error(where, "bornFrom " + lo.asLong() + " is after bornUntil " + hi.asLong());
        }
        if (v.has("area") && v.has("countries")) {
            report.error(where, "a row has an `area` or `countries`, not both");
        }
        boolean any = false;
        for (String f : RATE_FIELDS) {
            any |= v.has(f);
        }
        if (!any) {
            report.error(where, "no rate (one of " + String.join(", ", RATE_FIELDS) + ")");
        }
        JsonValue speaks = v.get("speaks");
        JsonValue only = v.get("only");
        for (JsonValue t : new JsonValue[] { speaks, only }) {
            if (t != null && t.isObject() && t.has("english")) {
                report.error(where + "." + t.name, "English is the default language: list only the others");
            }
        }
        if (sum(speaks) > 1 + 1e-9) {
            report.error(where + ".speaks", "the shares add up to more than 1 (one home language besides English)");
        }
        if (sum(v.get("parentsBorn")) > 1 + 1e-9) {
            report.error(where + ".parentsBorn", "the shares add up to more than 1");
        }
        for (JsonValue x = only == null || !only.isObject() ? null : only.child; x != null; x = x.next) {
            JsonValue s = speaks == null || !speaks.isObject() ? null : speaks.get(x.name);
            if (s == null) {
                report.error(where + ".only." + x.name, "a language in `only` must be in `speaks`");
            } else if (x.isNumber() && s.isNumber() && x.asDouble() > s.asDouble()) {
                report.error(where + ".only." + x.name, x.asDouble() + " speak only it but " + s.asDouble() + " speak it");
            }
        }
        for (String f : new String[] { "signsMen", "signsWomen" }) {
            JsonValue x = v.get(f);
            if (x != null && x.isNumber() && (x.asDouble() < 0 || x.asDouble() > 1)) {
                report.error(where + "." + f, "expected a share between 0 and 1");
            }
        }
        JsonValue rw = v.get("readWriteIfSigns");
        if (rw != null && rw.isArray()) {
            boolean ok = rw.size == READ_WRITE_LEVELS;
            double total = 0;
            for (JsonValue x = rw.child; x != null; x = x.next) {
                ok &= x.isNumber() && x.asDouble() >= 0;
                total += x.isNumber() ? x.asDouble() : 0;
            }
            if (!ok || Math.abs(total - 1) > 0.001) {
                report.error(where + ".readWriteIfSigns", "expected " + READ_WRITE_LEVELS
                        + " shares (signs his name, reads slowly, reads well, writes well) adding up to 1");
            }
        }
    }

    /**
     * R-11a checks across records (same as validate.py): the area weights of a country add up to 1; for one target
     * (area, country, or the default) and rate field the birth-year bands never overlap; a default row with
     * {@code signsMen} exists.
     */
    private void checkLanguageTables() {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (DataRecord a : all(AREA)) {
            weights.merge(a.ref("country"), a.json.getDouble("weight", 0), Double::sum);
        }
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            if (Math.abs(e.getValue() - 1) > 0.005) {
                report.error("areas.json", "the area weights of `" + e.getKey() + "` add up to "
                        + String.format(java.util.Locale.ROOT, "%.3f", e.getValue()) + ", not 1");
            }
        }
        List<DataRecord> rows = records("language_rate", null);
        Map<String, List<DataRecord>> spans = new HashMap<>();
        boolean hasDefault = false;
        for (DataRecord r : rows) {
            List<String> targets = new ArrayList<>();
            if (r.ref("area") != null) {
                targets.add("area " + r.ref("area"));
            } else if (r.has("countries")) {
                for (String c : r.refs("countries")) {
                    targets.add("country " + c);
                }
            } else {
                targets.add("default");
                hasDefault |= r.has("signsMen");
            }
            int lo = r.integer("bornFrom");
            int hi = r.integer("bornUntil");
            for (String t : targets) {
                for (String f : RATE_FIELDS) {
                    if (!r.has(f)) {
                        continue;
                    }
                    List<DataRecord> seen = spans.computeIfAbsent(t + "|" + f, k -> new ArrayList<>());
                    for (DataRecord o : seen) {
                        if (lo <= o.integer("bornUntil") && o.integer("bornFrom") <= hi) {
                            report.error(r.where, "`" + f + "` for " + t + " overlaps " + o.where + " (born "
                                    + o.integer("bornFrom") + "-" + o.integer("bornUntil") + ")");
                        }
                    }
                    seen.add(r);
                }
            }
        }
        if (!rows.isEmpty() && !hasDefault) {
            report.error("language_rates.json",
                    "no default row (no area, no countries) with `signsMen`: a man born abroad has no rate");
        }
    }

    /** @return a share table of a record ({@code speaks}, {@code parentsBorn}...) by canonical id; bad keys left out */
    public Map<String, Double> shares(DataRecord r, String field, String kind) {
        Map<String, Double> out = new LinkedHashMap<>();
        JsonValue t = r == null ? null : r.json.get(field);
        for (JsonValue x = t == null || !t.isObject() ? null : t.child; x != null; x = x.next) {
            String id = resolve(kind, x.name);
            if (id != null && x.isNumber()) {
                out.put(id, x.asDouble());
            }
        }
        return out;
    }

    /** G-02: 0 <= minDays <= maxDays; chances and strengthWhileHurt in 0..1 (each kind once: validate.py). */
    /** G-10: legend-damping and jitter are shares 0..1, the band positive (the cross-rule checks are validate.py's). */
    private void checkFanRule(JsonValue v, String where) {
        String rule = v.getString("rule", "");
        JsonValue x = v.get("value");
        if (x == null || !x.isNumber()) {
            return;
        }
        if (("legend-damping".equals(rule) || "jitter".equals(rule)) && (x.asDouble() < 0 || x.asDouble() > 1)) {
            report.error(where + ".value", "must be from 0 to 1");
        }
        if ("band".equals(rule) && x.asDouble() <= 0) {
            report.error(where + ".value", "must be positive");
        }
    }

    private void checkInjury(JsonValue v, String where) {
        int lo = v.getInt("minDays", 0);
        int hi = v.getInt("maxDays", 0);
        if (lo < 0) {
            report.error(where + ".minDays", "must not be negative");
        }
        if (lo > hi) {
            report.error(where, "minDays " + lo + " is above maxDays " + hi);
        }
        for (String f : new String[] { "chancePerMatch", "returnChance", "strengthWhileHurt" }) {
            JsonValue x = v.get(f);
            if (x != null && x.isNumber() && (x.asDouble() < 0 || x.asDouble() > 1)) {
                report.error(where + "." + f, "must be from 0 to 1");
            }
        }
    }

    private void checkSourced(JsonValue v, String where) {
        if (countSources(v) == 0 && !UNSOURCED_OK.contains(v.getString("confidence", ""))) {
            report.error(where, "no source (only an `estimate` or `placeholder` may have none)");
        }
    }

    private static int countSources(JsonValue v) {
        JsonValue s = v.get("sources");
        int n = 0;
        for (JsonValue x = s == null || !s.isArray() ? null : s.child; x != null; x = x.next) {
            if (x.isString() && !x.asString().trim().isEmpty()) {
                n++;
            }
        }
        return n;
    }

    private static double sum(JsonValue t) {
        double total = 0;
        for (JsonValue x = t == null || !t.isObject() ? null : t.child; x != null; x = x.next) {
            total += x.isNumber() ? x.asDouble() : 0;
        }
        return total;
    }

    /** yyyy / yyyy-mm / yyyy-mm-dd as yyyymmdd (a missing month or day counts as 0), to compare dates. */
    static int dateKey(String d) {
        if (d == null || !DATE_RE.matcher(d).matches()) {
            return 0;
        }
        String[] p = d.split("-");
        int y = Integer.parseInt(p[0]);
        int m = p.length > 1 ? Integer.parseInt(p[1]) : 0;
        int day = p.length > 2 ? Integer.parseInt(p[2]) : 0;
        return y * 10000 + m * 100 + day;
    }

    private void expect(boolean ok, JsonValue v, String where, String expected) {
        if (!ok) {
            String got = v.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
            report.error(where, "expected " + expected + ", got " + (got.length() > 60 ? got.substring(0, 60) : got));
        }
    }

    private static boolean isListOfStrings(JsonValue v) {
        if (!v.isArray()) {
            return false;
        }
        for (JsonValue x = v.child; x != null; x = x.next) {
            if (!x.isString()) {
                return false;
            }
        }
        return true;
    }

    /** An old id must be gone, its target must exist; a bad alias is reported and ignored. */
    private void checkAliases() {
        for (String kind : KINDS) {
            Map<String, String> table = aliases.get(kind);
            for (String old : new ArrayList<>(table.keySet())) {
                if (ids.get(kind).containsKey(old)) {
                    report.error("aliases.json", kind + " alias `" + old + "` is still a live id");
                    table.remove(old);
                }
            }
            for (Map.Entry<String, String> a : new ArrayList<>(table.entrySet())) {
                if (resolve(kind, a.getValue()) == null) {
                    report.error("aliases.json", kind + " alias `" + a.getKey() + "` -> `" + a.getValue()
                            + "`: target does not exist");
                    table.remove(a.getKey());
                }
            }
        }
    }

    /**
     * Resolves every reference after aliases. A record whose single reference doesn't resolve is skipped; an entry
     * of a list that doesn't resolve is left out of the list. Skipping a record can break others, so this repeats
     * until nothing changes.
     */
    private void resolveReferences() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (DataRecord r : loaded) {
                if (!r.live) {
                    continue;
                }
                if (r.parent != null && !r.parent.live) {
                    kill(r);
                    changed = true;
                    continue;
                }
                for (DataRecord.Ref ref : r.refs) {
                    if (ref.dropped) {
                        continue;
                    }
                    if (resolve(ref.kind, ref.id) == null) {
                        report.error(ref.where, "unknown " + ref.kind + " `" + ref.id + "`");
                        ref.dropped = true;
                        if (ref.index < 0) {
                            kill(r);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
        for (DataRecord r : loaded) {
            if (!r.live) {
                continue;
            }
            for (DataRecord.Ref ref : r.refs) {
                if (ref.dropped) {
                    continue;
                }
                String canonical = resolve(ref.kind, ref.id);
                if (ref.index < 0) {
                    r.resolvedRef.put(ref.field, canonical);
                } else {
                    r.resolvedRefs.computeIfAbsent(ref.field, k -> new ArrayList<>()).add(canonical);
                }
            }
        }
    }

    private void kill(DataRecord r) {
        r.live = false;
        if (r.kind != null && r.id() != null) {
            ids.get(r.kind).remove(r.id().toLowerCase(), r);
        }
        for (DataRecord c : r.children) {
            kill(c);
        }
    }

    /** Warnings for the human review (same as validate.py). */
    private void warnings() {
        for (DataRecord p : all(PERSON)) {
            if ("".equals(p.str("firstName"))) {
                report.warn("person `" + p.id() + "`", "no first name");
            }
        }
        Map<String, List<DataRecord>> squadsBySeason = new LinkedHashMap<>();
        for (DataRecord sq : records("squad", null)) {
            squadsBySeason.computeIfAbsent(sq.season, k -> new ArrayList<>()).add(sq);
        }
        int placeholders = 0;
        for (DataRecord p : all(PERSON)) {
            if ("placeholder".equals(p.str("confidence"))) {
                placeholders++;
            }
        }
        for (Map.Entry<String, List<DataRecord>> e : squadsBySeason.entrySet()) {
            String sid = e.getKey();
            Integer year = sid.length() >= 4 && sid.substring(0, 4).chars().allMatch(Character::isDigit)
                    ? Integer.valueOf(sid.substring(0, 4)) : null;
            Map<String, List<String>> seen = new LinkedHashMap<>();
            for (DataRecord sq : e.getValue()) {
                List<String> players = sq.refs("players");
                if (players.size() < 11) {
                    report.warn("season " + sid + " `" + sq.ref("club") + "`",
                            "only " + players.size() + " players (an XI needs 11)");
                }
                for (String pid : players) {
                    seen.computeIfAbsent(pid, k -> new ArrayList<>()).add(sq.ref("club"));
                    DataRecord person = get(PERSON, pid);
                    String born = person == null ? "" : person.str("born", "");
                    if (year != null && born.length() >= 4) {
                        int age = year - Integer.parseInt(born.substring(0, 4));
                        if (age < 15 || age > 45) {
                            report.warn("season " + sid + " `" + pid + "`", "age " + age + " at the season start");
                        }
                    }
                }
            }
            for (Map.Entry<String, List<String>> s : seen.entrySet()) {
                if (s.getValue().size() > 1) {
                    report.warn("season " + sid + " `" + s.getKey() + "`",
                            "in " + s.getValue().size() + " squads: " + String.join(", ", s.getValue()));
                }
            }
            if (placeholders > 0) {
                report.warn("season " + sid, placeholders + " people are placeholders");
            }
        }
    }

    // ---------------------------------------------------------------------------------------------- ids.json

    /** One pack's {@code ids.json}: its number range, the next free number and kind -> lower data id -> number. */
    static final class Registry {
        final String where;
        long first = IdAllocator.DATA_START;
        long last = IdAllocator.REGISTRY_MAX;
        long next = Long.MAX_VALUE;
        final Map<String, Map<String, Long>> ids = new LinkedHashMap<>();

        Registry(String where) {
            this.where = where;
        }
    }

    private void readRegistry(JsonValue data, String where, String pack) {
        Registry reg = new Registry(where);
        registries.put(pack, reg);
        if (!data.isObject()) {
            report.error(where, "expected an object with range, next, ids, retired");
            return;
        }
        JsonValue range = data.get("range");
        if (range != null && range.isArray() && range.size == 2 && range.child.isLong() && range.child.next.isLong()
                && range.child.asLong() >= IdAllocator.DATA_START && range.child.asLong() <= range.child.next.asLong()
                && range.child.next.asLong() <= IdAllocator.REGISTRY_MAX) {
            reg.first = range.child.asLong();
            reg.last = range.child.next.asLong();
        } else {
            report.error(where, "`range` must be [first, last] with 1 <= first <= last <= " + IdAllocator.REGISTRY_MAX);
        }
        JsonValue next = data.get("next");
        if (next != null && next.isLong()) {
            reg.next = next.asLong();
        } else {
            report.error(where, "`next` must be a whole number");
        }
        JsonValue kinds = data.get("ids");
        if (kinds == null || !kinds.isObject()) {
            report.error(where, "`ids` must be an object of kind -> {data id: number}");
            return;
        }
        for (JsonValue kind = kinds.child; kind != null; kind = kind.next) {
            if (!ids.containsKey(kind.name) || !kind.isObject()) {
                report.error(where, "unknown kind `" + kind.name + "` or not an object");
                continue;
            }
            Map<String, Long> table = reg.ids.computeIfAbsent(kind.name, k -> new LinkedHashMap<>());
            for (JsonValue e = kind.child; e != null; e = e.next) {
                String at = where + ".ids." + kind.name + "." + e.name;
                if (!e.isLong()) {
                    report.error(at, "expected a whole number");
                    continue;
                }
                long n = e.asLong();
                if (n < reg.first || n > reg.last || n >= reg.next) {
                    report.error(at, "number " + n + " is outside the pack's range [" + reg.first + ", " + reg.last
                            + "] or not below `next` (" + reg.next + ")");
                    continue;
                }
                table.put(e.name.toLowerCase(), n);
            }
        }
    }

    /**
     * Runtime ids: every live record with a data id gets its registry number (its own entry, else the lowest number
     * of the old ids that alias to it); a record without one gets a temporary number from
     * {@link IdAllocator#DERIVED_START} (warned). Then the allocator for objects built at load continues that range.
     */
    private void assignRuntimeIds() {
        // kind -> lower id -> number of every entry; a number used twice is an error and only its first entry counts
        Map<String, Map<String, Long>> entries = new HashMap<>();
        Map<Long, String> byNumber = new HashMap<>();
        for (Map.Entry<String, Registry> pr : registries.entrySet()) {
            Registry reg = pr.getValue();
            for (Map.Entry<String, Map<String, Long>> k : reg.ids.entrySet()) {
                for (Map.Entry<String, Long> e : k.getValue().entrySet()) {
                    String label = pr.getKey() + " " + k.getKey() + " `" + e.getKey() + "`";
                    String prev = byNumber.putIfAbsent(e.getValue(), label);
                    if (prev != null) {
                        report.error(reg.where, "number " + e.getValue() + " is used twice (" + prev + " and " + label + ")");
                        continue;
                    }
                    if (entries.computeIfAbsent(k.getKey(), x -> new HashMap<>()).putIfAbsent(e.getKey(), e.getValue()) != null) {
                        report.error(reg.where, k.getKey() + " `" + e.getKey() + "` has two entries");
                    }
                }
            }
        }
        // a renamed record inherits the number of its old id (the lowest one when several old ids point to it)
        Map<String, Map<String, Long>> inherited = new HashMap<>();
        for (Map.Entry<String, Map<String, Long>> k : entries.entrySet()) {
            for (Map.Entry<String, Long> e : k.getValue().entrySet()) {
                if (ids.get(k.getKey()).containsKey(e.getKey())) {
                    continue;
                }
                String target = resolve(k.getKey(), e.getKey());
                if (target != null) {
                    inherited.computeIfAbsent(k.getKey(), x -> new HashMap<>()).merge(target, e.getValue(), Math::min);
                }
            }
        }

        dataIds = new IdAllocator(IdAllocator.DERIVED_START);
        Set<String> warnedPacks = new HashSet<>();
        for (DataRecord r : loaded) {
            if (!r.live || r.kind == null || (r.parent != null && !r.parent.live)) {
                continue;
            }
            String low = r.id().toLowerCase();
            Long n = entries.getOrDefault(r.kind, Collections.emptyMap()).get(low);
            if (n == null) {
                n = inherited.getOrDefault(r.kind, Collections.emptyMap()).get(low);
            }
            if (n == null) {
                n = dataIds.nextId();
                if (!registries.containsKey(r.pack)) {
                    if (warnedPacks.add(r.pack)) {
                        report.warn(r.pack + "/ids.json", "missing: the runtime ids of this pack are temporary"
                                + " (run python tools/data/assign_ids.py)");
                    }
                } else {
                    report.warn(r.where, r.kind + " `" + r.id() + "` has no number in " + r.pack
                            + "/ids.json: temporary runtime id " + n + " (run python tools/data/assign_ids.py)");
                }
            }
            r.runtimeId = n;
            numbers.computeIfAbsent(r.kind, k -> new HashMap<>()).put(low, n);
        }
        // old (aliased) ids keep resolving to their record's number
        for (Map.Entry<String, Map<String, Long>> k : entries.entrySet()) {
            for (String old : k.getValue().keySet()) {
                String target = resolve(k.getKey(), old);
                Map<String, Long> table = numbers.get(k.getKey());
                if (target != null && table != null && table.containsKey(target)) {
                    table.putIfAbsent(old, table.get(target));
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------- sources

    /** Reads the data from the classpath (the game jar, or {@code target/classes} in development and tests). */
    public static class ClasspathSource implements Source {

        private final String root;
        private final ClassLoader loader;

        public ClasspathSource(String root) {
            this.root = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            this.loader = cl != null ? cl : GameData.class.getClassLoader();
        }

        @Override
        public String read(String path) {
            try (InputStream in = loader.getResourceAsStream(root + "/" + path)) {
                if (in == null) {
                    return null;
                }
                String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return text.startsWith("﻿") ? text.substring(1) : text;
            } catch (IOException e) {
                return null;
            }
        }

        private File folder(String path) {
            URL url = loader.getResource(path.isEmpty() ? root : root + "/" + path);
            if (url == null || !"file".equals(url.getProtocol())) {
                return null; // inside a jar: a folder cannot be listed
            }
            try {
                File f = new File(url.toURI());
                return f.isDirectory() ? f : null;
            } catch (URISyntaxException | IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public List<String> listJson(String folder) {
            File dir = folder(folder);
            if (dir == null) {
                return null;
            }
            List<String> out = new ArrayList<>();
            collect(dir, "", out);
            return out;
        }

        private static void collect(File dir, String prefix, List<String> out) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    collect(f, prefix + f.getName() + "/", out);
                } else if (f.getName().endsWith(".json")) {
                    out.add(prefix + f.getName());
                }
            }
        }

        @Override
        public List<String> listFolders() {
            File dir = folder("");
            if (dir == null) {
                return null;
            }
            List<String> out = new ArrayList<>();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        out.add(f.getName());
                    }
                }
            }
            return out;
        }
    }
}
