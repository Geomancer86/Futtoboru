package com.rndmodgames.futtoboru.tables.recruitment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.recruitment.CandidatePools;
import com.rndmodgames.futtoboru.engine.recruitment.Perception;
import com.rndmodgames.futtoboru.engine.recruitment.Recruitment;
import com.rndmodgames.futtoboru.engine.recruitment.Shortlists;
import com.rndmodgames.futtoboru.menu.GameScreenId;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * R-03: the candidate pools, read-only (name, age, origin, position, where he plays). Opens on the club's own region;
 * the region box shows the others (the Scottish pools are where the imports come from). R-04: the club's view of each
 * man (period words, or ranges behind the toggle); a click on a man opens his card (club view, each observer).
 * R-05: Add puts a man on the club's shortlist; the Shortlist button opens it. Signing is R-07.
 */
public class CandidatesScreenTable extends VisTable {

    private final Futtoboru game;
    private Club currentClub;
    /** the region shown, kept while the screen is reopened */
    private String regionId;
    private final VisTable rows = new VisTable(true);
    /** R-04: the man whose card is open (by player id), or null */
    private Long selected;

    public CandidatesScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
    }

    public void setCurrentClub(Club club) {
        if (club != currentClub) {
            regionId = null;
        }
        this.currentClub = club;
    }

    public void updateDynamicComponents() {
        this.clear();
        SaveGame save = game.getSession().getGame();
        RecruitmentData data = game.getSession().database().getRecruitmentData();
        if (currentClub == null || save == null || data == null || data.getPools().isEmpty()) {
            this.add(new VisLabel(text("finances_no_club"))).pad(UiTheme.space("lg"));
            return;
        }
        // a new game before its first day: the posts and pools it would get on that day
        if (save.getCandidatePoolSeason() == null && save.getGameDate() != null) {
            Recruitment.afterDay(save, save.getGameDate(), game.random());
        }
        List<PoolRegion> regions = new ArrayList<>(data.getPools().values());
        if (regionId == null || !data.getPools().containsKey(regionId)) {
            PoolRegion own = CandidatePools.regionOf(currentClub);
            regionId = own != null ? own.dataId() : regions.get(0).dataId();
        }
        PoolRegion region = data.getPools().get(regionId);

        VisLabel title = new VisLabel(LanguageModLoader.format("candidates_title", region.name()));
        title.setFontScale(UiTheme.fontScale("heading"));
        this.add(title).left().row();

        VisLabel hint = new VisLabel(text("candidates_hint"));
        hint.setWrap(true);
        hint.setColor(UiTheme.color("text.muted"));
        this.add(hint).left().width(UiTheme.size("text.wrap")).row();

        VisTable top = new VisTable(true);
        top.add(new VisLabel(text("candidates_region"))).left();
        VisSelectBox<String> box = new VisSelectBox<>();
        Array<String> names = new Array<>();
        for (PoolRegion r : regions) {
            names.add(r.name());
        }
        box.setItems(names);
        box.setSelected(region.name());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int i = box.getSelectedIndex();
                if (i >= 0 && i < regions.size() && !regions.get(i).dataId().equals(regionId)) {
                    regionId = regions.get(i).dataId();
                    updateDynamicComponents();
                }
            }
        });
        top.add(box).left();
        List<Candidate> men = CandidatePools.inRegion(save, regionId);
        VisLabel count = new VisLabel(LanguageModLoader.format("candidates_count", men.size()));
        count.setColor(UiTheme.color("text.secondary"));
        top.add(count).left();
        top.add(PerceptionCard.toggle(game.getSession(), this::updateDynamicComponents)).left()
                .padLeft(UiTheme.space("lg"));
        // R-05: the club's shortlist
        VisTextButton shortlist = new VisTextButton(LanguageModLoader.format("candidates_shortlist",
                Shortlists.listed(save, currentClub).size()));
        shortlist.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getGameEngine() != null && game.getGameEngine().getMainMenuManager() != null) {
                    game.getGameEngine().getMainMenuManager().setActiveMainScreen(GameScreenId.SHORTLIST);
                }
            }
        });
        top.add(shortlist).left().padLeft(UiTheme.space("lg"));
        this.add(top).left().row();

        Perception.ensure(save);
        VisTable content = new VisTable(true);
        Candidate open = null;
        for (Candidate c : men) {
            if (c.getPlayer() != null && selected != null && selected.equals(c.getPlayer().getId())) {
                open = c;
            }
        }
        if (open != null && open.getPlayer().getPerson() != null) {
            Person person = open.getPlayer().getPerson();
            content.add(PerceptionCard.card(game.getSession(), person.getName() + " " + person.getLastname(),
                    open.getPlayer(), Perception.report(save, open.getPlayer(), open, game.random()),
                    this::updateDynamicComponents)).left().padBottom(UiTheme.space("lg")).row();
        }
        buildRows(save, men);
        content.add(rows).left().top();
        VisScrollPane scroll = new VisScrollPane(content);
        scroll.setFadeScrollBars(false);
        this.add(scroll).grow().row();
        this.align(Align.top);
    }

    private void buildRows(SaveGame save, List<Candidate> men) {
        rows.clear();
        String[] headers = { "candidates_col_name", "candidates_col_age", "candidates_col_origin",
                "candidates_col_position", "candidates_col_plays_for", "candidates_col_view", "shortlist_col_action" };
        for (String h : headers) {
            VisLabel l = new VisLabel(text(h));
            l.setColor(UiTheme.color("text.heading"));
            rows.add(l).left();
        }
        rows.row();
        if (men.isEmpty()) {
            rows.add(new VisLabel(text("candidates_empty"))).colspan(headers.length).left();
            return;
        }
        LocalDateTime today = save.getGameDate() != null ? save.getGameDate() : LocalDateTime.now();
        for (Candidate c : men) {
            Player p = c.getPlayer();
            Person person = p != null ? p.getPerson() : null;
            if (person == null) {
                continue;
            }
            VisLabel name = new VisLabel(person.getName() + " " + person.getLastname());
            if (p.getId() != null && p.getId().equals(selected)) {
                name.setColor(UiTheme.color("accent"));
            }
            name.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selected = p.getId() != null && p.getId().equals(selected) ? null : p.getId();
                    updateDynamicComponents();
                }
            });
            rows.add(name).left().width(UiTheme.size("col.label"));
            rows.add(new VisLabel(person.getBirthDate() != null ? String.valueOf(CandidatePools.age(person, today)) : "-"))
                    .left();
            rows.add(new VisLabel(origin(c, person))).left().width(UiTheme.size("col.long"));
            rows.add(new VisLabel(p.getPosition() != null ? text(p.getPosition().labelKey()) : text("position_unknown")))
                    .left();
            VisLabel club = new VisLabel(c.getPlaysFor() != null ? c.getPlaysFor() : text("candidates_no_club"));
            if (c.getPlaysFor() == null) {
                club.setColor(UiTheme.color("text.secondary"));
            }
            rows.add(club).left();
            // R-04: the club's view in a line
            VisLabel view = new VisLabel(PerceptionCard.summary(Perception.report(save, p, c, game.random())
                    .consensus(), p, game.getSession().isPerceptionNumbers()));
            view.setColor(UiTheme.color("text.secondary"));
            rows.add(view).left();
            // R-05: on the shortlist, or Add
            ShortlistEntry entry = Shortlists.entry(save, currentClub.getId(), p.getId());
            if (entry != null && entry.isListed()) {
                VisLabel listed = new VisLabel(text("candidates_listed"));
                listed.setColor(UiTheme.color("text.secondary"));
                rows.add(listed).left();
            } else {
                VisTextButton add = new VisTextButton(text("candidates_add"));
                add.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Shortlists.add(save, currentClub, p, today);
                        updateDynamicComponents();
                    }
                });
                rows.add(add).left();
            }
            rows.row();
        }
    }

    /** "Ireland, came over aged 6 (Mill hand)": birth country, arrival, backstory. */
    static String origin(Candidate c, Person person) {
        String country = person.getCountry() != null ? person.getCountry().getCommonName() : "-";
        String born = c.getArrivedAge() != null
                ? LanguageModLoader.format("candidates_came_over", country, c.getArrivedAge())
                : country;
        if (c.getOrigin() == null) {
            return born;
        }
        return LanguageModLoader.format("candidates_origin_line", born, text(originKey(c.getOrigin())));
    }

    /** @return the bundle key of an origin backstory ({@code gentleman-amateur} -> {@code origin_gentleman_amateur}) */
    public static String originKey(String originId) {
        return "origin_" + originId.replace('-', '_');
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }
}
