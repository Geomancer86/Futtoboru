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
}