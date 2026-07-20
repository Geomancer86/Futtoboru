package com.rndmodgames.futtoboru.tables.main;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Home / Overview Screen Table v1
 *
 * FM/CM-style "front page" for the club you manage: at a glance, before digging into any other
 * screen, you see your league position and form, your next fixture, the latest inbox news, any
 * injured players, and a finances snapshot -- plus quick shortcuts to the deeper screens.
 *
 * Replaces the placeholder that showed a single "HOME screen placeholder" button since the
 * beginning of the project (see PO/human report 2026-07-19).
 *
 * @author Geomancer86
 */
public class HomeScreenTable extends VisTable {

    private final Futtoboru game;
    private MainMenuManager menuManager;

    private final VisScrollPane contentScrollPane;
    private final VisTable contentTable;

    private final LeagueStandingsManager standingsManager = new LeagueStandingsManager();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,###.00");

    private static final int RECENT_FORM_COUNT = 5;
    private static final int LATEST_NEWS_COUNT = 5;
    private static final int MAX_INJURIES_SHOWN = 8;

    public HomeScreenTable(Game parent) {
        super(true);

        this.game = (Futtoboru) parent;

        contentTable = new VisTable(true);
        contentScrollPane = new VisScrollPane(contentTable);
        contentScrollPane.setFadeScrollBars(false);
        contentScrollPane.setScrollingDisabled(false, false);

        this.add(contentScrollPane).grow().fill();
    }

    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    /**
     * Rebuilds the dashboard from the current save state. Cheap enough to call every time the
     * Home tab is opened (small, bounded amount of work: one league's standings, one club's
     * squad, and a handful of recent messages/fixtures -- no unbounded history scans).
     */
    public void updateDynamicComponents() {
        contentTable.clear();

        SaveGame currentGame = game.getCurrentGame();
        if (currentGame == null) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("home_no_save_loaded"))).pad(20).row();
            return;
        }

        Club club = currentGame.getCurrentClub();
        if (club == null) {
            addUnemployedState(currentGame);
            return;
        }

        LocalDateTime gameDate = currentGame.getGameDate();

        addHeader(currentGame, club, gameDate);
        contentTable.addSeparator().pad(4).row();

        // Two-column dashboard body: left = club/competition state, right = news/squad/finances.
        VisTable columns = new VisTable(true);

        VisTable leftColumn = new VisTable(true);
        addLeaguePosition(leftColumn, club);
        addRecentForm(leftColumn, club);
        addNextFixture(leftColumn, club, gameDate);

        VisTable rightColumn = new VisTable(true);
        addLatestNews(rightColumn, currentGame);
        addSquadStatus(rightColumn, club, gameDate);
        addFinances(rightColumn, club);

        columns.add(leftColumn).top().left().expandX().fillX().pad(4);
        columns.add(rightColumn).top().left().expandX().fillX().pad(4);
        contentTable.add(columns).growX().row();

        contentTable.addSeparator().pad(4).row();
        addQuickLinks(club);
    }

    // -----------------------------------------------------------------------------------------
    // Header
    // -----------------------------------------------------------------------------------------

    private void addHeader(SaveGame currentGame, Club club, LocalDateTime gameDate) {
        Person owner = currentGame.getOwner();

        VisLabel clubNameLabel = new VisLabel(club.getName() != null ? club.getName() : "");
        clubNameLabel.setFontScale(1.4f);
        contentTable.add(clubNameLabel).left().pad(4).row();

        StringBuilder subtitle = new StringBuilder();
        if (owner != null) {
            subtitle.append(LanguageModLoader.getValue("home_managed_by")).append(" ");
            subtitle.append(owner.getName() != null ? owner.getName() : "").append(" ");
            subtitle.append(owner.getLastname() != null ? owner.getLastname() : "");
        }
        League league = club.getCurrentLeague();
        if (league != null && league.getName() != null) {
            subtitle.append("  •  ").append(league.getName());
        }
        if (gameDate != null) {
            subtitle.append("  •  ").append(gameDate.format(dateFormatter));
        }
        contentTable.add(new VisLabel(subtitle.toString())).left().pad(4).row();
    }

    private void addUnemployedState(SaveGame currentGame) {
        VisLabel title = new VisLabel(LanguageModLoader.getValue("home_unemployed_title"));
        title.setFontScale(1.3f);
        contentTable.add(title).pad(10).row();

        Person owner = currentGame.getOwner();
        if (owner != null) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("home_unemployed_message")))
                    .left().pad(4).row();
        }

        VisTextButton findJobButton = new VisTextButton(LanguageModLoader.getValue("home_find_a_job"));
        findJobButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.JOB_BOARD_SCREEN);
                }
            }
        });
        contentTable.add(findJobButton).pad(10).row();
    }

    // -----------------------------------------------------------------------------------------
    // League position + form
    // -----------------------------------------------------------------------------------------

    private void addLeaguePosition(VisTable column, Club club) {
        column.add(sectionLabel("home_league_position")).left().row();
        column.addSeparator().pad(2).row();

        League league = club.getCurrentLeague();
        if (league == null || league.getLeagueClubs() == null || league.getLeagueClubs().isEmpty()) {
            column.add(new VisLabel(LanguageModLoader.getValue("home_no_league"))).left().pad(2).row();
            return;
        }

        List<Club> standings = standingsManager.calculateStandings(league);
        int found = HomeDashboardSupport.positionOf(club, standings);

        int totalClubs = league.getLeagueClubs().size();
        String positionText = found > 0
                ? HomeDashboardSupport.ordinal(found) + " " + LanguageModLoader.getValue("home_of") + " " + totalClubs
                : LanguageModLoader.getValue("home_unranked");
        VisLabel positionLabel = new VisLabel(positionText);
        positionLabel.setFontScale(1.15f);
        column.add(positionLabel).left().pad(2).row();

        String record = (club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0) + " "
                + LanguageModLoader.getValue("home_played") + "  •  "
                + (club.getMatchesWon() != null ? club.getMatchesWon() : 0) + "W "
                + (club.getMatchesDrawn() != null ? club.getMatchesDrawn() : 0) + "D "
                + (club.getMatchesLost() != null ? club.getMatchesLost() : 0) + "L  •  "
                + (club.getPoints() != null ? club.getPoints() : 0) + " " + LanguageModLoader.getValue("home_points");
        column.add(new VisLabel(record)).left().pad(2).row();
        column.add().height(8).row();
    }

    private void addRecentForm(VisTable column, Club club) {
        column.add(sectionLabel("home_recent_form")).left().row();
        column.addSeparator().pad(2).row();

        List<Match> recent = HomeDashboardSupport.collectRecentPlayedMatches(club, RECENT_FORM_COUNT);
        if (recent.isEmpty()) {
            column.add(new VisLabel(LanguageModLoader.getValue("home_no_recent_form"))).left().pad(2).row();
            column.add().height(8).row();
            return;
        }

        VisTable formRow = new VisTable(true);
        // oldest first, most recent on the right -- standard FM/CM convention
        for (Match match : recent) {
            char letter = HomeDashboardSupport.resultLetter(club, match);
            VisLabel resultLabel = new VisLabel(String.valueOf(letter));
            resultLabel.setColor(resultColor(letter));
            formRow.add(resultLabel).width(20).center();
        }
        column.add(formRow).left().pad(2).row();
        column.add().height(8).row();
    }

    // -----------------------------------------------------------------------------------------
    // Next fixture
    // -----------------------------------------------------------------------------------------

    private void addNextFixture(VisTable column, Club club, LocalDateTime gameDate) {
        column.add(sectionLabel("home_next_fixture")).left().row();
        column.addSeparator().pad(2).row();

        Match next = HomeDashboardSupport.findNextFixture(club, gameDate);
        if (next == null) {
            column.add(new VisLabel(LanguageModLoader.getValue("home_no_fixtures_scheduled"))).left().pad(2).row();
            column.add().height(8).row();
            return;
        }

        SaveGame currentGame = game.getCurrentGame();
        boolean isHome = club.getId() != null && club.getId().equals(next.getHomeClubId());
        Long opponentId = isHome ? next.getAwayClubId() : next.getHomeClubId();
        Club opponent = opponentId != null ? currentGame.getClubById(opponentId) : null;
        String opponentName = opponent != null && opponent.getName() != null
                ? opponent.getName() : LanguageModLoader.getValue("home_unknown_opponent");

        String venue = isHome ? LanguageModLoader.getValue("home_home_fixture")
                : LanguageModLoader.getValue("home_away_fixture");
        String matchLine = opponentName + " (" + venue + ")";
        VisLabel opponentLabel = new VisLabel(matchLine);
        opponentLabel.setFontScale(1.1f);
        column.add(opponentLabel).left().pad(2).row();

        String competitionLabel = competitionName(club, next);
        StringBuilder dateLine = new StringBuilder();
        if (next.getMatchDateTime() != null) {
            dateLine.append(next.getMatchDateTime().format(dateFormatter));
        }
        if (competitionLabel != null) {
            dateLine.append("  •  ").append(competitionLabel);
        }
        column.add(new VisLabel(dateLine.toString())).left().pad(2).row();

        boolean playableNow = gameDate != null && next.getMatchDateTime() != null
                && !next.getMatchDateTime().isAfter(gameDate) && !Boolean.TRUE.equals(next.getIsPlayed());
        VisTextButton actionButton = new VisTextButton(playableNow
                ? LanguageModLoader.getValue("home_play_match")
                : LanguageModLoader.getValue("home_view_schedule"));
        actionButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(playableNow
                            ? MainMenuManager.MATCH_PREVIEW_SCREEN
                            : MainMenuManager.SCHEDULE_SCREEN);
                }
            }
        });
        column.add(actionButton).left().pad(4).row();
        column.add().height(8).row();
    }

    // -----------------------------------------------------------------------------------------
    // Latest news
    // -----------------------------------------------------------------------------------------

    private void addLatestNews(VisTable column, SaveGame currentGame) {
        column.add(sectionLabel("home_latest_news")).left().row();
        column.addSeparator().pad(2).row();

        MessageManager messageManager = game.getGameEngine() != null ? game.getGameEngine().getMessageManager() : null;
        List<Message> messages = messageManager != null
                ? new ArrayList<>(messageManager.getAllActiveMessages())
                : new ArrayList<>();
        messages.sort(Comparator.comparing(Message::getMessageTime, Comparator.nullsLast(Comparator.reverseOrder())));

        if (messages.isEmpty()) {
            column.add(new VisLabel(LanguageModLoader.getValue("home_no_news"))).left().pad(2).row();
        } else {
            int shown = 0;
            for (Message message : messages) {
                if (shown >= LATEST_NEWS_COUNT) {
                    break;
                }
                column.add(newsRow(message)).left().growX().pad(2).row();
                shown++;
            }
        }

        int unread = messageManager != null ? messageManager.getUnreadCount() : 0;
        String inboxLabel = unread > 0
                ? LanguageModLoader.getValue("home_view_inbox") + " (" + unread + ")"
                : LanguageModLoader.getValue("home_view_inbox");
        VisTextButton inboxButton = new VisTextButton(inboxLabel);
        inboxButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                }
            }
        });
        column.add(inboxButton).left().pad(4).row();
        column.add().height(8).row();
    }

    private VisTable newsRow(Message message) {
        VisTable row = new VisTable(true);
        String bullet = Boolean.TRUE.equals(message.getIsRead()) ? "○ " : "● ";
        VisLabel titleLabel = new VisLabel(bullet + (message.getTitle() != null ? message.getTitle() : ""));
        row.add(titleLabel).left().expandX();
        if (message.getMessageTime() != null) {
            row.add(new VisLabel(message.getMessageTime().format(dateFormatter))).right();
        }
        return row;
    }

    // -----------------------------------------------------------------------------------------
    // Squad status (injuries)
    // -----------------------------------------------------------------------------------------

    private void addSquadStatus(VisTable column, Club club, LocalDateTime gameDate) {
        column.add(sectionLabel("home_squad_status")).left().row();
        column.addSeparator().pad(2).row();

        LocalDate asOf = gameDate != null ? gameDate.toLocalDate() : LocalDate.now();
        List<Player> players = club.getPlayers();
        int squadSize = players != null ? players.size() : 0;

        List<Player> injured = new ArrayList<>();
        if (players != null) {
            for (Player player : players) {
                PlayerCondition condition = PlayerConditionService.conditionOf(player);
                if (condition != null && condition.isInjured(asOf)) {
                    injured.add(player);
                }
            }
        }

        String summary = squadSize + " " + LanguageModLoader.getValue("players_at_club") + "  •  "
                + injured.size() + " " + LanguageModLoader.getValue("home_injured");
        column.add(new VisLabel(summary)).left().pad(2).row();

        if (injured.isEmpty()) {
            column.add(new VisLabel(LanguageModLoader.getValue("home_no_injuries"))).left().pad(2).row();
        } else {
            int shown = 0;
            for (Player player : injured) {
                if (shown >= MAX_INJURIES_SHOWN) {
                    column.add(new VisLabel("...")).left().pad(2).row();
                    break;
                }
                PlayerCondition condition = PlayerConditionService.conditionOf(player);
                String name = player.getPerson() != null
                        ? (player.getPerson().getName() + " " + player.getPerson().getLastname())
                        : LanguageModLoader.getValue("home_unknown_player");
                String estimate = condition != null ? condition.formatReturnEstimate(asOf) : "";
                column.add(new VisLabel("• " + name + " — " + estimate)).left().pad(1).row();
                shown++;
            }
        }
        column.add().height(8).row();
    }

    // -----------------------------------------------------------------------------------------
    // Finances snapshot
    // -----------------------------------------------------------------------------------------

    private void addFinances(VisTable column, Club club) {
        column.add(sectionLabel("home_finances")).left().row();
        column.addSeparator().pad(2).row();

        String balance = "$" + currencyFormat.format(club.getClubBalance() != null ? club.getClubBalance() : java.math.BigDecimal.ZERO);
        VisLabel balanceLabel = new VisLabel(LanguageModLoader.getValue("home_club_balance") + ": " + balance);
        balanceLabel.setFontScale(1.1f);
        column.add(balanceLabel).left().pad(2).row();

        String seasonLine = LanguageModLoader.getValue("home_season_income") + ": $"
                + currencyFormat.format(club.getSeasonIncome() != null ? club.getSeasonIncome() : java.math.BigDecimal.ZERO)
                + "   " + LanguageModLoader.getValue("home_season_expenditure") + ": $"
                + currencyFormat.format(club.getSeasonExpenditure() != null ? club.getSeasonExpenditure() : java.math.BigDecimal.ZERO);
        column.add(new VisLabel(seasonLine)).left().pad(2).row();

        VisTextButton financesButton = new VisTextButton(LanguageModLoader.getValue("home_view_finances"));
        financesButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.FINANCES_SCREEN);
                }
            }
        });
        column.add(financesButton).left().pad(4).row();
        column.add().height(8).row();
    }

    // -----------------------------------------------------------------------------------------
    // Quick links
    // -----------------------------------------------------------------------------------------

    private void addQuickLinks(Club club) {
        VisTable linksRow = new VisTable(true);
        linksRow.add(quickLinkButton("squad", MainMenuManager.MAIN_SQUAD_SCREEN)).pad(4);
        linksRow.add(quickLinkButton("tactics", MainMenuManager.TACTICS_SCREEN)).pad(4);
        linksRow.add(quickLinkButton("schedule", MainMenuManager.SCHEDULE_SCREEN)).pad(4);
        linksRow.add(quickLinkButton("finances", MainMenuManager.FINANCES_SCREEN)).pad(4);
        linksRow.add(quickLinkButton("inbox", MainMenuManager.INBOX_SCREEN)).pad(4);
        linksRow.add(quickLinkButton("club_info", MainMenuManager.CLUB_INFO_SCREEN)).pad(4);
        contentTable.add(linksRow).pad(4).row();
    }

    private VisTextButton quickLinkButton(String labelKey, int targetScreen) {
        VisTextButton button = new VisTextButton(LanguageModLoader.getValue(labelKey));
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(targetScreen);
                }
            }
        });
        return button;
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    private VisLabel sectionLabel(String key) {
        VisLabel label = new VisLabel(LanguageModLoader.getValue(key));
        label.setFontScale(1.05f);
        return label;
    }

    private Color resultColor(char letter) {
        switch (letter) {
            case 'W': return new Color(0.35f, 0.75f, 0.35f, 1f);
            case 'L': return new Color(0.8f, 0.3f, 0.3f, 1f);
            default: return new Color(0.8f, 0.75f, 0.3f, 1f);
        }
    }

    private String competitionName(Club club, Match match) {
        if (match.getMatchType() == null) {
            return null;
        }
        if (match.getMatchType() == Match.LEAGUE_MATCH) {
            League league = club.getCurrentLeague();
            return league != null ? league.getName() : LanguageModLoader.getValue("home_league_match");
        }
        if (match.getMatchType() == Match.CUP_MATCH) {
            return LanguageModLoader.getValue("home_cup_match");
        }
        return LanguageModLoader.getValue("home_friendly_match");
    }
}
