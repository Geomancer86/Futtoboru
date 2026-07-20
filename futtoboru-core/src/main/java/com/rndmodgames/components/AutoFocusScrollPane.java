package com.rndmodgames.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Auto Focus Scroll Pane v1
 * 
 *  - Automatically lets the user scroll a panel without the need to click on it first
 * 
 * Source: https://stackoverflow.com/questions/63432420/how-to-make-a-libgdx-scrollpane-scroll-without-clicking-on-it-first
 * 
 * @author https://stackoverflow.com/users/5288316/nicolas
 *
 */
public class AutoFocusScrollPane extends VisScrollPane {

    public AutoFocusScrollPane(VisTable table) {
        
        super(table);
        
        addListener(new InputListener() {
            
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                getStage().setScrollFocus(AutoFocusScrollPane.this);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Only clear scroll focus if this pane still owns it -- clearing unconditionally used
                // to also null out focus that a *different* actor already claimed in between this
                // pane's enter/exit events firing (e.g. quickly moving the pointer across two
                // auto-focus panes, or off this pane onto a plain VisScrollPane that only gets focus on
                // click), which left the mouse wheel doing nothing anywhere on screen until the user
                // clicked something -- exactly the kind of "scroll silently stops working" bug the
                // 2026-07-19 UI scroll audit was looking for. See docs/registries/blockers-resolved.yaml.
                if (getStage() != null && getStage().getScrollFocus() == AutoFocusScrollPane.this) {
                    getStage().setScrollFocus(null);
                }
            }
        });
    }
}