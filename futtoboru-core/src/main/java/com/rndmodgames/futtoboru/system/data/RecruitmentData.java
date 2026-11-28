package com.rndmodgames.futtoboru.system.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.recruitment.Origin;
import com.rndmodgames.futtoboru.data.recruitment.OriginClub;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.data.recruitment.RegistrationRule;
import com.rndmodgames.futtoboru.data.recruitment.WageRecord;

/**
 * R-02: the recruitment data of the packs ({@code recruitment/rules.json}, {@code wages.json}, {@code origins.json},
 * {@code origin_clubs.json}) as read-only objects, with the lookups later slices need (rules in force on a day, the
 * wage band of a standing) and R-03's candidate pool regions ({@code recruitment/pools.json}). Built once per
 * {@link GameData}.
 */
public final class RecruitmentData {

    private final List<RegistrationRule> rules = new ArrayList<>();
    private final List<WageRecord> wages = new ArrayList<>();
    private final Map<String, Origin> origins = new LinkedHashMap<>();
    private final Map<String, OriginClub> originClubs = new LinkedHashMap<>();
    private final Map<String, PoolRegion> pools = new LinkedHashMap<>();

    private RecruitmentData() {
    }

    public static RecruitmentData from(GameData data) {
        RecruitmentData out = new RecruitmentData();
        for (DataRecord r : data.all(GameData.RULE)) {
            out.rules.add(new RegistrationRule(r.id(), r.runtimeId(),
                    RegistrationRule.Kind.fromDataId(r.str("rule")), day(r.str("from")), day(r.str("until")),
                    r.str("body"), r.str("appliesTo"), r.integer("days"), r.integer("miles"), r.integer("years"),
                    r.integer("pence"), r.str("confidence"), r.strs("sources")));
        }
        for (DataRecord r : data.all(GameData.WAGE)) {
            out.wages.add(new WageRecord(r.id(), r.runtimeId(), "band".equals(r.str("type")), day(r.str("from")),
                    day(r.str("until")), r.str("standing"), r.ref("club"), r.ref("person"), r.integer("weeklyPence"),
                    r.integer("summerWeeklyPence"), r.integer("minWeeklyPence"), r.integer("maxWeeklyPence"),
                    r.str("confidence"), r.strs("sources")));
        }
        for (DataRecord r : data.all(GameData.ORIGIN)) {
            out.origins.put(r.id(), new Origin(r.id(), r.runtimeId(), r.str("name"), r.refs("occupations"),
                    r.strs("sourceKinds"), r.attrs("nudges"), r.integer("literacyShift"), r.str("confidence")));
        }
        for (DataRecord r : data.all(GameData.ORIGIN_CLUB)) {
            out.originClubs.put(r.id(), new OriginClub(r.id(), r.runtimeId(), r.str("name"), r.str("kind"),
                    r.str("place"), r.ref("country"), r.ref("area"), r.integer("founded"), r.ref("club"),
                    r.refs("origins")));
        }
        // R-03 candidate pool regions
        for (DataRecord r : data.all(GameData.POOL_REGION)) {
            com.badlogic.gdx.utils.JsonValue up = r.raw("upbringing");
            Map<String, Integer> tendencies = new LinkedHashMap<>();
            JsonValueReader.attrs(up == null ? null : up.get("tendencies"), tendencies);
            PoolRegion.Upbringing upbringing = new PoolRegion.Upbringing(up == null ? "" : up.getString("style", ""),
                    tendencies, up == null ? 0 : up.getDouble("chance", 0));
            Map<String, Double> foreign = new LinkedHashMap<>();
            com.badlogic.gdx.utils.JsonValue fb = r.raw("foreignBorn");
            for (com.badlogic.gdx.utils.JsonValue x = fb == null ? null : fb.child; x != null; x = x.next) {
                foreign.put(x.name, x.asDouble());
            }
            List<PoolRegion.PoolPerson> people = new ArrayList<>();
            com.badlogic.gdx.utils.JsonValue list = r.raw("people");
            int i = 0;
            for (com.badlogic.gdx.utils.JsonValue x = list == null ? null : list.child; x != null; x = x.next, i++) {
                // a nested reference is kept by its path as a one-entry list (P2-14)
                List<String> resolved = r.refs("people[" + i + "].person");
                String person = resolved.isEmpty() ? null : resolved.get(0);
                if (person == null) {
                    continue; // an unresolved person (reported by the load)
                }
                com.badlogic.gdx.utils.JsonValue until = x.get("until");
                people.add(new PoolRegion.PoolPerson(person, x.getInt("from"),
                        until == null || until.isNull() ? null : until.asInt(), x.getString("kind"),
                        x.getString("playsFor"), x.getString("position", null)));
            }
            out.pools.put(r.id(), new PoolRegion(r.id(), r.runtimeId(), r.str("name"), r.ref("country"), r.ref("area"),
                    r.strs("places"), r.refs("clubs"), r.integer("size"), r.integer("monthly"), r.strs("sourceKinds"),
                    r.refs("originClubs"), r.strs("localClubs"), foreign, upbringing, people));
        }
        return out;
    }

    /** yyyy, yyyy-mm or yyyy-mm-dd as its first day; null stays null. */
    static LocalDate day(String date) {
        if (date == null) {
            return null;
        }
        String[] p = date.split("-");
        return LocalDate.of(Integer.parseInt(p[0]), p.length > 1 ? Integer.parseInt(p[1]) : 1,
                p.length > 2 ? Integer.parseInt(p[2]) : 1);
    }

    public List<RegistrationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /** @return the rules in force on that day, in file order */
    public List<RegistrationRule> rulesInForce(LocalDate day) {
        List<RegistrationRule> out = new ArrayList<>();
        for (RegistrationRule r : rules) {
            if (r.inForceOn(day)) {
                out.add(r);
            }
        }
        return out;
    }

    /** @return the latest-starting rule of a kind in force on that day, or null */
    public RegistrationRule rule(RegistrationRule.Kind kind, LocalDate day) {
        RegistrationRule best = null;
        for (RegistrationRule r : rulesInForce(day)) {
            if (r.getKind() == kind && (best == null || r.getFrom().isAfter(best.getFrom()))) {
                best = r;
            }
        }
        return best;
    }

    public List<WageRecord> getWages() {
        return Collections.unmodifiableList(wages);
    }

    /** @return the wage band of a standing on that day (that standing first, else {@code any}), or null */
    public WageRecord band(String standing, LocalDate day) {
        WageRecord any = null;
        for (WageRecord w : wages) {
            if (w.isBand() && w.covers(day)) {
                if (w.getStanding().equals(standing)) {
                    return w;
                }
                if ("any".equals(w.getStanding()) && any == null) {
                    any = w;
                }
            }
        }
        return any;
    }

    public Map<String, Origin> getOrigins() {
        return Collections.unmodifiableMap(origins);
    }

    public Origin origin(String id) {
        return id == null ? null : origins.get(id);
    }

    public Map<String, OriginClub> getOriginClubs() {
        return Collections.unmodifiableMap(originClubs);
    }

    /** R-03: the candidate pool regions, in file order */
    public Map<String, PoolRegion> getPools() {
        return Collections.unmodifiableMap(pools);
    }

    /** @return the pool region of a game club (by its data id), or null when no region lists it */
    public PoolRegion poolOfClub(String clubDataId) {
        for (PoolRegion r : pools.values()) {
            if (clubDataId != null && r.clubs().contains(clubDataId)) {
                return r;
            }
        }
        return null;
    }

    /** Reads a JSON attribute object (name -> whole number). */
    private static final class JsonValueReader {
        static void attrs(com.badlogic.gdx.utils.JsonValue v, Map<String, Integer> out) {
            for (com.badlogic.gdx.utils.JsonValue x = v == null || !v.isObject() ? null : v.child; x != null;
                    x = x.next) {
                out.put(x.name, (int) Math.round(x.asDouble()));
            }
        }
    }

    /** @return the origin clubs of a rate area, in file order */
    public List<OriginClub> originClubsIn(String area) {
        List<OriginClub> out = new ArrayList<>();
        for (OriginClub c : originClubs.values()) {
            if (area != null && area.equals(c.getAreaDataId())) {
                out.add(c);
            }
        }
        return out;
    }
}
