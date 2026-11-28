package com.rndmodgames.futtoboru.system.save;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import com.badlogic.gdx.utils.JsonValue;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.language.LanguageSkill;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.LanguageData;
import com.rndmodgames.futtoboru.system.generators.LanguageGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;

/**
 * Save migrations (P2-5, the DarkBlade pattern): one step per save version, each turns the JSON tree of a version
 * {@code n} save into a version {@code n + 1} tree, before any object is read. Steps are never removed: a save of
 * any supported version climbs the steps up to {@link SaveCodec#SAVE_VERSION}.
 *
 * Version 1 is the first versioned format; v0.4 saves (no {@code saveVersion}) are not migrated (decision D15).
 * To change the format: raise {@link SaveCodec#SAVE_VERSION}, add the step {@code old -> old + 1} here, and keep the
 * real old saves under {@code src/test/resources/saves/} loading in the tests.
 */
public final class SaveMigrations {

    /** from version -> step to the next version. */
    private static final Map<Integer, UnaryOperator<JsonValue>> STEPS = new TreeMap<>();
    static {
        STEPS.put(1, SaveMigrations::addPositionAndDribbling);
        STEPS.put(2, SaveMigrations::addLanguages);
    }

    private SaveMigrations() {
    }

    /** @return the steps (read-only), for tests */
    public static Map<Integer, UnaryOperator<JsonValue>> steps() {
        return Collections.unmodifiableMap(STEPS);
    }

    /**
     * Climbs a save tree from {@code version} to {@code target}.
     *
     * @return the migrated tree (its {@code saveVersion} updated), or null when a step is missing
     */
    public static JsonValue migrate(JsonValue root, int version, int target) {
        JsonValue tree = root;
        for (int v = version; v < target; v++) {
            UnaryOperator<JsonValue> step = STEPS.get(v);
            if (step == null) {
                return null;
            }
            tree = step.apply(tree);
            JsonValue sv = tree.get(SaveCodec.SAVE_VERSION_FIELD);
            if (sv != null) {
                sv.set(v + 1, null);
            }
        }
        return tree;
    }

    /**
     * v1 -> v2 (E-01): every player gets {@code dribbling} (the squad's sourced value, else derived from his saved
     * attributes exactly as a new game derives it) and, when the data has one, {@code position} (his club's squad
     * entry). The random attributes stay as saved.
     */
    static JsonValue addPositionAndDribbling(JsonValue root) {
        GameData data = DatabaseLoader.getInstance().getGameData();
        Map<Long, String> personDataId = new HashMap<>();
        for (JsonValue p = child(root, "persons"); p != null; p = p.next) {
            if (p.has("id") && p.has("dataId")) {
                personDataId.put(p.getLong("id"), p.getString("dataId"));
            }
        }
        Map<Long, DataRecord> squadOfPlayer = new HashMap<>();
        if (data != null) {
            for (JsonValue c = child(root, "clubs"); c != null; c = c.next) {
                DataRecord squad = c.has("dataId") ? data.squadOf(c.getString("dataId")) : null;
                for (JsonValue id = squad == null ? null : child(c, "players"); id != null; id = id.next) {
                    if (id.isLong()) {
                        squadOfPlayer.put(id.asLong(), squad);
                    }
                }
            }
        }
        for (JsonValue p = child(root, "players"); p != null; p = p.next) {
            Long id = p.has("id") ? p.getLong("id") : null;
            Long personId = p.has("person") && p.get("person").isLong() ? p.getLong("person") : null;
            DataRecord squad = id == null ? null : squadOfPlayer.get(id);
            String person = data == null || personId == null ? null
                    : data.resolve(GameData.PERSON, personDataId.get(personId));
            if (!p.has("dribbling")) {
                Integer sourced = squad == null || person == null ? null : data.squadDribbling(squad).get(person);
                float value = sourced != null ? sourced
                        : PlayerAttributeGenerator.deriveDribbling(attr(p, "dexterity"), attr(p, "acceleration"),
                                attr(p, "trickShots"), attr(p, "speed"));
                p.addChild("dribbling", new JsonValue((long) value));
            }
            if (!p.has("position") && squad != null && person != null) {
                Position pos = data.squadPositions(squad).get(person);
                if (pos != null) {
                    p.addChild("position", new JsonValue(pos.name()));
                }
            }
        }
        return root;
    }

    /**
     * v2 -> v3 (R-11a): every person gets {@code languages}, {@code homeLanguage}, {@code birthArea} and
     * {@code parentsOrigin}, generated exactly as a new game generates them: on the person's own fork of the game's
     * database stream ({@code seeded(seed).fork("database").fork(LanguageGenerator.streamName(person))}), from his
     * saved country and birth date. A save without a seed is left as it is (the load fills the gap afterwards).
     */
    static JsonValue addLanguages(JsonValue root) {
        return addLanguages(root, DatabaseLoader.getInstance().getLanguageData(), DatabaseLoader.getInstance().getGameData());
    }

    /** The v2 -> v3 step with the data given (public for tests). */
    public static JsonValue addLanguages(JsonValue root, LanguageData languages, GameData data) {
        JsonValue seed = root.has("game") ? root.get("game").get("seed") : null;
        if (languages == null || seed == null || !seed.isLong()) {
            return root;
        }
        RandomSource database = RandomSource.seeded(seed.asLong()).fork("database");
        for (JsonValue p = child(root, "persons"); p != null; p = p.next) {
            if (p.has("languages") && p.get("languages").size > 0) {
                continue;
            }
            Person person = new Person();
            person.setDataId(p.getString("dataId", null));
            person.setId(p.has("id") && p.get("id").isLong() ? p.getLong("id") : null);
            person.setName(p.getString("name", null));
            person.setLastname(p.getString("lastname", null));
            String born = p.has("birthDate") && p.get("birthDate").isString() ? p.getString("birthDate") : null;
            person.setBirthDate(born == null ? null : java.time.LocalDateTime.parse(born));
            String country = p.has("country") && p.get("country").isString() ? p.getString("country") : null;
            if (country != null) {
                // the generator needs only the data id (no database lookup before the load rebuilds it)
                com.rndmodgames.futtoboru.data.Country c = new com.rndmodgames.futtoboru.data.Country();
                c.setDataId(country);
                person.setCountry(c);
            }
            LanguageGenerator.assign(person, languages, data, database);
            if (p.has("languages")) {
                p.remove("languages");
            }
            JsonValue list = new JsonValue(JsonValue.ValueType.array);
            for (LanguageSkill s : person.getLanguages()) {
                JsonValue o = new JsonValue(JsonValue.ValueType.object);
                o.addChild("language", new JsonValue(s.getLanguage()));
                o.addChild("speak", new JsonValue(s.getSpeak()));
                o.addChild("understand", new JsonValue(s.getUnderstand()));
                o.addChild("readWrite", new JsonValue(s.getReadWrite()));
                list.addChild(o);
            }
            p.addChild("languages", list);
            put(p, "homeLanguage", person.getHomeLanguage());
            put(p, "birthArea", person.getBirthArea());
            put(p, "parentsOrigin", person.getParentsOrigin());
        }
        return root;
    }

    private static void put(JsonValue o, String field, String value) {
        if (o.has(field)) {
            o.remove(field);
        }
        if (value != null) {
            o.addChild(field, new JsonValue(value));
        }
    }

    private static JsonValue child(JsonValue v, String field) {
        JsonValue list = v == null ? null : v.get(field);
        return list == null ? null : list.child;
    }

    private static Float attr(JsonValue player, String field) {
        JsonValue v = player.get(field);
        return v == null || !v.isNumber() ? null : v.asFloat();
    }
}
