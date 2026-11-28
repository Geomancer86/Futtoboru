package com.rndmodgames.futtoboru.tables.recruitment;

import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.recruitment.Advert;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.Newspaper;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.recruitment.CandidatePools;
import com.rndmodgames.futtoboru.engine.recruitment.CandidateSources;
import com.rndmodgames.futtoboru.engine.recruitment.Perception;
import com.rndmodgames.futtoboru.engine.recruitment.Shortlists;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.GameScreenId;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * R-05: the club's shortlist (from the Candidates screen): each man, how he reached the club and who suggested him,
 * the club's view (a click opens his card), Remove. Below, the adverts: a paper (by region, its cost), the line
 * wanted, the period wording, Place; and the club's adverts with their replies so far.
 */
public class ShortlistScreenTable extends VisTable {

    private final Futtoboru game;
    private MainMenuManager menuManager;
    private Club currentClub;
    /** the man whose card is open (by player id), or null */
    private Long selected;
    private String paperId;
    private String wanted = Advert.ANY;

    public ShortlistScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
    }

    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void setCurrentClub(Club club) {
        this.currentClub = club;
    }

    public void updateDynamicComponents() {
        this.clear();
        SaveGame save = game.getSession().getGame();
        if (currentClub == null || save == null) {
            this.add(new VisLabel(text("finances_no_club"))).pad(UiTheme.space("lg"));
            return;
        }
        LocalDateTime today = save.getGameDate() != null ? save.getGameDate() : LocalDateTime.now();
        List<ShortlistEntry> list = Shortlists.listed(save, currentClub);

        VisLabel title = new VisLabel(LanguageModLoader.format("shortlist_title", currentClub.getName()));
        title.setFontScale(UiTheme.fontScale("heading"));
        this.add(title).left().row();
        VisLabel hint = new VisLabel(text("shortlist_hint"));
        hint.setWrap(true);
        hint.setColor(UiTheme.color("text.muted"));
        this.add(hint).left().width(UiTheme.size("text.wrap")).row();

        VisTable top = new VisTable(true);
        VisTextButton back = new VisTextButton(text("shortlist_back"));
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(GameScreenId.CANDIDATES);
                }
            }
        });
        top.add(back).left();
        VisLabel count = new VisLabel(LanguageModLoader.format("shortlist_count", list.size()));
        count.setColor(UiTheme.color("text.secondary"));
        top.add(count).left();
        top.add(PerceptionCard.toggle(game.getSession(), this::updateDynamicComponents)).left()
                .padLeft(UiTheme.space("lg"));
        this.add(top).left().row();

        Perception.ensure(save);
        VisTable content = new VisTable(true);
        for (ShortlistEntry e : list) {
            Player p = Shortlists.man(save, e.getPlayer());
            if (p != null && p.getPerson() != null && e.getPlayer().equals(selected)) {
                content.add(PerceptionCard.card(game.getSession(), CandidateSourcesText.name(p.getPerson()), p,
                        Perception.report(save, p, game.random()), this::updateDynamicComponents)).left()
                        .padBottom(UiTheme.space("lg")).row();
            }
        }
        content.add(rows(save, list, today)).left().top().row();
        content.add(adverts(save, today)).left().top().padTop(UiTheme.space("lg")).row();
        VisScrollPane scroll = new VisScrollPane(content);
        scroll.setFadeScrollBars(false);
        this.add(scroll).grow().row();
        this.align(Align.top);
    }

    private VisTable rows(SaveGame save, List<ShortlistEntry> list, LocalDateTime today) {
        VisTable rows = new VisTable(true);
        String[] headers = { "candidates_col_name", "candidates_col_age", "candidates_col_position",
                "candidates_col_plays_for", "shortlist_col_source", "shortlist_col_suggested", "shortlist_col_since",
                "candidates_col_view", "shortlist_col_action" };
        for (String h : headers) {
            VisLabel l = new VisLabel(text(h));
            l.setColor(UiTheme.color("text.heading"));
            rows.add(l).left();
        }
        rows.row();
        if (list.isEmpty()) {
            rows.add(new VisLabel(text("shortlist_empty"))).colspan(headers.length).left();
            return rows;
        }
        for (ShortlistEntry e : list) {
            Player p = Shortlists.man(save, e.getPlayer());
            Person person = p != null ? p.getPerson() : null;
            if (person == null) {
                continue;
            }
            Candidate c = Perception.candidateOf(save, p);
            VisLabel name = new VisLabel(CandidateSourcesText.name(person));
            if (e.getPlayer().equals(selected)) {
                name.setColor(UiTheme.color("accent"));
            }
            name.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selected = e.getPlayer().equals(selected) ? null : e.getPlayer();
                    updateDynamicComponents();
                }
            });
            rows.add(name).left().width(UiTheme.size("col.label"));
            rows.add(new VisLabel(person.getBirthDate() != null ? String.valueOf(CandidatePools.age(person, today))
                    : "-")).left();
            rows.add(new VisLabel(p.getPosition() != null ? text(p.getPosition().labelKey()) : text("position_unknown")))
                    .left();
            String playsFor = c != null && c.getPlaysFor() != null ? c.getPlaysFor() : text("candidates_no_club");
            rows.add(new VisLabel(playsFor)).left();
            rows.add(new VisLabel(CandidateSourcesText.source(e))).left();
            VisLabel by = new VisLabel(CandidateSourcesText.suggestedBy(e));
            by.setColor(UiTheme.color("text.secondary"));
            rows.add(by).left();
            rows.add(new VisLabel(e.getSince() != null ? GameFormat.mediumDate(e.getSince().toLocalDate()) : "-"))
                    .left();
            VisLabel view = new VisLabel(PerceptionCard.summary(Perception.report(save, p, game.random()).consensus(),
                    p, game.getSession().isPerceptionNumbers()));
            view.setColor(UiTheme.color("text.secondary"));
            rows.add(view).left();
            VisTextButton remove = new VisTextButton(text("shortlist_remove"));
            remove.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Shortlists.remove(save, currentClub, e.getPlayer());
                    updateDynamicComponents();
                }
            });
            rows.add(remove).left();
            rows.row();
        }
        return rows;
    }

    /** the advert form and the club's adverts */
    private VisTable adverts(SaveGame save, LocalDateTime today) {
        VisTable t = new VisTable(true);
        VisLabel heading = new VisLabel(text("shortlist_advert_heading"));
        heading.setColor(UiTheme.color("text.heading"));
        t.add(heading).left().colspan(2).row();
        List<Newspaper> papers = CandidateSources.newspapers(today);
        if (papers.isEmpty()) {
            t.add(new VisLabel(text("shortlist_advert_none"))).left().colspan(2).row();
            return t;
        }
        Newspaper paper = papers.get(0);
        for (Newspaper n : papers) {
            if (n.dataId().equals(paperId)) {
                paper = n;
            }
        }
        paperId = paper.dataId();

        t.add(new VisLabel(text("shortlist_advert_paper"))).left();
        VisSelectBox<String> paperBox = new VisSelectBox<>();
        Array<String> names = new Array<>();
        for (Newspaper n : papers) {
            names.add(CandidateSourcesText.paper(n));
        }
        paperBox.setItems(names);
        paperBox.setSelectedIndex(papers.indexOf(paper));
        paperBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int i = paperBox.getSelectedIndex();
                if (i >= 0 && i < papers.size()) {
                    paperId = papers.get(i).dataId();
                    updateDynamicComponents();
                }
            }
        });
        t.add(paperBox).left().row();

        List<String> lines = CandidateSources.wantedLines();
        t.add(new VisLabel(text("shortlist_advert_wanted"))).left();
        VisSelectBox<String> lineBox = new VisSelectBox<>();
        Array<String> lineNames = new Array<>();
        for (String l : lines) {
            lineNames.add(text(CandidateSources.wantedKey(l)));
        }
        lineBox.setItems(lineNames);
        lineBox.setSelectedIndex(Math.max(0, lines.indexOf(wanted)));
        lineBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int i = lineBox.getSelectedIndex();
                if (i >= 0 && i < lines.size()) {
                    wanted = lines.get(i);
                    updateDynamicComponents();
                }
            }
        });
        t.add(lineBox).left().row();

        VisLabel wording = new VisLabel(CandidateSources.advertWording(currentClub, wanted));
        wording.setWrap(true);
        wording.setColor(UiTheme.color("text.secondary"));
        t.add(wording).left().colspan(2).width(UiTheme.size("text.wrap.narrow")).row();

        Advert running = CandidateSources.running(save, currentClub, paper.dataId(), today);
        if (running != null) {
            t.add(new VisLabel(text("shortlist_advert_running"))).left().colspan(2).row();
        } else {
            VisTextButton place = new VisTextButton(LanguageModLoader.format("shortlist_advert_place",
                    Money.format(Shortlists.pounds(paper.costPence()))));
            final String chosen = paper.dataId();
            place.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    CandidateSources.placeAdvert(save, currentClub, chosen, wanted, today, game.random());
                    updateDynamicComponents();
                }
            });
            t.add(place).left().colspan(2).row();
        }

        for (Advert a : CandidateSources.advertsOf(save, currentClub)) {
            Newspaper n = CandidateSources.newspapers(today).stream().filter(x -> x.dataId().equals(a.getNewspaper()))
                    .findFirst().orElse(null);
            VisLabel l = new VisLabel(LanguageModLoader.format("shortlist_advert_placed",
                    n != null ? n.name() : a.getNewspaper(), GameFormat.mediumDate(a.getPlaced().toLocalDate()),
                    text(CandidateSources.wantedKey(a.getWanted())), CandidateSources.repliesIn(save, a)));
            l.setColor(UiTheme.color("text.secondary"));
            t.add(l).left().colspan(2).row();
        }
        return t;
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }
}
