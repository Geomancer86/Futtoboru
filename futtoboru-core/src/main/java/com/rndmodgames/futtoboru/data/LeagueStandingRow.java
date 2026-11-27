package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * One line of a finished league table, kept on the season's {@link CompetitionEdition} (P2-6)
 * so the table survives the reset of the club statistics at season rollover.
 */
public class LeagueStandingRow implements Serializable {

    private static final long serialVersionUID = 4113672215650127901L;

    private Integer position;
    private Long clubId;
    private Integer played;
    private Integer won;
    private Integer drawn;
    private Integer lost;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer points;

    public LeagueStandingRow() {
        // for the save serializer
    }

    /** Copy a club's current league statistics */
    public static LeagueStandingRow of(int position, Club club) {
        LeagueStandingRow row = new LeagueStandingRow();
        row.position = position;
        row.clubId = club.getId();
        row.played = club.getMatchesPlayed();
        row.won = club.getMatchesWon();
        row.drawn = club.getMatchesDrawn();
        row.lost = club.getMatchesLost();
        row.goalsFor = club.getGoalsScored();
        row.goalsAgainst = club.getGoalsConceded();
        row.points = club.getPoints();
        return row;
    }

    public Integer getPosition() {
        return position;
    }

    public Long getClubId() {
        return clubId;
    }

    public Integer getPlayed() {
        return played;
    }

    public Integer getWon() {
        return won;
    }

    public Integer getDrawn() {
        return drawn;
    }

    public Integer getLost() {
        return lost;
    }

    public Integer getGoalsFor() {
        return goalsFor;
    }

    public Integer getGoalsAgainst() {
        return goalsAgainst;
    }

    public Integer getPoints() {
        return points;
    }
}
