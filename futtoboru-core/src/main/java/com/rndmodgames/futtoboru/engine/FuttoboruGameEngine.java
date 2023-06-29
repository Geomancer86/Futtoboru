package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;

import com.badlogic.gdx.Game;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.ScriptsManager;

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

    // 
    Futtoboru gameInstance;
    ScriptsManager scriptsManager;
    
    public FuttoboruGameEngine(Game parent, ScriptsManager scriptsManager) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
        this.scriptsManager = scriptsManager;
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
        
        // Check Game Scripts
        scriptsManager.checkGameScripts();
    }
}