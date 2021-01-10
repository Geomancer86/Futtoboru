package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * States v1
 * 
 * @author Geomancer86
 */
public class State implements Serializable {

    private static final long serialVersionUID = -7910668395454100878L;

    private Long id;
    private String name;
    
    //
    public State() {
        
    }

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
