package com.rndmodgames.futtoboru.data.tactics;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One position in an 11-slot formation template.
 */
public class FormationSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    private String slotId;
    private SlotRole role;
    private Map<String, Double> simWeights = new LinkedHashMap<>();

    public FormationSlot() {
    }

    public FormationSlot(String slotId, SlotRole role, Map<String, Double> simWeights) {
        this.slotId = slotId;
        this.role = role;
        if (simWeights != null) {
            this.simWeights = new LinkedHashMap<>(simWeights);
        }
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public SlotRole getRole() {
        return role;
    }

    public void setRole(SlotRole role) {
        this.role = role;
    }

    public Map<String, Double> getSimWeights() {
        return simWeights;
    }

    public void setSimWeights(Map<String, Double> simWeights) {
        this.simWeights = simWeights != null ? new LinkedHashMap<>(simWeights) : new LinkedHashMap<>();
    }
}
