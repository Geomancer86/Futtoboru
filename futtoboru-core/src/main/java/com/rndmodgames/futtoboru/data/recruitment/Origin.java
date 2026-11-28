package com.rndmodgames.futtoboru.data.recruitment;

import java.util.List;
import java.util.Map;

/**
 * R-02 (D28 Q12): an origin backstory of {@code recruitment/origins.json} (mill, pit, docks, schoolmaster, amateur...):
 * the occupations it covers, the candidate source kinds it occurs in, small attribute nudges (-2..2, gameplay tuning)
 * and a literacy shift (-1..1, read by the language generator). Read-only data; R-03 applies the nudges.
 */
public final class Origin {

    private final String dataId;
    private final long runtimeId;
    private final String name;
    private final List<String> occupations;
    private final List<String> sourceKinds;
    private final Map<String, Integer> nudges;
    private final int literacyShift;
    private final String confidence;

    public Origin(String dataId, long runtimeId, String name, List<String> occupations, List<String> sourceKinds,
            Map<String, Integer> nudges, int literacyShift, String confidence) {
        this.dataId = dataId;
        this.runtimeId = runtimeId;
        this.name = name;
        this.occupations = List.copyOf(occupations);
        this.sourceKinds = List.copyOf(sourceKinds);
        this.nudges = Map.copyOf(nudges);
        this.literacyShift = literacyShift;
        this.confidence = confidence;
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

    /** @return occupation data ids */
    public List<String> getOccupations() {
        return occupations;
    }

    /** @return the candidate source kinds (design §3.1) this backstory occurs in */
    public List<String> getSourceKinds() {
        return sourceKinds;
    }

    /** @return attribute name -> nudge */
    public Map<String, Integer> getNudges() {
        return nudges;
    }

    public int getLiteracyShift() {
        return literacyShift;
    }

    public String getConfidence() {
        return confidence;
    }
}
