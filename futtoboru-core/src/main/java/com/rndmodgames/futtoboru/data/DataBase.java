package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * DataBase v1
 * 
 * @author Geomancer86
 */
public class DataBase implements Serializable {

    private static final long serialVersionUID = 3488283540651867795L;

    private Long id;
    private String name;
    private Long number;
    private String version;
    
    //
    public DataBase() {
        
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

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return name + " " + version;
    }
}