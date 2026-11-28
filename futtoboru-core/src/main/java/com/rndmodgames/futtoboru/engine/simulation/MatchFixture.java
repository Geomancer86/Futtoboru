package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Club;

/**
 * What a {@link MatchEngine} gets to decide a match (E-17a).
 *
 * @param matchKey stable key of the match ({@code MatchFacts.key}); the match stream is forked from it
 * @param home     home club (its squad, ground)
 * @param away     away club
 * @param kickOff  date and time of the match, may be null
 * @param weather  ground and weather, seeded from date and ground ({@link MatchWeather})
 * @param neutral  played at a neutral ground (P2-14): no home advantage
 */
public record MatchFixture(String matchKey, Club home, Club away, LocalDateTime kickOff, MatchWeather weather,
        boolean neutral) {

    public MatchFixture {
        if (matchKey == null || home == null || away == null || weather == null) {
            throw new IllegalArgumentException("matchKey, both clubs and the weather are required");
        }
    }

    /** A match at the home club's ground. */
    public MatchFixture(String matchKey, Club home, Club away, LocalDateTime kickOff, MatchWeather weather) {
        this(matchKey, home, away, kickOff, weather, false);
    }
}
