package com.rndmodgames.futtoboru.system.generators;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Player Generator v1
 * 
 *  - Generates a Person plus the required Player Attributes
 * 
 * @author Geomancer86
 */
public class PlayerGenerator {

    Futtoboru game;
    SaveGame currentGame;
    
    public PlayerGenerator(Futtoboru parent) {
        
        //
        this.game = (Futtoboru) parent;
//        this.currentGame = this.game.getCurrentGame();
    }
    
    /**
     * 
     */
    public Player generateRandomPlayer(Person person) {
        
        Player player = new Player();
        
        //
        player.setPerson(person);
        
        return player;
    }
}