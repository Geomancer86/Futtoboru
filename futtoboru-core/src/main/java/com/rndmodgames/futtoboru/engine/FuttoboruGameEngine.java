package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;

import com.badlogic.gdx.Game;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Game Engine v1
 *  
 *  - TODO WIP:
 *  
 *      - Get an instance of this running with the Game
 *      - Move the CONTINUE GAME Button functionality to this class
 *      - Implement Script Execution by LocalDateTime
 *      -
 *      -
 *  
 * @author Geomancer86
 */
public class FuttoboruGameEngine {

    Futtoboru gameInstance;
    
    public FuttoboruGameEngine(Game parent) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
    }
    
    /**
     * Continue Game should be fully automatic, the Engine will take care of how long the game has to move forward
     *  
     *  - Day
     *  - Hours
     *  - Etc
     */
    public void continueGame() {
        
        System.out.println("ADVANCING THE SIMULATION - 1 DAY");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        // Increment By Required Unit
        gameInstance.getCurrentGame().setGameDate(current.plusDays(1));
    }
}