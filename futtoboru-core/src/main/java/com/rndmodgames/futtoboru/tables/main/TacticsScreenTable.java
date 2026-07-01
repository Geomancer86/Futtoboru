package com.rndmodgames.futtoboru.tables.main;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationSlot;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.engine.tactics.BestElevenSelector;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Tactics screen — formation picker and manual XI slots (Layer B4).
 */
public class TacticsScreenTable extends VisTable {

    private final Futtoboru game;
    private Club displayedClub;
    private final VisSelectBox<String> formationSelectBox = new VisSelectBox<>();
    private final VisTable slotTable = new VisTable(true);
    private final List<VisSelectBox<Player>> slotSelectBoxes = new ArrayList<>();

    public TacticsScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;

        Array<String> formationIds = new Array<>();
        for (FormationTemplate template : FormationRegistry.getAllTemplates()) {
            formationIds.add(template.getId());
        }
        formationSelectBox.setItems(formationIds);

        formationSelectBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event, Actor actor) {
                onFormationChanged();
            }
        });

        VisTextButton bestXiButton = new VisTextButton(LanguageModLoader.getValue("best_xi"));
        bestXiButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event, Actor actor) {
                applyBestEleven();
            }
        });

        VisTextButton clearXiButton = new VisTextButton(LanguageModLoader.getValue("clear_xi"));
        clearXiButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event, Actor actor) {
                clearLineup();
            }
        });

        this.add(new VisLabel(LanguageModLoader.getValue("main_tactics"))).colspan(3).padBottom(8);
        this.row();
        this.add(LanguageModLoader.getValue("formation_label") + ":").padRight(8);
        this.add(formationSelectBox).width(220);
        this.add().expandX();
        this.row().padTop(6);
        this.add(bestXiButton).padRight(4);
        this.add(clearXiButton).padRight(4);
        this.add().expandX();
        this.row().padTop(10);
        this.add(slotTable).colspan(3).growX().fillX();
    }

    public void setCurrentClub(Club club) {
        this.displayedClub = club;
    }

    public void updateDynamicComponents() {
        if (displayedClub == null) {
            slotTable.clear();
            slotTable.add("No club selected.");
            return;
        }

        ClubTacticsSetup setup = displayedClub.getTacticsSetupOrDefault();
        String formationId = setup.getFormationTemplateId() != null
                ? setup.getFormationTemplateId()
                : FormationRegistry.DEFAULT_FORMATION_ID;
        formationSelectBox.setSelected(formationId);
        rebuildSlotRows(setup, FormationRegistry.getRequiredTemplate(formationId));
    }

    private void onFormationChanged() {
        if (displayedClub == null) {
            return;
        }
        String selectedId = formationSelectBox.getSelected();
        ClubTacticsSetup setup = displayedClub.getTacticsSetupOrDefault();
        setup.setFormationTemplateId(selectedId);
        displayedClub.setTacticsSetup(setup);
        rebuildSlotRows(setup, FormationRegistry.getRequiredTemplate(selectedId));
    }

    private void applyBestEleven() {
        if (displayedClub == null || game.getCurrentGame() == null) {
            return;
        }
        java.time.LocalDate asOf = game.getCurrentGame().getGameDate().toLocalDate();
        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(
                displayedClub, formationSelectBox.getSelected(), asOf);
        displayedClub.setTacticsSetup(setup);
        updateDynamicComponents();
    }

    private void clearLineup() {
        if (displayedClub == null) {
            return;
        }
        ClubTacticsSetup setup = displayedClub.getTacticsSetupOrDefault();
        setup.clearSlotAssignments();
        displayedClub.setTacticsSetup(setup);
        updateDynamicComponents();
    }

    private void rebuildSlotRows(ClubTacticsSetup setup, FormationTemplate template) {
        slotTable.clear();
        slotSelectBoxes.clear();

        slotTable.add("Slot").width(80);
        slotTable.add("Role").width(60);
        slotTable.add(LanguageModLoader.getValue("slot_player")).growX();
        slotTable.row();
        slotTable.addSeparator().colspan(3);
        slotTable.row();

        Array<Player> squadItems = buildSquadItems();
        java.time.LocalDate asOf = game.getCurrentGame() != null
                ? game.getCurrentGame().getGameDate().toLocalDate()
                : java.time.LocalDate.now();

        for (FormationSlot slot : template.getSlots()) {
            VisSelectBox<Player> playerBox = new VisSelectBox<>();
            playerBox.setItems(squadItems);
            Long assignedId = setup.getSlotAssignments().get(slot.getSlotId());
            if (assignedId != null) {
                for (Player player : squadItems) {
                    if (player != null && assignedId.equals(player.getId())) {
                        playerBox.setSelected(player);
                        break;
                    }
                }
            }

            playerBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event, Actor actor) {
                    assignPlayerToSlot(slot.getSlotId(), playerBox.getSelected(), setup, template, asOf);
                }
            });

            slotSelectBoxes.add(playerBox);
            slotTable.add(slot.getSlotId()).width(80);
            slotTable.add(slot.getRole() != null ? slot.getRole().name() : "").width(60);
            slotTable.add(playerBox).growX().fillX();
            slotTable.row();
        }
    }

    private void assignPlayerToSlot(String slotId, Player selected, ClubTacticsSetup setup,
            FormationTemplate template, java.time.LocalDate asOf) {
        if (displayedClub == null || slotId == null) {
            return;
        }
        if (selected != null && selected.getId() != null) {
            for (FormationSlot slot : template.getSlots()) {
                Long otherId = setup.getSlotAssignments().get(slot.getSlotId());
                if (selected.getId().equals(otherId) && !slotId.equals(slot.getSlotId())) {
                    setup.assignPlayer(slot.getSlotId(), null);
                }
            }
            setup.assignPlayer(slotId, selected.getId());
        } else {
            setup.assignPlayer(slotId, null);
        }
        displayedClub.setTacticsSetup(setup);
        rebuildSlotRows(setup, template);
    }

    private Array<Player> buildSquadItems() {
        Array<Player> items = new Array<>();
        items.add(null);
        if (displayedClub != null && displayedClub.getPlayers() != null) {
            for (Player player : displayedClub.getPlayers()) {
                if (player != null) {
                    items.add(player);
                }
            }
        }
        return items;
    }
}
