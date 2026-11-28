package com.rndmodgames.futtoboru.tables.recruitment;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.recruitment.Perception;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Attr;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Group;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Range;
import com.rndmodgames.futtoboru.game.GameSession;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * R-04 (design §4.2, D28 Q5/Q6): what the club knows of a man who is not ours, in period words by default ("a rare
 * turn of speed", "wants pace") or as numeric ranges ("11-15") behind a toggle. The club view first (weighted
 * consensus, per attribute), then each observer who has seen him on his own line. Used by the Candidates screen and
 * by the Player detail of another club's man.
 */
public final class PerceptionCard {

    private PerceptionCard() {
    }

    /** @return the one-line summary of a view: words (phrases, how sure) or the group ranges */
    public static String summary(Map<Attr, Range> ranges, Player p, boolean numbers) {
        boolean keeper = p.getPosition() == Position.GOALKEEPER;
        if (numbers) {
            if (!seen(ranges)) {
                return text("perceive_not_seen");
            }
            String line = LanguageModLoader.format("perceive_groups_numbers", groupRange(ranges, Group.PHYSICAL),
                    groupRange(ranges, Group.TECHNICAL), groupRange(ranges, Group.MENTAL));
            return keeper ? LanguageModLoader.format("perceive_groups_numbers_keeper", line,
                    groupRange(ranges, Group.KEEPER)) : line;
        }
        StringBuilder words = new StringBuilder();
        for (String key : Perception.phraseKeys(ranges, keeper)) {
            words.append(words.length() == 0 ? "" : text("perceive_join")).append(text(key));
        }
        String sure = Perception.confidenceKey(ranges, keeper);
        return sure == null ? words.toString()
                : LanguageModLoader.format("perceive_summary_line", words.toString(), text(sure));
    }

    /** @return "11-15", the whole points that hold the range, or "?" unseen */
    public static String rangeText(Range r) {
        if (r == null || !r.seen()) {
            return text("perceive_band_unknown");
        }
        return LanguageModLoader.format("perceive_range", r.shownLo(), r.shownHi());
    }

    /** @return the word or the range of one attribute */
    public static String attributeText(Range r, boolean numbers) {
        return numbers ? rangeText(r) : text(Perception.bandKey(r));
    }

    /** The toggle words / numbers; flips the session setting and calls {@code refresh}. */
    public static VisTextButton toggle(GameSession session, Runnable refresh) {
        VisTextButton button = new VisTextButton(
                text(session.isPerceptionNumbers() ? "perceive_toggle_words" : "perceive_toggle_numbers"));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                session.setPerceptionNumbers(!session.isPerceptionNumbers());
                refresh.run();
            }
        });
        return button;
    }

    /**
     * The card: title, toggle, the club view per group, then who has seen him.
     *
     * @param title the man's name
     */
    public static VisTable card(GameSession session, String title, Player p, Perception.Report report,
            Runnable refresh) {
        boolean numbers = session.isPerceptionNumbers();
        VisTable card = new VisTable(true);
        VisTable top = new VisTable(true);
        VisLabel heading = new VisLabel(LanguageModLoader.format("perceive_card_title", title));
        heading.setColor(UiTheme.color("text.heading"));
        top.add(heading).left();
        top.add(toggle(session, refresh)).left().padLeft(UiTheme.space("lg"));
        card.add(top).left().colspan(2).row();

        VisLabel club = new VisLabel(LanguageModLoader.format("perceive_club_view", summary(report.consensus(), p,
                numbers)));
        club.setWrap(true);
        card.add(club).left().width(UiTheme.size("text.wrap")).colspan(2).row();

        VisTable groups = new VisTable(true);
        boolean keeper = p.getPosition() == Position.GOALKEEPER;
        groups.add(group(report.consensus(), Group.PHYSICAL, "player_physical_attributes", numbers)).top().left();
        groups.add(group(report.consensus(), Group.MENTAL, "player_mental_attributes", numbers)).top().left();
        groups.add(group(report.consensus(), Group.TECHNICAL, "player_technical_attributes", numbers)).top().left();
        if (keeper) {
            groups.add(group(report.consensus(), Group.KEEPER, "player_goalkeeper_attributes", numbers)).top().left();
        }
        card.add(groups).left().colspan(2).row();

        VisLabel who = new VisLabel(text("perceive_observers"));
        who.setColor(UiTheme.color("text.heading"));
        card.add(who).left().colspan(2).row();
        List<Perception.ObserverView> views = report.observers();
        if (views.isEmpty()) {
            card.add(new VisLabel(text("perceive_no_observers"))).left().colspan(2).row();
        }
        for (Perception.ObserverView v : views) {
            VisLabel name = new VisLabel(LanguageModLoader.format("perceive_observer_name",
                    text(roleKey(v.observer().role())), v.observer().name()));
            name.setColor(UiTheme.color("text.secondary"));
            card.add(name).left().top().width(UiTheme.size("col.xwide"));
            String seen = v.minutes() <= 0 ? text("perceive_not_seen")
                    : LanguageModLoader.format("perceive_observer_line", Math.round(v.minutes()),
                            summary(v.ranges(), p, numbers));
            VisLabel line = new VisLabel(seen);
            line.setWrap(true);
            if (v.minutes() <= 0) {
                line.setColor(UiTheme.color("text.muted"));
            }
            card.add(line).left().width(UiTheme.size("text.wrap.narrow")).row();
        }
        return card;
    }

    private static VisTable group(Map<Attr, Range> ranges, Group g, String headingKey, boolean numbers) {
        VisTable t = new VisTable(true);
        VisLabel h = new VisLabel(text(headingKey));
        h.setColor(UiTheme.color("text.heading"));
        t.add(h).left().colspan(2).row();
        for (Map.Entry<Attr, Range> e : ranges.entrySet()) {
            if (e.getKey().group() != g) {
                continue;
            }
            t.add(new VisLabel(attributeLabel(e.getKey()))).left().width(UiTheme.size("col.label"));
            VisLabel v = new VisLabel(attributeText(e.getValue(), numbers));
            if (!e.getValue().seen()) {
                v.setColor(UiTheme.color("text.muted"));
            }
            t.add(v).left().width(UiTheme.size("col.long")).row();
        }
        return t;
    }

    /** @return the attribute's label ({@code attr_long_shots}) with a colon */
    public static String attributeLabel(Attr a) {
        String key = ATTR_KEY + a.id();
        return LanguageModLoader.format("label_with_colon", text(key));
    }

    static final String ATTR_KEY = "attr_";
    static final String ROLE_KEY = "perceive_role_";

    /** @return the bundle key of an observer's role */
    static String roleKey(String role) {
        return ROLE_KEY + role;
    }

    private static boolean seen(Map<Attr, Range> ranges) {
        for (Range r : ranges.values()) {
            if (r.seen()) {
                return true;
            }
        }
        return false;
    }

    /** @return the mean range of a group's seen attributes ("9-14"), or "?" */
    static String groupRange(Map<Attr, Range> ranges, Group g) {
        double lo = 0;
        double hi = 0;
        int n = 0;
        for (Map.Entry<Attr, Range> e : ranges.entrySet()) {
            if (e.getKey().group() == g && e.getValue().seen()) {
                lo += e.getValue().lo();
                hi += e.getValue().hi();
                n++;
            }
        }
        if (n == 0) {
            return text("perceive_band_unknown");
        }
        return LanguageModLoader.format("perceive_range", (int) Math.floor(lo / n), (int) Math.ceil(hi / n));
    }

    private static String text(String key) {
        return LanguageModLoader.getValue(key);
    }
}
