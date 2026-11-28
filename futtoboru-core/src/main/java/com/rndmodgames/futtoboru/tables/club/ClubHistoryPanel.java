package com.rndmodgames.futtoboru.tables.club;

import java.util.List;

import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.history.HistoryFacts;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.HistoryName;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-10: the "History" panel of the club screens: hook line, history paragraphs, season narratives, nicknames, rivals
 * (links to their club screens) and name history, in a scroll pane. The facts come from {@link HistoryFacts}.
 */
public final class ClubHistoryPanel {

    private ClubHistoryPanel() {
    }

    /** @return the scrollable panel, or null when the club has no history data */
    public static VisTable build(Club club, SaveGame game, MainMenuManager menu) {
        if (club == null || club.getDataId() == null) {
            return null;
        }
        HistoryFacts facts = new HistoryFacts(DatabaseLoader.getGameData(),
                HistoryFacts.dataLanguage(LanguageModLoader.getCurrentLanguage().getId()));
        HistoryFacts.ClubHistory h = facts.club(club.getDataId());
        if (h.isEmpty()) {
            return null;
        }
        float wrap = UiTheme.size("text.wrap.narrow");
        VisTable body = new VisTable(true);
        if (h.hook != null && !h.hook.isEmpty()) {
            body.add(wrapped(h.hook, "text.primary")).left().width(wrap).row();
        }
        for (String paragraph : h.history) {
            body.add(wrapped(paragraph, "text.secondary")).left().width(wrap).row();
        }
        for (java.util.Map.Entry<String, String> season : h.seasons.entrySet()) {
            body.add(heading(LanguageModLoader.format("history_seasons", season.getKey()))).left().row();
            body.add(wrapped(season.getValue(), "text.secondary")).left().width(wrap).row();
        }
        if (!h.nicknames.isEmpty()) {
            body.add(heading(LanguageModLoader.getValue("history_nicknames"))).left().row();
            for (HistoryName n : h.nicknames) {
                String years = HistoryFacts.years(n);
                String note = years.isEmpty() ? (n.isPeriod() ? "" : LanguageModLoader.getValue("history_nickname_later"))
                        : years + (n.isPeriod() ? "" : ", " + LanguageModLoader.getValue("history_nickname_later"));
                body.add(wrapped(note.isEmpty() ? n.getName()
                        : LanguageModLoader.format("history_nickname_line", n.getName(), note), "text.primary"))
                        .left().width(wrap).row();
            }
        }
        if (!h.rivals.isEmpty()) {
            body.add(heading(LanguageModLoader.getValue("history_rivals"))).left().row();
            VisTable rivals = new VisTable(true);
            for (String rivalId : h.rivals) {
                rivals.add(rivalLink(facts, rivalId, game, menu)).left().row();
            }
            body.add(rivals).left().row();
        }
        if (!h.nameHistory.isEmpty()) {
            body.add(heading(LanguageModLoader.getValue("history_name_history"))).left().row();
            for (HistoryName n : h.nameHistory) {
                String years = HistoryFacts.years(n);
                body.add(wrapped(years.isEmpty() ? n.getName()
                        : LanguageModLoader.format("history_name_line", n.getName(), years), "text.primary"))
                        .left().width(wrap).row();
            }
        }

        VisScrollPane scroll = new VisScrollPane(body);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        VisTable panel = new VisTable(true);
        panel.add(heading(LanguageModLoader.getValue("history_header"))).left().row();
        panel.add(scroll).left().width(wrap + 2 * UiTheme.space("lg")).height(UiTheme.size("history.panel.h"));
        return panel;
    }

    private static VisLabel heading(String text) {
        VisLabel label = new VisLabel(text);
        label.setFontScale(UiTheme.fontScale("subheading"));
        label.setColor(UiTheme.color("text.heading"));
        return label;
    }

    private static VisLabel wrapped(String text, String colorToken) {
        VisLabel label = new VisLabel(text);
        label.setWrap(true);
        label.setColor(UiTheme.color(colorToken));
        return label;
    }

    /** A rival: a link to its club screen when the game has the club, else just the name. */
    private static com.badlogic.gdx.scenes.scene2d.Actor rivalLink(HistoryFacts facts, String rivalId, SaveGame game,
            final MainMenuManager menu) {
        final Club target = findClub(game, rivalId);
        String name = target != null ? target.getName() : facts.clubName(rivalId);
        if (target == null || menu == null) {
            return new VisLabel(name);
        }
        LinkLabel link = new LinkLabel(name);
        link.setListener(new LinkLabel.LinkLabelListener() {
            @Override
            public void clicked(String url) {
                Log.info(Log.UI_CLUB, "ClubHistoryPanel", "Rival link: " + target.getName());
                menu.setSelectedClubForDetail(target);
                menu.setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
            }
        });
        return link;
    }

    private static Club findClub(SaveGame game, String dataId) {
        List<Club> clubs = game == null ? null : game.getAllClubs();
        for (Club c : clubs == null ? java.util.Collections.<Club>emptyList() : clubs) {
            if (c != null && dataId.equalsIgnoreCase(c.getDataId())) {
                return c;
            }
        }
        return null;
    }
}
