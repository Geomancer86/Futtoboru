package com.rndmodgames.localization;

import java.io.Serializable;

/**
 * Language v1
 * 
 * @author Geomancer86
 */
public class Language implements Serializable {

	private static final long serialVersionUID = 4265125606395245302L;

	private String id;
	private String name;
	private String description;
	
	// No-Arg
	public Language() {
		
	}

	public Language(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return name != null ? name : "no_bundled_name";
	}
}