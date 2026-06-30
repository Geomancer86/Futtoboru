package com.rndmodgames.futtoboru.data.tactics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;

/**
 * Per-club tactics persisted in save (formation + XI slot assignments).
 */
public class ClubTacticsSetup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String formationTemplateId = FormationRegistry.DEFAULT_FORMATION_ID;
    private Map<String, Long> slotAssignments = new LinkedHashMap<>();
    private TeamInstructions teamInstructions = TeamInstructions.createDefault();

    public static ClubTacticsSetup createDefault() {
        ClubTacticsSetup setup = new ClubTacticsSetup();
        setup.setFormationTemplateId(FormationRegistry.DEFAULT_FORMATION_ID);
        setup.setTeamInstructions(TeamInstructions.createDefault());
        return setup;
    }

    public String getFormationTemplateId() {
        return formationTemplateId;
    }

    public void setFormationTemplateId(String formationTemplateId) {
        this.formationTemplateId = formationTemplateId;
    }

    public Map<String, Long> getSlotAssignments() {
        return slotAssignments;
    }

    public void setSlotAssignments(Map<String, Long> slotAssignments) {
        this.slotAssignments = slotAssignments != null ? new LinkedHashMap<>(slotAssignments) : new LinkedHashMap<>();
    }

    public TeamInstructions getTeamInstructions() {
        return teamInstructions;
    }

    public void setTeamInstructions(TeamInstructions teamInstructions) {
        this.teamInstructions = teamInstructions != null ? teamInstructions : TeamInstructions.createDefault();
    }

    public void assignPlayer(String slotId, Long playerId) {
        if (slotId == null) {
            return;
        }
        if (playerId == null) {
            slotAssignments.remove(slotId);
        } else {
            slotAssignments.put(slotId, playerId);
        }
    }

    public void clearSlotAssignments() {
        slotAssignments.clear();
    }

    public List<Long> orderedPlayerIds(FormationTemplate template) {
        List<Long> ids = new ArrayList<>();
        if (template == null || template.getSlots() == null) {
            return ids;
        }
        for (FormationSlot slot : template.getSlots()) {
            Long playerId = slotAssignments.get(slot.getSlotId());
            if (playerId != null) {
                ids.add(playerId);
            }
        }
        return ids;
    }

    public boolean hasCompleteValidLineup(Club club, FormationTemplate template) {
        if (club == null || template == null || template.getSlots() == null) {
            return false;
        }
        if (template.getSlots().size() != FormationRegistry.REQUIRED_LINEUP_SIZE) {
            return false;
        }
        Set<Long> seen = new HashSet<>();
        for (FormationSlot slot : template.getSlots()) {
            Long playerId = slotAssignments.get(slot.getSlotId());
            if (playerId == null || !seen.add(playerId)) {
                return false;
            }
            if (findPlayer(club, playerId) == null) {
                return false;
            }
        }
        return true;
    }

    private static Player findPlayer(Club club, Long playerId) {
        if (club.getPlayers() == null || playerId == null) {
            return null;
        }
        for (Player player : club.getPlayers()) {
            if (player != null && playerId.equals(player.getId())) {
                return player;
            }
        }
        return null;
    }
}
