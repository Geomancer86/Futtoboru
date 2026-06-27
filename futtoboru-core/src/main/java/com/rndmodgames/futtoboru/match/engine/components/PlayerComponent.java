package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.rndmodgames.futtoboru.data.Player;

/**
 * PlayerComponent - Player-specific data
 */
public class PlayerComponent implements Component, Poolable {
    public Player player;
    public boolean isHomeTeam = true;
    public float speed = 5.0f; // meters per second
    public float maxSpeed = 8.0f;
    
    @Override
    public void reset() {
        player = null;
        isHomeTeam = true;
        speed = 5.0f;
        maxSpeed = 8.0f;
    }
}
