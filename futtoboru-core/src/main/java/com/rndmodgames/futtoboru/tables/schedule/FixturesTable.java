package com.rndmodgames.futtoboru.tables.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.engine.temporal.FriendlyArranger;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.GroundArt;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * The club's calendar (U-08): {@link #WEEKS} weeks from the Monday of the current week, one cell per day with the
 * club's matches (played with the score, fixed, or a friendly invitation waiting for an answer). A click on a day
 * selects it for {@link ArrangeFriendlyTable}.
 */
public class FixturesTable extends VisTable {

    static final int WEEKS = 12;
    private static final String[] DAY_KEYS = { "schedule_mon", "schedule_tue", "schedule_wed", "schedule_thu",
            "schedule_fri", "schedule_sat", "schedule_sun" };

    private final Futtoboru game;
    private final ScheduleScreenTable parentTable;

    private Club currentClub;
    private LocalDateTime selectedDate;

    private final VisTable matchListTable = new VisTable(true);

    public FixturesTable(Futtoboru parent, ScheduleScreenTable scheduleScreenTable) {
        super(true);
        this.game = parent;
        this.parentTable = scheduleScreenTable;
        this.add(matchListTable);
    }

    public void updateDynamicComponents() {
        matchListTable.clear();
        SaveGame save = game.getSession().getGame();
        if (save == null) {
            return;
        }
        LocalDate today = save.getGameDate().toLocalDate();
        LocalDate firstMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        matchListTable.row();
        for (String key : DAY_KEYS) {
            matchListTable.add(text(key));
        }
        matchListTable.row();
        for (int i = 0; i < WEEKS * 7; i++) {
            LocalDate day = firstMonday.plusDays(i);
            matchListTable.add(dayCell(day, today, matchesOn(day))).top().left();
            if ((i + 1) % 7 == 0) {
                matchListTable.row();
            }
        }
    }

    /** The club's matches on {@code day}: played, fixed and proposed. */
    private List<Match> matchesOn(LocalDate day) {
        List<Match> found = new ArrayList<>();
        if (currentClub == null) {
            return found;
        }
        addOn(found, currentClub.getPlayedMatches(), day);
        addOn(found, currentClub.getScheduledMatches(), day);
        addOn(found, currentClub.getProposedMatches(), day);
        return found;
    }

    private void addOn(List<Match> found, List<Match> matches, LocalDate day) {
        if (matches == null) {
            return;
        }
        for (Match m : matches) {
            if (m != null && m.getMatchDateTime() != null && m.getMatchDateTime().toLocalDate().equals(day)
                    && FriendlyArranger.isClubFixture(currentClub, m) && !found.contains(m)) {
                found.add(m);
            }
        }
    }

    private VisTable dayCell(LocalDate day, LocalDate today, List<Match> matches) {
        VisTable cell = new VisTable(true);
        cell.setTouchable(Touchable.enabled);
        cell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedDate = day.atStartOfDay();
                parentTable.updateDynamicComponents();
            }
        });

        VisLabel dateLabel = new VisLabel(dayFormatter().format(day));
        boolean selected = selectedDate != null && selectedDate.toLocalDate().equals(day);
        if (selected) {
            dateLabel.setColor(UiTheme.color("selection"));
        } else if (day.equals(today)) {
            dateLabel.setColor(UiTheme.color("accent"));
        } else if (day.isBefore(today)) {
            dateLabel.setColor(UiTheme.color("text.muted"));
        }
        cell.add(dateLabel).left();
        if (selected || day.equals(today)) {
            cell.row();
            cell.add(new VisLabel(selected ? text("schedule_selected") : text("schedule_today"))).left();
        }

        for (Match match : matches) {
            cell.row();
            cell.add(new VisLabel(typeLabel(match))).left();
            cell.row();
            cell.add(new VisLabel(opponentLine(match))).left();
            if (NeutralVenues.isNeutral(match)) {
                cell.row();
                cell.add(neutralGroundThumb(match)).left();
            }
            cell.row();
            cell.add(new VisLabel(stateLabel(match))).left();
        }
        cell.align(Align.topLeft);
        return cell;
    }

    /** The day in a cell ({@code schedule_day_pattern}: "d MMM", numeric in Spanish). */
    private static DateTimeFormatter dayFormatter() {
        // U-12: month names in the game language; a bad pattern in a language file falls back to "d MMM"
        return GameFormat.formatter("schedule_day_pattern", "d MMM");
    }

    private static String typeLabel(Match match) {
        Integer type = match.getMatchType();
        if (type != null && type == Match.LEAGUE_MATCH) {
            return text("match_type_league");
        }
        if (type != null && type == Match.CUP_MATCH) {
            return text("match_type_cup");
        }
        return text("match_type_friendly");
    }

    private String opponentLine(Match match) {
        boolean home = currentClub.getId().equals(match.getHomeClubId());
        boolean away = currentClub.getId().equals(match.getAwayClubId());
        if (!home && !away) {
            return text("schedule_opponent_tbd");
        }
        Long opponentId = home ? match.getAwayClubId() : match.getHomeClubId();
        Club opponent = opponentId != null ? game.getSession().getGame().getClubById(opponentId) : null;
        String name = opponent != null ? opponent.getName() : text("schedule_opponent_tbd");
        if (NeutralVenues.isNeutral(match)) { // P2-14: a cup semi-final or final at a neutral ground
            Stadium ground = NeutralVenues.stadiumOf(match, game.getSession().getGame());
            return FriendlyArranger.fill(text("schedule_vs_neutral"), name,
                    ground != null ? ground.getName() : match.getVenue());
        }
        return FriendlyArranger.fill(text(home ? "schedule_vs_home" : "schedule_vs_away"), name);
    }

    /** P2-14: the picture of a neutral ground (the home and away grounds are implied by "home" / "away") */
    private Actor neutralGroundThumb(Match match) {
        Stadium ground = NeutralVenues.stadiumOf(match, game.getSession().getGame());
        Actor thumb = game.getGroundArt().picture(GroundArt.groundId(ground), true, UiTheme.size("art.thumb.w"),
                UiTheme.size("art.thumb.h"));
        if (ground != null) {
            UiTooltips.tipText(thumb, ground.getName());
        }
        return thumb;
    }

    private String stateLabel(Match match) {
        if (Boolean.TRUE.equals(match.getIsPlayed())) {
            boolean home = currentClub.getId().equals(match.getHomeClubId());
            int own = home ? match.getHomeGoals() : match.getAwayGoals();
            int other = home ? match.getAwayGoals() : match.getHomeGoals();
            return FriendlyArranger.fill(text("schedule_played"), own, other);
        }
        if (Boolean.TRUE.equals(match.getIsAccepted())) {
            return text("schedule_fixed");
        }
        return text("schedule_awaiting_answer");
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }

    public LocalDateTime getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDateTime selectedDate) {
        this.selectedDate = selectedDate;
    }
}
