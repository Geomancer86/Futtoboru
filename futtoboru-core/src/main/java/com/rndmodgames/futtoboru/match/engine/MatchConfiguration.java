package com.rndmodgames.futtoboru.match.engine;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.Stadium;
import java.time.LocalDateTime;

/**
 * MatchConfiguration - Configuration data for match engine
 * Passed from MatchEngineDebugScreen to MatchEngineScreen
 */
public class MatchConfiguration {
    public Club homeTeam;
    public Club awayTeam;
    public Stadium stadium;
    public Season season;
    public LocalDateTime matchDate;
    public String matchType;
    public String timeOfDay;
    public String weather;
    public float temperature;
    public String pitchCondition;
    public int attendance;
    public String crowdAtmosphere;
    
    // Advanced options
    public float homeTeamCondition = 100f;
    public float awayTeamCondition = 100f;
    public String homeAdvantage;
    public String homeFormBonus;
    public String awayFormBonus;
}
