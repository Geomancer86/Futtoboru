package com.rndmodgames.futtoboru.system.save;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import com.badlogic.gdx.utils.JsonValue;

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

    /** from version -> step to the next version. Empty: v1 is the current and first format. */
    private static final Map<Integer, UnaryOperator<JsonValue>> STEPS = new TreeMap<>();

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
}
