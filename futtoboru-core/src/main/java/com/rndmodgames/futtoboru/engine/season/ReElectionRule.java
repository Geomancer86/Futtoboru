package com.rndmodgames.futtoboru.engine.season;

import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-08: a League's re-election rule, read from its competition record ({@code competitions.json}):
 * after each season that starts in {@code reElectionFrom..reElectionUntil}, the bottom {@code reElectionPlaces}
 * clubs retire and seek re-election at the annual meeting, {@code annualMeetingAfterDays} after the season is
 * complete. Sourced for 1888-89 (the bottom four, all re-elected); the meeting date is a placeholder (D30).
 */
public final class ReElectionRule {

    private final int places;
    private final int fromYear;
    private final int untilYear;
    private final int meetingAfterDays;

    public ReElectionRule(int places, int fromYear, int untilYear, int meetingAfterDays) {
        this.places = places;
        this.fromYear = fromYear;
        this.untilYear = untilYear;
        this.meetingAfterDays = meetingAfterDays;
    }

    /** @return the rule of a competition record, or null when it has none */
    public static ReElectionRule of(DataRecord competition) {
        if (competition == null || competition.integer("reElectionPlaces") == null) {
            return null;
        }
        int places = competition.integer("reElectionPlaces");
        Integer from = competition.integer("reElectionFrom");
        Integer until = competition.integer("reElectionUntil");
        Integer after = competition.integer("annualMeetingAfterDays");
        return new ReElectionRule(places, from != null ? from : Integer.MIN_VALUE,
                until != null ? until : Integer.MAX_VALUE, after != null ? after : 7);
    }

    /** @return the rule of the competition with this data id in the game data, or null */
    public static ReElectionRule of(GameData data, String competitionDataId) {
        if (data == null || competitionDataId == null) {
            return null;
        }
        return of(data.get(GameData.COMPETITION, competitionDataId));
    }

    /** @return true when the season starting in this year ends with a re-election */
    public boolean appliesTo(int seasonStartYear) {
        return places > 0 && seasonStartYear >= fromYear && seasonStartYear <= untilYear;
    }

    public int getPlaces() {
        return places;
    }

    public int getMeetingAfterDays() {
        return meetingAfterDays;
    }
}
