package com.rndmodgames.futtoboru.data.language;

import java.util.List;
import java.util.Map;

/**
 * R-11a: one row of {@code language_rates.json}: for an area, a list of countries, or everyone (the default row), and
 * a birth-year band, some of: share speaking each non-English language ({@code speaks}), share speaking only it
 * ({@code only}), share of men / women who signed the marriage register, share of the people born in another
 * country (the chance of a parent born there), and the split of those who sign into the four read/write levels.
 * A missing rate is null (the lookup falls back to the country row, then the default row).
 */
public final class LanguageRate {

    private final String area;
    private final List<String> countries;
    private final int bornFrom;
    private final int bornUntil;
    private final Map<String, Double> speaks;
    private final Map<String, Double> only;
    private final Double signsMen;
    private final Double signsWomen;
    private final Map<String, Double> parentsBorn;
    private final double[] readWriteIfSigns;
    private final String confidence;

    public LanguageRate(String area, List<String> countries, int bornFrom, int bornUntil, Map<String, Double> speaks,
            Map<String, Double> only, Double signsMen, Double signsWomen, Map<String, Double> parentsBorn,
            double[] readWriteIfSigns, String confidence) {
        this.area = area;
        this.countries = countries == null ? null : List.copyOf(countries);
        this.bornFrom = bornFrom;
        this.bornUntil = bornUntil;
        this.speaks = speaks;
        this.only = only;
        this.signsMen = signsMen;
        this.signsWomen = signsWomen;
        this.parentsBorn = parentsBorn;
        this.readWriteIfSigns = readWriteIfSigns;
        this.confidence = confidence;
    }

    public boolean covers(int birthYear) {
        return bornFrom <= birthYear && birthYear <= bornUntil;
    }

    public boolean isDefault() {
        return area == null && countries == null;
    }

    public String getArea() {
        return area;
    }

    public List<String> getCountries() {
        return countries;
    }

    public int getBornFrom() {
        return bornFrom;
    }

    public int getBornUntil() {
        return bornUntil;
    }

    /** @return language id -> share speaking it (English never listed), or null when this row has none */
    public Map<String, Double> getSpeaks() {
        return speaks;
    }

    public Map<String, Double> getOnly() {
        return only;
    }

    public Double getSignsMen() {
        return signsMen;
    }

    public Double getSignsWomen() {
        return signsWomen;
    }

    public Map<String, Double> getParentsBorn() {
        return parentsBorn;
    }

    /** @return shares of levels 1..4 among those who sign (a copy), or null */
    public double[] getReadWriteIfSigns() {
        return readWriteIfSigns == null ? null : readWriteIfSigns.clone();
    }

    public String getConfidence() {
        return confidence;
    }
}
