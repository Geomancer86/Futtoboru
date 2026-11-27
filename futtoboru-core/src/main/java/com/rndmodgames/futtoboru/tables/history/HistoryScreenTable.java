package com.rndmodgames.futtoboru.tables.history;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.history.HistoryEntry;
import com.rndmodgames.futtoboru.history.HistoryProvider;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * History screen (U-07): champions and cup winners, newest first, and below it the final table (league) or the
 * results (cup) of the selected entry. Seasons before the game's start come from the real results (E-00b, D17
 * reference data); played seasons from the game's own final tables. Data comes from {@link HistoryProvider}.
 */
public class HistoryScreenTable extends VisTable {

    private final Futtoboru game;
    private final VisTable content = new VisTable(true);
    private List<HistoryEntry> entries;
    private HistoryEntry selected;

    public HistoryScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
        VisScrollPane scroll = new VisScrollPane(content);
        scroll.setFadeScrollBars(false);
        add(scroll).grow().fill();
    }

    /** Reads the history again (a season may have finished) and shows it. */
    public void updateDynamicComponents() {
        entries = HistoryProvider.forGame(game.getCurrentGame(), DatabaseLoader.getGameData());
        if (selected == null || !entries.contains(selected)) {
            selected = firstWithDetail();
        }
        Log.info(Log.UI_COMPETITIONS, "HistoryScreenTable", entries.size() + " history entries");
        render();
    }

    private HistoryEntry firstWithDetail() {
        for (HistoryEntry e : entries) {
            if (!e.table.isEmpty() || !e.ties.isEmpty()) {
                return e;
            }
        }
        return null;
    }

    private static String t(String key) {
        return LanguageModLoader.getValue(key);
    }

    private void render() {
        content.clear();
        content.top().left();

        VisLabel title = new VisLabel(t("history_title"));
        title.setFontScale(UiTheme.fontScale("heading"));
        content.add(title).left().pad(UiTheme.space("sm")).row();
        content.addSeparator().pad(UiTheme.space("xs")).row();

        if (entries.isEmpty()) {
            content.add(new VisLabel(t("history_none"))).left().pad(UiTheme.space("sm")).row();
            return;
        }

        heading(t("history_honours"));
        VisTable honours = new VisTable(true);
        honours.add(new VisLabel(t("history_season"))).width(UiTheme.size("col.mid")).left();
        honours.add(new VisLabel(t("history_competition"))).width(UiTheme.size("col.wide")).left();
        honours.add(new VisLabel(t("history_winner"))).width(UiTheme.size("col.club")).left();
        honours.add(new VisLabel(t("history_runner_up"))).width(UiTheme.size("col.club")).left();
        honours.add(new VisLabel(t("history_source"))).width(UiTheme.size("col.short")).left();
        honours.add().row();
        for (HistoryEntry e : HistoryProvider.winners(entries)) {
            honours.add(new VisLabel(e.season)).left();
            honours.add(new VisLabel(e.competition)).left();
            VisLabel winner = new VisLabel(e.winner);
            if (e == selected) {
                winner.setColor(UiTheme.color("state.winner"));
            }
            honours.add(winner).left();
            honours.add(new VisLabel(e.runnerUp != null ? e.runnerUp : "-")).left();
            honours.add(new VisLabel(sourceLabel(e))).left();
            if (!e.table.isEmpty() || !e.ties.isEmpty()) {
                VisTextButton view = new VisTextButton(t("history_view"));
                view.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selected = e;
                        render();
                    }
                });
                honours.add(view).left();
            } else {
                honours.add();
            }
            honours.row();
        }
        content.add(honours).left().pad(UiTheme.space("xs")).row();

        if (selected != null) {
            content.addSeparator().pad(UiTheme.space("xs")).row();
            heading(selected.season + " " + selected.competition + " - "
                    + t(selected.cup ? "history_results" : "history_final_table") + " (" + sourceLabel(selected) + ")");
            content.add(selected.cup ? ties(selected) : table(selected)).left().pad(UiTheme.space("xs")).row();
        }
    }

    private void heading(String text) {
        VisLabel label = new VisLabel(text);
        label.setColor(UiTheme.color("text.heading"));
        content.add(label).left().padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs")).row();
    }

    private static String sourceLabel(HistoryEntry e) {
        return t(e.source == HistoryEntry.Source.REFERENCE ? "history_source_reference" : "history_source_played");
    }

    private static VisTable table(HistoryEntry e) {
        VisTable tbl = new VisTable(true);
        tbl.add(UiTooltips.header("history_pos", "tip_pos")).width(UiTheme.size("table.league.pos"));
        tbl.add(new VisLabel(t("history_club"))).width(UiTheme.size("table.league.club")).left();
        String[][] stats = { { "history_p", "tip_played" }, { "history_w", "tip_won" }, { "history_d", "tip_drawn" },
                { "history_l", "tip_lost" }, { "history_f", "tip_goals_for" }, { "history_a", "tip_goals_against" } };
        for (String[] k : stats) {
            tbl.add(UiTooltips.header(k[0], k[1])).width(UiTheme.size("table.league.stat"));
        }
        tbl.add(UiTooltips.header("history_pts", "tip_points")).width(UiTheme.size("table.league.pts"));
        tbl.row();
        for (HistoryEntry.TableRow r : e.table) {
            tbl.add(new VisLabel(String.valueOf(r.position)));
            VisLabel club = new VisLabel(r.club);
            if (r.position == 1) {
                club.setColor(UiTheme.color("state.winner"));
            }
            tbl.add(club).left();
            for (int v : new int[] { r.played, r.won, r.drawn, r.lost, r.goalsFor, r.goalsAgainst, r.points }) {
                tbl.add(new VisLabel(String.valueOf(v)));
            }
            tbl.row();
        }
        return tbl;
    }

    private static VisTable ties(HistoryEntry e) {
        VisTable tbl = new VisTable(true);
        tbl.add(new VisLabel(t("history_round"))).width(UiTheme.size("col.short")).left();
        tbl.add(new VisLabel(t("history_date"))).width(UiTheme.size("col.mid")).left();
        tbl.add(new VisLabel(t("history_home"))).width(UiTheme.size("col.club")).left();
        tbl.add(new VisLabel(t("history_score"))).width(UiTheme.size("col.score"));
        tbl.add(new VisLabel(t("history_away"))).width(UiTheme.size("col.club")).left();
        tbl.add(new VisLabel(t("history_venue"))).width(UiTheme.size("col.wide")).left();
        tbl.row();
        for (HistoryEntry.Tie tie : e.ties) {
            tbl.add(new VisLabel(tie.round)).left();
            tbl.add(new VisLabel(tie.date != null ? tie.date : "-")).left();
            tbl.add(new VisLabel(tie.home)).left();
            String score = tie.walkover != null ? t("history_walkover")
                    : tie.homeGoals + " - " + tie.awayGoals;
            tbl.add(new VisLabel(score));
            tbl.add(new VisLabel(tie.away)).left();
            tbl.add(new VisLabel(tie.venue != null ? tie.venue : "")).left();
            tbl.row();
        }
        if (e.winner != null) {
            VisLabel w = new VisLabel(t("history_winner") + ": " + e.winner);
            w.setColor(UiTheme.color("state.winner"));
            tbl.add(w).colspan(6).left().padTop(UiTheme.space("xs")).row();
        }
        return tbl;
    }
}
