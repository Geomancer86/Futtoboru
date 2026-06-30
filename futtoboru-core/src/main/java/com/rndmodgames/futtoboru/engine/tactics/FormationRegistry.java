package com.rndmodgames.futtoboru.engine.tactics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.FormationSlot;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.data.tactics.PlayerAttributeKeys;
import com.rndmodgames.futtoboru.data.tactics.SlotRole;

/**
 * In-memory formation registry (1888 England templates). Mod loader interface reserved for TAC-6.
 */
public final class FormationRegistry {

    public static final String DEFAULT_FORMATION_ID = "2-3-5-1888";
    public static final int REQUIRED_LINEUP_SIZE = 11;

    private static final Map<String, FormationTemplate> TEMPLATES = buildTemplates();

    private FormationRegistry() {
    }

    public static FormationTemplate getTemplate(String id) {
        if (id == null) {
            return TEMPLATES.get(DEFAULT_FORMATION_ID);
        }
        return TEMPLATES.get(id);
    }

    public static FormationTemplate getRequiredTemplate(String id) {
        FormationTemplate template = getTemplate(id);
        if (template == null) {
            throw new IllegalArgumentException("Unknown formation template: " + id);
        }
        return template;
    }

    public static FormationTemplate getDefaultTemplate() {
        return TEMPLATES.get(DEFAULT_FORMATION_ID);
    }

    public static List<FormationTemplate> getAllTemplates() {
        return List.copyOf(TEMPLATES.values());
    }

    public static double scorePlayerForSlot(Player player, FormationSlot slot) {
        if (player == null || slot == null || slot.getSimWeights() == null || slot.getSimWeights().isEmpty()) {
            return 0.0;
        }
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (Map.Entry<String, Double> entry : slot.getSimWeights().entrySet()) {
            double weight = entry.getValue() != null ? entry.getValue() : 0.0;
            if (weight <= 0) {
                continue;
            }
            weightedSum += PlayerAttributeAccessor.getAttribute(player, entry.getKey()) * weight;
            weightTotal += weight;
        }
        return weightTotal > 0 ? weightedSum / weightTotal : 0.0;
    }

    private static Map<String, FormationTemplate> buildTemplates() {
        Map<String, FormationTemplate> map = new LinkedHashMap<>();
        map.put("2-3-5-1888", buildTwoThreeFive());
        map.put("2-2-6-1888", buildTwoTwoSix());
        map.put("3-2-5-1888", buildThreeTwoFive());
        return Collections.unmodifiableMap(map);
    }

    private static FormationTemplate buildTwoThreeFive() {
        FormationTemplate template = baseTemplate("2-3-5-1888", "Pyramid 2-3-5", 1888, 1910);
        template.getSlots().add(gk("gk"));
        template.getSlots().add(def("rb"));
        template.getSlots().add(def("lb"));
        template.getSlots().add(mid("rhb"));
        template.getSlots().add(mid("chb"));
        template.getSlots().add(mid("lhb"));
        template.getSlots().add(fwd("rw"));
        template.getSlots().add(fwd("ir"));
        template.getSlots().add(fwd("cf"));
        template.getSlots().add(fwd("il"));
        template.getSlots().add(fwd("lw"));
        return template;
    }

    private static FormationTemplate buildTwoTwoSix() {
        FormationTemplate template = baseTemplate("2-2-6-1888", "Double centre-half 2-2-6", 1888, 1910);
        template.getSlots().add(gk("gk"));
        template.getSlots().add(def("rb"));
        template.getSlots().add(def("lb"));
        template.getSlots().add(mid("rchb"));
        template.getSlots().add(mid("lchb"));
        template.getSlots().add(fwd("or"));
        template.getSlots().add(fwd("ir"));
        template.getSlots().add(fwd("cr"));
        template.getSlots().add(fwd("cl"));
        template.getSlots().add(fwd("il"));
        template.getSlots().add(fwd("ol"));
        return template;
    }

    private static FormationTemplate buildThreeTwoFive() {
        FormationTemplate template = baseTemplate("3-2-5-1888", "Three half-back 3-2-5", 1888, 1910);
        template.getSlots().add(gk("gk"));
        template.getSlots().add(def("rb"));
        template.getSlots().add(def("cb"));
        template.getSlots().add(def("lb"));
        template.getSlots().add(mid("rh"));
        template.getSlots().add(mid("lh"));
        template.getSlots().add(fwd("rw"));
        template.getSlots().add(fwd("ir"));
        template.getSlots().add(fwd("cf"));
        template.getSlots().add(fwd("il"));
        template.getSlots().add(fwd("lw"));
        return template;
    }

    private static FormationTemplate baseTemplate(String id, String name, int fromYear, int toYear) {
        FormationTemplate template = new FormationTemplate();
        template.setId(id);
        template.setDisplayName(name);
        template.setEraFromYear(fromYear);
        template.setEraToYear(toYear);
        return template;
    }

    private static FormationSlot gk(String slotId) {
        Map<String, Double> weights = weights(
                PlayerAttributeKeys.SHOT_STOPPING, 3.0,
                PlayerAttributeKeys.AREA_CONTROL, 2.0,
                PlayerAttributeKeys.POSITIONING, 2.0,
                PlayerAttributeKeys.DETERMINATION, 1.0);
        return new FormationSlot(slotId, SlotRole.GK, weights);
    }

    private static FormationSlot def(String slotId) {
        Map<String, Double> weights = weights(
                PlayerAttributeKeys.TACKLING, 2.5,
                PlayerAttributeKeys.MARKING, 2.0,
                PlayerAttributeKeys.STRENGTH, 2.0,
                PlayerAttributeKeys.POSITIONING, 1.5);
        return new FormationSlot(slotId, SlotRole.DEF, weights);
    }

    private static FormationSlot mid(String slotId) {
        Map<String, Double> weights = weights(
                PlayerAttributeKeys.PASSING, 2.0,
                PlayerAttributeKeys.TACKLING, 1.5,
                PlayerAttributeKeys.STAMINA, 2.0,
                PlayerAttributeKeys.TEAMWORK, 1.5);
        return new FormationSlot(slotId, SlotRole.MID, weights);
    }

    private static FormationSlot fwd(String slotId) {
        Map<String, Double> weights = weights(
                PlayerAttributeKeys.KICKING, 2.5,
                PlayerAttributeKeys.SPEED, 2.0,
                PlayerAttributeKeys.ACCELERATION, 2.0,
                PlayerAttributeKeys.HEADING, 1.0);
        return new FormationSlot(slotId, SlotRole.FWD, weights);
    }

    private static Map<String, Double> weights(Object... keysAndWeights) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndWeights.length; i += 2) {
            map.put((String) keysAndWeights[i], ((Number) keysAndWeights[i + 1]).doubleValue());
        }
        return map;
    }
}
