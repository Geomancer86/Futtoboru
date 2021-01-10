package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Club Screen - Main Game
 * 
 * 
 * 
 * Sources:
 *  - https://redledslick.weebly.com/uploads/1/2/3/7/123738267/627008043.png
 *  - http://3.bp.blogspot.com/-C0-GO_Wvrow/TpWkRL53I-I/AAAAAAAAAMI/dpGyMl7AC-0/s1600/clubscreen.jpg
 *  - https://twitter.com/davorfernandez/status/1301744546666237959/photo/1
 *  - https://i2.wp.com/culturageek.com.ar/wp-content/uploads/2020/05/manchester-FM.gif?resize=1000%2C538&ssl=1
 * 
 * @author Geomancer86
 */
public class ClubScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public ClubScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable clubScreenTable = new VisTable(true);
        
        clubScreenTable.add(new VisTextButton("CLUB screen placeholder"));
        
        this.row();
        this.add(clubScreenTable);
    }
}