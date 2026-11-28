package com.rndmodgames.futtoboru.system.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * H-05: the prose about a club or a person from {@code text/<lang>/history.json}: a club has a one-line hook, a
 * few paragraphs of history and a narrative per season; a person has a bio. Every entry carries its sources.
 */
public final class HistoryText {

    private final String hook;
    private final List<String> history;
    private final Map<String, String> seasons;
    private final String bio;
    private final List<String> sources;

    HistoryText(String hook, List<String> history, Map<String, String> seasons, String bio, List<String> sources) {
        this.hook = hook;
        this.history = Collections.unmodifiableList(history);
        this.seasons = Collections.unmodifiableMap(new LinkedHashMap<>(seasons));
        this.bio = bio;
        this.sources = Collections.unmodifiableList(sources);
    }

    /** @return the one-line summary of a club, or null for a person */
    public String getHook() {
        return hook;
    }

    /** @return the paragraphs of a club's history (empty for a person) */
    public List<String> getHistory() {
        return history;
    }

    /** @return the narrative of a club's season ({@code 1888-89}), or null when there is none */
    public String getSeason(String season) {
        return seasons.get(season);
    }

    public Map<String, String> getSeasons() {
        return seasons;
    }

    /** @return the short biography of a person, or null for a club */
    public String getBio() {
        return bio;
    }

    public List<String> getSources() {
        return sources;
    }
}
