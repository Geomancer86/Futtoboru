package com.rndmodgames.futtoboru.data.recruitment;

import java.util.List;

/**
 * R-02: a period junior, works, church or Scottish club a candidate can come from ({@code recruitment/origin_clubs.json},
 * sourced, with an {@code ids.json} number). Read-only data; R-03 draws candidates' origin clubs from it.
 */
public final class OriginClub {

    private final String dataId;
    private final long runtimeId;
    private final String name;
    private final String kind;
    private final String place;
    private final String countryDataId;
    private final String areaDataId;
    private final Integer founded;
    private final String clubDataId;
    private final List<String> origins;

    public OriginClub(String dataId, long runtimeId, String name, String kind, String place, String countryDataId,
            String areaDataId, Integer founded, String clubDataId, List<String> origins) {
        this.dataId = dataId;
        this.runtimeId = runtimeId;
        this.name = name;
        this.kind = kind;
        this.place = place;
        this.countryDataId = countryDataId;
        this.areaDataId = areaDataId;
        this.founded = founded;
        this.clubDataId = clubDataId;
        this.origins = List.copyOf(origins);
    }

    public String getDataId() {
        return dataId;
    }

    public long getRuntimeId() {
        return runtimeId;
    }

    public String getName() {
        return name;
    }

    /** @return {@code church}, {@code sunday-school}, {@code works}, {@code junior}, {@code school} or {@code scottish} */
    public String getKind() {
        return kind;
    }

    public String getPlace() {
        return place;
    }

    public String getCountryDataId() {
        return countryDataId;
    }

    /** @return the language/rate area ({@code areas.json}), or null */
    public String getAreaDataId() {
        return areaDataId;
    }

    public Integer getFounded() {
        return founded;
    }

    /** @return the data club it is (or became), or null */
    public String getClubDataId() {
        return clubDataId;
    }

    /** @return origin backstory ids typical of its men */
    public List<String> getOrigins() {
        return origins;
    }
}
