package com.rndmodgames.futtoboru.tables.schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.temporal.FriendlyArranger;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Arrange a friendly (U-08): the day comes from a click on the calendar ({@link FixturesTable}), then the ground
 * and the opponent. "Send the invitation" puts the friendly in the club's proposed list
 * ({@link FriendlyArranger#propose}); the other club answers by letter the next day.
 */
public class ArrangeFriendlyTable extends VisTable {

    private final Futtoboru game;
    private final ScheduleScreenTable parentTable;

    private Club currentClub;
    /** Set from the calendar */
    private LocalDateTime selectedDate;
    /** The clubs behind the opponent select box (index 0 is "Choose a club") */
    private final List<Club> opponents = new ArrayList<>();

    private final VisLabel selectedDateLabel = new VisLabel();
    private final VisSelectBox<String> groundSelectBox = new VisSelectBox<>();
    private final VisSelectBox<String> opponentSelectBox = new VisSelectBox<>();
    private final VisLabel statusLabel = new VisLabel();
    private final VisTextButton sendButton = new VisTextButton(text("friendly_send"),
            UiTheme.style("button.primary"));

    public ArrangeFriendlyTable(Futtoboru parent, ScheduleScreenTable scheduleScreenTable) {
        super(true);
        this.game = parent;
        this.parentTable = scheduleScreenTable;

        VisLabel title = new VisLabel(text("arrange_friendly"));
        title.setFontScale(UiTheme.fontScale("subheading"));
        this.add(title).colspan(2).left();

        this.row();
        this.add(text("friendly_date")).left();
        this.add(selectedDateLabel).left();

        groundSelectBox.setItems(text("friendly_ground_home"), text("friendly_ground_away"));
        this.row();
        this.add(text("friendly_ground")).left();
        this.add(groundSelectBox).fill();

        this.row();
        this.add(text("friendly_opponent")).left();
        this.add(opponentSelectBox).fill();

        ChangeListener validate = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateSendButton();
            }
        };
        opponentSelectBox.addListener(validate);

        VisLabel hint = new VisLabel(text("friendly_hint"));
        hint.setWrap(true);
        hint.setColor(UiTheme.color("text.muted"));
        this.row();
        this.add(hint).colspan(2).left().width(UiTheme.size("text.wrap.narrow"));

        statusLabel.setWrap(true);
        this.row();
        this.add(statusLabel).colspan(2).left().width(UiTheme.size("text.wrap.narrow"));

        sendButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                send();
            }
        });
        VisTextButton clearButton = new VisTextButton(text("friendly_clear"));
        clearButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                statusLabel.setText("");
                clearForm();
            }
        });

        this.row().padTop(UiTheme.space("lg"));
        this.add(sendButton).bottom();
        this.add(clearButton).bottom();
        this.align(Align.top);

        updateDynamicComponents();
    }

    private Club selectedOpponent() {
        int index = opponentSelectBox.getSelectedIndex();
        return index > 0 && index < opponents.size() ? opponents.get(index) : null;
    }

    private void updateSendButton() {
        sendButton.setDisabled(currentClub == null || selectedDate == null || selectedOpponent() == null);
    }

    private void send() {
        Club opponent = selectedOpponent();
        if (currentClub == null || selectedDate == null || opponent == null) {
            return;
        }
        FriendlyArranger arranger = new FriendlyArranger(game);
        FriendlyArranger.Reason reason = arranger.check(currentClub, opponent, selectedDate.toLocalDate());
        if (reason != FriendlyArranger.Reason.NONE) {
            statusLabel.setColor(UiTheme.color("state.negative"));
            statusLabel.setText(text(reason.key));
            return;
        }
        boolean atHome = groundSelectBox.getSelectedIndex() == 0;
        Match match = arranger.propose(currentClub, opponent, selectedDate.toLocalDate(), atHome);
        if (match == null) {
            return;
        }
        Log.info(Log.UI_SCHEDULE, "ArrangeFriendlyTable: invitation " + match.getId() + " sent to " + opponent.getName());
        statusLabel.setColor(UiTheme.color("state.positive"));
        statusLabel.setText(FriendlyArranger.fill(text("friendly_sent"), opponent.getName(),
                FriendlyArranger.formatDate(match.getMatchDateTime())));
        clearForm();
    }

    /** Clears the day and the opponent, here and on the calendar. */
    public void clearForm() {
        selectedDate = null;
        parentTable.fixturesTable.setSelectedDate(null);
        parentTable.fixturesTable.updateDynamicComponents();
        updateDynamicComponents();
        opponentSelectBox.setSelectedIndex(0);
        updateSendButton();
    }

    public void updateDynamicComponents() {
        selectedDateLabel.setText(selectedDate != null ? FriendlyArranger.formatDate(selectedDate)
                : text("select_a_date"));

        Club kept = selectedOpponent();
        opponents.clear();
        opponents.add(null);
        List<String> names = new ArrayList<>();
        names.add(text("friendly_choose_opponent"));
        if (currentClub != null && game.getSession().getGame() != null) {
            for (Club club : new FriendlyArranger(game).possibleOpponents(currentClub)) {
                opponents.add(club);
                names.add(club.getName());
            }
        }
        opponentSelectBox.setItems(names.toArray(new String[0]));
        int index = kept != null ? opponents.indexOf(kept) : 0;
        opponentSelectBox.setSelectedIndex(Math.max(index, 0));
        updateSendButton();
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
