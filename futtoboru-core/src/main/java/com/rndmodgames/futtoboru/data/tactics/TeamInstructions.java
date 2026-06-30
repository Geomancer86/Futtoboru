package com.rndmodgames.futtoboru.data.tactics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Extensible team instructions (MVP: defaults only; sim ignores for B1).
 */
public class TeamInstructions implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mentality = "balanced";
    private String width = "normal";
    private Map<String, Object> extras = new HashMap<>();

    public static TeamInstructions createDefault() {
        return new TeamInstructions();
    }

    public String getMentality() {
        return mentality;
    }

    public void setMentality(String mentality) {
        this.mentality = mentality;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras != null ? new HashMap<>(extras) : new HashMap<>();
    }
}
