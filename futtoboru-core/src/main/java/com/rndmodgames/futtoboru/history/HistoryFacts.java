package com.rndmodgames.futtoboru.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.HistoryName;
import com.rndmodgames.futtoboru.system.data.HistoryText;

/**
 * G-10: the history facts the club and player screens show (H-05 data), looked up headless so they can be tested:
 * a club's hook, history, season narratives, nicknames, rivals and name history; a person's fan-love tier,
 * nicknames, known-as name and bio. Texts fall back to English. Nothing here touches the UI.
 */
public final class HistoryFacts {

    /** Highest fan-love tier (legend); the labels are the bundle keys {@code fan_tier_0} .. {@code fan_tier_5}. */
    public static final int MAX_TIER = 5;

    private final GameData data;
    private final String lang;
    private final Map<String, Map<String, Integer>> tiersByClub = new HashMap<>();

    /** @param data the loaded game data (may be null: everything is then empty); @param lang text language, e.g. "en" */
    public HistoryFacts(GameData data, String lang) {
        this.data = data;
        this.lang = lang == null ? "en" : lang;
    }

    /** @return the data text language of a bundle language id ({@code sp_SP} is Spanish, everything else English) */
    public static String dataLanguage(String bundleLanguageId) {
        return bundleLanguageId != null && bundleLanguageId.toLowerCase(Locale.ROOT).startsWith("sp") ? "es" : "en";
    }

    /** @return the bundle key of a tier's label, the tier clamped to 0..5 */
    public static String tierKey(int tier) {
        return "fan_tier_" + Math.max(0, Math.min(MAX_TIER, tier));
    }

    /** What the club panel shows; every list may be empty and the strings null. */
    public static final class ClubHistory {
        public final String hook;
        public final List<String> history;
        /** season id to narrative, in file order */
        public final Map<String, String> seasons;
        public final List<HistoryName> nicknames;
        /** canonical ids of the rival clubs */
        public final List<String> rivals;
        public final List<HistoryName> nameHistory;

        ClubHistory(String hook, List<String> history, Map<String, String> seasons, List<HistoryName> nicknames,
                List<String> rivals, List<HistoryName> nameHistory) {
            this.hook = hook;
            this.history = history;
            this.seasons = seasons;
            this.nicknames = nicknames;
            this.rivals = rivals;
            this.nameHistory = nameHistory;
        }

        public boolean isEmpty() {
            return (hook == null || hook.isEmpty()) && history.isEmpty() && seasons.isEmpty() && nicknames.isEmpty()
                    && rivals.isEmpty() && nameHistory.isEmpty();
        }
    }

    /** What the player screen shows. */
    public static final class PersonHistory {
        /** fan-love tier 0..5 (0 = unknown) */
        public final int tier;
        public final List<HistoryName> nicknames;
        /** the display name where it differs, or null */
        public final String knownAs;
        public final String bio;

        PersonHistory(int tier, List<HistoryName> nicknames, String knownAs, String bio) {
            this.tier = tier;
            this.nicknames = nicknames;
            this.knownAs = knownAs;
            this.bio = bio;
        }
    }

    /** @return the history of a club by data id (never null) */
    public ClubHistory club(String clubDataId) {
        DataRecord rec = data == null || clubDataId == null ? null : data.get(GameData.CLUB, clubDataId);
        if (rec == null) {
            return new ClubHistory(null, Collections.<String>emptyList(), Collections.<String, String>emptyMap(),
                    Collections.<HistoryName>emptyList(), Collections.<String>emptyList(),
                    Collections.<HistoryName>emptyList());
        }
        HistoryText t = data.historyText(GameData.CLUB, clubDataId, lang);
        List<String> rivals = new ArrayList<>();
        for (String r : rec.refs("rivals")) {
            String canonical = data.resolve(GameData.CLUB, r);
            if (canonical != null && !rivals.contains(canonical)) {
                rivals.add(canonical);
            }
        }
        return new ClubHistory(t == null ? null : t.getHook(),
                t == null ? Collections.<String>emptyList() : t.getHistory(),
                t == null ? Collections.<String, String>emptyMap() : t.getSeasons(),
                data.nicknames(rec), rivals, data.nameHistory(rec));
    }

    /** @return the history of a person by data id (never null); {@code clubDataId} picks the squad for the fan tier */
    public PersonHistory person(String personDataId, String clubDataId) {
        DataRecord rec = data == null || personDataId == null ? null : data.get(GameData.PERSON, personDataId);
        if (rec == null) {
            return new PersonHistory(0, Collections.<HistoryName>emptyList(), null, null);
        }
        HistoryText t = data.historyText(GameData.PERSON, personDataId, lang);
        String knownAs = rec.str("knownAs");
        return new PersonHistory(fanTier(personDataId, clubDataId), data.nicknames(rec),
                knownAs == null || knownAs.isEmpty() ? null : knownAs, t == null ? null : t.getBio());
    }

    /** @return the fan-love tier 0..5 of a person in a club's squad; 0 when the data holds none */
    public int fanTier(String personDataId, String clubDataId) {
        if (data == null || personDataId == null || clubDataId == null) {
            return 0;
        }
        String club = data.resolve(GameData.CLUB, clubDataId);
        String person = data.resolve(GameData.PERSON, personDataId);
        if (club == null || person == null) {
            return 0;
        }
        Map<String, Integer> tiers = tiersByClub.get(club);
        if (tiers == null) {
            tiers = data.squadFanTiers(data.squadOf(club));
            tiersByClub.put(club, tiers);
        }
        Integer tier = tiers.get(person);
        return tier == null ? 0 : Math.max(0, Math.min(MAX_TIER, tier));
    }

    /** @return the club's name from the data, or the id when unknown */
    public String clubName(String clubDataId) {
        DataRecord rec = data == null || clubDataId == null ? null : data.get(GameData.CLUB, clubDataId);
        return rec == null ? clubDataId : rec.str("name", clubDataId);
    }

    /** @return "1888-1890", "1888-" (open) or "-1890" for a name's years, "" when none are known */
    public static String years(HistoryName n) {
        if (n.getFrom() == 0 && n.getTo() == 0) {
            return "";
        }
        if (n.getTo() == 0) {
            return n.getFrom() + "-";
        }
        return (n.getFrom() == 0 ? "" : String.valueOf(n.getFrom())) + "-" + n.getTo();
    }
}
