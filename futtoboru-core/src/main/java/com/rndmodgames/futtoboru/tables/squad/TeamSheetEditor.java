package com.rndmodgames.futtoboru.tables.squad;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.team.TeamRatings;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-01: one club's team sheet, editable (the player's club: Squad screen tab and Match Preview) or read-only (any
 * other club, or a sheet as played). Shape, style and captain at the top, an Auto-pick button, then one row per
 * place: the place, the man in it (a select box when editable: picking a man who is in another place swaps the two),
 * and his own position (abbreviated, with a tooltip; orange when he plays out of position). Warnings under the rows:
 * empty places, playing short, men out of position, no captain.
 */
public class TeamSheetEditor extends VisTable {

    /** A man (or "nobody") in a select box. */
    static final class Choice {
        final Long id;
        final String label;

        Choice(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Club club;
    private TeamSheet sheet;
    private boolean editable;
    private Runnable onChange;

    public TeamSheetEditor() {
        super(true);
    }

    /**
     * Shows {@code sheet} of {@code club}. Editing changes {@code sheet} itself (pass the club's standing sheet).
     *
     * @param onChange run after every edit (may be null)
     */
    public void show(Club club, TeamSheet sheet, boolean editable, Runnable onChange) {
        this.club = club;
        this.sheet = sheet;
        this.editable = editable;
        this.onChange = onChange;
        rebuild();
    }

    /** After an edit: rebuilt on the next frame, so the select box that fired is not torn down in its own event. */
    private void changed() {
        Runnable refresh = () -> {
            rebuild();
            if (onChange != null) {
                onChange.run();
            }
        };
        if (com.badlogic.gdx.Gdx.app != null) {
            com.badlogic.gdx.Gdx.app.postRunnable(refresh);
        } else {
            refresh.run();
        }
    }

    private void rebuild() {
        clearChildren();
        top().left();
        if (club == null || sheet == null) {
            add(new VisLabel(LanguageModLoader.getValue("no_registered_players_at_club"))).left();
            return;
        }
        Map<Long, Player> squad = TeamSheets.squadById(club);

        // shape, style, captain
        VisTable head = new VisTable(true);
        head.add(UiTooltips.tip(new VisLabel(LanguageModLoader.getValue("team_sheet_shape")),
                sheet.getShape().tipKey())).left().width(UiTheme.size("col.long"));
        head.add(editable ? shapeBox() : UiTooltips.tip(new VisLabel(sheet.getShape().label()),
                sheet.getShape().tipKey())).left().width(UiTheme.size("col.wide"));
        head.row();
        head.add(UiTooltips.tip(new VisLabel(LanguageModLoader.getValue("team_sheet_style")),
                sheet.getStyle().tipKey())).left().width(UiTheme.size("col.long"));
        head.add(editable ? styleBox() : UiTooltips.tip(new VisLabel(LanguageModLoader.getValue(
                sheet.getStyle().labelKey())), sheet.getStyle().tipKey())).left().width(UiTheme.size("col.wide"));
        head.row();
        head.add(new VisLabel(LanguageModLoader.getValue("team_sheet_captain"))).left().width(UiTheme.size("col.long"));
        Player captain = sheet.getCaptainId() != null ? squad.get(sheet.getCaptainId()) : null;
        head.add(editable ? captainBox(squad) : new VisLabel(captain != null ? name(captain) : "-")).left()
                .width(UiTheme.size("col.wide"));
        head.row();
        if (editable) {
            VisTextButton auto = new VisTextButton(LanguageModLoader.getValue("team_sheet_auto_pick"));
            UiTooltips.tip(auto, "tip_team_sheet_auto_pick");
            auto.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    TeamSheets.autoPick(club);
                    sheet = TeamSheets.standing(club);
                    TeamSheetEditor.this.changed();
                }
            });
            head.add(auto).colspan(2).left().padTop(UiTheme.space("xs"));
            head.row();
        }
        add(head).left();
        row();

        // the places
        VisTable places = new VisTable(true);
        places.add(new VisLabel(LanguageModLoader.getValue("team_sheet_place"))).left().width(UiTheme.size("col.long"));
        places.add(new VisLabel(LanguageModLoader.getValue("team_sheet_man"))).left().width(UiTheme.size("col.wide"));
        places.add(UiTooltips.header("team_sheet_pos", "tip_team_sheet_pos")).left().width(UiTheme.size("col.short"));
        places.add(UiTooltips.header("team_sheet_captain_mark", "tip_team_sheet_captain_mark")).left()
                .width(UiTheme.size("col.short"));
        places.row();
        places.addSeparator().colspan(4);
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Position slot = sheet.position(s);
            Player p = sheet.getPlayerId(s) != null ? squad.get(sheet.getPlayerId(s)) : null;
            places.add(new VisLabel(LanguageModLoader.getValue(slot.labelKey()))).left();
            if (editable) {
                places.add(manBox(s, squad)).left().width(UiTheme.size("col.wide"));
            } else {
                VisLabel man = new VisLabel(p != null ? name(p) : "-");
                if (p == null) {
                    man.setColor(UiTheme.color("text.muted"));
                }
                places.add(man).left();
            }
            places.add(ownPosition(p, slot)).left();
            boolean isCaptain = p != null && p.getId().equals(sheet.getCaptainId());
            places.add(isCaptain ? UiTooltips.tip(new VisLabel(LanguageModLoader.getValue("team_sheet_captain_mark")),
                    "tip_team_sheet_captain_mark") : new VisLabel("")).left();
            places.row();
        }
        add(places).left().padTop(UiTheme.space("sm"));
        row();

        // warnings
        TeamSheets.Check check = TeamSheets.check(sheet, club);
        VisTable warnings = new VisTable(true);
        if (check.playsShort()) {
            warning(warnings, LanguageModLoader.format("team_sheet_short", check.fitMen()), "state.critical");
        } else if (check.hasEmptyPlaces()) {
            warning(warnings, LanguageModLoader.format("team_sheet_empty_places", check.filled()), "state.warning");
        }
        if (!check.outOfPosition().isEmpty()) {
            warning(warnings, LanguageModLoader.format("team_sheet_out_of_position_count",
                    check.outOfPosition().size()), "state.warning");
        }
        if (editable && check.noCaptain()) {
            warning(warnings, LanguageModLoader.getValue("team_sheet_no_captain"), "state.warning");
        }
        add(warnings).left().padTop(UiTheme.space("xs"));
    }

    private static void warning(VisTable table, String text, String colour) {
        VisLabel label = new VisLabel(text);
        label.setColor(UiTheme.color(colour));
        label.setWrap(true);
        table.add(label).left().width(UiTheme.size("panel.list"));
        table.row();
    }

    /** His own position, abbreviated with a tooltip; orange (and saying so) when it is not the place's. */
    private static Actor ownPosition(Player p, Position slot) {
        if (p == null || p.getPosition() == null) {
            return new VisLabel("");
        }
        VisLabel label = new VisLabel(LanguageModLoader.getValue(abbreviationKey(p.getPosition())));
        String own = LanguageModLoader.getValue(p.getPosition().labelKey());
        if (TeamRatings.outOfPosition(p, slot)) {
            label.setColor(UiTheme.color("state.warning"));
            UiTooltips.tipText(label, LanguageModLoader.format("team_sheet_out_of_position", own));
        } else {
            UiTooltips.tipText(label, own);
        }
        return label;
    }

    /** {@code position_abbr_centre_forward} */
    static String abbreviationKey(Position p) {
        return "position_abbr_" + p.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String name(Player p) {
        if (p.getPerson() == null) {
            return String.valueOf(p.getId());
        }
        String first = p.getPerson().getName() == null ? "" : p.getPerson().getName();
        String last = p.getPerson().getLastname() == null ? "" : p.getPerson().getLastname();
        return (first + " " + last).trim();
    }

    private VisSelectBox<TeamShape> shapeBox() {
        VisSelectBox<TeamShape> box = new VisSelectBox<TeamShape>() {
            @Override
            protected String toString(TeamShape shape) {
                return shape.label();
            }
        };
        box.setItems(TeamShape.values());
        box.setSelected(sheet.getShape());
        UiTooltips.tip(box, sheet.getShape().tipKey());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sheet.reshape(box.getSelected());
                TeamSheetEditor.this.changed();
            }
        });
        return box;
    }

    private VisSelectBox<ClubStyle> styleBox() {
        VisSelectBox<ClubStyle> box = new VisSelectBox<ClubStyle>() {
            @Override
            protected String toString(ClubStyle style) {
                return LanguageModLoader.getValue(style.labelKey());
            }
        };
        box.setItems(ClubStyle.values());
        box.setSelected(sheet.getStyle());
        UiTooltips.tip(box, sheet.getStyle().tipKey());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sheet.setStyle(box.getSelected());
                TeamSheetEditor.this.changed();
            }
        });
        return box;
    }

    private VisSelectBox<Choice> captainBox(Map<Long, Player> squad) {
        Array<Choice> items = new Array<>();
        Choice none = new Choice(null, "-");
        items.add(none);
        Choice selected = none;
        for (Long id : sheet.selectedIds()) {
            Player p = squad.get(id);
            if (p != null) {
                Choice c = new Choice(id, name(p));
                items.add(c);
                if (id.equals(sheet.getCaptainId())) {
                    selected = c;
                }
            }
        }
        VisSelectBox<Choice> box = new VisSelectBox<>();
        box.setItems(items);
        box.setSelected(selected);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sheet.setCaptainId(box.getSelected().id);
                TeamSheetEditor.this.changed();
            }
        });
        return box;
    }

    /** Every squad man, by line and position, then name; "-" for an empty place. */
    private VisSelectBox<Choice> manBox(int slot, Map<Long, Player> squad) {
        List<Player> men = new ArrayList<>(TeamSheets.available(club));
        men.sort(Comparator.comparing((Player p) -> p.getPosition() == null ? Integer.MAX_VALUE
                : p.getPosition().ordinal()).thenComparing(TeamSheetEditor::name).thenComparing(Player::getId));
        Array<Choice> items = new Array<>();
        Choice none = new Choice(null, "-");
        items.add(none);
        Choice selected = none;
        for (Player p : men) {
            String pos = p.getPosition() != null
                    ? " (" + LanguageModLoader.getValue(abbreviationKey(p.getPosition())) + ")" : "";
            Choice c = new Choice(p.getId(), name(p) + pos);
            items.add(c);
            if (p.getId().equals(sheet.getPlayerId(slot))) {
                selected = c;
            }
        }
        VisSelectBox<Choice> box = new VisSelectBox<>();
        box.setItems(items);
        box.setSelected(selected);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sheet.setPlayerId(slot, box.getSelected().id);
                TeamSheetEditor.this.changed();
            }
        });
        return box;
    }
}
