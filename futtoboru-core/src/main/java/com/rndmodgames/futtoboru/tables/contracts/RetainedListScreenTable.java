package com.rndmodgames.futtoboru.tables.contracts;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-04: the club's retained list (from the Squad screen or the April letter). Each man whose engagement ends this
 * season: retained, with the weekly wage and summer pay offered for next season, or not retained. "Send the terms"
 * sends them; each man answers by letter the next day, and a man who declined can be offered more until 30 April.
 * Undecided men are decided by the committee at the 1 May rollover ({@link ContractManager}). Outside 16-30 April the
 * list is read-only.
 */
public class RetainedListScreenTable extends VisTable {

    private final Futtoboru game;
    private MainMenuManager menuManager;
    private Club currentClub;
    private final VisTable rows = new VisTable(true);
    private final VisLabel statusLabel = new VisLabel();

    public RetainedListScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
    }

    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void setCurrentClub(Club club) {
        this.currentClub = club;
    }

    private ContractManager contracts() {
        return game.getGameEngine() != null ? game.getGameEngine().getContractManager() : null;
    }

    public void updateDynamicComponents() {
        this.clear();
        SaveGame save = game.getCurrentGame();
        ContractManager manager = contracts();
        if (currentClub == null || save == null || manager == null) {
            this.add(new VisLabel(text("finances_no_club"))).pad(UiTheme.space("lg"));
            return;
        }
        int season = manager.runningSeason(save);
        boolean open = manager.isRetainedListOpen();

        VisLabel title = new VisLabel(LanguageModLoader.format("contracts_retained_list_title",
                ContractCalendar.seasonName(season + 1)));
        title.setFontScale(UiTheme.fontScale("heading"));
        this.add(title).left().row();

        String state;
        if (open) {
            state = LanguageModLoader.format("contracts_list_open",
                    ContractManager.formatDate(ContractCalendar.seasonEnd(season).toLocalDate()));
        } else {
            state = LanguageModLoader.format("contracts_list_opens",
                    ContractManager.formatDate(ContractCalendar.retainedListOpens(season)));
        }
        VisLabel stateLabel = new VisLabel(state);
        stateLabel.setColor(UiTheme.color(open ? "state.positive" : "text.secondary"));
        this.add(stateLabel).left().row();

        VisLabel hint = new VisLabel(text("contracts_list_hint"));
        hint.setWrap(true);
        hint.setColor(UiTheme.color("text.muted"));
        this.add(hint).left().width(UiTheme.size("text.wrap")).row();

        this.add(new VisLabel(LanguageModLoader.format("contracts_wage_bill",
                Money.format(ContractTerms.wageBill(currentClub, false)),
                Money.format(ContractTerms.wageBill(currentClub, true))))).left().row();

        buildRows(manager, open);
        VisScrollPane scroll = new VisScrollPane(rows);
        scroll.setFadeScrollBars(false);
        this.add(scroll).grow().row();

        VisTextButton send = new VisTextButton(text("contracts_send_terms"), UiTheme.style("button.primary"));
        send.setDisabled(!open);
        send.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int sent = manager.sendTerms(currentClub);
                updateDynamicComponents();
                statusLabel.setColor(UiTheme.color(sent > 0 ? "state.positive" : "text.secondary"));
                statusLabel.setText(sent > 0 ? LanguageModLoader.format("contracts_terms_sent", sent)
                        : text("contracts_nothing_to_send"));
            }
        });
        VisTable bottom = new VisTable(true);
        bottom.add(send).left();
        bottom.add(statusLabel).left();
        this.add(bottom).left().row();
        this.align(Align.top);
    }

    private void buildRows(ContractManager manager, boolean open) {
        rows.clear();
        String[] headers = { "contracts_col_name", "contracts_col_position", "contracts_col_age", "contracts_col_type",
                "contracts_col_now", "contracts_col_decision", "contracts_col_offer", "contracts_col_summer",
                "contracts_col_reply" };
        for (String h : headers) {
            VisLabel l = new VisLabel(text(h));
            l.setColor(UiTheme.color("text.heading"));
            rows.add(l).left();
        }
        rows.row();
        List<PlayerContract> ending = manager.endingContracts(currentClub);
        if (ending.isEmpty()) {
            rows.add(new VisLabel(text("contracts_list_empty"))).colspan(headers.length).left();
            return;
        }
        for (PlayerContract c : ending) {
            Player p = ContractManager.playerOf(currentClub, c);
            if (p == null) {
                continue;
            }
            addRow(manager, p, c, open);
        }
    }

    private void addRow(ContractManager manager, Player p, PlayerContract c, boolean open) {
        boolean accepted = c.getAnswer() == PlayerContract.Answer.ACCEPTED;
        boolean waiting = ContractManager.isSentAndWaiting(c);
        boolean retained = c.getRetain() == PlayerContract.Retain.RETAINED;
        boolean editable = open && !accepted && !waiting;

        // an in-game link (INTERFACE_GUIDE): the man's player screen
        LinkLabel name = new LinkLabel(p.getPerson().getName() + " " + p.getPerson().getLastname());
        name.setListener(new LinkLabel.LinkLabelListener() {
            @Override
            public void clicked(String url) {
                if (menuManager != null) {
                    menuManager.setSelectedPlayer(p);
                    menuManager.setActiveMainScreen(GameScreenId.PLAYER_DETAIL);
                }
            }
        });
        rows.add(name).left().width(UiTheme.size("col.label"));
        rows.add(new VisLabel(p.getPosition() != null ? text(p.getPosition().labelKey()) : text("position_unknown")))
                .left();
        rows.add(new VisLabel(String.valueOf(ContractTerms.age(p, game.getCurrentGame().getGameDate().toLocalDate()))))
                .left();
        rows.add(new VisLabel(text(ContractType.labelKey(c.getContractType())))).left();
        rows.add(new VisLabel(ContractManager.terms(c))).left();

        // decision
        VisTable decision = new VisTable(true);
        String decidedKey = retained ? "contracts_retained"
                : c.getRetain() == PlayerContract.Retain.NOT_RETAINED ? "contracts_not_retained" : "contracts_undecided";
        VisLabel decided = new VisLabel(LanguageModLoader.getValue(decidedKey));
        decided.setColor(UiTheme.color(retained ? "state.positive"
                : c.getRetain() == PlayerContract.Retain.NOT_RETAINED ? "state.negative" : "text.secondary"));
        decision.add(decided).left().width(UiTheme.size("col.long"));
        VisTextButton retain = new VisTextButton(text("contracts_retain"));
        retain.setDisabled(!editable || retained);
        retain.addListener(change(() -> manager.retain(c)));
        VisTextButton release = new VisTextButton(text("contracts_release"));
        release.setDisabled(!editable || c.getRetain() == PlayerContract.Retain.NOT_RETAINED);
        release.addListener(change(() -> manager.release(c)));
        decision.add(retain);
        decision.add(release);
        rows.add(decision).left();

        // offer
        VisTable offer = new VisTable(true);
        boolean canOffer = editable && retained && !c.isAmateur();
        VisTextButton less = new VisTextButton(text("contracts_less"));
        less.setDisabled(!canOffer);
        less.addListener(change(() -> manager.changeOfferedWage(currentClub, c, -1)));
        VisTextButton more = new VisTextButton(text("contracts_more"));
        more.setDisabled(!canOffer);
        more.addListener(change(() -> manager.changeOfferedWage(currentClub, c, 1)));
        String offered = !retained ? "-" : c.isAmateur() ? text("contracts_terms_amateur_short")
                : Money.format(c.getOfferedWage());
        offer.add(less);
        offer.add(new VisLabel(offered)).width(UiTheme.size("col.value"));
        offer.add(more);
        rows.add(offer).left();

        VisCheckBox summer = new VisCheckBox(text("contracts_summer_short"));
        summer.setChecked(retained && c.getOfferedSummerWage() != null && c.getOfferedSummerWage().signum() > 0);
        summer.setDisabled(!canOffer);
        summer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (summer.isDisabled()) {
                    return;
                }
                manager.setOfferedSummerPay(currentClub, c, summer.isChecked());
                updateDynamicComponents();
            }
        });
        rows.add(summer).left();

        String reply = accepted ? "contracts_reply_state_accepted"
                : c.getAnswer() == PlayerContract.Answer.DECLINED ? "contracts_reply_state_declined"
                        : waiting ? "contracts_reply_state_waiting" : "contracts_reply_state_none";
        VisLabel replyLabel = new VisLabel(text(reply));
        replyLabel.setColor(UiTheme.color(accepted ? "state.positive"
                : c.getAnswer() == PlayerContract.Answer.DECLINED ? "state.negative" : "text.secondary"));
        rows.add(replyLabel).left();
        rows.row();
    }

    /** A button listener that runs the change and redraws the list */
    private ChangeListener change(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof VisTextButton && ((VisTextButton) actor).isDisabled()) {
                    return;
                }
                action.run();
                updateDynamicComponents();
            }
        };
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }
}
