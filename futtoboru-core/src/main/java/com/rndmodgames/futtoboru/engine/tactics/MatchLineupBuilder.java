package com.rndmodgames.futtoboru.engine.tactics;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;

/**
 * Builds {@link MatchLineup} snapshots for simulation (TAC-3).
 */
public final class MatchLineupBuilder {

    private MatchLineupBuilder() {
    }

    public static MatchLineup buildForClub(Club club, Match match) {
        if (club == null) {
            return emptyLineup(null, null);
        }

        ClubTacticsSetup setup = club.getTacticsSetupOrDefault();
        FormationTemplate template = FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId());

        if (!setup.hasCompleteValidLineup(club, template)) {
            setup = BestElevenSelector.selectBestEleven(club, setup.getFormationTemplateId());
            club.setTacticsSetup(setup);
        }

        MatchLineup lineup = new MatchLineup();
        lineup.setClubId(club.getId());
        lineup.setMatchId(match != null ? match.getId() : null);
        lineup.setFormationTemplateId(setup.getFormationTemplateId());
        lineup.setPlayerIdsInSlotOrder(setup.orderedPlayerIds(template));
        lineup.setInstructionsSnapshot(setup.getTeamInstructions());
        return lineup;
    }

    public static MatchLineup fromPlayerIds(Club club, List<Long> playerIdsInSlotOrder, String formationId) {
        MatchLineup lineup = new MatchLineup();
        if (club != null) {
            lineup.setClubId(club.getId());
        }
        lineup.setFormationTemplateId(
                formationId != null ? formationId : FormationRegistry.DEFAULT_FORMATION_ID);
        if (playerIdsInSlotOrder != null) {
            lineup.setPlayerIdsInSlotOrder(new ArrayList<>(playerIdsInSlotOrder));
        }
        return lineup;
    }

    public static List<PlayerRef> playersInLineup(MatchLineup lineup, Club club) {
        List<PlayerRef> result = new ArrayList<>();
        if (lineup == null || club == null || lineup.getPlayerIdsInSlotOrder() == null) {
            return result;
        }
        FormationTemplate template = FormationRegistry.getRequiredTemplate(lineup.getFormationTemplateId());
        for (int i = 0; i < lineup.getPlayerIdsInSlotOrder().size(); i++) {
            Long playerId = lineup.getPlayerIdsInSlotOrder().get(i);
            com.rndmodgames.futtoboru.data.Player player = LineupStrengthCalculator.findPlayer(club, playerId);
            SlotRoleRef role = SlotRoleRef.MID;
            if (i < template.getSlots().size() && template.getSlots().get(i).getRole() != null) {
                role = SlotRoleRef.valueOf(template.getSlots().get(i).getRole().name());
            }
            if (player != null) {
                result.add(new PlayerRef(player, role));
            }
        }
        return result;
    }

    private static MatchLineup emptyLineup(Long clubId, Long matchId) {
        MatchLineup lineup = new MatchLineup();
        lineup.setClubId(clubId);
        lineup.setMatchId(matchId);
        lineup.setFormationTemplateId(FormationRegistry.DEFAULT_FORMATION_ID);
        return lineup;
    }

    public enum SlotRoleRef {
        GK, DEF, MID, FWD
    }

    public static final class PlayerRef {
        public final com.rndmodgames.futtoboru.data.Player player;
        public final SlotRoleRef role;

        PlayerRef(com.rndmodgames.futtoboru.data.Player player, SlotRoleRef role) {
            this.player = player;
            this.role = role;
        }
    }
}
