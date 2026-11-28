package com.rndmodgames.futtoboru.system.save;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.SerializationException;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.BuildInfo;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Save format v1 (P2-5, decisions D11/D15; the DarkBlade pattern).
 *
 * <pre>
 * { "saveVersion": 2, "gameVersion", "dataVersion", "packs": [{id, version, dataVersion}], "savedAt",
 *   "summary": {name, manager, club, clubDataId, gameDate},          (for the load list)
 *   "game": { the SaveGame's own fields },
 *   "authorities": [...], "competitions": [...], "editions": [...], "leagues": [...],
 *   "clubs": [...], "persons": [...], "players": [...], "matches": [...] }
 * </pre>
 *
 * <ul>
 *   <li><b>Static data</b> (countries, continents, professions, occupations, a ground's facts) is written as its data
 *       id and re-linked on load to the objects of {@link DatabaseLoader} (aliases followed). Never copied.</li>
 *   <li><b>Game objects</b> (the eight tables) are written exactly once, sorted by runtime id; everywhere else a
 *       reference to one is its runtime id. On load every table entry becomes one object first, then the fields are
 *       read, so every reference resolves to that single object.</li>
 *   <li>Everything else (messages, contracts, finances, jobs, scripts, snapshots, the id allocator) is plain state
 *       owned by one object and written inside it.</li>
 *   <li>A reference that doesn't resolve is left out (null, or dropped from its list) and reported in
 *       {@link LoadResult#getProblems()}; loading goes on.</li>
 * </ul>
 * Older versions climb {@link SaveMigrations} first; a file with no {@code saveVersion} (v0.4) is not loaded.
 */
public final class SaveCodec {

    /**
     * The format written by this version of the game (2: players have {@code position} and {@code dribbling}, E-01;
     * 3: persons have {@code languages}, {@code homeLanguage}, {@code birthArea}, {@code parentsOrigin}, R-11a).
     */
    public static final int SAVE_VERSION = 3;

    static final String SAVE_VERSION_FIELD = "saveVersion";

    public static final String OLDER_VERSION_MESSAGE = "This save was made with an older version of Futtoboru and can't be loaded.";
    public static final String NEWER_VERSION_MESSAGE = "This save was made with a newer version of Futtoboru and can't be loaded.";
    public static final String UNREADABLE_MESSAGE = "This file is not a Futtoboru save or it is damaged.";

    private SaveCodec() {
    }

    // ------------------------------------------------------------------------------------------- game objects

    /** A table of game objects: written once, referenced by runtime id. */
    static final class Kind<T> {
        final String table;
        final Class<T> type;
        final Function<T, Long> getId;
        final BiConsumer<T, Long> setId;
        final Supplier<T> create;

        Kind(String table, Class<T> type, Function<T, Long> getId, BiConsumer<T, Long> setId, Supplier<T> create) {
            this.table = table;
            this.type = type;
            this.getId = getId;
            this.setId = setId;
            this.create = create;
        }

        Long id(Object o) {
            return getId.apply(type.cast(o));
        }
    }

    /** In the order they are written and read. */
    static final List<Kind<?>> KINDS = List.of(
            new Kind<>("authorities", Authority.class, Authority::getId, Authority::setId, Authority::new),
            new Kind<>("competitions", Competition.class, Competition::getId, Competition::setId, Competition::new),
            new Kind<>("editions", CompetitionEdition.class, CompetitionEdition::getId, CompetitionEdition::setId,
                    CompetitionEdition::new),
            new Kind<>("leagues", League.class, League::getId, League::setId, League::new),
            new Kind<>("clubs", Club.class, Club::getId, Club::setId, Club::new),
            new Kind<>("persons", Person.class, Person::getId, Person::setId, Person::new),
            new Kind<>("players", Player.class, Player::getId, Player::setId, Player::new),
            new Kind<>("matches", Match.class, Match::getId, Match::setId, Match::new));

    // ------------------------------------------------------------------------------------------------ writing

    /**
     * @param saveName the name the player gave the save (shown in the load list)
     * @return the save as pretty-printed standard JSON
     */
    public static String encode(SaveGame game, String saveName, LocalDateTime savedAt) {
        Writing w = new Writing(game);
        Json json = w.json;

        // pass 1: find every game object reachable from the game (writing to nowhere)
        json.setWriter(Writer.nullWriter());
        json.writeObjectStart();
        json.writeFields(game);
        json.writeObjectEnd();
        while (!w.pending.isEmpty()) {
            json.setWriter(Writer.nullWriter());
            json.writeObjectStart();
            json.writeFields(w.pending.poll());
            json.writeObjectEnd();
        }

        // pass 2: the file
        StringWriter out = new StringWriter();
        json.setWriter(out);
        json.writeObjectStart();
        json.writeValue(SAVE_VERSION_FIELD, SAVE_VERSION);
        json.writeValue("gameVersion", BuildInfo.getVersion());
        GameData data = DatabaseLoader.getGameData();
        json.writeValue("dataVersion", data != null ? data.getDataVersion() : 0);
        json.writeArrayStart("packs");
        if (data != null) {
            for (Map.Entry<String, int[]> p : data.getPackVersions().entrySet()) {
                json.writeObjectStart();
                json.writeValue("id", p.getKey());
                json.writeValue("version", p.getValue()[0]);
                json.writeValue("dataVersion", p.getValue()[1]);
                json.writeObjectEnd();
            }
        }
        json.writeArrayEnd();
        json.writeValue("savedAt", savedAt.withNano(0).toString());
        writeSummary(json, game, saveName);
        json.writeObjectStart("game");
        json.writeFields(game);
        json.writeObjectEnd();
        for (Kind<?> kind : KINDS) {
            json.writeArrayStart(kind.table);
            for (Object o : w.tables.get(kind).values()) {
                json.writeObjectStart();
                json.writeFields(o);
                json.writeObjectEnd();
            }
            json.writeArrayEnd();
        }
        json.writeObjectEnd();
        return new JsonReader().parse(out.toString()).prettyPrint(OutputType.json, 100);
    }

    private static void writeSummary(Json json, SaveGame game, String saveName) {
        json.writeObjectStart("summary");
        json.writeValue("name", saveName);
        Person owner = game.getOwner();
        if (owner != null) {
            json.writeValue("manager", ((owner.getName() == null ? "" : owner.getName()) + " "
                    + (owner.getLastname() == null ? "" : owner.getLastname())).trim());
        }
        Club club = game.getCurrentClub();
        if (club != null) {
            json.writeValue("club", club.getName());
            json.writeValue("clubDataId", club.getDataId());
        }
        if (game.getGameDate() != null) {
            json.writeValue("gameDate", game.getGameDate().toString());
        }
        json.writeObjectEnd();
    }

    /** State of one encode: the Json with its serializers and the game objects found, per table, by id. */
    static final class Writing {
        final SaveGame game;
        final Json json = baseJson();
        final Map<Kind<?>, Map<Long, Object>> tables = new LinkedHashMap<>();
        final Map<Object, Boolean> seen = new IdentityHashMap<>();
        final Deque<Object> pending = new ArrayDeque<>();

        Writing(SaveGame game) {
            this.game = game;
            for (Kind<?> kind : KINDS) {
                tables.put(kind, new TreeMap<>());
                register(kind);
            }
            json.setSerializer(Country.class, new StaticRef<>(Country::getDataId, DatabaseLoader::getCountryByDataId, "country"));
            json.setSerializer(Continent.class, new StaticRef<>(Continent::getDataId, DatabaseLoader::getContinentByDataId, "continent"));
            json.setSerializer(Profession.class, new StaticRef<>(Profession::getDataId, DatabaseLoader::getProfessionByDataId, "profession"));
            json.setSerializer(PlayerProfession.class, new StaticRef<>(PlayerProfession::getDataId,
                    DatabaseLoader::getPlayerProfessionByDataId, "occupation"));
            json.setSerializer(Stadium.class, new StadiumSerializer(null));
            json.setSerializer(BasicScript.class, new ScriptSerializer(null));
        }

        private <T> void register(Kind<T> kind) {
            json.setSerializer(kind.type, new Json.Serializer<T>() {
                @Override
                public void write(Json j, T o, @SuppressWarnings("rawtypes") Class knownType) {
                    raw(j, reference(kind, o));
                }

                @Override
                public T read(Json j, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
                    throw new IllegalStateException("write only");
                }
            });
        }

        /** @return the id written for a game object, finding it on the way (an object without id gets one) */
        <T> Long reference(Kind<T> kind, T o) {
            if (seen.put(o, Boolean.TRUE) == null) {
                Long id = kind.getId.apply(o);
                if (id == null) {
                    id = game.getIdAllocator().nextId();
                    kind.setId.accept(o, id);
                }
                Object prev = tables.get(kind).putIfAbsent(id, o);
                if (prev != null) {
                    // two objects with one id would become one on load; keep the first, report the second
                    com.rndmodgames.futtoboru.system.Log.error(
                            com.rndmodgames.futtoboru.system.Log.SYSTEM_INIT, "SaveCodec",
                            "two " + kind.type.getSimpleName() + " objects share id " + id + "; only the first is saved");
                } else {
                    pending.add(o);
                }
            }
            return kind.getId.apply(o);
        }
    }

    // ------------------------------------------------------------------------------------------------ reading

    /**
     * Reads a save. Rebuilds the database objects from the game data first (so no object of an earlier game is left
     * in the lookups), then binds the lookups to the loaded game. Never throws.
     */
    public static LoadResult decode(String text) {
        JsonValue root;
        try {
            root = new JsonReader().parse(text);
        } catch (SerializationException | IllegalArgumentException e) {
            return LoadResult.failed(LoadResult.Status.UNREADABLE, UNREADABLE_MESSAGE);
        }
        if (root == null || !root.isObject()) {
            return LoadResult.failed(LoadResult.Status.UNREADABLE, UNREADABLE_MESSAGE);
        }
        JsonValue sv = root.get(SAVE_VERSION_FIELD);
        if (sv == null || !sv.isLong()) {
            return LoadResult.failed(LoadResult.Status.OLDER_VERSION, OLDER_VERSION_MESSAGE);
        }
        int version = sv.asInt();
        if (version > SAVE_VERSION) {
            return LoadResult.failed(LoadResult.Status.NEWER_VERSION, NEWER_VERSION_MESSAGE);
        }
        if (version < SAVE_VERSION) {
            root = SaveMigrations.migrate(root, version, SAVE_VERSION);
            if (root == null) {
                return LoadResult.failed(LoadResult.Status.OLDER_VERSION, OLDER_VERSION_MESSAGE);
            }
        }
        try {
            // the database is rebuilt from the game's own seed (P2-0), as it was when the game started
            JsonValue seed = root.has("game") ? root.get("game").get("seed") : null;
            if (seed != null && seed.isLong()) {
                DatabaseLoader.setRandom(RandomSource.seeded(seed.asLong()).fork("database"));
            }
            DatabaseLoader.rebuild();
            LoadResult result = new Reading(root).read();
            if (result.isLoaded() && result.getGame() != null) {
                // E-01b: a player the save and the data give no position (e.g. moved in the data since) gets the
                // slot his squad is shortest of
                com.rndmodgames.futtoboru.system.generators.PositionAssigner.fillMissing(
                        result.getGame().getAllClubs(), "loaded save");
                // R-11a: a person the save gives no languages (no seed to migrate with) gets them as a new game would
                com.rndmodgames.futtoboru.system.generators.LanguageGenerator.fillMissing(
                        result.getGame().getAllPersons(), DatabaseLoader.getLanguageData(),
                        DatabaseLoader.getGameData(), DatabaseLoader.getRandom());
            }
            return result;
        } catch (RuntimeException e) {
            com.rndmodgames.futtoboru.system.Log.error(
                    com.rndmodgames.futtoboru.system.Log.SYSTEM_INIT, "SaveCodec",
                    "save could not be read: " + e);
            return LoadResult.failed(LoadResult.Status.UNREADABLE, UNREADABLE_MESSAGE);
        }
    }

    /** State of one decode: one object per table entry, the problems found. */
    static final class Reading {
        final JsonValue root;
        final Json json = baseJson();
        final Map<Kind<?>, Map<Long, Object>> tables = new LinkedHashMap<>();
        final List<String> problems = new ArrayList<>();

        Reading(JsonValue root) {
            this.root = root;
            for (Kind<?> kind : KINDS) {
                tables.put(kind, new HashMap<>());
                register(kind);
            }
            json.setSerializer(Country.class, new StaticRef<>(Country::getDataId, DatabaseLoader::getCountryByDataId, "country", problems));
            json.setSerializer(Continent.class, new StaticRef<>(Continent::getDataId, DatabaseLoader::getContinentByDataId, "continent", problems));
            json.setSerializer(Profession.class, new StaticRef<>(Profession::getDataId, DatabaseLoader::getProfessionByDataId, "profession", problems));
            json.setSerializer(PlayerProfession.class, new StaticRef<>(PlayerProfession::getDataId,
                    DatabaseLoader::getPlayerProfessionByDataId, "occupation", problems));
            json.setSerializer(Stadium.class, new StadiumSerializer(problems));
            json.setSerializer(BasicScript.class, new ScriptSerializer(problems));
        }

        private <T> void register(Kind<T> kind) {
            json.setSerializer(kind.type, new Json.Serializer<T>() {
                @Override
                public void write(Json j, T o, @SuppressWarnings("rawtypes") Class knownType) {
                    throw new IllegalStateException("read only");
                }

                @Override
                public T read(Json j, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
                    if (v == null || v.isNull()) {
                        return null;
                    }
                    if (!v.isLong()) {
                        problems.add(kind.type.getSimpleName() + " reference `" + v + "` is not an id");
                        return null;
                    }
                    Object o = tables.get(kind).get(v.asLong());
                    if (o == null) {
                        problems.add(kind.type.getSimpleName() + " " + v.asLong() + " is referenced but not saved");
                    }
                    return kind.type.cast(o);
                }
            });
        }

        LoadResult read() {
            // every table entry becomes one object before any field is read: references resolve to it
            for (Kind<?> kind : KINDS) {
                JsonValue list = root.get(kind.table);
                for (JsonValue e = list == null ? null : list.child; e != null; e = e.next) {
                    JsonValue id = e.get("id");
                    if (id == null || !id.isLong()) {
                        problems.add(kind.table + ": an entry has no id, left out");
                        continue;
                    }
                    if (tables.get(kind).containsKey(id.asLong())) {
                        problems.add(kind.table + ": id " + id.asLong() + " is saved twice, the second is left out");
                        continue;
                    }
                    tables.get(kind).put(id.asLong(), create(kind, id.asLong()));
                }
            }
            for (Kind<?> kind : KINDS) {
                JsonValue list = root.get(kind.table);
                for (JsonValue e = list == null ? null : list.child; e != null; e = e.next) {
                    JsonValue id = e.get("id");
                    Object o = id != null && id.isLong() ? tables.get(kind).get(id.asLong()) : null;
                    if (o != null && kind.id(o) == id.asLong()) {
                        json.readFields(o, e);
                    }
                }
            }
            SaveGame game = new SaveGame();
            JsonValue g = root.get("game");
            if (g == null || !g.isObject()) {
                return LoadResult.failed(LoadResult.Status.UNREADABLE, UNREADABLE_MESSAGE);
            }
            json.readFields(game, g);

            SaveLinks.afterLoad(game, this);
            return new LoadResult(LoadResult.Status.OK, game, null, problems);
        }

        private static <T> T create(Kind<T> kind, long id) {
            T o = kind.create.get();
            kind.setId.accept(o, id);
            return o;
        }

        @SuppressWarnings("unchecked")
        <T> List<T> all(Class<T> type) {
            for (Kind<?> kind : KINDS) {
                if (kind.type == type) {
                    return new ArrayList<>((java.util.Collection<T>) tables.get(kind).values());
                }
            }
            return Collections.emptyList();
        }
    }

    // ------------------------------------------------------------------------------------------- serializers

    /** Writes a bare value (a number, a text or null) where the Json expects one. */
    static void raw(Json json, Object value) {
        try {
            json.getWriter().value(value);
        } catch (java.io.IOException e) {
            throw new SerializationException(e);
        }
    }

    /** The Json both directions start from: standard JSON, unknown fields ignored, compact dates and amounts. */
    static Json baseJson() {
        Json json = new Json(OutputType.json);
        json.setIgnoreUnknownFields(true);
        json.setSerializer(LocalDateTime.class, new Json.Serializer<LocalDateTime>() {
            @Override
            public void write(Json j, LocalDateTime d, @SuppressWarnings("rawtypes") Class knownType) {
                raw(j, d.toString());
            }

            @Override
            public LocalDateTime read(Json j, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
                if (v == null || v.isNull()) {
                    return null;
                }
                return LocalDateTime.parse(v.isObject() ? v.getString("value") : v.asString());
            }
        });
        json.setSerializer(BigDecimal.class, new Json.Serializer<BigDecimal>() {
            @Override
            public void write(Json j, BigDecimal n, @SuppressWarnings("rawtypes") Class knownType) {
                raw(j, n.toPlainString());
            }

            @Override
            public BigDecimal read(Json j, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
                if (v == null || v.isNull()) {
                    return null;
                }
                return new BigDecimal(v.isObject() ? v.getString("value") : v.asString());
            }
        });
        return json;
    }

    /** Static data: written as its data id, read back as the database's object (aliases followed). */
    static final class StaticRef<T> implements Json.Serializer<T> {
        private final Function<T, String> dataId;
        private final Function<String, T> lookup;
        private final String what;
        private final List<String> problems;

        StaticRef(Function<T, String> dataId, Function<String, T> lookup, String what) {
            this(dataId, lookup, what, null);
        }

        StaticRef(Function<T, String> dataId, Function<String, T> lookup, String what, List<String> problems) {
            this.dataId = dataId;
            this.lookup = lookup;
            this.what = what;
            this.problems = problems;
        }

        @Override
        public void write(Json json, T o, @SuppressWarnings("rawtypes") Class knownType) {
            raw(json, dataId.apply(o));
        }

        @Override
        public T read(Json json, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
            if (v == null || v.isNull()) {
                return null;
            }
            T o = v.isString() ? lookup.apply(v.asString()) : null;
            if (o == null && problems != null) {
                problems.add("unknown " + what + " `" + v.asString() + "` (not in the game data any more)");
            }
            return o;
        }
    }

    /**
     * A club's ground: its facts (name, opening year, description, source) come from the data by data id; what can
     * change in a game (capacity, value, ownership, rent, owner) is saved.
     */
    static final class StadiumSerializer implements Json.Serializer<Stadium> {
        private final List<String> problems;

        StadiumSerializer(List<String> problems) {
            this.problems = problems;
        }

        @Override
        public void write(Json json, Stadium s, @SuppressWarnings("rawtypes") Class knownType) {
            json.writeObjectStart();
            json.writeValue("id", s.getId());
            json.writeValue("dataId", s.getDataId());
            if (s.getDataId() == null) {
                json.writeValue("name", s.getName());
                json.writeValue("fullname", s.getFullname());
                json.writeValue("nickname", s.getNickname());
                json.writeValue("builtYear", s.getBuiltYear());
                json.writeValue("description", s.getDescription());
                json.writeValue("urlSource", s.getUrlSource());
            }
            json.writeValue("clubOwnerId", s.getClubOwnerId());
            json.writeValue("capacity", s.getCapacity());
            json.writeValue("value", s.getValue(), BigDecimal.class);
            json.writeValue("landValue", s.getLandValue(), BigDecimal.class);
            json.writeValue("owned", s.isOwned());
            json.writeValue("annualRent", s.getAnnualRent(), BigDecimal.class);
            json.writeObjectEnd();
        }

        @Override
        public Stadium read(Json json, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
            if (v == null || v.isNull()) {
                return null;
            }
            Stadium s = new Stadium();
            s.setId(v.has("id") && !v.get("id").isNull() ? v.getLong("id") : null);
            String dataId = v.getString("dataId", null);
            s.setDataId(dataId);
            if (dataId != null) {
                GameData data = DatabaseLoader.getGameData();
                DataRecord g = data != null ? data.get(GameData.GROUND, dataId) : null;
                if (g != null) {
                    s.setName(g.str("name"));
                    s.setBuiltYear(g.integer("opened"));
                    s.setDescription(g.str("description"));
                    s.setUrlSource(g.first("sources"));
                } else {
                    s.setName(dataId);
                    if (problems != null) {
                        problems.add("unknown ground `" + dataId + "` (not in the game data any more)");
                    }
                }
            } else {
                s.setName(v.getString("name", null));
                s.setFullname(v.getString("fullname", null));
                s.setNickname(v.getString("nickname", null));
                s.setBuiltYear(v.has("builtYear") && !v.get("builtYear").isNull() ? v.getInt("builtYear") : null);
                s.setDescription(v.getString("description", null));
                s.setUrlSource(v.getString("urlSource", null));
            }
            s.setClubOwnerId(v.has("clubOwnerId") && !v.get("clubOwnerId").isNull() ? v.getLong("clubOwnerId") : null);
            s.setCapacity(v.has("capacity") && !v.get("capacity").isNull() ? v.getInt("capacity") : null);
            s.setValue(json.readValue(BigDecimal.class, v.get("value")));
            s.setLandValue(json.readValue(BigDecimal.class, v.get("landValue")));
            s.setOwned(v.getBoolean("owned", false));
            s.setAnnualRent(json.readValue(BigDecimal.class, v.get("annualRent")));
            return s;
        }
    }

    /**
     * A script: its fields plus its values, each with its type (a value of type Object would lose it in JSON: club
     * ids would come back as floats, dates as text).
     */
    static final class ScriptSerializer implements Json.Serializer<BasicScript> {
        private final List<String> problems;

        ScriptSerializer(List<String> problems) {
            this.problems = problems;
        }

        @Override
        public void write(Json json, BasicScript s, @SuppressWarnings("rawtypes") Class knownType) {
            json.writeObjectStart();
            json.writeValue("id", s.getId());
            json.writeValue("name", s.getName());
            json.writeValue("description", s.getDescription());
            json.writeValue("scriptType", s.getScriptType());
            json.writeValue("executionTime", s.getExecutionTime(), LocalDateTime.class);
            json.writeValue("isExecuted", s.getIsExecuted());
            json.writeObjectStart("values");
            for (Map.Entry<String, Object> e : new TreeMap<>(s.getScriptValues()).entrySet()) {
                json.writeObjectStart(e.getKey());
                Object x = e.getValue();
                if (x == null) {
                    json.writeValue("t", "null");
                } else if (x instanceof Long) {
                    json.writeValue("t", "long");
                    json.writeValue("v", x, Long.class);
                } else if (x instanceof Integer) {
                    json.writeValue("t", "int");
                    json.writeValue("v", x, Integer.class);
                } else if (x instanceof Boolean) {
                    json.writeValue("t", "bool");
                    json.writeValue("v", x, Boolean.class);
                } else if (x instanceof LocalDateTime) {
                    json.writeValue("t", "date");
                    json.writeValue("v", x.toString());
                } else if (x instanceof Array) {
                    json.writeValue("t", "longs");
                    json.writeArrayStart("v");
                    for (Object item : (Array<?>) x) {
                        raw(json, item instanceof Number ? ((Number) item).longValue() : null);
                    }
                    json.writeArrayEnd();
                } else {
                    json.writeValue("t", "text");
                    json.writeValue("v", String.valueOf(x));
                }
                json.writeObjectEnd();
            }
            json.writeObjectEnd();
            json.writeObjectEnd();
        }

        @Override
        public BasicScript read(Json json, JsonValue v, @SuppressWarnings("rawtypes") Class type) {
            if (v == null || v.isNull()) {
                return null;
            }
            BasicScript s = new BasicScript();
            s.setId(v.has("id") && !v.get("id").isNull() ? v.getLong("id") : null);
            s.setName(v.getString("name", null));
            s.setDescription(v.getString("description", null));
            s.setScriptType(v.has("scriptType") && !v.get("scriptType").isNull() ? v.getInt("scriptType") : null);
            s.setExecutionTime(json.readValue(LocalDateTime.class, v.get("executionTime")));
            s.setIsExecuted(v.getBoolean("isExecuted", false));
            JsonValue values = v.get("values");
            for (JsonValue e = values == null ? null : values.child; e != null; e = e.next) {
                String t = e.getString("t", "null");
                JsonValue x = e.get("v");
                switch (t) {
                case "long":
                    s.getScriptValues().put(e.name, x.asLong());
                    break;
                case "int":
                    s.getScriptValues().put(e.name, x.asInt());
                    break;
                case "bool":
                    s.getScriptValues().put(e.name, x.asBoolean());
                    break;
                case "date":
                    s.getScriptValues().put(e.name, LocalDateTime.parse(x.asString()));
                    break;
                case "longs":
                    Array<Long> longs = new Array<>();
                    for (JsonValue i = x.child; i != null; i = i.next) {
                        longs.add(i.asLong());
                    }
                    s.getScriptValues().put(e.name, longs);
                    break;
                case "text":
                    s.getScriptValues().put(e.name, x.asString());
                    break;
                default:
                    s.getScriptValues().put(e.name, null);
                    if (!"null".equals(t) && problems != null) {
                        problems.add("script `" + s.getName() + "`: value " + e.name + " has unknown type " + t);
                    }
                    break;
                }
            }
            return s;
        }
    }
}
