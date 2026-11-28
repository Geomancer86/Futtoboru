package com.rndmodgames.futtoboru.tables.schedule;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.components.AutoFocusScrollPane;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * The Schedule screen (U-08): the club's calendar on the left, the form to arrange a friendly on the right.
 */
public class ScheduleScreenTable extends VisTable {

    private Club currentClub;

    final FixturesTable fixturesTable;
    final ArrangeFriendlyTable arrangeFriendlyTable;

    public ScheduleScreenTable(Game parent) {
        super(true);
        Futtoboru game = (Futtoboru) parent;

        fixturesTable = new FixturesTable(game, this);
        AutoFocusScrollPane fixturesScrollPane = new AutoFocusScrollPane(fixturesTable);
        arrangeFriendlyTable = new ArrangeFriendlyTable(game, this);

        this.row();
        this.add(fixturesScrollPane).fill().expand();
        this.add(arrangeFriendlyTable).top();
    }

    public void updateDynamicComponents() {
        fixturesTable.setCurrentClub(currentClub);
        arrangeFriendlyTable.setCurrentClub(currentClub);
        arrangeFriendlyTable.setSelectedDate(fixturesTable.getSelectedDate());
        fixturesTable.updateDynamicComponents();
        arrangeFriendlyTable.updateDynamicComponents();
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}
