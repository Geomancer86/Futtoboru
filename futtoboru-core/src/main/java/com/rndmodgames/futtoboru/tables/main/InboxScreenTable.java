package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Inbox Screen - Main Game
 * 
 * @author Geomancer86
 */
public class InboxScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public InboxScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable inboxScreenTab = new VisTable(true);
        
        inboxScreenTab.add(new VisTextButton("INBOX screen placeholder"));
        
        this.row();
        this.add(inboxScreenTab);
    }
}