package com.rndmodgames.futtoboru.tables.club;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubCommittee;
import com.rndmodgames.futtoboru.data.CommitteeMember;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.committee.Committee;
import com.rndmodgames.futtoboru.engine.committee.CommitteeManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-05: the Committee screen of the player's club (club menu): the season's expectations, the confidence in the
 * secretary in words, warnings given, and the members (name, role, occupation, year of birth). No numbers for the
 * confidence: the committee speaks in words.
 */
public class CommitteeScreenTable extends VisTable {

    private final Futtoboru game;

    public CommitteeScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
    }

    public void updateDynamicComponents() {
        clear();
        SaveGame currentGame = game.getSession().getGame();
        Club club = currentGame != null ? currentGame.getCurrentClub() : null;
        if (club == null) {
            add(new VisLabel(LanguageModLoader.getValue("club_none_selected"))).pad(UiTheme.space("lg"));
            return;
        }
        ClubCommittee c = club.getCommittee();

        VisTable body = new VisTable(true);
        VisLabel header = new VisLabel(LanguageModLoader.format("committee_title", club.getName()));
        header.setFontScale(UiTheme.fontScale("heading"));
        header.setColor(UiTheme.color("text.heading"));
        body.add(header).colspan(4).left().pad(UiTheme.space("sm"));
        body.row();
        body.addSeparator().colspan(4).pad(UiTheme.space("xs"));
        body.row();

        if (c == null || c.getSeasonYear() == null) {
            body.add(new VisLabel(LanguageModLoader.getValue("committee_no_season"))).colspan(4).left();
            add(body).expand().top().left().pad(UiTheme.space("md"));
            return;
        }

        // secretary, confidence, warnings
        Person secretary = c.getSecretaryId() != null ? personById(currentGame, c.getSecretaryId()) : null;
        row(body, LanguageModLoader.getValue("committee_secretary"), secretary != null
                ? (secretary.getName() + " " + secretary.getLastname()).trim()
                : LanguageModLoader.getValue("committee_secretary_none"), null);
        row(body, LanguageModLoader.getValue("committee_confidence"),
                LanguageModLoader.getValue(Committee.confidenceKey(c.getConfidence())), confidenceColour(c.getConfidence()));
        String warnings = c.getWarnings() >= 2 ? "committee_warnings_final"
                : c.getWarnings() == 1 ? "committee_warnings_one" : "committee_warnings_none";
        row(body, LanguageModLoader.getValue("committee_warnings"), LanguageModLoader.getValue(warnings),
                c.getWarnings() > 0 ? "state.warning" : null);

        // expectations
        section(body, LanguageModLoader.format("committee_expectations", Season.label(c.getSeasonYear())));
        for (String line : CommitteeManager.expectationLines(currentGame, c, club)) {
            VisLabel label = new VisLabel("- " + line);
            label.setWrap(true);
            body.add(label).colspan(4).left().width(UiTheme.size("text.wrap"));
            body.row();
        }

        // members
        section(body, LanguageModLoader.getValue("committee_members"));
        for (String key : new String[] { "committee_col_name", "committee_col_role", "committee_col_occupation",
                "committee_col_born" }) {
            VisLabel col = new VisLabel(LanguageModLoader.getValue(key));
            col.setColor(UiTheme.color("text.secondary"));
            body.add(col).left().minWidth(UiTheme.size("col.label"));
        }
        body.row();
        for (CommitteeMember m : c.getMembers()) {
            body.add(new VisLabel(m.getFullName())).left();
            body.add(new VisLabel(LanguageModLoader.getValue(Committee.roleKey(m.getRole())))).left();
            body.add(new VisLabel(LanguageModLoader.getValue(Committee.occupationKey(m.getOccupation())))).left();
            body.add(new VisLabel(m.getBirthYear() != null ? String.valueOf(m.getBirthYear()) : "")).left();
            body.row();
        }
        add(body).expand().top().left().pad(UiTheme.space("md"));
    }

    private static void row(VisTable body, String label, String value, String colourToken) {
        body.add(new VisLabel(label)).left().minWidth(UiTheme.size("col.wide"));
        VisLabel v = new VisLabel(value);
        if (colourToken != null) {
            v.setColor(UiTheme.color(colourToken));
        }
        body.add(v).colspan(3).left().expandX();
        body.row();
    }

    private static void section(VisTable body, String title) {
        body.addSeparator().colspan(4).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
        body.row();
        VisLabel header = new VisLabel(title);
        header.setFontScale(UiTheme.fontScale("subheading"));
        body.add(header).colspan(4).left();
        body.row();
    }

    private static String confidenceColour(int confidence) {
        return confidence >= 65 ? "state.positive" : confidence >= 35 ? null : confidence >= 20 ? "state.warning"
                : "state.negative";
    }

    private static Person personById(SaveGame game, Long id) {
        for (Person p : game.getAllPersons()) {
            if (p != null && id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }
}
