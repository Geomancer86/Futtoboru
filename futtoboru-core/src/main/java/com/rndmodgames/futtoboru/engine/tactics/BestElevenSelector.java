package com.rndmodgames.futtoboru.engine.tactics;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationSlot;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;

/**
 * Picks starting XI by slot-weighted attribute scores (MVP-1).
 */
public final class BestElevenSelector {

    private BestElevenSelector() {
    }

    public static ClubTacticsSetup selectBestEleven(Club club) {
        return selectBestEleven(club, FormationRegistry.DEFAULT_FORMATION_ID);
    }

    public static ClubTacticsSetup selectBestEleven(Club club, java.time.LocalDate asOf) {
        return selectBestEleven(club, FormationRegistry.DEFAULT_FORMATION_ID, asOf);
    }

    public static ClubTacticsSetup selectBestEleven(Club club, String formationId) {
        return selectBestEleven(club, formationId, java.time.LocalDate.now());
    }

    public static ClubTacticsSetup selectBestEleven(Club club, String formationId, java.time.LocalDate asOf) {
        return fillLineup(club, formationId, true, asOf);
    }

    public static ClubTacticsSetup selectWorstEleven(Club club, String formationId) {
        return selectWorstEleven(club, formationId, java.time.LocalDate.now());
    }

    public static ClubTacticsSetup selectWorstEleven(Club club, String formationId, java.time.LocalDate asOf) {
        return fillLineup(club, formationId, false, asOf);
    }

    private static ClubTacticsSetup fillLineup(Club club, String formationId, boolean pickBest,
            java.time.LocalDate asOf) {
        FormationTemplate template = FormationRegistry.getRequiredTemplate(formationId);
        ClubTacticsSetup setup = new ClubTacticsSetup();
        setup.setFormationTemplateId(formationId);
        setup.clearSlotAssignments();

        if (club == null || club.getPlayers() == null || club.getPlayers().isEmpty()) {
            return setup;
        }

        Set<Long> used = new HashSet<>();
        for (FormationSlot slot : template.getSlots()) {
            Player chosen = null;
            double chosenScore = pickBest ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

            for (Player player : club.getPlayers()) {
                if (player == null || player.getId() == null || used.contains(player.getId())) {
                    continue;
                }
                if (!PlayerConditionService.isAvailableForLineup(player, asOf)) {
                    continue;
                }
                double score = FormationRegistry.scorePlayerForSlot(player, slot);
                if (pickBest) {
                    if (score > chosenScore) {
                        chosenScore = score;
                        chosen = player;
                    }
                } else if (score < chosenScore) {
                    chosenScore = score;
                    chosen = player;
                }
            }

            if (chosen != null) {
                setup.assignPlayer(slot.getSlotId(), chosen.getId());
                used.add(chosen.getId());
            }
        }
        return setup;
    }

    /**
     * Overall squad strength sum for ordering players (tests / fallback).
     */
    public static double overallPlayerScore(Player player) {
        if (player == null) {
            return 0.0;
        }
        FormationTemplate template = FormationRegistry.getDefaultTemplate();
        double total = 0.0;
        for (FormationSlot slot : template.getSlots()) {
            total += FormationRegistry.scorePlayerForSlot(player, slot);
        }
        return total / Math.max(1, template.getSlots().size());
    }

    public static Comparator<Player> byOverallScoreDescending() {
        return (a, b) -> Double.compare(overallPlayerScore(b), overallPlayerScore(a));
    }
}
