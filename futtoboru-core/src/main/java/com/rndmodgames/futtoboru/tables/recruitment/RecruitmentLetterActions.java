package com.rndmodgames.futtoboru.tables.recruitment;

import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.recruitment.CandidateSources;
import com.rndmodgames.futtoboru.engine.recruitment.Shortlists;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * R-05: the buttons under a recruitment letter in the inbox. Per man in it: Add to shortlist / Ignore (or what was
 * done); an agent's letter: Pay the fee / Ignore for all his men at once.
 */
public final class RecruitmentLetterActions {

    private RecruitmentLetterActions() {
    }

    /** @return the actions of this letter, or null when it brought no men */
    public static VisTable of(SaveGame game, Message letter, RandomSource root, Runnable refresh) {
        Club club = game != null ? game.getCurrentClub() : null;
        if (club == null || letter == null || letter.getId() == null) {
            return null;
        }
        List<ShortlistEntry> entries = Shortlists.ofLetter(game, letter.getId());
        if (entries.isEmpty()) {
            return null;
        }
        LocalDateTime today = game.getGameDate();
        VisTable t = new VisTable(true);
        int unpaid = 0;
        for (ShortlistEntry e : entries) {
            if (ShortlistEntry.UNPAID.equals(e.getStatus())) {
                unpaid += e.getFeePence();
            }
        }
        if (unpaid > 0) {
            VisTextButton pay = new VisTextButton(LanguageModLoader.format("recruit_pay",
                    Money.format(Shortlists.pounds(unpaid))));
            pay.addListener(changed(() -> CandidateSources.payAgent(game, club, letter.getId(), today, root), refresh));
            VisTextButton ignore = new VisTextButton(LanguageModLoader.getValue("recruit_ignore"));
            ignore.addListener(changed(() -> entries.forEach(Shortlists::ignore), refresh));
            t.add(pay).left();
            t.add(ignore).left().row();
            return t;
        }
        for (ShortlistEntry e : entries) {
            Player p = Shortlists.man(game, e.getPlayer());
            if (p == null || p.getPerson() == null) {
                continue;
            }
            t.add(new VisLabel(CandidateSourcesText.name(p.getPerson()))).left().width(UiTheme.size("col.label"));
            if (ShortlistEntry.OFFERED.equals(e.getStatus())) {
                VisTextButton add = new VisTextButton(LanguageModLoader.getValue("recruit_add"));
                add.addListener(changed(() -> Shortlists.accept(e, today), refresh));
                VisTextButton ignore = new VisTextButton(LanguageModLoader.getValue("recruit_ignore"));
                ignore.addListener(changed(() -> Shortlists.ignore(e), refresh));
                t.add(add).left();
                t.add(ignore).left();
            } else {
                String done = LanguageModLoader.getValue("recruit_ignored");
                if (e.isListed()) {
                    done = LanguageModLoader.getValue("recruit_listed");
                }
                VisLabel doneLabel = new VisLabel(done);
                doneLabel.setColor(UiTheme.color("text.secondary"));
                t.add(doneLabel).left().colspan(2);
            }
            t.row();
        }
        return t;
    }

    private static ChangeListener changed(Runnable action, Runnable refresh) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
                refresh.run();
            }
        };
    }
}
