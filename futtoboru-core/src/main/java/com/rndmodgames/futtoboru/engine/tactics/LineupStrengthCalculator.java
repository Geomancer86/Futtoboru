package com.rndmodgames.futtoboru.engine.tactics;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.FormationSlot;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;

/**
 * Computes team strength from a {@link MatchLineup} and formation slot weights (MVP-3).
 */
public final class LineupStrengthCalculator {

    private LineupStrengthCalculator() {
    }

    public static double calculate(MatchLineup lineup, Club club) {
        if (lineup == null || club == null) {
            return 50.0;
        }
        FormationTemplate template = FormationRegistry.getRequiredTemplate(lineup.getFormationTemplateId());
        if (template.getSlots().size() != FormationRegistry.REQUIRED_LINEUP_SIZE) {
            return 50.0;
        }
        if (lineup.getPlayerIdsInSlotOrder() == null
                || lineup.getPlayerIdsInSlotOrder().size() != FormationRegistry.REQUIRED_LINEUP_SIZE) {
            return 50.0;
        }

        double total = 0.0;
        int counted = 0;
        for (int i = 0; i < FormationRegistry.REQUIRED_LINEUP_SIZE; i++) {
            FormationSlot slot = template.getSlots().get(i);
            Long playerId = lineup.getPlayerIdsInSlotOrder().get(i);
            Player player = findPlayer(club, playerId);
            if (player == null) {
                continue;
            }
            total += FormationRegistry.scorePlayerForSlot(player, slot)
                    * PlayerConditionService.fitnessStrengthFactor(player)
                    * PlayerConditionService.sharpnessStrengthFactor(player);
            counted++;
        }
        if (counted == 0) {
            return 50.0;
        }
        return total / counted;
    }

    static Player findPlayer(Club club, Long playerId) {
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
