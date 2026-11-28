package com.rndmodgames.futtoboru.tables.match.result;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.report.MatchReport;
import com.rndmodgames.futtoboru.engine.simulation.GoalMinutes;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match Result Screen Table v1
 *
 *  - Shown right after the player's match is played (the top bar), or for any played match picked in Match History,
 *    a cup's schedule or a league's latest results (G-03: AI matches too)
 *
 *  - Displays match details: teams, score, date, attendance, venue; the goals with their minutes in order with the
 *    half-time line, and the match report (headline and paragraph, {@link MatchReport})
 *
 * @author Geomancer86
 */
public class MatchResultScreenTable extends VisTable {

    //
    Futtoboru game;

    /** The match to show; null = the current club's latest played match (the one just played). */
    private Match selectedMatch;

    //
    public MatchResultScreenTable(Game parent) {

        //
        super(true);

        this.game = (Futtoboru) parent;
    }

    /** G-03: show this played match (null: the current club's latest). */
    public void setSelectedMatch(Match match) {
        this.selectedMatch = match;
    }

    public Match getSelectedMatch() {
        return selectedMatch;
    }

    /** The match this screen shows now. */
    Match matchToShow() {
        if (selectedMatch != null) {
            return selectedMatch;
        }
        Club currentClub = game.getCurrentGame().getCurrentClub();
        if (currentClub == null || currentClub.getPlayedMatches() == null || currentClub.getPlayedMatches().isEmpty()) {
            return null;
        }
        // the most recently played match (last in chronological order)
        return currentClub.getPlayedMatches().get(currentClub.getPlayedMatches().size() - 1);
    }

    //
    public void updateDynamicComponents() {

        //
        this.clear();
        this.top();

        Match match = matchToShow();
        if (match == null) {
            this.add(LanguageModLoader.getValue("match_no_results"));
            return;
        }

        // Get clubs
        Club homeClub = game.getCurrentGame().getClubById(match.getHomeClubId());
        Club awayClub = game.getCurrentGame().getClubById(match.getAwayClubId());

        if (homeClub == null || awayClub == null) {
            this.add(LanguageModLoader.getValue("match_load_error"));
            Log.error(Log.UI_MATCH, "MatchResultScreenTable", "Home or away club not found for match");
            return;
        }

        MatchReport report = MatchReport.of(game.getCurrentGame(), match);

        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String matchDate = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : LanguageModLoader.getValue("unknown");
        String matchTime = match.getMatchDateTime() != null ? match.getMatchDateTime().format(timeFormatter) : LanguageModLoader.getValue("unknown");

        // Match Header
        this.row().padTop(UiTheme.space("lg"));
        VisLabel headerLabel = new VisLabel(LanguageModLoader.getValue("match_result_title"));
        headerLabel.setFontScale(UiTheme.fontScale("display"));
        this.add(headerLabel).padBottom(UiTheme.space("lg")).row();

        // Details: date, ground, type, attendance, capacity
        VisTable details = new VisTable(true);
        details.add(LanguageModLoader.getValue("match_date")).right().padRight(UiTheme.space("sm"));
        details.add(LanguageModLoader.format("match_date_time", matchDate, matchTime)).left().row();
        details.add(LanguageModLoader.getValue("match_venue")).right().padRight(UiTheme.space("sm"));
        String venueName = homeClub.getStadium() != null && homeClub.getStadium().getName() != null
            ? homeClub.getStadium().getName()
            : LanguageModLoader.getValue("match_unknown_ground");
        details.add(venueName).left().row();
        details.add(LanguageModLoader.getValue("match_type_label")).right().padRight(UiTheme.space("sm"));
        String matchType = match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH
            ? LanguageModLoader.getValue("match_type_friendly")
            : LanguageModLoader.getValue("match_type_competition");
        details.add(matchType).left().row();
        details.add(LanguageModLoader.getValue("match_attendance")).right().padRight(UiTheme.space("sm"));
        String attendance = match.getAttendance() != null ? String.valueOf(match.getAttendance()) : LanguageModLoader.getValue("unknown");
        details.add(attendance).left().row();
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            details.add(LanguageModLoader.getValue("match_capacity")).right().padRight(UiTheme.space("sm"));
            details.add(String.valueOf(homeClub.getStadium().getCapacity())).left().row();
        }
        this.add(details).row();

        // Separator
        this.addSeparator().padTop(UiTheme.space("md")).padBottom(UiTheme.space("md"));

        // Teams and Score
        int homeGoals = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
        int awayGoals = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
        VisTable scoreTable = new VisTable(true);
        scoreTable.add(new VisLabel(homeClub.getName())).width(UiTheme.size("col.club")).right();
        VisLabel scoreLabel = new VisLabel(homeGoals + " - " + awayGoals);
        scoreLabel.setFontScale(UiTheme.fontScale("heading"));
        scoreTable.add(scoreLabel).width(UiTheme.size("col.value")).center().padLeft(UiTheme.space("lg"))
            .padRight(UiTheme.space("lg"));
        VisLabel awayLabel = new VisLabel(awayClub.getName());
        scoreTable.add(awayLabel).width(UiTheme.size("col.club")).left();
        this.add(scoreTable).row();

        // Result
        String result;
        if (match.getHomeGoals() != null && match.getAwayGoals() != null) {
            if (match.getHomeGoals() > match.getAwayGoals()) {
                result = LanguageModLoader.format("match_won", homeClub.getName());
            } else if (match.getAwayGoals() > match.getHomeGoals()) {
                result = LanguageModLoader.format("match_won", awayClub.getName());
            } else {
                result = LanguageModLoader.getValue("match_draw");
            }
        } else {
            result = LanguageModLoader.getValue("match_no_result");
        }
        VisLabel resultLabel = new VisLabel(result);
        resultLabel.setColor(UiTheme.color("text.secondary"));
        this.add(resultLabel).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("md")).row();

        // Goals (G-03): both sides in minute order, the half-time line between the halves
        this.add(goalsTable(report)).row();

        // Report (G-03)
        this.addSeparator().padTop(UiTheme.space("md")).padBottom(UiTheme.space("md"));
        VisLabel reportHeading = new VisLabel(LanguageModLoader.getValue("match_report_heading"));
        reportHeading.setColor(UiTheme.color("text.heading"));
        this.add(reportHeading).row();
        VisLabel headline = new VisLabel(report.getHeadline());
        headline.setFontScale(UiTheme.fontScale("subheading"));
        this.add(headline).padTop(UiTheme.space("xs")).row();
        VisLabel paragraph = new VisLabel(report.getParagraph());
        paragraph.setWrap(true);
        paragraph.setAlignment(Align.left);
        this.add(paragraph).width(UiTheme.size("text.wrap")).padTop(UiTheme.space("sm"))
            .padBottom(UiTheme.space("lg")).row();
    }

    /** Home scorers right of the centre column (running score), away scorers left of it. */
    private VisTable goalsTable(MatchReport report) {
        VisTable table = new VisTable(true);
        VisLabel heading = new VisLabel(LanguageModLoader.getValue("match_goals_heading"));
        heading.setColor(UiTheme.color("text.heading"));
        table.add(heading).colspan(3).padBottom(UiTheme.space("xs")).row();

        if (report.getGoals().isEmpty()) {
            VisLabel none = new VisLabel(LanguageModLoader.getValue("match_no_goals"));
            none.setColor(UiTheme.color("text.muted"));
            table.add(none).colspan(3).row();
            return table;
        }

        boolean halfTimeShown = false;
        for (MatchReport.GoalLine goal : report.getGoals()) {
            if (report.isMinutesKnown() && !halfTimeShown && goal.minute() > GoalMinutes.HALF) {
                addHalfTime(table, report);
                halfTimeShown = true;
            }
            String scorer = goal.scorer() != null ? goal.scorer() : LanguageModLoader.getValue("match_scorer_unknown");
            String minute = report.isMinutesKnown() ? LanguageModLoader.format("match_goal_minute", goal.minute()) : "";
            String homeText = goal.home() ? (minute.isEmpty() ? scorer : scorer + "  " + minute) : "";
            String awayText = goal.home() ? "" : (minute.isEmpty() ? scorer : minute + "  " + scorer);
            String running = report.isMinutesKnown() ? goal.homeScore() + " - " + goal.awayScore() : "";
            table.add(new VisLabel(homeText)).width(UiTheme.size("col.club")).right();
            table.add(new VisLabel(running)).width(UiTheme.size("col.value")).center();
            table.add(new VisLabel(awayText)).width(UiTheme.size("col.club")).left().row();
        }
        if (report.isMinutesKnown() && !halfTimeShown) {
            addHalfTime(table, report);
        }
        if (!report.isMinutesKnown()) {
            VisLabel note = new VisLabel(LanguageModLoader.getValue("match_no_minutes"));
            note.setColor(UiTheme.color("text.muted"));
            table.add(note).colspan(3).padTop(UiTheme.space("xs")).row();
        }
        return table;
    }

    private static void addHalfTime(VisTable table, MatchReport report) {
        VisLabel halfTime = new VisLabel(LanguageModLoader.format("match_half_time", report.getHalfTimeHome(),
            report.getHalfTimeAway()));
        halfTime.setColor(UiTheme.color("text.muted"));
        table.add(halfTime).colspan(3).padTop(UiTheme.space("xxs")).padBottom(UiTheme.space("xxs")).row();
    }
}
