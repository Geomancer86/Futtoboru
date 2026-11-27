package com.rndmodgames.futtoboru.system.generators;

import java.time.LocalDateTime;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
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
    private PlayerAttributeGenerator attributeGenerator;
    
    public PlayerGenerator(Futtoboru parent) {
        
        //
        this.game = (Futtoboru) parent;
        this.attributeGenerator = new PlayerAttributeGenerator();
//        this.currentGame = this.game.getCurrentGame();
    }
    
    /**
     * Generate a random player with attributes
     */
    public Player generateRandomPlayer(Person person) {
        
        Player player = new Player();
        
        //
        player.setPerson(person);
        
        // Generate attributes if game is available
        if (game != null && game.getCurrentGame() != null) {
            attributeGenerator.generatePlayerAttributes(player, person, game.getCurrentGame().getGameDate(), attributeRandom(person));
        } else {
            // Fallback: use current date
            attributeGenerator.generatePlayerAttributes(player, person, LocalDateTime.now(), attributeRandom(person));
        }
        
        return player;
    }
    
    /**
     * Generate attributes for an existing player (used when loading games)
     */
    public void generateAttributesForPlayer(Player player, LocalDateTime currentDate) {
        if (player != null && player.getPerson() != null) {
            attributeGenerator.generatePlayerAttributes(player, player.getPerson(), currentDate, attributeRandom(player.getPerson()));
        }
    }
    
    /** Per-person attribute stream of the game's random source (P2-0). */
    private RandomSource attributeRandom(Person person) {
        return Futtoboru.randomOf(game).fork("attributes:" + person.getId());
    }
}