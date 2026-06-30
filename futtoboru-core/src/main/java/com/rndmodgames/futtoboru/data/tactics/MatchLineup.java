package com.rndmodgames.futtoboru.data.tactics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of the XI used for one match simulation.
 */
public class MatchLineup implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long matchId;
    private Long clubId;
    private String formationTemplateId;
    private List<Long> playerIdsInSlotOrder = new ArrayList<>();
    private TeamInstructions instructionsSnapshot = TeamInstructions.createDefault();

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public String getFormationTemplateId() {
        return formationTemplateId;
    }

    public void setFormationTemplateId(String formationTemplateId) {
        this.formationTemplateId = formationTemplateId;
    }

    public List<Long> getPlayerIdsInSlotOrder() {
        return playerIdsInSlotOrder;
    }

    public void setPlayerIdsInSlotOrder(List<Long> playerIdsInSlotOrder) {
        this.playerIdsInSlotOrder = playerIdsInSlotOrder != null ? new ArrayList<>(playerIdsInSlotOrder) : new ArrayList<>();
    }

    public TeamInstructions getInstructionsSnapshot() {
        return instructionsSnapshot;
    }

    public void setInstructionsSnapshot(TeamInstructions instructionsSnapshot) {
        this.instructionsSnapshot = instructionsSnapshot != null ? instructionsSnapshot : TeamInstructions.createDefault();
    }

    public int size() {
        return playerIdsInSlotOrder != null ? playerIdsInSlotOrder.size() : 0;
    }
}
