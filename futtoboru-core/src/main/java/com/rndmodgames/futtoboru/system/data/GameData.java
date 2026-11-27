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
 * never stops the game. Every record with a data id gets a runtime id below {@link IdAllocator#GAME_START}, in
 * load order (pack, file, record).
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

    static final List<String> KINDS = Arrays.asList(CONTINENT, COUNTRY, PROFESSION, OCCUPATION, COMPETITION, CLUB,
            GROUND, PERSON, REGION, SEASON, EDITION);

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
        spec("competition", "id", "str!", "name", "str!", "longName", "str", "type", "enum:competition_type!",
                "sources", "strs!", "notes", "str");
        spec("club", "id", "str!", "name", "str!", "fullName", "str", "founded", "int", "country", "ref:country!",
                "sources", "strs!", "notes", "str");
        spec("ground", "id", "str!", "name", "str!", "opened", "int", "capacity", "int", "value", "num",
                "description", "str", "landValue", "num", "ownedByClub", "bool", "annualRent", "num",
                "sources", "strs!", "notes", "str");
        spec("person", "id", "str!", "firstName", "str!", "lastName", "str!", "born", "date!", "country",
                "ref:country!", "confidence", "enum:confidence!", "sources", "strs!", "notes", "str");
        spec("season", "id", "str!", "name", "str!", "description", "str", "start", "datetime!", "sources", "strs!",
                "countries", "refs:country!", "editions", "list:edition!");
        spec("edition", "id", "str!", "competition", "ref:competition!", "name", "str!", "participants",
                "refs:club!", "sources", "strs!", "notes", "str");
        spec("squad", "club", "ref:club!", "ground", "ref:ground", "balance", "num", "players", "refs:person!",
                "notes", "str");
        spec("nationality_modifier", "country", "ref:country!", "modifiers", "attrs!", "notes", "str",
                "footballingStrength", "num");
        spec("region", "id", "str!", "name", "str!", "country", "ref:country!", "modifiers", "attrs!",
                "places", "strs", "notes", "str");
        spec("pack", "id", "str!", "name", "str!", "version", "int!", "dataVersion", "int!", "seasons", "strs!",
                "files", "strs!");
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
        SEASON_FILES.put("squads.json", new String[] { "squad", null });
        SEASON_FILES.put("nationality_modifiers.json", new String[] { "nationality_modifier", null });
        SEASON_FILES.put("region_modifiers.json", new String[] { "region", REGION });
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

    /** @return the allocator for runtime copies made while building the database (continues after the records) */
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
            } else if (type.startsWith("ref:")) {
                if (!v.isString()) {
                    expect(false, v, where, "an id");
                } else if (rec != null) {
                    rec.refs.add(new DataRecord.Ref(field, type.substring(4), v.asString(), -1, where));
                }
            } else if (type.startsWith("refs:")) {
                if (!isListOfStrings(v)) {
                    expect(false, v, where, "a list of ids");
                    break;
                }
                Map<String, Integer> seen = new LinkedHashMap<>();
                int i = 0;
                for (JsonValue x = v.child; x != null; x = x.next, i++) {
                    seen.merge(x.asString(), 1, Integer::sum);
                    if (rec != null) {
                        rec.refs.add(new DataRecord.Ref(field, type.substring(5), x.asString(), i, where + "[" + i + "]"));
                    }
                }
                for (Map.Entry<String, Integer> s : seen.entrySet()) {
                    if (s.getValue() > 1) {
                        report.error(where, "`" + s.getKey() + "` listed " + s.getValue() + " times");
                    }
                }
            } else if (type.startsWith("list:")) {
                // nested records (a season's editions) are checked one by one as records of their own
                expect(v.isArray(), v, where, "a list");
            }
            break;
        }
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

    /** Runtime ids for every live record with a data id, in load order; then the allocator for copies. */
    private void assignRuntimeIds() {
        dataIds = new IdAllocator(IdAllocator.DATA_START);
        for (DataRecord r : loaded) {
            if (r.live && r.kind != null && (r.parent == null || r.parent.live)) {
                r.runtimeId = dataIds.nextId();
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
