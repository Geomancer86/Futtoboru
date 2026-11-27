package com.rndmodgames.futtoboru.tables.main;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Home Screen - Main Game (U-04c)
 *
 * A dashboard of what needs attention: next fixture, unread messages, league position, balance and the last result
 * (a job seeker sees job applications instead of the club cards). Each card has a button to the screen with the
 * details. The data comes from {@link HomeDashboard}; this class only lays it out. Two columns of
 * {@code dashboard.card.w} fit the content area at the 1280x720 minimum (D18).
 *
 * @author Geomancer86
 */
public class HomeScreenTable extends VisTable {

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.ENGLISH);

    private final Game game;
    private MainMenuManager menuManager;

    /**
     *
     * @param parent
     */
    public HomeScreenTable(Game parent) {
        super(true);
        this.game = parent;
    }

    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    /** Reads the current game again and rebuilds the cards (called each time Home is shown). */
    public void updateDynamicComponents() {

        clearChildren();
        HomeDashboard dashboard = HomeDashboard.of(((Futtoboru) game).getCurrentGame());

        // header: manager, club, date
        VisLabel title = new VisLabel(dashboard.hasClub() ? dashboard.getClubName() : text("dashboard_unemployed"));
        title.setFontScale(UiTheme.fontScale("title"));
        add(title).left().colspan(2).padBottom(UiTheme.space("xxs"));
        row();
        StringBuilder subtitle = new StringBuilder();
        if (dashboard.getManagerName() != null) {
            subtitle.append(dashboard.getManagerName());
        }
        if (dashboard.getDate() != null) {
            subtitle.append(subtitle.length() > 0 ? "  -  " : "").append(LONG_DATE.format(dashboard.getDate()));
        }
        VisLabel subtitleLabel = new VisLabel(subtitle.toString());
        subtitleLabel.setColor(UiTheme.color("text.secondary"));
        add(subtitleLabel).left().colspan(2).padBottom(UiTheme.space("md"));
        row();

        // cards: two columns packed to the left, whatever the window width
        VisTable cards = new VisTable(true);
        if (dashboard.hasClub()) {
            addCard(cards, fixtureCard(dashboard));
            addCard(cards, leagueCard(dashboard));
            cards.row();
            addCard(cards, messagesCard(dashboard));
            addCard(cards, balanceCard(dashboard));
            cards.row();
            addCard(cards, lastResultCard(dashboard));
        } else {
            addCard(cards, messagesCard(dashboard));
            addCard(cards, jobsCard(dashboard));
        }
        add(cards).left().top().colspan(2);
        row();
        add().colspan(2).grow();
        top().left();
    }

    private static void addCard(VisTable cards, VisTable card) {
        cards.add(card).width(UiTheme.size("dashboard.card.w")).top().left().pad(UiTheme.space("xs"));
    }

    // ------------------------------------------------------------------ cards

    private VisTable fixtureCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_next_fixture");
        HomeDashboard.Fixture fixture = dashboard.getNextFixture();
        if (fixture == null) {
            line(card, text("dashboard_no_fixture"), "text.muted");
        } else {
            String opponent = fixture.opponent() != null ? fixture.opponent() : text("dashboard_opponent_tbd");
            line(card, format("dashboard_fixture_line", opponent, text(fixture.home() ? "dashboard_home" : "dashboard_away")),
                    "text.primary");
            String competition = fixture.competition() != null ? fixture.competition() : matchType(fixture.matchType());
            line(card, competition + ", " + SHORT_DATE.format(fixture.date()), "text.secondary");
            String when = fixture.daysUntil() <= 0 ? text("dashboard_today")
                    : fixture.daysUntil() == 1 ? text("dashboard_tomorrow")
                    : format("dashboard_in_days", fixture.daysUntil());
            line(card, when, fixture.daysUntil() <= 1 ? "state.warning" : "text.secondary");
        }
        link(card, "schedule", MainMenuManager.SCHEDULE_SCREEN);
        return card;
    }

    private VisTable leagueCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_league_position");
        HomeDashboard.LeaguePosition league = dashboard.getLeaguePosition();
        if (league == null) {
            line(card, text("dashboard_no_league"), "text.muted");
        } else {
            line(card, league.league(), "text.primary");
            line(card, format("dashboard_position", league.position(), league.clubs()),
                    league.position() == 1 ? "state.winner" : "text.primary");
            line(card, format("dashboard_played_points", league.played(), league.points()), "text.secondary");
        }
        link(card, "competitions", MainMenuManager.COMPETITIONS_SCREEN);
        return card;
    }

    private VisTable messagesCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_messages");
        line(card, format("dashboard_unread", dashboard.getUnreadMessages()),
                dashboard.getUnreadMessages() > 0 ? "text.primary" : "text.muted");
        if (dashboard.getUnreadNeedingAction() > 0) {
            line(card, format("dashboard_action_needed", dashboard.getUnreadNeedingAction()), "state.warning");
        }
        link(card, "inbox", MainMenuManager.INBOX_SCREEN);
        return card;
    }

    private VisTable balanceCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_balance");
        BigDecimal balance = dashboard.getBalance();
        line(card, Money.format(balance), balance != null && balance.signum() < 0 ? "state.negative" : "text.primary");
        link(card, "finances", MainMenuManager.FINANCES_SCREEN);
        return card;
    }

    private VisTable lastResultCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_last_result");
        HomeDashboard.Result result = dashboard.getLastResult();
        if (result == null) {
            line(card, text("dashboard_no_result"), "text.muted");
        } else {
            String outcome;
            String color;
            if (result.goalsFor() > result.goalsAgainst()) {
                outcome = text("dashboard_won");
                color = "state.positive";
            } else if (result.goalsFor() < result.goalsAgainst()) {
                outcome = text("dashboard_lost");
                color = "state.negative";
            } else {
                outcome = text("dashboard_drawn");
                color = "text.primary";
            }
            String opponent = result.opponent() != null ? result.opponent() : "?";
            line(card, format("dashboard_result_line", outcome, result.goalsFor(), result.goalsAgainst(), opponent), color);
            line(card, text(result.home() ? "dashboard_home" : "dashboard_away") + ", " + SHORT_DATE.format(result.date()),
                    "text.secondary");
        }
        link(card, "match_history", MainMenuManager.MATCH_HISTORY_SCREEN);
        return card;
    }

    private VisTable jobsCard(HomeDashboard dashboard) {
        VisTable card = card("dashboard_job_search");
        line(card, format("dashboard_applications", dashboard.getPendingApplications()), "text.primary");
        line(card, format("dashboard_offers", dashboard.getPendingOffers()),
                dashboard.getPendingOffers() > 0 ? "state.positive" : "text.secondary");
        link(card, "my_applications", MainMenuManager.MY_APPLICATIONS_SCREEN);
        return card;
    }

    // ------------------------------------------------------------------ helpers

    private static VisTable card(String titleKey) {
        VisTable card = new VisTable(true);
        VisLabel title = new VisLabel(text(titleKey));
        title.setFontScale(UiTheme.fontScale("heading"));
        title.setColor(UiTheme.color("text.heading"));
        card.add(title).left().growX();
        card.row();
        card.addSeparator();
        return card;
    }

    private static void line(VisTable card, String text, String colorToken) {
        VisLabel label = new VisLabel(text);
        label.setColor(UiTheme.color(colorToken));
        label.setWrap(true);
        label.setAlignment(Align.left);
        card.add(label).left().growX();
        card.row();
    }

    /** A button to the screen with the details; it opens that menu entry (so its button lights up). */
    private void link(VisTable card, String labelKey, final int screen) {
        VisTextButton button = new VisTextButton(text(labelKey));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (menuManager != null) {
                    menuManager.openMenuEntry(screen);
                }
            }
        });
        card.add(button).left().padTop(UiTheme.space("xs"));
        card.row();
    }

    private static String matchType(int type) {
        switch (type) {
        case Match.LEAGUE_MATCH:
            return text("match_type_league");
        case Match.CUP_MATCH:
            return text("match_type_cup");
        default:
            return text("match_type_friendly");
        }
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }

    /** The label with {0}, {1}... replaced by {@code args} (the bundles hold plain patterns). */
    static String format(String key, Object... args) {
        return fill(text(key), args);
    }

    static String fill(String pattern, Object... args) {
        String result = pattern;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }
}
