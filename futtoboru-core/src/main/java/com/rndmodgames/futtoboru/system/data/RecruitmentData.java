package com.rndmodgames.futtoboru.system.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.recruitment.Origin;
import com.rndmodgames.futtoboru.data.recruitment.OriginClub;
import com.rndmodgames.futtoboru.data.recruitment.RegistrationRule;
import com.rndmodgames.futtoboru.data.recruitment.WageRecord;

/**
 * R-02: the recruitment data of the packs ({@code recruitment/rules.json}, {@code wages.json}, {@code origins.json},
 * {@code origin_clubs.json}) as read-only objects, with the lookups later slices need (rules in force on a day, the
 * wage band of a standing). Built once per {@link GameData}; nothing in play uses it yet (R-03, R-07).
 */
public final class RecruitmentData {

    private final List<RegistrationRule> rules = new ArrayList<>();
    private final List<WageRecord> wages = new ArrayList<>();
    private final Map<String, Origin> origins = new LinkedHashMap<>();
    private final Map<String, OriginClub> originClubs = new LinkedHashMap<>();

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
