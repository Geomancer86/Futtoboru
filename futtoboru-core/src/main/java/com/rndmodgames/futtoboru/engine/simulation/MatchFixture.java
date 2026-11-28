package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDateTime;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;

/**
 * What a {@link MatchEngine} gets to decide a match (E-17a).
 *
 * @param matchKey      stable key of the match ({@code MatchFacts.key}); the match stream is forked from it
 * @param home          home club (its squad, ground)
 * @param away          away club
 * @param kickOff       date and time of the match, may be null
 * @param weather       ground and weather, seeded from date and ground ({@link MatchWeather})
 * @param neutral       played at a neutral ground (P2-14): no home advantage
 * @param homeSheet     home side's team sheet frozen at kick-off (G-01, {@code TeamSheets.kickOff}): the line-up,
 *                      shape, style and captain; the engines read who plays from here and nothing else
 * @param awaySheet     away side's frozen sheet
 * @param homeInjuries  men of the home side hurt in the match (G-02, rolled by {@link MatchSimulator} on their own
 *                      stream before the result), empty when none
 * @param awayInjuries  away side's
 * @param injuryRules   rules the injuries were rolled with
 */
public record MatchFixture(String matchKey, Club home, Club away, LocalDateTime kickOff, MatchWeather weather,
        boolean neutral, TeamSheet homeSheet, TeamSheet awaySheet, List<MatchInjury> homeInjuries,
        List<MatchInjury> awayInjuries, InjuryRules injuryRules) {

    public MatchFixture {
        if (matchKey == null || home == null || away == null || weather == null || homeSheet == null
                || awaySheet == null) {
            throw new IllegalArgumentException("matchKey, both clubs, the weather and both team sheets are required");
        }
        homeInjuries = homeInjuries == null ? List.of() : homeInjuries;
        awayInjuries = awayInjuries == null ? List.of() : awayInjuries;
    }

    /** A match nobody is hurt in. */
    public MatchFixture(String matchKey, Club home, Club away, LocalDateTime kickOff, MatchWeather weather,
            boolean neutral, TeamSheet homeSheet, TeamSheet awaySheet) {
        this(matchKey, home, away, kickOff, weather, neutral, homeSheet, awaySheet, List.of(), List.of(), null);
    }
}
