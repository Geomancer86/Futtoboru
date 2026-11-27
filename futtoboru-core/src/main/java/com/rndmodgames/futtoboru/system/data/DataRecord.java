package com.rndmodgames.futtoboru.system.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.JsonValue;

/**
 * One record of a data file that passed the checks of {@link GameData}: its JSON, its data id (if its kind has
 * one), its runtime id and its references already resolved through the aliases.
 */
public class DataRecord {

    final String spec;
    final String kind;
    final JsonValue json;
    final String where;
    final String pack;
    final String season;
    final DataRecord parent;

    long runtimeId;
    boolean live = true;

    /** References still to check, then resolved values: field -> canonical id / ids. */
    final List<Ref> refs = new ArrayList<>();
    final Map<String, String> resolvedRef = new HashMap<>();
    final Map<String, List<String>> resolvedRefs = new HashMap<>();
    final List<DataRecord> children = new ArrayList<>();

    DataRecord(String spec, String kind, JsonValue json, String where, String pack, String season, DataRecord parent) {
        this.spec = spec;
        this.kind = kind;
        this.json = json;
        this.where = where;
        this.pack = pack;
        this.season = season;
        this.parent = parent;
    }

    /** A reference found in a field; {@code index} is -1 for a single reference, else its place in the list. */
    static final class Ref {
        final String field;
        final String kind;
        final String id;
        final int index;
        final String where;
        boolean dropped;

        Ref(String field, String kind, String id, int index, String where) {
            this.field = field;
            this.kind = kind;
            this.id = id;
            this.index = index;
            this.where = where;
        }
    }

    /** @return the data id (lower-case slug), or null for a kind without ids (squads, names, modifiers) */
    public String id() {
        return kind == null ? null : json.getString("id", null);
    }

    /** @return the runtime id handed out at load (0 for kinds without ids) */
    public long runtimeId() {
        return runtimeId;
    }

    /** @return the season folder this record came from, or null for pack-level files */
    public String season() {
        return season;
    }

    /** @return the pack this record came from */
    public String pack() {
        return pack;
    }

    /** @return where the record is (file and index), for messages */
    public String where() {
        return where;
    }

    public boolean has(String field) {
        JsonValue v = json.get(field);
        return v != null && !v.isNull();
    }

    public String str(String field) {
        return str(field, null);
    }

    public String str(String field, String def) {
        JsonValue v = json.get(field);
        return v == null || v.isNull() ? def : v.asString();
    }

    public Integer integer(String field) {
        JsonValue v = json.get(field);
        return v == null || v.isNull() ? null : v.asInt();
    }

    public BigDecimal decimal(String field) {
        JsonValue v = json.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.isLong() ? BigDecimal.valueOf(v.asLong()) : BigDecimal.valueOf(v.asDouble());
    }

    public Boolean bool(String field, Boolean def) {
        JsonValue v = json.get(field);
        return v == null || v.isNull() ? def : v.asBoolean();
    }

    public List<String> strs(String field) {
        JsonValue v = json.get(field);
        if (v == null || v.isNull()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>(v.size);
        for (JsonValue x = v.child; x != null; x = x.next) {
            out.add(x.asString());
        }
        return out;
    }

    /** @return the first entry of a list of texts (e.g. the first source), or null */
    public String first(String field) {
        List<String> all = strs(field);
        return all.isEmpty() ? null : all.get(0);
    }

    /** @return an attribute object as name -> whole number, in file order */
    public Map<String, Integer> attrs(String field) {
        Map<String, Integer> out = new LinkedHashMap<>();
        JsonValue v = json.get(field);
        if (v != null && v.isObject()) {
            for (JsonValue x = v.child; x != null; x = x.next) {
                out.put(x.name, (int) Math.round(x.asDouble()));
            }
        }
        return out;
    }

    /** @return the canonical id a reference field points to (after aliases), or null when absent */
    public String ref(String field) {
        return resolvedRef.get(field);
    }

    /** @return the canonical ids a list-of-references field points to (unresolved entries are left out) */
    public List<String> refs(String field) {
        List<String> out = resolvedRefs.get(field);
        return out == null ? Collections.emptyList() : out;
    }

    /** @return the live nested records (a season's editions) */
    public List<DataRecord> children() {
        List<DataRecord> out = new ArrayList<>(children.size());
        for (DataRecord c : children) {
            if (c.live) {
                out.add(c);
            }
        }
        return out;
    }

    @Override
    public String toString() {
        return where;
    }
}
