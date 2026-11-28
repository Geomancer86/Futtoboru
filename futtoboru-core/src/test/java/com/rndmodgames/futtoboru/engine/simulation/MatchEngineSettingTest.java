package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Preferences;
import com.rndmodgames.PreferencesManager;

/** U-13: the Settings match engine option: preference round trip, default, and the flag override. */
class MatchEngineSettingTest {

    @AfterEach
    void back() {
        MatchEngines.reset();
    }

    private static PreferencesManager memoryPrefs(Map<String, String> store) {
        Preferences p = (Preferences) Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                new Class<?>[] { Preferences.class }, (proxy, m, a) -> {
                    switch (m.getName()) {
                    case "putString": store.put((String) a[0], (String) a[1]); return proxy;
                    case "getString": return store.getOrDefault(a[0], a.length > 1 ? (String) a[1] : "");
                    default: return null;
                    }
                });
        return new PreferencesManager(p);
    }

    @Test
    void defaultIsInstantAndPreferenceRoundTrips() {
        Map<String, String> store = new HashMap<>();
        PreferencesManager prefs = memoryPrefs(store);
        assertEquals(MatchEngine.INSTANT, prefs.getMatchEngine());
        prefs.updateMatchEngine(MatchEngine.ECS);
        assertEquals(MatchEngine.ECS, memoryPrefs(store).getMatchEngine());
        prefs.updateMatchEngine(MatchEngine.INSTANT);
        assertEquals(MatchEngine.INSTANT, memoryPrefs(store).getMatchEngine());
    }

    @Test
    void preferenceSelectsEngineWhenNoFlag() {
        assertEquals(MatchEngine.INSTANT, MatchEngines.choose(null, null));
        assertEquals(MatchEngine.ECS, MatchEngines.choose(null, "ecs"));
        assertEquals(MatchEngine.ECS, MatchEngines.choose("", "ecs"));
        assertEquals(MatchEngine.INSTANT, MatchEngines.choose(null, "instant"));
    }

    @Test
    void flagOverridesPreference() {
        assertEquals(MatchEngine.INSTANT, MatchEngines.choose("instant", "ecs"));
        assertEquals(MatchEngine.ECS, MatchEngines.choose("ecs", "instant"));
    }
}
