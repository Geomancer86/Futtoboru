package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Continent v1
 * 
 * @author Geomancer86
 */
public class Continent implements Serializable {

    private static final long serialVersionUID = -7414986916668198644L;

    private Long id;


    // stable data id from the game data (P2-4, D11); null for objects made during play
    private String dataId;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
