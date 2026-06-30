package com.rndmodgames.futtoboru.data.tactics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Static formation definition (registry / mod file).
 */
public class FormationTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private Integer eraFromYear;
    private Integer eraToYear;
    private List<FormationSlot> slots = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getEraFromYear() {
        return eraFromYear;
    }

    public void setEraFromYear(Integer eraFromYear) {
        this.eraFromYear = eraFromYear;
    }

    public Integer getEraToYear() {
        return eraToYear;
    }

    public void setEraToYear(Integer eraToYear) {
        this.eraToYear = eraToYear;
    }

    public List<FormationSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<FormationSlot> slots) {
        this.slots = slots != null ? slots : new ArrayList<>();
    }

    public int slotCount() {
        return slots != null ? slots.size() : 0;
    }
}
